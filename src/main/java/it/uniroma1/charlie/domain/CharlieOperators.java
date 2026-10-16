package it.uniroma1.charlie.domain;

import it.uniroma1.charlie.htn.Domain;
import it.uniroma1.charlie.htn.Operator;
import it.uniroma1.charlie.htn.State;
import it.uniroma1.charlie.model.CharlieState;
import it.uniroma1.charlie.model.WorldConstants;

import java.util.List;

/**
 * Catalogo unico di tutti gli operatori primitivi atomici del dominio di
 * Charlie.
 * Ogni operatore corrisponde a un'azione fisica elementare indivisibile, con:
 * - Precondizioni (canApply): booleane, valutate sullo stato corrente.
 * - Effetti (apply): mutano la copia fornita dello stato e la restituiscono.
 * - Nomenclatura delle costanti dei nomi (usate da CharlieMethods e dai test):
 * - !move_right, !move_left: spostamento orizzontale (Δh ≤ 1)
 * - !hold_ladder, !drop_ladder: raccolta/rilascio scala
 * - !climb_ladder_right, !climb_ladder_left: salita/discesa da
 * scala (Δh = 2)
 * - !push_right, !push_left: spinta scatola con effetto gravità
 * - !dig: scavo della scatola su cui si poggia
 * - !grab_banana: raccolta della banana (precond: sotto di 1)
 */
public final class CharlieOperators {

    private CharlieOperators() {
    }

    // Costanti dei nomi canonici degli operatori

    public static final String OP_MOVE_RIGHT = "move_right";
    public static final String OP_MOVE_LEFT = "move_left";
    public static final String OP_HOLD_LADDER = "hold_ladder";
    public static final String OP_DROP_LADDER = "drop_ladder";
    public static final String OP_CLIMB_LADDER_RIGHT = "climb_ladder_right";
    public static final String OP_CLIMB_LADDER_LEFT = "climb_ladder_left";
    public static final String OP_PUSH_RIGHT = "push_right";
    public static final String OP_PUSH_LEFT = "push_left";
    public static final String OP_PUSH_LADDER_RAMP_RIGHT = "push_ladder_ramp_right";
    public static final String OP_PUSH_LADDER_RAMP_LEFT = "push_ladder_ramp_left";
    public static final String OP_PUSH_FROM_LADDER_RIGHT = "push_from_ladder_right";
    public static final String OP_PUSH_FROM_LADDER_LEFT = "push_from_ladder_left";
    public static final String OP_DIG = "dig";
    public static final String OP_GRAB_BANANA = "grab_banana";

    /**
     * Registra tutti gli operatori definiti in questa classe nel dominio
     * specificato.
     *
     * @param domain Il dominio HTN in cui registrare gli operatori.
     */
    public static void registerAll(Domain domain) {
        domain.registerOperator(new MoveRightOp());
        domain.registerOperator(new MoveLeftOp());
        domain.registerOperator(new HoldLadderOp());
        domain.registerOperator(new DropLadderOp());
        domain.registerOperator(new ClimbLadderRightOp());
        domain.registerOperator(new ClimbLadderLeftOp());
        domain.registerOperator(new PushRightOp());
        domain.registerOperator(new PushLeftOp());
        domain.registerOperator(new PushBoxLadderRampRightOp());
        domain.registerOperator(new PushBoxLadderRampLeftOp());
        domain.registerOperator(new PushFromLadderRightOp());
        domain.registerOperator(new PushFromLadderLeftOp());
        domain.registerOperator(new DigOp());
        domain.registerOperator(new GrabBananaOp());
    }

