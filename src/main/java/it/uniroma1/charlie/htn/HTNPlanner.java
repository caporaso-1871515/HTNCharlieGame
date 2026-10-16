package it.uniroma1.charlie.htn;

import java.util.*;

/**
 * Implementazione del motore di pianificazione HTN (Hierarchical Task Network)
 * basata sull'algoritmo di Ordered Task Decomposition.
 * 
 * Ottimizzazioni:
 * - Rilevamento cicli sul ramo attivo: una firma (stato, task) già visitata
 * sullo stesso percorso DFS viene scartata immediatamente; viene rimossa al
 * backtrack per non bloccare rami paralleli.
 * - Soglia su nodi visitati: maxNodes evita esplosioni dello spazio
 * di ricerca in domìni patologici.
 * - Traccia del piano parziale: utile per la GUI step-by-step.
 */
public class HTNPlanner implements Planner {

    private final Domain domain;
    private final int maxDepth;
    private final int maxNodes;
    private boolean debug = false;

    /** Numero cumulativo di nodi visitati nell'ultima chiamata a plan */
    private int nodesVisitedCounter = 0;

    public HTNPlanner(Domain domain) {
        this(domain, 500, 200_000);
    }

    public HTNPlanner(Domain domain, int maxDepth, int maxNodes) {
        this.domain = Objects.requireNonNull(domain, "domain cannot be null");
        this.maxDepth = maxDepth;
        this.maxNodes = maxNodes;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Domain getDomain() {
        return domain;
    }

    @Override
    public Plan plan(State initialState, List<Task> tasks) {
        Objects.requireNonNull(initialState, "initialState cannot be null");
        Objects.requireNonNull(tasks, "tasks cannot be null");

        if (!initialState.isConsistent()) {
            return Plan.failure("Stato iniziale inconsistente", 0, 0);
        }

        nodesVisitedCounter = 0;
        long startTime = System.currentTimeMillis();

        if (debug) {
            System.out.println("[HTNPlanner] ── Avvio pianificazione ─────────────────────────────");
            System.out.println("[HTNPlanner]    Task iniziali  : " + tasks.size());
            System.out.println("[HTNPlanner]    Stato iniziale : " + initialState);
        }

        Set<StateBranchSignature> branchVisited = new HashSet<>();

        List<Task> solution = seekPlan(
                initialState,
                new ArrayList<>(tasks),
                new ArrayList<>(),
                0,
                branchVisited);

        long elapsed = System.currentTimeMillis() - startTime;

        if (solution != null) {
            if (debug) {
                System.out.println("[HTNPlanner] ── SUCCESSO ────────────────────────────────────");
                System.out.printf("[HTNPlanner]    Azioni: %d | Nodi: %d | Tempo: %dms%n",
                        solution.size(), nodesVisitedCounter, elapsed);
            }
            return Plan.success(solution, elapsed, nodesVisitedCounter);

        } else {
            String reason = nodesVisitedCounter >= maxNodes
                    ? "Limite massimo di nodi visitati (" + maxNodes + ") raggiunto"
                    : "Nessun piano valido trovato nello spazio di ricerca HTN";
            if (debug) {
                System.out.println("[HTNPlanner] ── FALLIMENTO: " + reason);
            }
            return Plan.failure(reason, elapsed, nodesVisitedCounter);
        }
    }

    private List<Task> seekPlan(State state,
            List<Task> tasks,
            List<Task> planSoFar,
            int depth,
            Set<StateBranchSignature> branchVisited) {
        nodesVisitedCounter++;

        // Terminazione per limiti di ricerca
        if (nodesVisitedCounter > maxNodes || depth > maxDepth) {
            if (debug && depth > maxDepth) {
                System.err.println("[HTNPlanner] WARN: profondità massima raggiunta (depth=" + depth + ")");
            }
            return null;
        }

        // caso base: task list vuota: piano completo trovato
        if (tasks.isEmpty()) {
            return new ArrayList<>(planSoFar);
        }

        Task currentTask = tasks.get(0);
        List<Task> remainingTasks = tasks.subList(1, tasks.size());

        // Rilevamento cicli (evita loop di decomposizione infiniti sul ramo attivo)
        StateBranchSignature sig = new StateBranchSignature(state, currentTask);
        if (branchVisited.contains(sig)) {
            return null;
        }
        branchVisited.add(sig);

        try {
            // caso 1: task primitivo
            if (currentTask.isPrimitive() || domain.hasOperator(currentTask.getName())) {
                return handlePrimitive(state, currentTask, remainingTasks, planSoFar, depth, branchVisited);
            }

            // caso 2: task composto
            return handleCompound(state, currentTask, remainingTasks, planSoFar, depth, branchVisited);

        } finally {
            // Rimuoviamo la firma al backtrack per non bloccare altri rami
            branchVisited.remove(sig);
        }
    }

    /** Gestisce l'applicazione di un operatore primitivo. */
    private List<Task> handlePrimitive(State state,
            Task task,
            List<Task> remaining,
            List<Task> planSoFar,
            int depth,
            Set<StateBranchSignature> branchVisited) {
        Operator op = domain.getOperator(task.getName());
        if (op == null) {
            if (debug) {
                System.err.println("[HTNPlanner] Operatore non trovato: " + task.getName());
            }
            return null;
        }

        if (!op.canApply(state, task.getArgs())) {
            if (debug) {
                System.out.println("[HTNPlanner]   FAIL-PRIM (precond) depth=" + depth + " task=" + task);
            }
            return null;
        }

        // Applica l'azione su una copia dello stato
        State nextState = op.apply(state.copy(), task.getArgs());

        if (debug) {
            System.out.println("[HTNPlanner]   APPLY depth=" + depth + " op=" + task);
        }

        List<Task> nextPlan = new ArrayList<>(planSoFar);
        nextPlan.add(task);

        return seekPlan(nextState, remaining, nextPlan, depth + 1, branchVisited);
    }

    /** Gestisce la decomposizione di un task composto attraverso i suoi metodi. */
    private List<Task> handleCompound(State state,
            Task task,
            List<Task> remaining,
            List<Task> planSoFar,
            int depth,
            Set<StateBranchSignature> branchVisited) {
        List<Method> methods = domain.getMethods(task.getName());

        if (methods.isEmpty()) {
            if (debug) {
                System.err.println("[HTNPlanner] Nessun metodo per task composto: " + task.getName());
            }
            return null;
        }

        // Prova i metodi nell'ordine di registrazione (ordine di priorità SHOP2)
        for (Method method : methods) {
            if (!method.canApply(state, task.getArgs())) {
                continue;
            }

            List<Task> subtasks = method.decompose(state, task.getArgs());
            if (subtasks == null) {
                continue;
            }

            if (debug) {
                System.out.println("[HTNPlanner]   DECOMPOSE depth=" + depth
                        + " task=" + task + " → method=" + method.getName()
                        + " (" + subtasks.size() + " subtask)");
            }

            // Concatena i sottotask davanti ai task rimanenti
            List<Task> nextTasks = new ArrayList<>(subtasks);
            nextTasks.addAll(remaining);

            List<Task> result = seekPlan(state, nextTasks, planSoFar, depth + 1, branchVisited);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private static final class StateBranchSignature {
        private final State state;
        private final Task task;

        StateBranchSignature(State state, Task task) {
            this.state = state;
            this.task = task;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof StateBranchSignature that))
                return false;
            return Objects.equals(state, that.state) && Objects.equals(task, that.task);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, task);
        }
    }
}
