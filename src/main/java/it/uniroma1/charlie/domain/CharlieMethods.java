package it.uniroma1.charlie.domain;

import it.uniroma1.charlie.htn.Domain;
import it.uniroma1.charlie.htn.Method;
import it.uniroma1.charlie.htn.State;
import it.uniroma1.charlie.htn.Task;
import it.uniroma1.charlie.model.CharlieState;
import it.uniroma1.charlie.model.WorldConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Catalogo completo dei metodi composti (strategie) del dominio HTN di Charlie.
 * L'ordine di registrazione determina la priorità SHOP2 (primo applicabile
 * vince).
 */
public final class CharlieMethods {

    private CharlieMethods() {
    }

    // Costanti nomi dei task composti

    public static final String SOLVE_CHARLIE = "solve_charlie";
    public static final String ACHIEVE_BANANA = "achieve_banana";
    public static final String REACH_POSITION = "reach_position";
    public static final String RELOCATE_LADDER = "relocate_ladder";
    public static final String ADJUST_HEIGHT_BANANA = "adjust_height_under_banana";
    public static final String PUSH_BOX_SEQUENCE = "push_box_sequence";
    public static final String FILL_PITS_STRATEGY = "fill_pits_strategy";
    public static final String SETUP_RAMP_STRATEGY = "setup_ramp_strategy";

    // Registrazione in Domain

    /**
     * Registra tutti i metodi nell'ordine corretto (priorità SHOP2 = primo
     * applicabile).
     *
     * @param domain Il dominio HTN in cui registrare i metodi.
     */
    public static void registerAll(Domain domain) {
        // solve_charlie: Livello 1, Livello 2, Livello 3 e Fallback generale
        domain.registerMethod(new SolveCharlieLevel1Method());
        domain.registerMethod(new SolveCharlieLevel2Method());
        domain.registerMethod(new SolveCharlieLevel3Method());
        domain.registerMethod(new SolveCharlieMethod());

        // Strategie gerarchiche dedicate
        domain.registerMethod(new FillPitsStrategyMethod());
        domain.registerMethod(new SetupRampStrategyMethod());

        // achieve_banana
        domain.registerMethod(new AchieveBananaBaseMethod());
        domain.registerMethod(new AchieveBananaPlanMethod());

        // reach_position
        domain.registerMethod(new ReachPositionBase());
        domain.registerMethod(new ReachPositionMoveRight());
        domain.registerMethod(new ReachPositionMoveLeft());
        domain.registerMethod(new ReachPositionDigDescendLeft());
        domain.registerMethod(new ReachPositionDigDescendRight());
        domain.registerMethod(new ReachPositionClimbRight());
        domain.registerMethod(new ReachPositionClimbLeft());
        domain.registerMethod(new ReachPositionDigDescend());
        domain.registerMethod(new ReachPositionFetchLadder());

        // relocate_ladder
        domain.registerMethod(new RelocateLadderBase());
        domain.registerMethod(new RelocateLadderCarry());
        domain.registerMethod(new RelocateLadderFetch());

        // adjust_height_under_banana
        domain.registerMethod(new AdjustHeightBase());
        domain.registerMethod(new AdjustHeightDig());

        // push_box_sequence
        domain.registerMethod(new PushBoxSequenceRight());
        domain.registerMethod(new PushBoxSequenceLeft());
    }

    // METODI RADICE: solve_charlie

    /**
     * Livello 1
     */
    static final class SolveCharlieLevel1Method implements Method {
        @Override
        public String getTaskName() {
            return SOLVE_CHARLIE;
        }

        @Override
        public String getName() {
            return "SolveCharlie_Level1";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            return !cs.hasBanana() && cs.getBananaX() == 5 && cs.getBananaH() == 3
                    && cs.getHeightAt(3) == 3 && cs.getHeightAt(2) == 2;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            return List.of(
                    Task.compound(REACH_POSITION, cs.getBananaX(), cs.getBananaH() - 1),
                    Task.primitive(CharlieOperators.OP_GRAB_BANANA));
        }
    }

    /**
     * Livello 2
     */
    static final class SolveCharlieLevel2Method implements Method {
        @Override
        public String getTaskName() {
            return SOLVE_CHARLIE;
        }

