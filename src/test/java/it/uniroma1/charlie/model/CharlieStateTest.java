package it.uniroma1.charlie.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharlieStateTest {

    @Test
    @DisplayName("Lo stato di Figura 1.1(a) deve essere configurato correttamente")
    void testFigure1ScenarioInitialization() {
        CharlieState state = ScenarioFactory.createFigure1Scenario();

        assertNotNull(state);
        assertEquals(13, state.getMonkeyX());
        assertEquals(3, state.getMonkeyH());
        assertFalse(state.isHoldingLadder());
        assertEquals(2, state.getLadderX());
        assertEquals(2, state.getLadderH());
        assertEquals(8, state.getBananaX());
        assertEquals(4, state.getBananaH());
        assertFalse(state.hasBanana());

        int[] heights = state.getColumnHeights();
        assertEquals(WorldConstants.GRID_WIDTH, heights.length);
        assertEquals(2, heights[0]);
        assertEquals(2, heights[2]);
        assertEquals(3, heights[3]);
        assertEquals(4, heights[12]);
        assertEquals(3, heights[13]);
        assertEquals(5, heights[17]);
    }

    @Test
    @DisplayName("La copia dello stato deve essere profonda e indipendente")
    void testStateDeepCopy() {
        CharlieState original = ScenarioFactory.createFigure1Scenario();
        CharlieState copy = original.copy();

        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());

        // Modifica della copia
        copy.setMonkeyX(10);
        copy.decrementHeightAt(13);

        assertNotEquals(original.getMonkeyX(), copy.getMonkeyX());
        assertNotEquals(original.getHeightAt(13), copy.getHeightAt(13));
        assertNotEquals(original, copy);
    }

    @Test
    @DisplayName("Verifica corretta condizione 'isDirectlyUnderBanana'")
    void testDirectlyUnderBanana() {
        CharlieState state = ScenarioFactory.createFigure1Scenario();
        assertFalse(state.isDirectlyUnderBanana());

        state.setMonkeyX(8);
        state.setMonkeyH(3);
        assertTrue(state.isDirectlyUnderBanana());
    }
}
