package it.uniroma1.charlie.gui;

import it.uniroma1.charlie.core.Action;
import it.uniroma1.charlie.core.ClassicalPlanner;
import it.uniroma1.charlie.core.HTNPlanner;
import it.uniroma1.charlie.core.WorldState;
import it.uniroma1.charlie.model.ScenarioFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Finestra principale Split-Screen per il confronto visivo e computazionale
 * tra Pianificazione Classica (State-Space Flat Search) e Pianificazione
 * Gerarchica (HTN).
 * <p>
 * Offre selezione deterministica su 3 livelli di difficoltà, selezione modalità
 * (Solo Classica, Solo HTN, Entrambe) ed esecuzione parallela sincronizzata con
 * animazione vettoriale.
 */
public class MainFrame extends JFrame {

    private final JComboBox<String> levelCombo;
    private final JRadioButton rbClassicalOnly;
    private final JRadioButton rbHtnOnly;
    private final JRadioButton rbBoth;

    private final JButton btnPlan;
    private final JButton btnReset;
    private final JButton btnPlayPause;
    private final JButton btnStep;
    private final JSlider speedSlider;

    private final JSplitPane splitPane;

    private final GridCanvas classicalCanvas;
    private final JLabel lblClassicalStatus;
    private final JLabel lblClassicalTime;
    private final JLabel lblClassicalNodes;
    private final JLabel lblClassicalSteps;

    private final GridCanvas htnCanvas;
    private final JLabel lblHtnStatus;
    private final JLabel lblHtnTime;
    private final JLabel lblHtnNodes;
    private final JLabel lblHtnSteps;

    private final ClassicalPlanner classicalPlanner;
    private final HTNPlanner htnPlanner;

    private WorldState initialScenarioState;
    private WorldState classicalCurrentState;
    private WorldState htnCurrentState;

    private List<Action> classicalPlan = new ArrayList<>();
    private List<Action> htnPlan = new ArrayList<>();
    private int classicalStepIndex = 0;
    private int htnStepIndex = 0;

    private Timer playbackTimer;

    public MainFrame() {
        super("Charlie Planning Benchmarker: Classica (Flat STRIPS) vs HTN (SHOP2) - Sapienza AI");

        this.classicalPlanner = new ClassicalPlanner(15000L);
        this.htnPlanner = new HTNPlanner();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 820);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        levelCombo = new JComboBox<>(new String[] {
                "Livello 1: Facile (Navigazione Base & Gradini)",
                "Livello 2: Medio (Ostacolo Verticale & Scala)",
                "Livello 3: Difficile (Scenario Completo Fig. 1.1a)",
                "Livello 4: Custom"
        });
        levelCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        rbClassicalOnly = new JRadioButton("Solo Classica");
        rbHtnOnly = new JRadioButton("Solo HTN");
        rbBoth = new JRadioButton("Entrambe", true);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(rbClassicalOnly);
        modeGroup.add(rbHtnOnly);
        modeGroup.add(rbBoth);

        btnPlan = createStyledButton("Avvia Pianificazione", new Color(37, 99, 235));
        btnReset = createStyledButton("Reset", new Color(100, 116, 139));
        btnPlayPause = createStyledButton("Play / Pausa", new Color(16, 185, 129));
        btnStep = createStyledButton("Passo", new Color(14, 165, 233));

        speedSlider = new JSlider(100, 1200, 450);
        speedSlider.setToolTipText("Velocità animazione (ms per passo)");
        speedSlider.setPreferredSize(new Dimension(90, 24));

        JPanel topBar = createTopToolbar();
        add(topBar, BorderLayout.NORTH);

        initialScenarioState = ScenarioFactory.createLevel1();
        classicalCurrentState = initialScenarioState.copy();
        htnCurrentState = initialScenarioState.copy();

        classicalCanvas = new GridCanvas(classicalCurrentState);
        htnCanvas = new GridCanvas(htnCurrentState);