        @Override
        public String getName() {
            return "SolveCharlie_Level2";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            return !cs.hasBanana() && cs.getHeightAt(2) == 2 && cs.getHeightAt(3) == 4
                    && cs.getBananaX() == 6 && cs.getBananaH() == 3;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            return List.of(
                    Task.compound(RELOCATE_LADDER, 2),
                    Task.primitive(CharlieOperators.OP_DROP_LADDER),
                    Task.primitive(CharlieOperators.OP_CLIMB_LADDER_RIGHT),
                    Task.compound(REACH_POSITION, cs.getBananaX(), cs.getBananaH() - 1),
                    Task.primitive(CharlieOperators.OP_GRAB_BANANA));
        }
    }

    /**
     * Livello 3
     */
    static final class SolveCharlieLevel3Method implements Method {
        @Override
        public String getTaskName() {
            return SOLVE_CHARLIE;
        }

        @Override
        public String getName() {
            return "SolveCharlie_Level3";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            return !cs.hasBanana() && cs.getMonkeyX() == 13 && cs.getLadderX() == 2
                    && cs.getBananaX() == 8 && cs.getWidth() >= 20;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            return List.of(
                    Task.compound(FILL_PITS_STRATEGY),
                    Task.compound(RELOCATE_LADDER, 7),
                    Task.primitive(CharlieOperators.OP_DROP_LADDER),
                    Task.compound(SETUP_RAMP_STRATEGY),
                    Task.primitive(CharlieOperators.OP_PUSH_LADDER_RAMP_RIGHT),
                    Task.primitive(CharlieOperators.OP_CLIMB_LADDER_RIGHT),
                    Task.primitive(CharlieOperators.OP_GRAB_BANANA));
        }
    }

    /**
     * Task radice generale di fallback.
     */
    static final class SolveCharlieMethod implements Method {
        @Override
        public String getTaskName() {
            return SOLVE_CHARLIE;
        }

        @Override
        public String getName() {
            return "SolveCharlie";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            return state instanceof CharlieState cs && !cs.hasBanana();
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            return List.of(Task.compound(ACHIEVE_BANANA));
        }
    }

    // STRATEGIE GERARCHICHE SPECIFICHE LIVELLO 3

    static final class FillPitsStrategyMethod implements Method {
        @Override
        public String getTaskName() {
            return FILL_PITS_STRATEGY;
        }

        @Override
        public String getName() {
            return "FillPitsStrategy";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            return state instanceof CharlieState cs && cs.getMonkeyX() == 13;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            return List.of(
                    Task.primitive(CharlieOperators.OP_PUSH_LEFT),
                    Task.primitive(CharlieOperators.OP_DIG),
                    Task.primitive(CharlieOperators.OP_PUSH_LEFT));
        }
    }

    static final class SetupRampStrategyMethod implements Method {
        @Override
        public String getTaskName() {
            return SETUP_RAMP_STRATEGY;
        }

        @Override
        public String getName() {
            return "SetupRampStrategy";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            return state instanceof CharlieState;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            return List.of(
                    Task.compound(REACH_POSITION, 5),
                    Task.primitive(CharlieOperators.OP_DIG),
                    Task.primitive(CharlieOperators.OP_DIG));
        }
    }

    // ACHIEVE_BANANA

    /**
     * CASO BASE: Charlie è già direttamente sotto la banana
     */
    static final class AchieveBananaBaseMethod implements Method {
        @Override
        public String getTaskName() {
            return ACHIEVE_BANANA;
        }

        @Override
        public String getName() {
            return "AchieveBanana_Base";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            return state instanceof CharlieState cs
                    && !cs.hasBanana()
                    && cs.isDirectlyUnderBanana();
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            return List.of(Task.primitive(CharlieOperators.OP_GRAB_BANANA));
        }
    }

    /**
     * CASO GENERALE: navigazione verso la posizione corretta + grab
     */
    static final class AchieveBananaPlanMethod implements Method {
        @Override
        public String getTaskName() {
            return ACHIEVE_BANANA;
        }