    /**
     * Spostamento orizzontale di Charlie verso destra di una posizione (colonna
     * x+1).
     *
     * Fisica STRIPS:
     * - Precondizioni: monkeyX + 1 <= MAX_X e dislivello a gradino
     * standard |heights[x+1] - monkeyH| <= 1. Nessun salto di 2 è consentito
     * senza scalata.
     * - Add List: monkeyX <- x+1, monkeyH <- heights[x+1]. Se
     * trasporta la scala (holdingLadder), le coordinate della scala
     * seguono solidalmente Charlie.
     * - Delete List: vecchia posizione (monkeyX, monkeyH).
     * Se trasporta la scala (holdingLadder), le coordinate della scala
     * seguono solidalmente Charlie.
     */
    static final class MoveRightOp implements Operator {
        @Override
        public String getName() {
            return OP_MOVE_RIGHT;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            int nx = cs.getMonkeyX() + 1;
            if (nx > WorldConstants.MAX_X)
                return false;
            int dh = Math.abs(cs.getHeightAt(nx) - cs.getMonkeyH());
            return dh <= WorldConstants.MAX_STEP_WITHOUT_LADDER;
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int nx = cs.getMonkeyX() + 1;
            int nh = cs.getHeightAt(nx);
            cs.setMonkeyX(nx);
            cs.setMonkeyH(nh);
            if (cs.isHoldingLadder()) {
                cs.setLadderX(nx);
                cs.setLadderH(nh);
            }
            return cs;
        }
    }

    /**
     * Spostamento orizzontale di Charlie verso sinistra di una posizione (colonna
     * x-1).
     *
     * Fisica STRIPS:
     * Precondizioni: monkeyX - 1 >= MIN_X e dislivello a gradino
     * standard |heights[x-1] - monkeyH| <= 1.
     * Add List: monkeyX <- x-1, monkeyH <- heights[x-1].
     * Se trasporta la scala, segue solidalmente.
     * Delete List: vecchia posizione (monkeyX, monkeyH).
     * Se trasporta la scala, segue solidalmente.
     */
    static final class MoveLeftOp implements Operator {
        @Override
        public String getName() {
            return OP_MOVE_LEFT;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            int nx = cs.getMonkeyX() - 1;
            if (nx < WorldConstants.MIN_X)
                return false;
            int dh = Math.abs(cs.getHeightAt(nx) - cs.getMonkeyH());
            return dh <= WorldConstants.MAX_STEP_WITHOUT_LADDER;
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int nx = cs.getMonkeyX() - 1;
            int nh = cs.getHeightAt(nx);
            cs.setMonkeyX(nx);
            cs.setMonkeyH(nh);
            if (cs.isHoldingLadder()) {
                cs.setLadderX(nx);
                cs.setLadderH(nh);
            }
            return cs;
        }
    }

    /**
     * Charlie raccoglie la scala a terra nella sua posizione corrente.
     *
     * Pre: ¬holdingLadder ∧ monkeyX == ladderX ∧ monkeyH == ladderH.
     * Eff: holdingLadder = true; la scala non è più "a terra".
     */
    static final class HoldLadderOp implements Operator {
        @Override
        public String getName() {
            return OP_HOLD_LADDER;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            return cs.isAtLadderPosition();
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            cs.setHoldingLadder(true);
            cs.setLadderX(cs.getMonkeyX());
            cs.setLadderH(cs.getMonkeyH());
            return cs;
        }
    }

    /**
     * Charlie posa la scala a terra nella sua posizione corrente.
     *
     * Pre: holdingLadder.
     * Eff: holdingLadder = false; ladderX = monkeyX; ladderH = monkeyH.
     */
    static final class DropLadderOp implements Operator {
        @Override
        public String getName() {
            return OP_DROP_LADDER;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            return cs.isHoldingLadder();
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            cs.setHoldingLadder(false);
            cs.setLadderX(cs.getMonkeyX());
            cs.setLadderH(cs.getMonkeyH());
            return cs;
        }
    }