        lblClassicalStatus = createStatusBadge("Pronto", new Color(100, 116, 139));
        lblClassicalTime = createMetricLabel("Tempo: -- ms");
        lblClassicalNodes = createMetricLabel("Nodi Espansi: --");
        lblClassicalSteps = createMetricLabel("Passi: --");

        lblHtnStatus = createStatusBadge("Pronto", new Color(100, 116, 139));
        lblHtnTime = createMetricLabel("Tempo: -- ms");
        lblHtnNodes = createMetricLabel("Nodi Decomposti: --");
        lblHtnSteps = createMetricLabel("Passi: --");

        JPanel topSectionPanel = createPlannerSection(
                "PIANIFICAZIONE CLASSICA (State-Space Flat Search - BFS)",
                new Color(30, 41, 59),
                lblClassicalStatus, lblClassicalTime, lblClassicalNodes, lblClassicalSteps,
                classicalCanvas);

        JPanel bottomSectionPanel = createPlannerSection(
                "PIANIFICAZIONE GERARCHICA (HTN - SHOP2 Ordered Task Decomposition)",
                new Color(15, 76, 129),
                lblHtnStatus, lblHtnTime, lblHtnNodes, lblHtnSteps,
                htnCanvas);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSectionPanel, bottomSectionPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(6);
        add(splitPane, BorderLayout.CENTER);

        playbackTimer = new Timer(speedSlider.getValue(), e -> advanceStep());

        setupEventHandlers();