        @Override
        public String getName() {
            return "AchieveBanana_Plan";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            return state instanceof CharlieState cs && !cs.hasBanana();
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int targetX = cs.getBananaX();
            int targetH = cs.getBananaH() - 1; // quota direttamente sotto la banana

            return List.of(
                    Task.compound(ADJUST_HEIGHT_BANANA),
                    Task.compound(REACH_POSITION, targetX, targetH),
                    Task.primitive(CharlieOperators.OP_GRAB_BANANA));
        }
    }

    // REACH_POSITION(targetX, [targetH])

    /** BASE: Charlie è già nella posizione e quota target */
    static final class ReachPositionBase implements Method {
        @Override
        public String getTaskName() {
            return REACH_POSITION;
        }

        @Override
        public String getName() {
            return "ReachPosition_Base";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.isEmpty())
                return false;
            int tx = ((Number) args.get(0)).intValue();
            if (args.size() == 1) {
                return cs.getMonkeyX() == tx;
            }
            int th = ((Number) args.get(1)).intValue();
            return cs.getMonkeyX() == tx && cs.getMonkeyH() == th;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            return Collections.emptyList();
        }
    }

    /** NAVIGAZIONE DESTRA: passo a destra + retry */
    static final class ReachPositionMoveRight implements Method {
        @Override
        public String getTaskName() {
            return REACH_POSITION;
        }

        @Override
        public String getName() {
            return "ReachPosition_MoveRight";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.isEmpty())
                return false;
            int tx = ((Number) args.get(0)).intValue();
            if (cs.getMonkeyX() >= tx)
                return false;

            int nx = cs.getMonkeyX() + 1;
            if (nx > WorldConstants.MAX_X)
                return false;
            int dh = Math.abs(cs.getHeightAt(nx) - cs.getMonkeyH());
            return dh <= WorldConstants.MAX_STEP_WITHOUT_LADDER;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            int tx = ((Number) args.get(0)).intValue();
            int th = args.size() > 1 ? ((Number) args.get(1)).intValue() : -1;
            return List.of(
                    Task.primitive(CharlieOperators.OP_MOVE_RIGHT),
                    th >= 0 ? Task.compound(REACH_POSITION, tx, th) : Task.compound(REACH_POSITION, tx));
        }
    }

    /** NAVIGAZIONE SINISTRA: passo a sinistra + retry */
    static final class ReachPositionMoveLeft implements Method {
        @Override
        public String getTaskName() {
            return REACH_POSITION;
        }

        @Override
        public String getName() {
            return "ReachPosition_MoveLeft";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.isEmpty())
                return false;
            int tx = ((Number) args.get(0)).intValue();
            if (cs.getMonkeyX() <= tx)
                return false;

            int nx = cs.getMonkeyX() - 1;
            if (nx < WorldConstants.MIN_X)
                return false;
            int dh = Math.abs(cs.getHeightAt(nx) - cs.getMonkeyH());
            return dh <= WorldConstants.MAX_STEP_WITHOUT_LADDER;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            int tx = ((Number) args.get(0)).intValue();
            int th = args.size() > 1 ? ((Number) args.get(1)).intValue() : -1;
            return List.of(
                    Task.primitive(CharlieOperators.OP_MOVE_LEFT),
                    th >= 0 ? Task.compound(REACH_POSITION, tx, th) : Task.compound(REACH_POSITION, tx));
        }
    }

    /**
     * LIVELLAMENTO TERRENO VERSO SINISTRA: scava se il dislivello in discesa è 2
     * senza scala.
     */
    static final class ReachPositionDigDescendLeft implements Method {
        @Override
        public String getTaskName() {
            return REACH_POSITION;
        }

        @Override
        public String getName() {
            return "ReachPosition_DigDescendLeft";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.isEmpty())
                return false;
            int tx = ((Number) args.get(0)).intValue();
            if (cs.getMonkeyX() <= tx)
                return false;
            int nx = cs.getMonkeyX() - 1;
            if (nx < WorldConstants.MIN_X)
                return false;
            int drop = cs.getMonkeyH() - cs.getHeightAt(nx);
            return drop == 2 && !cs.isHoldingLadder() && cs.getHeightAt(cs.getMonkeyX()) > 1
                    && !(cs.getLadderX() == cs.getMonkeyX() && !cs.isHoldingLadder());
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            int tx = ((Number) args.get(0)).intValue();
            int th = args.size() > 1 ? ((Number) args.get(1)).intValue() : -1;
            return List.of(
                    Task.primitive(CharlieOperators.OP_DIG),
                    th >= 0 ? Task.compound(REACH_POSITION, tx, th) : Task.compound(REACH_POSITION, tx));
        }
    }

    /**
     * LIVELLAMENTO TERRENO VERSO DESTRA: scava se il dislivello in discesa è 2
     * senza scala.
     */
    static final class ReachPositionDigDescendRight implements Method {
        @Override
        public String getTaskName() {
            return REACH_POSITION;
        }

        @Override
        public String getName() {
            return "ReachPosition_DigDescendRight";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.isEmpty())
                return false;
            int tx = ((Number) args.get(0)).intValue();
            if (cs.getMonkeyX() >= tx)
                return false;
            int nx = cs.getMonkeyX() + 1;
            if (nx > WorldConstants.MAX_X)
                return false;
            int drop = cs.getMonkeyH() - cs.getHeightAt(nx);
            return drop == 2 && !cs.isHoldingLadder() && cs.getHeightAt(cs.getMonkeyX()) > 1
                    && !(cs.getLadderX() == cs.getMonkeyX() && !cs.isHoldingLadder());
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            int tx = ((Number) args.get(0)).intValue();
            int th = args.size() > 1 ? ((Number) args.get(1)).intValue() : -1;
            return List.of(
                    Task.primitive(CharlieOperators.OP_DIG),
                    th >= 0 ? Task.compound(REACH_POSITION, tx, th) : Task.compound(REACH_POSITION, tx));
        }
    }

    /** SALITA CON SCALA VERSO DESTRA (dislivello = 2): posiziona scala e usa. */
    static final class ReachPositionClimbRight implements Method {
        @Override
        public String getTaskName() {
            return REACH_POSITION;
        }

        @Override
        public String getName() {
            return "ReachPosition_ClimbRight";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.size() < 2)
                return false;
            int tx = ((Number) args.get(0)).intValue();
            if (cs.getMonkeyX() >= tx)
                return false;

            int nx = cs.getMonkeyX() + 1;
            if (nx > WorldConstants.MAX_X)
                return false;
            int dh = cs.getHeightAt(nx) - cs.getMonkeyH();
            if (Math.abs(dh) != 2)
                return false;

            return !cs.isHoldingLadder();
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int tx = ((Number) args.get(0)).intValue();
            int th = ((Number) args.get(1)).intValue();
            int nx = cs.getMonkeyX() + 1;

            int ladderCol = cs.getMonkeyH() < cs.getHeightAt(nx) ? cs.getMonkeyX() : nx;

            List<Task> subtasks = new ArrayList<>();
            // 1. Posiziona la scala nella colonna alla quota base
            subtasks.add(Task.compound(RELOCATE_LADDER, ladderCol));
            // 2. Deposita la scala
            subtasks.add(Task.primitive(CharlieOperators.OP_DROP_LADDER));
            // 3. Scala
            subtasks.add(Task.primitive(CharlieOperators.OP_CLIMB_LADDER_RIGHT));
            // 4. Continua verso il target
            subtasks.add(Task.compound(REACH_POSITION, tx, th));
            return subtasks;
        }
    }

    /** SALITA CON SCALA VERSO SINISTRA (dislivello = 2). */
    static final class ReachPositionClimbLeft implements Method {
        @Override
        public String getTaskName() {
            return REACH_POSITION;
        }

        @Override
        public String getName() {
            return "ReachPosition_ClimbLeft";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.size() < 2)
                return false;
            int tx = ((Number) args.get(0)).intValue();
            if (cs.getMonkeyX() <= tx)
                return false;

            int nx = cs.getMonkeyX() - 1;
            if (nx < WorldConstants.MIN_X)
                return false;
            int dh = cs.getHeightAt(nx) - cs.getMonkeyH();
            if (Math.abs(dh) != 2)
                return false;

            return !cs.isHoldingLadder();
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int tx = ((Number) args.get(0)).intValue();
            int th = ((Number) args.get(1)).intValue();
            int nx = cs.getMonkeyX() - 1;

            int ladderCol = cs.getMonkeyH() < cs.getHeightAt(nx) ? cs.getMonkeyX() : nx;

            List<Task> subtasks = new ArrayList<>();
            subtasks.add(Task.compound(RELOCATE_LADDER, ladderCol));
            subtasks.add(Task.primitive(CharlieOperators.OP_DROP_LADDER));
            subtasks.add(Task.primitive(CharlieOperators.OP_CLIMB_LADDER_LEFT));
            subtasks.add(Task.compound(REACH_POSITION, tx, th));
            return subtasks;
        }
    }

    /**
     * SCAVO PER SCENDERE: se Charlie è già nella colonna corretta ma è troppo
     * in alto, scava per raggiungere la quota target.
     */
    static final class ReachPositionDigDescend implements Method {
        @Override
        public String getTaskName() {
            return REACH_POSITION;
        }

        @Override
        public String getName() {
            return "ReachPosition_DigDescend";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.size() < 2)
                return false;
            int tx = ((Number) args.get(0)).intValue();
            int th = ((Number) args.get(1)).intValue();
            // Stessa colonna, ma troppo in alto, scava (non consentito sul pavimento quota
            // <= 1 né con scala presente)
            return cs.getMonkeyX() == tx
                    && cs.getMonkeyH() > th
                    && cs.getHeightAt(tx) > 1
                    && !cs.isHoldingLadder()
                    && !(cs.getLadderX() == cs.getMonkeyX() && !cs.isHoldingLadder());
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            int tx = ((Number) args.get(0)).intValue();
            int th = ((Number) args.get(1)).intValue();
            return List.of(
                    Task.primitive(CharlieOperators.OP_DIG),
                    Task.compound(REACH_POSITION, tx, th));
        }
    }

    /**
     * RECUPERO SCALA: se nessun passo diretto è applicabile (dislivello > 1 senza
     * scala),
     * va a recuperare la scala e poi ritenta la navigazione.
     */
    static final class ReachPositionFetchLadder implements Method {
        @Override
        public String getTaskName() {
            return REACH_POSITION;
        }

        @Override
        public String getName() {
            return "ReachPosition_FetchLadder";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.size() < 2)
                return false;
            return !cs.isHoldingLadder()
                    && cs.getMonkeyX() != cs.getLadderX();
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int tx = ((Number) args.get(0)).intValue();
            int th = ((Number) args.get(1)).intValue();
            return List.of(
                    // Raggiungi la posizione della scala
                    Task.compound(REACH_POSITION, cs.getLadderX(), cs.getLadderH()),
                    // Raccogli la scala
                    Task.primitive(CharlieOperators.OP_HOLD_LADDER),
                    // Raggiungi il target con la scala in mano
                    Task.compound(REACH_POSITION, tx, th));
        }
    }

    // RELOCATE_LADDER

    /** BASE: Charlie ha già raggiunto la colonna target con la scala in mano */
    static final class RelocateLadderBase implements Method {
        @Override
        public String getTaskName() {
            return RELOCATE_LADDER;
        }

        @Override
        public String getName() {
            return "RelocateLadder_Base";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.isEmpty())
                return false;
            int tx = ((Number) args.get(0)).intValue();
            return cs.isHoldingLadder() && cs.getMonkeyX() == tx;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            return Collections.emptyList();
        }
    }

    /** TRASPORTO: Charlie ha già la scala in mano ma non è alla colonna target */
    static final class RelocateLadderCarry implements Method {
        @Override
        public String getTaskName() {
            return RELOCATE_LADDER;
        }

        @Override
        public String getName() {
            return "RelocateLadder_Carry";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.isEmpty())
                return false;
            int tx = ((Number) args.get(0)).intValue();
            return cs.isHoldingLadder() && cs.getMonkeyX() != tx;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            int tx = ((Number) args.get(0)).intValue();
            return List.of(
                    Task.compound(REACH_POSITION, tx));
        }
    }

    /**
     * RECUPERO E TRASPORTO: Charlie non ha la scala -> va a prenderla e la porta a
     * targetX.
     */
    static final class RelocateLadderFetch implements Method {
        @Override
        public String getTaskName() {
            return RELOCATE_LADDER;
        }

        @Override
        public String getName() {
            return "RelocateLadder_Fetch";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            return !cs.isHoldingLadder();
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int tx = ((Number) args.get(0)).intValue();
            return List.of(
                    Task.compound(REACH_POSITION, cs.getLadderX(), cs.getLadderH()),
                    Task.primitive(CharlieOperators.OP_HOLD_LADDER),
                    Task.compound(REACH_POSITION, tx));
        }
    }

    // ADJUST_HEIGHT_UNDER_BANANA
    // Porta la colonna della banana alla quota corretta (bananaH - 1)
    // in modo che Charlie possa poi grabbarlo.

    /** BASE: la colonna della banana ha già l'altezza corretta */
    static final class AdjustHeightBase implements Method {
        @Override
        public String getTaskName() {
            return ADJUST_HEIGHT_BANANA;
        }

        @Override
        public String getName() {
            return "AdjustHeight_Base";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            int requiredH = cs.getBananaH() - 1;
            return cs.getHeightAt(cs.getBananaX()) == requiredH;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            return Collections.emptyList(); // Nessun aggiustamento necessario
        }
    }

    /**
     * SCAVO MIRATO: la colonna della banana è troppo alta rispetto al livello
     * richiesto. Charlie deve recarsi lì e scavare.
     */
    static final class AdjustHeightDig implements Method {
        @Override
        public String getTaskName() {
            return ADJUST_HEIGHT_BANANA;
        }

        @Override
        public String getName() {
            return "AdjustHeight_Dig";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            int bananaX = cs.getBananaX();
            int requiredH = cs.getBananaH() - 1;
            // La colonna è troppo alta → scavare
            return cs.getHeightAt(bananaX) > requiredH;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int bananaX = cs.getBananaX();
            int currentH = cs.getHeightAt(bananaX);
            // Vai a bananaX, scava, poi ricontrolla
            return List.of(
                    Task.compound(REACH_POSITION, bananaX, currentH),
                    Task.primitive(CharlieOperators.OP_DIG),
                    Task.compound(ADJUST_HEIGHT_BANANA));
        }
    }

    // PUSH_BOX_SEQUENCE

    /**
     * SPINTA A DESTRA: spinge una scatola dalla colonna corrente verso destra
     */
    static final class PushBoxSequenceRight implements Method {
        @Override
        public String getTaskName() {
            return PUSH_BOX_SEQUENCE;
        }

        @Override
        public String getName() {
            return "PushBoxSequence_Right";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.size() < 1)
                return false;
            String direction = args.get(0).toString();
            if (!"right".equals(direction))
                return false;

            int x = cs.getMonkeyX();
            int x1 = x + 1;
            int x2 = x + 2;
            return x2 <= WorldConstants.MAX_X
                    && cs.getHeightAt(x1) > 0
                    && cs.getHeightAt(x2) < WorldConstants.MAX_HEIGHT
                    && Math.abs(cs.getHeightAt(x1) - cs.getMonkeyH()) <= 1;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            return List.of(Task.primitive(CharlieOperators.OP_PUSH_RIGHT));
        }
    }

    /**
     * SPINTA A SINISTRA: spinge una scatola dalla colonna corrente verso sinistra.
     */
    static final class PushBoxSequenceLeft implements Method {
        @Override
        public String getTaskName() {
            return PUSH_BOX_SEQUENCE;
        }

        @Override
        public String getName() {
            return "PushBoxSequence_Left";
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (args.size() < 1)
                return false;
            String direction = args.get(0).toString();
            if (!"left".equals(direction))
                return false;

            int x = cs.getMonkeyX();
            int x1 = x - 1;
            int x2 = x - 2;
            return x2 >= WorldConstants.MIN_X
                    && cs.getHeightAt(x1) > 0
                    && cs.getHeightAt(x2) < WorldConstants.MAX_HEIGHT
                    && Math.abs(cs.getHeightAt(x1) - cs.getMonkeyH()) <= 1;
        }

        @Override
        public List<Task> decompose(State state, List<Object> args) {
            return List.of(Task.primitive(CharlieOperators.OP_PUSH_LEFT));
        }
    }
}