    /**
     * Charlie usa la scala a terra per superare un dislivello di 2 verso destra.
     *
     * Fisica: la scala è posizionata nella colonna adiacente
     * a quota uguale a quella di Charlie; Charlie sale sulla colonna superiore
     * di 2 livelli usando la scala come appoggio. La scala rimane al suolo.
     *
     * Pre: ¬holdingLadder ∧ x+1 valido ∧
     * ladderAt(x+1, monkeyH) ∧ |h(x+1) - monkeyH| == 2.
     * Eff: monkeyX = x+1; monkeyH = h(x+1).
     */
    static final class ClimbLadderRightOp implements Operator {
        @Override
        public String getName() {
            return OP_CLIMB_LADDER_RIGHT;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (cs.isHoldingLadder())
                return false;

            int nx = cs.getMonkeyX() + 1;
            if (nx > WorldConstants.MAX_X)
                return false;

            int targetH = cs.getHeightAt(nx);
            int dh = targetH - cs.getMonkeyH(); // deve essere +2 o -2
            if (Math.abs(dh) != 2)
                return false;

            // La scala deve trovarsi nella colonna di arrivo (a terra)
            // alla quota base del salto (quella di Charlie o quella inferiore)
            int ladderRequiredH = Math.min(cs.getMonkeyH(), targetH);
            boolean ladderAtLower = (cs.getLadderX() == cs.getMonkeyX() || cs.getLadderX() == nx)
                    && cs.getLadderH() == ladderRequiredH;
            return ladderAtLower;
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int nx = cs.getMonkeyX() + 1;
            cs.setMonkeyX(nx);
            cs.setMonkeyH(cs.getHeightAt(nx));
            return cs;
        }
    }

    /**
     * Simmetrico di ClimbLadderRight ma verso sinistra (x−1).
     */
    static final class ClimbLadderLeftOp implements Operator {
        @Override
        public String getName() {
            return OP_CLIMB_LADDER_LEFT;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (cs.isHoldingLadder())
                return false;

            int nx = cs.getMonkeyX() - 1;
            if (nx < WorldConstants.MIN_X)
                return false;

            int targetH = cs.getHeightAt(nx);
            int dh = targetH - cs.getMonkeyH();
            if (Math.abs(dh) != 2)
                return false;

            int ladderRequiredH = Math.min(cs.getMonkeyH(), targetH);
            boolean ladderAtLower = (cs.getLadderX() == cs.getMonkeyX() || cs.getLadderX() == nx)
                    && cs.getLadderH() == ladderRequiredH;
            return ladderAtLower;
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int nx = cs.getMonkeyX() - 1;
            cs.setMonkeyX(nx);
            cs.setMonkeyH(cs.getHeightAt(nx));
            return cs;
        }
    }

    /**
     * Charlie spinge la scatola in cima alla colonna adiacente destra verso
     * la colonna successiva (effetto gravità).
     *
     * Fisica:
     * Charlie è in x a quota h.
     * La cima di x+1 è alla stessa quota h (condizione "stessa altezza") oppure
     * Charlie può comunque spingere con Δh ≤ 1 prendendo slancio.
     * La cima di x+2 deve essere < MAX_HEIGHT (spazio per ricevere la
     * scatola).
     * Dopo la spinta: height[x+1]−−; height[x+2]++; Charlie avanza a x+1.
     *
     * Pre: monkeyX+1 ≤ MAX_X-1 ∧ h(x+1) > 0 ∧ h(x+2) < MAX_HEIGHT
     * ∧ |h(x+1) - monkeyH| ≤ 1 (spinta orizzontale).
     * Eff: height[x+1]--; height[x+2]++; monkeyX = x+1; monkeyH = h(x+1).
     */
    static final class PushRightOp implements Operator {
        @Override
        public String getName() {
            return OP_PUSH_RIGHT;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (cs.isHoldingLadder())
                return false;
            int x = cs.getMonkeyX();
            int x1 = x + 1;
            int x2 = x + 2;
            if (x2 > WorldConstants.MAX_X)
                return false;
            int boxH = cs.getHeightAt(x1);
            if (boxH != cs.getMonkeyH() + 1 || boxH <= 0)
                return false;
            return cs.getHeightAt(x2) < boxH;
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int x = cs.getMonkeyX();
            int x1 = x + 1;
            int x2 = x + 2;
            cs.decrementHeightAt(x1);
            cs.incrementHeightAt(x2);
            cs.setMonkeyX(x1);
            cs.setMonkeyH(cs.getHeightAt(x1));
            return cs;
        }
    }

