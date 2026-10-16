package it.uniroma1.charlie.htn;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Rappresenta il risultato prodotto dall'algoritmo di pianificazione HTN.
 * Se la pianificazione ha successo, contiene la sequenza ordinata di task primitivi (azioni)
 * da eseguire nel mondo.
 */
public final class Plan implements Serializable {

    private final boolean success;
    private final List<Task> actions;
    private final String message;
    private final long planningTimeMs;
    private final int nodesVisited;

    private Plan(boolean success, List<Task> actions, String message, long planningTimeMs, int nodesVisited) {
        this.success = success;
        this.actions = actions != null ? List.copyOf(actions) : Collections.emptyList();
        this.message = message;
        this.planningTimeMs = planningTimeMs;
        this.nodesVisited = nodesVisited;
    }

    public static Plan success(List<Task> actions, long planningTimeMs, int nodesVisited) {
        return new Plan(true, actions, "Piano trovato con successo", planningTimeMs, nodesVisited);
    }

    public static Plan failure(String reason, long planningTimeMs, int nodesVisited) {
        return new Plan(false, Collections.emptyList(), reason, planningTimeMs, nodesVisited);
    }

    public boolean isSuccess() {
        return success;
    }

    public List<Task> getActions() {
        return actions;
    }

    public int size() {
        return actions.size();
    }

    public boolean isEmpty() {
        return actions.isEmpty();
    }

    public String getMessage() {
        return message;
    }

    public long getPlanningTimeMs() {
        return planningTimeMs;
    }

    public int getNodesVisited() {
        return nodesVisited;
    }

    @Override
    public String toString() {
        if (!success) {
            return "Plan[FAILED: " + message + ", nodi: " + nodesVisited + ", tempo: " + planningTimeMs + "ms]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Plan[PASSI=").append(actions.size())
          .append(", nodi=").append(nodesVisited)
          .append(", tempo=").append(planningTimeMs).append("ms]:\n");
        for (int i = 0; i < actions.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(actions.get(i)).append("\n");
        }
        return sb.toString();
    }
}
