package it.uniroma1.charlie.gui;

import it.uniroma1.charlie.core.WorldState;
import it.uniroma1.charlie.model.WorldConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Canvas grafico Swing dedicato al rendering vettoriale 2D di Charlie.
 */
public class GridCanvas extends JPanel {

    private WorldState state;
    private boolean disabled = false;
    private String disabledReason = "Disattivato";

    // Palette cromatica moderna e armoniosa
    private static final Color BG_COLOR_TOP = new Color(245, 248, 252);
    private static final Color BG_COLOR_BOTTOM = new Color(228, 235, 244);
    private static final Color GRID_LINE_COLOR = new Color(205, 215, 228);
    private static final Color BASELINE_COLOR = new Color(50, 60, 72);
    private static final Color CEILING_COLOR = new Color(42, 52, 65);
    private static final Color BOX_FILL_TOP = new Color(145, 158, 172);
    private static final Color BOX_FILL_BOTTOM = new Color(112, 124, 138);
    private static final Color BOX_BORDER = new Color(65, 75, 88);
    private static final Color LADDER_WOOD = new Color(165, 88, 48);
    private static final Color LADDER_RUNG = new Color(195, 115, 70);
    private static final Color BANANA_COLOR = new Color(255, 210, 20);
    private static final Color MONKEY_BODY = new Color(142, 86, 46);
    private static final Color MONKEY_FACE = new Color(230, 192, 152);

