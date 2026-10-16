package it.uniroma1.charlie.domain;

import it.uniroma1.charlie.htn.Domain;
import it.uniroma1.charlie.htn.Operator;
import it.uniroma1.charlie.model.CharlieState;
import it.uniroma1.charlie.model.ScenarioFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class OperatorTest {

    private Domain domain;

    @BeforeEach
    void setUp() {
        domain = CharlieDomain.createDomain();
    }

    @Test
    @DisplayName("MoveLeft e MoveRight su Charlie nello stato iniziale")
    void testMoveOperators() {
        CharlieState state = ScenarioFactory.createFigure1Scenario();
        // Charlie è a x=13 (h=3), a sinistra x=12 ha h=4 (diff 1 -> consentito)
        Operator moveLeft = domain.getOperator(CharlieOperators.OP_MOVE_LEFT);
        assertNotNull(moveLeft);
        assertTrue(moveLeft.canApply(state, Collections.emptyList()));

        moveLeft.apply(state, Collections.emptyList());
        assertEquals(12, state.getMonkeyX());
        assertEquals(4, state.getMonkeyH());

        // Torna a destra a x=13
        Operator moveRight = domain.getOperator(CharlieOperators.OP_MOVE_RIGHT);
        assertNotNull(moveRight);
        assertTrue(moveRight.canApply(state, Collections.emptyList()));
        moveRight.apply(state, Collections.emptyList());
        assertEquals(13, state.getMonkeyX());
        assertEquals(3, state.getMonkeyH());
    }

    @Test
    @DisplayName("Raccolta e rilascio della scala")
    void testHoldAndDropLadder() {
        CharlieState state = ScenarioFactory.createFigure1Scenario();
        Operator holdOp = domain.getOperator(CharlieOperators.OP_HOLD_LADDER);
        Operator dropOp = domain.getOperator(CharlieOperators.OP_DROP_LADDER);
        assertNotNull(holdOp);
        assertNotNull(dropOp);

        // Charlie è a x=13, scala è a x=2: non può raccoglierla da lontano
        assertFalse(holdOp.canApply(state, Collections.emptyList()));
        assertFalse(dropOp.canApply(state, Collections.emptyList()));

        // Spostiamo Charlie sopra la scala (x=2, h=2)
        state.setMonkeyX(2);
        state.setMonkeyH(2);
        assertTrue(holdOp.canApply(state, Collections.emptyList()));

        holdOp.apply(state, Collections.emptyList());
        assertTrue(state.isHoldingLadder());

        // Ora può rilasciarla
        assertTrue(dropOp.canApply(state, Collections.emptyList()));
        dropOp.apply(state, Collections.emptyList());
        assertFalse(state.isHoldingLadder());
        assertEquals(2, state.getLadderX());
        assertEquals(2, state.getLadderH());
    }

    @Test
    @DisplayName("Scavo (Dig) della scatola sotto i piedi")
    void testDigOperator() {
        CharlieState state = ScenarioFactory.createFigure1Scenario();
        Operator digOp = domain.getOperator(CharlieOperators.OP_DIG);
        assertNotNull(digOp);

        int prevH = state.getMonkeyH();
        assertTrue(digOp.canApply(state, Collections.emptyList()));

        digOp.apply(state, Collections.emptyList());
        assertEquals(prevH - 1, state.getMonkeyH());
        assertEquals(prevH - 1, state.getHeightAt(state.getMonkeyX()));
    }
}
