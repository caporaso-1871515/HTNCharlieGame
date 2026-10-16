package it.uniroma1.charlie.model;

import it.uniroma1.charlie.core.WorldState;

/**
 * Rappresenta lo stato completo del mondo per il problema di Charlie
 * (Esercitazione B.2.8).
 * Estende WorldState per garantire interoperabilità completa tra planner
 * classici e HTN.
 */
public class CharlieState extends WorldState {

    private static final long serialVersionUID = 1L;

    public CharlieState(int monkeyX,
            int monkeyH,
            boolean holdingLadder,
            int ladderX,
            int ladderH,
            int bananaX,
            int bananaH,
            boolean hasBanana,
            int[] columnHeights) {
        super(monkeyX, monkeyH, holdingLadder, ladderX, ladderH, bananaX, bananaH, hasBanana, columnHeights);
    }

    public CharlieState(WorldState state) {
        super(state);
    }

    @Override
    public CharlieState copy() {
        return new CharlieState(this);
    }

    @Override
    public CharlieState clone() {
        return copy();
    }
}
