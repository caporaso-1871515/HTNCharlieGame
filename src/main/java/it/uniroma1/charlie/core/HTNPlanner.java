package it.uniroma1.charlie.core;

import it.uniroma1.charlie.htn.Domain;
import it.uniroma1.charlie.htn.Plan;
import it.uniroma1.charlie.htn.Task;
import it.uniroma1.charlie.model.CharlieState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Facade e Adapter ad alto livello per la pianificazione HTN (Hierarchical Task
 * Network).
 */
public class HTNPlanner {

    private final Domain domain;
    private final it.uniroma1.charlie.htn.HTNPlanner internalPlanner;

    /**
     * Costruisce il Facade HTN caricando automaticamente il dominio completo di
     * Charlie
     */
    public HTNPlanner() {
        this(CharlieHTNDomain.createDomain());
    }

    /**
     * Costruisce il Facade HTN associando un dominio personalizzato.
     *
     * @param domain il dominio HTN contenente gli operatori e metodi registrati
     */
    public HTNPlanner(Domain domain) {
        this.domain = domain;
        this.internalPlanner = new it.uniroma1.charlie.htn.HTNPlanner(domain);
    }

    /**
     * Abilita o disabilita il logging diagnostico dettagliato dei passi di
     * decomposizione su stdout.
     *
     * @param debug per attivare i log di tracciamento
     */
    public void setDebug(boolean debug) {
        this.internalPlanner.setDebug(debug);
    }

    /**
     * Risultato strutturato e immutabile della pianificazione HTN.
     * Contiene la lista di azioni tipizzate, metriche temporali e
     * conteggio dei nodi visitati.
     */
    public static class HTNPlanResult {
        private final boolean success;
        private final List<Action> actions;
        private final long planningTimeMs;
        private final int expandedNodes;
        private final String message;
        private final Plan rawPlan;

        public HTNPlanResult(boolean success,
                List<Action> actions,
                long planningTimeMs,
                int expandedNodes,
                String message,
                Plan rawPlan) {
            this.success = success;
            this.actions = actions != null ? Collections.unmodifiableList(actions) : Collections.emptyList();
            this.planningTimeMs = planningTimeMs;
            this.expandedNodes = expandedNodes;
            this.message = message;
            this.rawPlan = rawPlan;
        }

        public boolean isSuccess() {
            return success;
        }

        public List<Action> getActions() {
            return actions;
        }

        public int getPlanLength() {
            return actions.size();
        }

        public long getPlanningTimeMs() {
            return planningTimeMs;
        }

        public int getExpandedNodes() {
            return expandedNodes;
        }

        public String getMessage() {
            return message;
        }

        public Plan getRawPlan() {
            return rawPlan;
        }

        @Override
        public String toString() {
            return String.format("HTNPlanResult[success=%b, steps=%d, nodes=%d, time=%dms, msg='%s']",
                    success, actions.size(), expandedNodes, planningTimeMs, message);
        }
    }

    /**
     * Pianifica la risoluzione del problema partendo dallo stato iniziale dato,
     * decomponendo il task radice
     *
     * @param initialState stato di partenza
     * @return risultato con piano atomico e metriche prestazionali
     */
    public HTNPlanResult plan(WorldState initialState) {
        return plan(initialState, Task.compound(CharlieHTNDomain.ROOT_TASK));
    }

    /**
     * Pianifica la decomposizione a partire da un task specifico.
     */
    public HTNPlanResult plan(WorldState initialState, Task rootTask) {
        CharlieState cs = initialState instanceof CharlieState c ? c : new CharlieState(initialState);
        Plan plan = internalPlanner.plan(cs, rootTask);

        if (!plan.isSuccess()) {
            return new HTNPlanResult(
                    false,
                    List.of(),
                    plan.getPlanningTimeMs(),
                    plan.getNodesVisited(),
                    plan.getMessage(),
                    plan);
        }

        List<Action> actionList = new ArrayList<>();
        for (Task t : plan.getActions()) {
            Action a = Action.fromHtnName(t.getName());
            if (a != null) {
                actionList.add(a);
            } else {
                // Fallback se nome non mappato direttamente
                System.err.println("Attenzione: operatore non mappato in enum Action: " + t.getName());
            }
        }

        return new HTNPlanResult(
                true,
                actionList,
                plan.getPlanningTimeMs(),
                plan.getNodesVisited(),
                "Piano HTN trovato con successo",
                plan);
    }
}
