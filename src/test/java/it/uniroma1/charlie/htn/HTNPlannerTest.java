package it.uniroma1.charlie.htn;

import it.uniroma1.charlie.domain.CharlieDomain;
import it.uniroma1.charlie.domain.CharlieMethods;
import it.uniroma1.charlie.model.CharlieState;
import it.uniroma1.charlie.model.ScenarioFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HTNPlannerTest {

    @Test
    @DisplayName("Pianificazione HTN su un micro-scenario raggiungibile")
    void testSimplePlanGeneration() {
        Domain domain = CharlieDomain.createDomain();
        HTNPlanner planner = new HTNPlanner(domain);

        // Micro-scenario: Charlie è in x=0 (h=2), banana in x=5 (h=3, target cell sotto
        // è h=2)
        CharlieState state = ScenarioFactory.createSimpleScenario();

        Task goalTask = Task.compound(CharlieMethods.SOLVE_CHARLIE);
        Plan plan = planner.plan(state, goalTask);

        assertNotNull(plan);
        assertTrue(plan.isSuccess(),
                "Il piano HTN dovrebbe trovare una sequenza di passi valida: " + plan.getMessage());
        assertFalse(plan.isEmpty());

        System.out.println("Test Simple Plan: " + plan);

        // Simuliamo l'esecuzione del piano e verifichiamo che raggiunga l'obiettivo
        CharlieState execState = state.copy();
        for (Task action : plan.getActions()) {
            Operator op = domain.getOperator(action.getName());
            assertNotNull(op, "Operatore registrato non trovato: " + action.getName());
            assertTrue(op.canApply(execState, action.getArgs()), "L'operatore " + action + " deve essere applicabile");
            execState = (CharlieState) op.apply(execState, action.getArgs());
        }

        assertTrue(execState.hasBanana(), "Charlie deve aver afferrato la banana al termine del piano");
    }
}
