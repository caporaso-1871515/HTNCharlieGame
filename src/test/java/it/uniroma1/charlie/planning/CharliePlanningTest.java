package it.uniroma1.charlie.planning;

import it.uniroma1.charlie.domain.CharlieDomain;
import it.uniroma1.charlie.domain.CharlieMethods;
import it.uniroma1.charlie.domain.CharlieOperators;
import it.uniroma1.charlie.htn.*;
import it.uniroma1.charlie.model.CharlieState;
import it.uniroma1.charlie.model.ScenarioFactory;
import it.uniroma1.charlie.model.WorldConstants;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di test di integrazione end-to-end per l'HTN Planner del dominio di
 * Charlie.
 *
 * <h2>Struttura dei test</h2>
 * <ol>
 * <li><b>Scenario Semplice</b>: micro-mondo piatto, piano in ≤ 5 passi.</li>
 * <li><b>Scenario Figura 1.1(a)</b>: istanza originale dell'esercitazione
 * B.2.8.</li>
 * <li><b>Verifica Invarianti Fisici</b>: ogni azione del piano rispetta le
 * precondizioni
 * e gli effetti attesi (simulazione passo-passo).</li>
 * <li><b>Test degli Operatori Primitivi</b>: precondizioni e
 * post-condizioni.</li>
 * <li><b>Test Parametrici</b>: scenari sintetici con configurazioni
 * diverse.</li>
 * </ol>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CharliePlanningTest {

    private static Domain domain;
    private static HTNPlanner planner;

    @BeforeAll
    static void initDomain() {
        domain = CharlieDomain.createDomain();
        planner = new HTNPlanner(domain, 500, 200_000);
        // Abilitiamo il debug per avere un trace completo nei log dei test
        planner.setDebug(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 1. SCENARIO SEMPLICE
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("Scenario semplice: piano trovato e banana raccolta")
    void testSimpleScenario_planFound() {
        CharlieState initialState = ScenarioFactory.createSimpleScenario();
        System.out.println("\n[TEST] Stato Iniziale (semplice): " + initialState);

        Plan plan = planner.plan(initialState, Task.compound(CharlieMethods.SOLVE_CHARLIE));

        assertNotNull(plan, "Il piano non deve essere null");
        assertTrue(plan.isSuccess(),
                "Il planner deve trovare un piano valido.\nMotivo fallimento: " + plan.getMessage());
        assertFalse(plan.isEmpty(), "Il piano non deve essere vuoto");

        System.out.println("[TEST] Piano semplice trovato: " + plan);

        // ── Simulazione passo-passo ──
        CharlieState execState = executePlan(initialState, plan);
        assertTrue(execState.hasBanana(),
                "Dopo l'esecuzione del piano Charlie deve avere la banana");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 2. SCENARIO FIGURA 1.1(a)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("Scenario Fig 1.1(a): piano trovato")
    void testFigure1Scenario_planFound() {
        CharlieState initialState = ScenarioFactory.createFigure1Scenario();
        System.out.println("\n[TEST] Stato Iniziale (Figura 1.1a): " + initialState);

        Plan plan = planner.plan(initialState, Task.compound(CharlieMethods.SOLVE_CHARLIE));

        assertNotNull(plan);
        assertTrue(plan.isSuccess(),
                "Il planner DEVE trovare un piano valido per lo scenario di Figura 1.1(a).\n"
                        + "Motivo fallimento: " + plan.getMessage()
                        + "\nNodi visitati: " + plan.getNodesVisited());

        System.out.println("[TEST] Piano Figura 1.1(a): " + plan);
    }

    @Test
    @Order(3)
    @DisplayName("Scenario Fig 1.1(a): piano eseguito correttamente passo-passo")
    void testFigure1Scenario_executionCorrectness() {
        CharlieState initialState = ScenarioFactory.createFigure1Scenario();
        Plan plan = planner.plan(initialState, Task.compound(CharlieMethods.SOLVE_CHARLIE));

        assumeTrue(plan.isSuccess(), "Questo test richiede che un piano valido sia stato trovato");

        System.out.println("\n[TEST] Esecuzione passo-passo piano Fig 1.1(a):");
        CharlieState state = initialState.copy();

        for (int i = 0; i < plan.getActions().size(); i++) {
            Task action = plan.getActions().get(i);
            System.out.printf("  Passo %2d: %s%n", i + 1, action);
            System.out.println("           Stato prima: " + state);

            Operator op = domain.getOperator(action.getName());
            assertNotNull(op,
                    "Operatore '" + action.getName() + "' non trovato nel dominio al passo " + (i + 1));

            // VERIFICA PRECONDIZIONI prima di ogni azione
            assertTrue(op.canApply(state, action.getArgs()),
                    "Precondizioni non soddisfatte al passo " + (i + 1) + " per: " + action
                            + "\nStato corrente: " + state);

            // ESECUZIONE: su copia dello stato (semantica non-distruttiva)
            state = (CharlieState) op.apply(state.copy(), action.getArgs());

            // VERIFICA CONSISTENZA dello stato dopo ogni azione
            assertTrue(state.isConsistent(),
                    "Stato inconsistente dopo passo " + (i + 1));

            System.out.println("           Stato dopo : " + state);
        }

        // VERIFICA OBIETTIVO FINALE
        assertTrue(state.hasBanana(),
                "Charlie deve avere la banana al termine del piano.\nStato finale: " + state);
        System.out.println("[TEST] SUCCESSO: banana raccolta! Stato finale: " + state);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 3. TEST INVARIANTI FISICI DEGLI OPERATORI
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Test Operatori Primitivi - Fisica del Dominio")
    class OperatorPhysicsTests {

        @Test
        @DisplayName("move_right: avanza e aggiorna posizione correttamente")
        void testMoveRight_basicNavigation() {
            // Scenario: terreno piatto a h=2
            int[] heights = uniformHeights(2);
            CharlieState state = new CharlieState(5, 2, false, 0, 2, 10, 3, false, heights);

            Operator op = domain.getOperator(CharlieOperators.OP_MOVE_RIGHT);
            assertTrue(op.canApply(state, List.of()));

            state = (CharlieState) op.apply(state.copy(), List.of());
            assertEquals(6, state.getMonkeyX());
            assertEquals(2, state.getMonkeyH()); // terreno piatto, stessa quota
        }

        @Test
        @DisplayName("move_right: bloccato da dislivello > 1 senza scala")
        void testMoveRight_blockedByStep() {
            int[] heights = uniformHeights(2);
            heights[6] = 4; // dislivello di +2 a destra
            CharlieState state = new CharlieState(5, 2, false, 0, 2, 10, 3, false, heights);

            Operator op = domain.getOperator(CharlieOperators.OP_MOVE_RIGHT);
            assertFalse(op.canApply(state, List.of()),
                    "Non deve poter muoversi con dislivello > 1 senza scala");
        }

        @Test
        @DisplayName("move_right con scala: dislivello di 2 non ammesso senza scalata")
        void testMoveRight_withLadder_cannotSurmountDiff2WithoutClimb() {
            int[] heights = uniformHeights(2);
            heights[6] = 4; // dislivello di +2
            // Charlie porta la scala
            CharlieState state = new CharlieState(5, 2, true, 5, 2, 10, 3, false, heights);

            Operator op = domain.getOperator(CharlieOperators.OP_MOVE_RIGHT);
            assertFalse(op.canApply(state, List.of()),
                    "Senza posare la scala e scalarla, move_right non supera dislivello 2");
        }

        @Test
        @DisplayName("hold_ladder / drop_ladder: ciclo raccolta-rilascio")
        void testLadderPickupDrop_cycle() {
            int[] heights = uniformHeights(2);
            // Scala in posizione di Charlie
            CharlieState state = new CharlieState(5, 2, false, 5, 2, 10, 3, false, heights);

            // 1. Verifica che la raccolta sia applicabile
            Operator holdOp = domain.getOperator(CharlieOperators.OP_HOLD_LADDER);
            assertTrue(holdOp.canApply(state, List.of()));
            state = (CharlieState) holdOp.apply(state.copy(), List.of());
            assertTrue(state.isHoldingLadder(), "Charlie deve avere la scala");

            // 2. Non può raccogliere di nuovo
            assertFalse(holdOp.canApply(state, List.of()),
                    "Non può raccogliere se già in mano");

            // 3. Rilascio della scala
            Operator dropOp = domain.getOperator(CharlieOperators.OP_DROP_LADDER);
            assertTrue(dropOp.canApply(state, List.of()));
            state = (CharlieState) dropOp.apply(state.copy(), List.of());
            assertFalse(state.isHoldingLadder(), "Charlie non deve più avere la scala");
            assertEquals(5, state.getLadderX());
            assertEquals(2, state.getLadderH());
        }

        @Test
        @DisplayName("dig: scava correttamente e non applicabile su pavimento h=0")
        void testDig_physicsAndFloorConstraint() {
            int[] heights = uniformHeights(2);
            CharlieState state = new CharlieState(5, 2, false, 0, 2, 10, 3, false, heights);

            Operator digOp = domain.getOperator(CharlieOperators.OP_DIG);
            assertTrue(digOp.canApply(state, List.of()));

            state = (CharlieState) digOp.apply(state.copy(), List.of());
            assertEquals(1, state.getMonkeyH(), "Charlie deve scendere di 1");
            assertEquals(1, state.getHeightAt(5), "Colonna deve diminuire di 1");

            // Sul pavimento h=1 non si può scavare
            assertFalse(digOp.canApply(state, List.of()),
                    "Non deve poter scavare sul pavimento indistruttibile (h=1)");
        }

        @Test
        @DisplayName("push_right: spinge scatola e aggiorna altezze correttamente")
        void testPushRight_gravityEffect() {
            int[] heights = uniformHeights(2);
            heights[5] = 2; // Charlie a h=2
            heights[6] = 3; // scatola in cima a x=6 (boxH = monkeyH + 1 = 3)
            heights[7] = 1; // Destinazione ha spazio (1 < 3)
            CharlieState state = new CharlieState(5, 2, false, 0, 2, 10, 3, false, heights);

            Operator pushOp = domain.getOperator(CharlieOperators.OP_PUSH_RIGHT);
            assertTrue(pushOp.canApply(state, List.of()), "Deve poter spingere a destra");

            state = (CharlieState) pushOp.apply(state.copy(), List.of());
            assertEquals(2, state.getHeightAt(6), "Colonna 6 deve scendere a quota 2");
            assertEquals(2, state.getHeightAt(7), "Colonna 7 deve salire di 1");
            assertEquals(6, state.getMonkeyX(), "Charlie avanza nella colonna liberata a quota 2");
        }

        @Test
        @DisplayName("grab_banana: precondizione 'direttamente sotto'")
        void testGrabBanana_precondition() {
            int[] heights = uniformHeights(2);
            CharlieState state = new CharlieState(5, 2, false, 0, 2, 8, 5, false, heights);

            Operator grabOp = domain.getOperator(CharlieOperators.OP_GRAB_BANANA);

            // Non in posizione
            assertFalse(grabOp.canApply(state, List.of()));

            // Portiamo Charlie direttamente sotto la banana
            state.setMonkeyX(8);
            state.setMonkeyH(4); // bananaH - 1 = 5 - 1 = 4
            assertTrue(grabOp.canApply(state, List.of()), "Deve poter prendere la banana");

            state = (CharlieState) grabOp.apply(state.copy(), List.of());
            assertTrue(state.hasBanana(), "Charlie deve avere la banana");
        }

        @Test
        @DisplayName("climb_ladder_right: usa scala per dislivello esatto di 2")
        void testClimbLadderRight() {
            int[] heights = uniformHeights(2);
            heights[6] = 4; // Dislivello di +2 rispetto a Charlie in x=5 h=2
            // Scala posizionata in x=6 a quota h=2 (quota base del salto)
            CharlieState state = new CharlieState(5, 2, false, 6, 2, 10, 3, false, heights);

            Operator climbOp = domain.getOperator(CharlieOperators.OP_CLIMB_LADDER_RIGHT);
            assertTrue(climbOp.canApply(state, List.of()),
                    "Deve poter salire con la scala (dislivello = 2)");

            state = (CharlieState) climbOp.apply(state.copy(), List.of());
            assertEquals(6, state.getMonkeyX());
            assertEquals(4, state.getMonkeyH());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 4. TEST PARAMETRICI CON SCENARI SINTETICI
    // ══════════════════════════════════════════════════════════════════════════

    @ParameterizedTest(name = "Scenario [{index}]: Charlie in x={0}, banana in x={1} h={2}, altezza col banana={3}")
    @CsvSource({
            // charlieX, bananaX, bananaH, heightAtBananaCol
            "5, 8, 4, 3", // banana raggiungibile in piano dopo 3 passi destra
            "0, 3, 3, 2", // banana a sinistra, Charlie parte da destra rispetto a essa
            "10, 10, 3, 2", // banana nella stessa colonna di Charlie, altezza già corretta
    })
    @Order(4)
    @DisplayName("Test parametrico: scenari sintetici con diverse posizioni")
    void testParametricScenarios(int charlieX, int bananaX, int bananaH, int heightAtBananaCol) {
        int[] heights = uniformHeights(2);
        heights[bananaX] = heightAtBananaCol; // Altezza della colonna della banana
        int charlieH = heights[charlieX];

        CharlieState state = new CharlieState(
                charlieX, charlieH,
                false, charlieX, charlieH, // scala in posizione di Charlie
                bananaX, bananaH,
                false, heights);

        Plan plan = planner.plan(state, Task.compound(CharlieMethods.SOLVE_CHARLIE));

        assertNotNull(plan);
        assertTrue(plan.isSuccess(),
                String.format("Piano non trovato per charlieX=%d, bananaX=%d, bananaH=%d: %s",
                        charlieX, bananaX, bananaH, plan.getMessage()));

        CharlieState finalState = executePlan(state, plan);
        assertTrue(finalState.hasBanana(),
                "Charlie deve afferrare la banana nel piano parametrico");
    }

    @Test
    @Order(5)
    @DisplayName("Charlie è già direttamente sotto la banana: piano di 1 sola azione")
    void testAlreadyUnderBanana_singleStepPlan() {
        int[] heights = uniformHeights(2);
        // Banana a bananaX=5, bananaH=3; Charlie a x=5, h=2 (bananaH-1=2)
        heights[5] = 2;
        CharlieState state = new CharlieState(5, 2, false, 0, 2, 5, 3, false, heights);

        assertTrue(state.isDirectlyUnderBanana(), "Pre-condizione: Charlie deve essere sotto la banana");

        Plan plan = planner.plan(state, Task.compound(CharlieMethods.SOLVE_CHARLIE));

        assertTrue(plan.isSuccess());
        assertEquals(1, plan.size(),
                "Il piano deve contenere una sola azione (grab_banana), trovate: " + plan.getActions());
        assertEquals(CharlieOperators.OP_GRAB_BANANA, plan.getActions().get(0).getName());
    }

    /**
     * Esegue il piano sull'istanza di stato fornita passo-passo,
     * verificando che le precondizioni di ogni operatore siano soddisfatte.
     *
     * @param initialState Lo stato di partenza (non modificato; si lavora su
     *                     copie).
     * @param plan         Il piano da eseguire.
     * @return Lo stato finale dopo l'esecuzione di tutte le azioni del piano.
     */
    private CharlieState executePlan(CharlieState initialState, Plan plan) {
        CharlieState state = initialState.copy();
        List<Task> actions = plan.getActions();

        for (int i = 0; i < actions.size(); i++) {
            Task action = actions.get(i);
            Operator op = domain.getOperator(action.getName());
            assertNotNull(op, "Operatore non trovato: " + action.getName());

            assertTrue(op.canApply(state, action.getArgs()),
                    "Precondizioni non soddisfatte al passo " + (i + 1)
                            + " per azione: " + action
                            + "\nStato corrente: " + state);

            state = (CharlieState) op.apply(state.copy(), action.getArgs());
            assertTrue(state.isConsistent(), "Stato inconsistente dopo passo " + (i + 1));
        }
        return state;
    }

    /** Crea un array di altezze uniforme su tutta la griglia. */
    private static int[] uniformHeights(int h) {
        int[] heights = new int[WorldConstants.GRID_WIDTH];
        java.util.Arrays.fill(heights, h);
        return heights;
    }

    /**
     * Assume la condizione
     * Se falso, il test viene saltato (non fallisce).
     */
    private static void assumeTrue(boolean condition, String message) {
        org.junit.jupiter.api.Assumptions.assumeTrue(condition, message);
    }
}