        loadLevel(0);
    }

    private JPanel createTopToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(248, 250, 252));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 230, 240)),
                new EmptyBorder(8, 14, 8, 14)));

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftGroup.setOpaque(false);

        JLabel lblLvl = new JLabel("Livello:");
        lblLvl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblLvl.setForeground(new Color(45, 55, 72));
        leftGroup.add(lblLvl);
        leftGroup.add(levelCombo);

        leftGroup.add(Box.createHorizontalStrut(8));
        JLabel lblMode = new JLabel("Modalità:");
        lblMode.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblMode.setForeground(new Color(45, 55, 72));
        leftGroup.add(lblMode);
        leftGroup.add(rbClassicalOnly);
        leftGroup.add(rbHtnOnly);
        leftGroup.add(rbBoth);

        bar.add(leftGroup, BorderLayout.WEST);

        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightGroup.setOpaque(false);

        rightGroup.add(btnPlan);
        rightGroup.add(btnReset);
        rightGroup.add(btnPlayPause);
        rightGroup.add(btnStep);

        JLabel lblSpeed = new JLabel("Velocità:");
        lblSpeed.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rightGroup.add(lblSpeed);
        rightGroup.add(speedSlider);

        bar.add(rightGroup, BorderLayout.EAST);
        return bar;
    }

    private JPanel createPlannerSection(String title,
            Color headerBg,
            JLabel statusBadge,
            JLabel timeLabel,
            JLabel nodesLabel,
            JLabel stepsLabel,
            GridCanvas canvas) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(headerBg);
        header.setBorder(new EmptyBorder(6, 14, 6, 14));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl, BorderLayout.WEST);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        badges.setOpaque(false);
        badges.add(statusBadge);
        badges.add(timeLabel);
        badges.add(nodesLabel);
        badges.add(stepsLabel);
        header.add(badges, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        panel.add(canvas, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createStatusBadge(String text, Color bg) {
        JLabel badge = new JLabel(" " + text + " ");
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setBackground(bg);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return badge;
    }

    private JLabel createMetricLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(new Color(230, 240, 255));
        return lbl;
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setBackground(baseColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(baseColor.darker(), 1),
                new EmptyBorder(6, 12, 6, 12)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupEventHandlers() {
        levelCombo.addActionListener(e -> loadLevel(levelCombo.getSelectedIndex()));

        rbClassicalOnly.addActionListener(e -> updateModeView());
        rbHtnOnly.addActionListener(e -> updateModeView());
        rbBoth.addActionListener(e -> updateModeView());

        btnPlan.addActionListener(e -> startPlanning());
        btnReset.addActionListener(e -> resetCurrentScenario());
        btnPlayPause.addActionListener(e -> togglePlayPause());
        btnStep.addActionListener(e -> advanceStep());

        speedSlider.addChangeListener(e -> playbackTimer.setDelay(speedSlider.getValue()));
    }

    private void loadLevel(int levelIndex) {
        if (playbackTimer.isRunning())
            playbackTimer.stop();

        switch (levelIndex) {
            case 0 -> initialScenarioState = ScenarioFactory.createLevel1();
            case 1 -> initialScenarioState = ScenarioFactory.createLevel2();
            case 2 -> initialScenarioState = ScenarioFactory.createLevel3();
            case 3 -> initialScenarioState = ScenarioFactory.createLevel4();
            default -> initialScenarioState = ScenarioFactory.createLevel1();
        }

        resetCurrentScenario();
    }

    private void updateModeView() {
        if (rbClassicalOnly.isSelected()) {
            classicalCanvas.setDisabled(false, null);
            htnCanvas.setDisabled(true, "Disattivato (Modalità Solo Classica)");
        } else if (rbHtnOnly.isSelected()) {
            classicalCanvas.setDisabled(true, "Disattivato (Modalità Solo HTN)");
            htnCanvas.setDisabled(false, null);
        } else {
            classicalCanvas.setDisabled(false, null);
            htnCanvas.setDisabled(false, null);
        }
    }

    private void resetCurrentScenario() {
        if (playbackTimer.isRunning())
            playbackTimer.stop();

        classicalCurrentState = initialScenarioState.copy();
        htnCurrentState = initialScenarioState.copy();

        classicalCanvas.setState(classicalCurrentState);
        htnCanvas.setState(htnCurrentState);

        classicalPlan.clear();
        htnPlan.clear();
        classicalStepIndex = 0;
        htnStepIndex = 0;

        updateStatusBadge(lblClassicalStatus, "Pronto", new Color(100, 116, 139));
        lblClassicalTime.setText("Tempo: -- ms");
        lblClassicalNodes.setText("Nodi Espansi: --");
        lblClassicalSteps.setText("Passi: --");

        updateStatusBadge(lblHtnStatus, "Pronto", new Color(100, 116, 139));
        lblHtnTime.setText("Tempo: -- ms");
        lblHtnNodes.setText("Nodi Decomposti: --");
        lblHtnSteps.setText("Passi: --");

        btnPlan.setEnabled(true);
        updateModeView();
    }

    private void updateStatusBadge(JLabel badge, String text, Color bg) {
        badge.setText(" " + text + " ");
        badge.setBackground(bg);
    }

    private void startPlanning() {
        if (playbackTimer.isRunning())
            playbackTimer.stop();
        btnPlan.setEnabled(false);

        boolean runClassical = rbClassicalOnly.isSelected() || rbBoth.isSelected();
        boolean runHtn = rbHtnOnly.isSelected() || rbBoth.isSelected();

        if (runClassical) {
            updateStatusBadge(lblClassicalStatus, "In Ricerca...", new Color(234, 179, 8));
        }
        if (runHtn) {
            updateStatusBadge(lblHtnStatus, "Pianificazione...", new Color(234, 179, 8));
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            ClassicalPlanner.ClassicalPlanResult classicalRes = null;
            HTNPlanner.HTNPlanResult htnRes = null;

            @Override
            protected Void doInBackground() {
                // Esecuzione Classical Planner
                if (runClassical) {
                    classicalRes = classicalPlanner.plan(initialScenarioState);
                }
                // Esecuzione HTN Planner
                if (runHtn) {
                    htnRes = htnPlanner.plan(initialScenarioState);
                }
                return null;
            }

            @Override
            protected void done() {
                btnPlan.setEnabled(true);

                // Aggiornamento Risultati Classici
                if (classicalRes != null) {
                    lblClassicalTime.setText(String.format("Tempo: %d ms", classicalRes.getPlanningTimeMs()));
                    lblClassicalNodes.setText(String.format("Nodi: %d (gen %d)",
                            classicalRes.getExpandedNodes(), classicalRes.getGeneratedNodes()));

                    if (classicalRes.isSuccess()) {
                        classicalPlan = new ArrayList<>(classicalRes.getActions());
                        lblClassicalSteps.setText("Passi: " + classicalPlan.size());
                        updateStatusBadge(lblClassicalStatus, "Successo", new Color(16, 185, 129));
                    } else if (classicalRes.isTimeout()) {
                        classicalPlan.clear();
                        lblClassicalSteps.setText("Passi: --");
                        updateStatusBadge(lblClassicalStatus, "Timeout (15s)", new Color(239, 68, 68));
                    } else {
                        classicalPlan.clear();
                        lblClassicalSteps.setText("Passi: Fallito");
                        updateStatusBadge(lblClassicalStatus, "Fallito", new Color(239, 68, 68));
                    }
                }

                // Aggiornamento Risultati HTN
                if (htnRes != null) {
                    lblHtnTime.setText(String.format("Tempo: %d ms", htnRes.getPlanningTimeMs()));
                    lblHtnNodes.setText(String.format("Nodi: %d", htnRes.getExpandedNodes()));

                    if (htnRes.isSuccess()) {
                        htnPlan = new ArrayList<>(htnRes.getActions());
                        lblHtnSteps.setText("Passi: " + htnPlan.size());
                        updateStatusBadge(lblHtnStatus, "Successo", new Color(16, 185, 129));
                    } else {
                        htnPlan.clear();
                        lblHtnSteps.setText("Passi: Fallito");
                        updateStatusBadge(lblHtnStatus, "Fallito", new Color(239, 68, 68));
                    }
                }

                // Avvia playback automatico se almeno un piano è disponibile
                if (!classicalPlan.isEmpty() || !htnPlan.isEmpty()) {
                    classicalStepIndex = 0;
                    htnStepIndex = 0;
                    playbackTimer.start();
                }
            }
        };

        worker.execute();
    }

    private void togglePlayPause() {
        if (playbackTimer.isRunning()) {
            playbackTimer.stop();
        } else {
            playbackTimer.start();
        }
    }

    private void advanceStep() {
        boolean advancedClassical = false;
        boolean advancedHtn = false;

        // Avanzamento Classico
        if (classicalStepIndex < classicalPlan.size()) {
            Action act = classicalPlan.get(classicalStepIndex);
            if (act.isApplicable(classicalCurrentState)) {
                classicalCurrentState = act.apply(classicalCurrentState);
                classicalCanvas.setState(classicalCurrentState);
            }
            classicalStepIndex++;
            lblClassicalSteps.setText(String.format("Passo: %d/%d (%s)",
                    classicalStepIndex, classicalPlan.size(), act.name()));
            advancedClassical = true;
        }

        // Avanzamento HTN
        if (htnStepIndex < htnPlan.size()) {
            Action act = htnPlan.get(htnStepIndex);
            if (act.isApplicable(htnCurrentState)) {
                htnCurrentState = act.apply(htnCurrentState);
                htnCanvas.setState(htnCurrentState);
            }
            htnStepIndex++;
            lblHtnSteps.setText(String.format("Passo: %d/%d (%s)",
                    htnStepIndex, htnPlan.size(), act.name()));
            advancedHtn = true;
        }

        // Se entrambi hanno completato tutti i passi, stop al timer
        if (!advancedClassical && !advancedHtn) {
            playbackTimer.stop();
        }
    }
}