    /**
     * Simmetrico di PushRight ma verso sinistra (x-1 -> x-2).
     */
    static final class PushLeftOp implements Operator {
        @Override
        public String getName() {
            return OP_PUSH_LEFT;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (cs.isHoldingLadder())
                return false;
            int x = cs.getMonkeyX();
            int x1 = x - 1;
            int x2 = x - 2;
            if (x2 < WorldConstants.MIN_X)
                return false;
            int boxH = cs.getHeightAt(x1);
            if (boxH != cs.getMonkeyH() + 1 || boxH <= 0)
                return false;
            return cs.getHeightAt(x2) < boxH;
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int x = cs.getMonkeyX();
            int x1 = x - 1;
            int x2 = x - 2;
            cs.decrementHeightAt(x1);
            cs.incrementHeightAt(x2);
            cs.setMonkeyX(x1);
            cs.setMonkeyH(cs.getHeightAt(x1));
            return cs;
        }
    }

    /**
     * Charlie spinge una scatola facendola salire su una rampa formata dalla scala
     * a terra in configurazione a 4 posizioni contigue (x, x+1, x+2, x+3).
     * Fisica: Charlie in x (quota h), scatola in cima a x+1 (quota h+1),
     * scala a terra in x+2 (quota h), colonna di arrivo x+3 a quota h+1.
     * La scatola sale sulla scala (+2 posizioni, +1 quota) impilandosi su x+3 a
     * quota h+2.
     * Charlie avanza di 2 posizioni posizionandosi in x+2 dentro la scala a quota
     * h.
     */
    static final class PushBoxLadderRampRightOp implements Operator {
        @Override
        public String getName() {
            return OP_PUSH_LADDER_RAMP_RIGHT;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (cs.isHoldingLadder())
                return false;
            int x = cs.getMonkeyX();
            int x1 = x + 1;
            int x2 = x + 2;
            int x3 = x + 3;
            if (x3 > WorldConstants.MAX_X)
                return false;
            if (cs.getHeightAt(x1) != cs.getMonkeyH() + 1)
                return false;
            if (cs.getLadderX() != x2 || cs.getLadderH() != cs.getMonkeyH())
                return false;
            if (cs.getHeightAt(x2) != cs.getMonkeyH())
                return false;
            return cs.getHeightAt(x3) == cs.getMonkeyH() + 1;
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int x = cs.getMonkeyX();
            int x1 = x + 1;
            int x2 = x + 2;
            int x3 = x + 3;
            cs.decrementHeightAt(x1);
            cs.incrementHeightAt(x3);
            cs.setMonkeyX(x2);
            cs.setMonkeyH(cs.getHeightAt(x2));
            return cs;
        }
    }

    static final class PushBoxLadderRampLeftOp implements Operator {
        @Override
        public String getName() {
            return OP_PUSH_LADDER_RAMP_LEFT;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (cs.isHoldingLadder())
                return false;
            int x = cs.getMonkeyX();
            int x1 = x - 1;
            int x2 = x - 2;
            int x3 = x - 3;
            if (x3 < WorldConstants.MIN_X)
                return false;
            if (cs.getHeightAt(x1) != cs.getMonkeyH() + 1)
                return false;
            if (cs.getLadderX() != x2 || cs.getLadderH() != cs.getMonkeyH())
                return false;
            if (cs.getHeightAt(x2) != cs.getMonkeyH())
                return false;
            return cs.getHeightAt(x3) == cs.getMonkeyH() + 1;
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int x = cs.getMonkeyX();
            int x1 = x - 1;
            int x2 = x - 2;
            int x3 = x - 3;
            cs.decrementHeightAt(x1);
            cs.incrementHeightAt(x3);
            cs.setMonkeyX(x2);
            cs.setMonkeyH(cs.getHeightAt(x2));
            return cs;
        }
    }