    public GridCanvas(WorldState initialState) {
        this.state = initialState;
        setBackground(BG_COLOR_TOP);
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(900, 280));
    }

    public void setState(WorldState state) {
        this.state = state;
        repaint();
    }

    public WorldState getState() {
        return state;
    }

    public void setDisabled(boolean disabled, String reason) {
        this.disabled = disabled;
        this.disabledReason = reason != null ? reason : "Disattivato";
        repaint();
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        GradientPaint bgGradient = new GradientPaint(0, 0, BG_COLOR_TOP, 0, height, BG_COLOR_BOTTOM);
        g2.setPaint(bgGradient);
        g2.fillRect(0, 0, width, height);

        if (state == null) {
            g2.dispose();
            return;
        }

        int marginLeft = 55;
        int marginRight = 30;
        int marginTop = 35;
        int marginBottom = 35;

        int gridW = Math.max(10, width - marginLeft - marginRight);
        int gridH = Math.max(10, height - marginTop - marginBottom);

        int numCols = state.getWidth();
        int numRows = WorldConstants.MAX_HEIGHT; // 5

        double cellW = (double) gridW / numCols;
        double cellH = (double) gridH / (numRows + 1.2);

        double baselineY = marginTop + (numRows + 0.6) * cellH;
        double ceilingY = marginTop + 0.4 * cellH;

        g2.setColor(CEILING_COLOR);
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Double(marginLeft, ceilingY, marginLeft + gridW, ceilingY));

        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(new Color(110, 122, 138));
        g2.drawString("SOFFITTO", marginLeft + 5, (int) ceilingY - 5);

        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3, 3 }, 0));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));

        for (int h = 0; h <= numRows; h++) {
            double y = baselineY - h * cellH;
            g2.setColor(GRID_LINE_COLOR);
            g2.draw(new Line2D.Double(marginLeft, y, marginLeft + gridW, y));

            g2.setColor(new Color(80, 92, 108));
            String yLabel = "h" + h;
            FontMetrics fm = g2.getFontMetrics();
            int labelW = fm.stringWidth(yLabel);
            g2.drawString(yLabel, marginLeft - labelW - 8, (int) y + 4);
        }

        for (int x = 0; x <= numCols; x++) {
            double cellX = marginLeft + x * cellW;
            g2.setColor(GRID_LINE_COLOR);
            g2.setStroke(
                    new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3, 3 }, 0));
            g2.draw(new Line2D.Double(cellX, ceilingY, cellX, baselineY));

            if (x < numCols) {
                double labelCenterX = cellX + cellW / 2.0;
                String xLabel = String.valueOf(x);
                FontMetrics fm = g2.getFontMetrics();
                int labelW = fm.stringWidth(xLabel);
                g2.setColor(new Color(80, 92, 108));
                g2.drawString(xLabel, (int) (labelCenterX - labelW / 2.0), (int) baselineY + 16);
            }
        }

        g2.setColor(BASELINE_COLOR);
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new Line2D.Double(marginLeft, baselineY, marginLeft + gridW, baselineY));
        int[] heights = state.getColumnHeights();
        for (int x = 0; x < numCols; x++) {
            int colH = heights[x];
            double blockX = marginLeft + x * cellW + 1.5;
            double blockW = Math.max(1, cellW - 3);

            for (int h = 0; h < colH; h++) {
                double blockY = baselineY - (h + 1) * cellH + 1.5;
                double blockH = Math.max(1, cellH - 3);

                GradientPaint boxGradient = new GradientPaint(
                        (float) blockX, (float) blockY, BOX_FILL_TOP,
                        (float) (blockX + blockW), (float) (blockY + blockH), BOX_FILL_BOTTOM);
                g2.setPaint(boxGradient);
                g2.fill(new RoundRectangle2D.Double(blockX, blockY, blockW, blockH, 4, 4));

                g2.setColor(BOX_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Double(blockX, blockY, blockW, blockH, 4, 4));

                // Venatura/croce scatola
                g2.setColor(new Color(255, 255, 255, 35));
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new Line2D.Double(blockX + 2, blockY + 2, blockX + blockW - 2, blockY + blockH - 2));
            }
        }

        drawBanana(g2, marginLeft, cellW, cellH, ceilingY, baselineY);

        if (!state.isHoldingLadder()) {
            drawLadder(g2, marginLeft, cellW, cellH, baselineY, state.getLadderX(), state.getLadderH());
        }

        drawMonkey(g2, marginLeft, cellW, cellH, baselineY,
                state.getMonkeyX(), state.getMonkeyH(), state.isHoldingLadder());

        if (disabled) {
            drawDisabledOverlay(g2, width, height);
        }

        g2.dispose();
    }

    private void drawBanana(Graphics2D g2, int marginLeft, double cellW, double cellH, double ceilingY,
            double baselineY) {
        int bananaX = state.getBananaX();
        int bananaH = state.getBananaH();

        double cx = marginLeft + (bananaX + 0.5) * cellW;
        double targetY = baselineY - (bananaH + 0.15) * cellH;

        g2.setColor(new Color(130, 110, 90));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3, 3 }, 0));
        g2.draw(new Line2D.Double(cx, ceilingY, cx, targetY));

        if (!state.hasBanana()) {
            g2.setColor(new Color(255, 235, 59, 100));
            g2.fill(new Ellipse2D.Double(cx - 14, targetY - 12, 28, 24));

            g2.setColor(BANANA_COLOR);
            g2.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Arc2D bananaArc = new Arc2D.Double(cx - 10, targetY - 8, 20, 16, 200, 140, Arc2D.OPEN);
            g2.draw(bananaArc);
            g2.setColor(new Color(90, 50, 20));
            g2.setStroke(new BasicStroke(2.0f));
            g2.draw(new Line2D.Double(cx + 6, targetY - 5, cx + 9, targetY - 8));
        } else {
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(new Color(34, 160, 80));
            g2.drawString("PRESA! ★", (int) cx - 22, (int) targetY);
        }
    }

    private void drawLadder(Graphics2D g2, int marginLeft, double cellW, double cellH, double baselineY, int lx,
            int lh) {
        double cx = marginLeft + (lx + 0.5) * cellW;
        double baseBoxY = baselineY - lh * cellH;

        double ladderHeight = cellH * 1.35;
        double leftX1 = cx - cellW * 0.22;
        double leftX2 = cx - cellW * 0.10;
        double rightX1 = cx - cellW * 0.05;
        double rightX2 = cx + cellW * 0.07;

        double bottomY = baseBoxY;
        double topY = baseBoxY - ladderHeight;

        g2.setColor(LADDER_WOOD);
        g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2.draw(new Line2D.Double(leftX1, bottomY, leftX2, topY));
        g2.draw(new Line2D.Double(rightX1, bottomY, rightX2, topY));

        int rungs = 4;
        g2.setColor(LADDER_RUNG);
        g2.setStroke(new BasicStroke(1.8f));
        for (int i = 1; i <= rungs; i++) {
            double t = (double) i / (rungs + 1);
            double rx1 = leftX1 + t * (leftX2 - leftX1);
            double ry1 = bottomY + t * (topY - bottomY);
            double rx2 = rightX1 + t * (rightX2 - rightX1);
            double ry2 = bottomY + t * (topY - bottomY);
            g2.draw(new Line2D.Double(rx1, ry1, rx2, ry2));
        }
    }

    private void drawMonkey(Graphics2D g2, int marginLeft, double cellW, double cellH, double baselineY,
            int mx, int mh, boolean holdingLadder) {
        double cx = marginLeft + (mx + 0.5) * cellW;
        double footY = baselineY - mh * cellH;

        double monkeySize = Math.min(cellW * 0.75, cellH * 0.85);
        double monkeyY = footY - monkeySize;

        if (holdingLadder) {
            g2.setColor(LADDER_WOOD);
            g2.setStroke(new BasicStroke(2.2f));
            g2.draw(new Line2D.Double(cx - monkeySize * 0.5, monkeyY + monkeySize * 0.8,
                    cx + monkeySize * 0.6, monkeyY - monkeySize * 0.2));
            g2.draw(new Line2D.Double(cx - monkeySize * 0.3, monkeyY + monkeySize * 0.9,
                    cx + monkeySize * 0.8, monkeyY - monkeySize * 0.1));
        }

        g2.setColor(MONKEY_BODY);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Arc2D tail = new Arc2D.Double(cx + monkeySize * 0.1, monkeyY + monkeySize * 0.3,
                monkeySize * 0.5, monkeySize * 0.5, 0, 180, Arc2D.OPEN);
        g2.draw(tail);

        g2.setColor(MONKEY_BODY);
        g2.fill(new Ellipse2D.Double(cx - monkeySize * 0.35, monkeyY + monkeySize * 0.35,
                monkeySize * 0.7, monkeySize * 0.6));
        double headSize = monkeySize * 0.55;
        double headX = cx - headSize / 2.0;
        double headY = monkeyY + monkeySize * 0.05;
        g2.setColor(MONKEY_BODY);
        g2.fill(new Ellipse2D.Double(headX, headY, headSize, headSize));

        double earSize = headSize * 0.32;
        g2.fill(new Ellipse2D.Double(headX - earSize * 0.35, headY + headSize * 0.2, earSize, earSize));
        g2.fill(new Ellipse2D.Double(headX + headSize - earSize * 0.65, headY + headSize * 0.2, earSize, earSize));
        g2.setColor(MONKEY_FACE);
        g2.fill(new Ellipse2D.Double(headX + headSize * 0.15, headY + headSize * 0.25, headSize * 0.7,
                headSize * 0.65));

        g2.setColor(Color.BLACK);
        double eyeSize = Math.max(2, headSize * 0.12);
        g2.fill(new Ellipse2D.Double(headX + headSize * 0.28, headY + headSize * 0.38, eyeSize, eyeSize));
        g2.fill(new Ellipse2D.Double(headX + headSize * 0.58, headY + headSize * 0.38, eyeSize, eyeSize));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new Arc2D.Double(headX + headSize * 0.32, headY + headSize * 0.52, headSize * 0.36, headSize * 0.25,
                200, 140, Arc2D.OPEN));

        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.setColor(new Color(60, 40, 20));
        g2.drawString("Charlie", (int) (cx - 14), (int) monkeyY - 3);
    }

    private void drawDisabledOverlay(Graphics2D g2, int width, int height) {
        g2.setColor(new Color(230, 235, 245, 195));
        g2.fillRect(0, 0, width, height);

        int badgeW = 340;
        int badgeH = 46;
        int bx = (width - badgeW) / 2;
        int by = (height - badgeH) / 2;

        g2.setColor(new Color(50, 65, 85, 235));
        g2.fill(new RoundRectangle2D.Double(bx, by, badgeW, badgeH, 12, 12));
        g2.setColor(new Color(160, 175, 195));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(bx, by, badgeW, badgeH, 12, 12));

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(disabledReason);
        g2.drawString(disabledReason, bx + (badgeW - tw) / 2, by + 28);
    }
}