    /**
     * Charlie si trova in x sopra la scala a terra e spinge una scatola adiacente
     * verso la colonna successiva x+2.
     */
    static final class PushFromLadderRightOp implements Operator {
        @Override
        public String getName() {
            return OP_PUSH_FROM_LADDER_RIGHT;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (cs.isHoldingLadder())
                return false;
            int x = cs.getMonkeyX();
            if (cs.getLadderX() != x || cs.getLadderH() != cs.getMonkeyH())
                return false;
            int x1 = x + 1;
            int x2 = x + 2;
            if (x2 > WorldConstants.MAX_X)
                return false;
            int boxH = cs.getHeightAt(x1);
            if (boxH != cs.getMonkeyH() + 1 && boxH != cs.getMonkeyH() + 2)
                return false;
            return cs.getHeightAt(x2) <= boxH - 1;
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int x = cs.getMonkeyX();
            int x1 = x + 1;
            int x2 = x + 2;
            cs.decrementHeightAt(x1);
            cs.incrementHeightAt(x2);
            cs.setMonkeyX(x1);
            cs.setMonkeyH(cs.getHeightAt(x1));
            return cs;
        }
    }

    static final class PushFromLadderLeftOp implements Operator {
        @Override
        public String getName() {
            return OP_PUSH_FROM_LADDER_LEFT;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (cs.isHoldingLadder())
                return false;
            int x = cs.getMonkeyX();
            if (cs.getLadderX() != x || cs.getLadderH() != cs.getMonkeyH())
                return false;
            int x1 = x - 1;
            int x2 = x - 2;
            if (x2 < WorldConstants.MIN_X)
                return false;
            int boxH = cs.getHeightAt(x1);
            if (boxH != cs.getMonkeyH() + 1 && boxH != cs.getMonkeyH() + 2)
                return false;
            return cs.getHeightAt(x2) <= boxH - 1;
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int x = cs.getMonkeyX();
            int x1 = x - 1;
            int x2 = x - 2;
            cs.decrementHeightAt(x1);
            cs.incrementHeightAt(x2);
            cs.setMonkeyX(x1);
            cs.setMonkeyH(cs.getHeightAt(x1));
            return cs;
        }
    }

    /**
     * Charlie distrugge la scatola su cui si trova, scendendo di un livello.
     *
     * Fisica: la scatola superiore della colonna corrente viene
     * rimossa. Charlie scende di 1 quota. Non è consentito se la scala è a terra
     * nella stessa colonna, né su pavimento indistruttibile (quota <= 1).
     * Pre: not holdingLadder and ladderX != monkeyX and height[monkeyX] > 1.
     * Eff: height[monkeyX]--, monkeyH--.
     */
    static final class DigOp implements Operator {
        @Override
        public String getName() {
            return OP_DIG;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            if (cs.isHoldingLadder())
                return false;
            int x = cs.getMonkeyX();
            if (!cs.isHoldingLadder() && cs.getLadderX() == x)
                return false;
            return cs.getHeightAt(x) > 1 && cs.getMonkeyH() == cs.getHeightAt(x);
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            int x = cs.getMonkeyX();
            cs.decrementHeightAt(x);
            cs.setMonkeyH(cs.getHeightAt(x));
            return cs;
        }
    }

    /**
     * Charlie afferra la banana appesa al soffitto.
     *
     * Fisica: Charlie deve trovarsi in piedi sulla sommità
     * della colonna della banana, esattamente una quota sotto la banana (quota =
     * bananaH − 1),
     * a mani vuote senza trasportare la scala.
     * Pre: not holdingLadder and not hasBanana and monkeyX == bananaX and monkeyH
     * ==
     * bananaH - 1.
     * Eff: hasBanana = true, bananaGrabbed = true.
     */
    static final class GrabBananaOp implements Operator {
        @Override
        public String getName() {
            return OP_GRAB_BANANA;
        }

        @Override
        public boolean canApply(State state, List<Object> args) {
            if (!(state instanceof CharlieState cs))
                return false;
            return !cs.isHoldingLadder() && !cs.hasBanana()
                    && cs.getMonkeyX() == cs.getBananaX()
                    && cs.getMonkeyH() == cs.getBananaH() - 1;
        }

        @Override
        public State apply(State state, List<Object> args) {
            CharlieState cs = (CharlieState) state;
            cs.setHasBanana(true);
            cs.setBananaGrabbed(true);
            return cs;
        }
    }
}
