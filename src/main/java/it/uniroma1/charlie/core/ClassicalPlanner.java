package it.uniroma1.charlie.core;

import java.util.*;

/** Motore di pianificazione classica nello spazio degli stati */
public class ClassicalPlanner {

    /** Timeout predefinito di ricerca: 15.000 millisecondi (15 secondi). */
    public static final long DEFAULT_TIMEOUT_MS = 15000L;

    /**
     * Soglia massima predefinita di nodi espansi prima dell'interruzione per
     * sicurezza.
     */
    public static final int DEFAULT_MAX_NODES = 100000;

    private final long timeoutMs;
    private final int maxNodes;

    /**
     * Costruisce un pianificatore classico con parametri di timeout e memoria
     * predefiniti.
     */
    public ClassicalPlanner() {
        this(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_NODES);
    }

    /**
     * Costruisce un pianificatore classico con un timeout personalizzato in
     * millisecondi.
     *
     * @param timeoutMs limite temporale di calcolo prima del timeout
     */
    public ClassicalPlanner(long timeoutMs) {
        this(timeoutMs, DEFAULT_MAX_NODES);
    }

    /**
     * Costruisce un pianificatore classico con vincoli espliciti di tempo e numero
     * massimo di nodi.
     *
     * @param timeoutMs limite temporale massimo in millisecondi
     * @param maxNodes  limite massimo di nodi espansi consentiti
     */
    public ClassicalPlanner(long timeoutMs, int maxNodes) {
        this.timeoutMs = timeoutMs;
        this.maxNodes = maxNodes;
    }

    /**
     * Nodo dell'albero di ricerca nello spazio degli stati.
     * Mantiene lo stato attuale, il puntatore al nodo genitore per il backtracking
     * del piano,
     * l'azione atomica che ha originato la transizione e la profondità attuale
     * nell'albero.
     */
    public static class Node {
        final WorldState state;
        final Node parent;
        final Action action;
        final int depth;

        /**
         * Crea un nuovo nodo di ricerca.
         *
         * @param state  lo stato associato al nodo
         * @param parent il nodo genitore
         * @param action l'azione eseguita per raggiungere questo stato
         * @param depth  la profondità nel grafo di ricerca
         */
        public Node(WorldState state, Node parent, Action action, int depth) {
            this.state = state;
            this.parent = parent;
            this.action = action;
            this.depth = depth;
        }
    }

    /**
     * Struttura immutabile contenente le metriche prestazionali e l'esito della
     * ricerca classica.
     */
    public static class ClassicalPlanResult {
        private final boolean success;
        private final boolean timeout;
        private final List<Action> actions;
        private final long planningTimeMs;
        private final int expandedNodes;
        private final int generatedNodes;
        private final String message;

        public ClassicalPlanResult(boolean success,
                boolean timeout,
                List<Action> actions,
                long planningTimeMs,
                int expandedNodes,
                int generatedNodes,
                String message) {
            this.success = success;
            this.timeout = timeout;
            this.actions = actions != null ? Collections.unmodifiableList(actions) : Collections.emptyList();
            this.planningTimeMs = planningTimeMs;
            this.expandedNodes = expandedNodes;
            this.generatedNodes = generatedNodes;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isTimeout() {
            return timeout;
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

        public int getGeneratedNodes() {
            return generatedNodes;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format(
                    "ClassicalPlanResult[success=%b, timeout=%b, steps=%d, expanded=%d, generated=%d, time=%dms, msg='%s']",
                    success, timeout, actions.size(), expandedNodes, generatedNodes, planningTimeMs, message);
        }
    }

    /**
     * Esegue la ricerca BFS in avanti partendo dallo stato iniziale fornito con
     * timeout standard.
     *
     * @param initialState stato iniziale del mondo
     * @return risultato dettagliato della pianificazione classica con metriche
     */
    public ClassicalPlanResult plan(WorldState initialState) {
        return plan(initialState, this.timeoutMs, this.maxNodes);
    }

    /**
     * Esegue la ricerca BFS in avanti con parametri personalizzati di timeout e
     * limite di nodi.
     *
     * @param initialState    stato di partenza della ricerca
     * @param customTimeoutMs tempo massimo in millisecondi consentito
     * @param customMaxNodes  numero massimo di nodi espandibili prima di
     *                        interrompere
     * @return risultato strutturato della pianificazione con piano ottimo o
     *         motivazione del fallimento
     */
    public ClassicalPlanResult plan(WorldState initialState, long customTimeoutMs, int customMaxNodes) {
        long startTime = System.currentTimeMillis();

        if (initialState == null) {
            return new ClassicalPlanResult(false, false, List.of(), 0, 0, 0, "Stato iniziale nullo");
        }

        // Se lo stato iniziale è già obiettivo
        if (initialState.isGoal()) {
            return new ClassicalPlanResult(true, false, List.of(), 0, 0, 1,
                    "Obiettivo già soddisfatto nello stato iniziale");
        }

        Queue<Node> openQueue = new ArrayDeque<>();
        Set<WorldState> closedList = new HashSet<>();

        Node root = new Node(initialState.copy(), null, null, 0);
        openQueue.add(root);
        closedList.add(initialState.copy());

        int expandedCount = 0;
        int generatedCount = 1;
        boolean timeout = false;

        Action[] allActions = Action.values();

        while (!openQueue.isEmpty()) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > customTimeoutMs) {
                timeout = true;
                break;
            }

            if (expandedCount >= customMaxNodes) {
                timeout = true;
                break;
            }

            Node current = openQueue.poll();
            expandedCount++;

            // Espansione di tutte le azioni STRIPS applicabili
            for (Action action : allActions) {
                if (action.isApplicable(current.state)) {
                    WorldState nextState = action.apply(current.state);

                    if (!closedList.contains(nextState)) {
                        closedList.add(nextState);
                        generatedCount++;

                        Node child = new Node(nextState, current, action, current.depth + 1);

                        // Verifica immediata dell'obiettivo per potare l'ultima frontiera
                        if (nextState.isGoal()) {
                            long totalTime = System.currentTimeMillis() - startTime;
                            List<Action> plan = reconstructPath(child);
                            return new ClassicalPlanResult(
                                    true,
                                    false,
                                    plan,
                                    totalTime,
                                    expandedCount,
                                    generatedCount,
                                    "Piano ottimo trovato con successo via BFS");
                        }

                        openQueue.add(child);
                    }
                }
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        String msg = timeout
                ? String.format("TIMEOUT: limite di %d ms o %d nodi raggiunto", customTimeoutMs, customMaxNodes)
                : "Spazio degli stati esausto: nessun piano esistente";

        return new ClassicalPlanResult(
                false,
                timeout,
                List.of(),
                totalTime,
                expandedCount,
                generatedCount,
                msg);
    }

    private List<Action> reconstructPath(Node goalNode) {
        LinkedList<Action> path = new LinkedList<>();
        Node curr = goalNode;
        while (curr != null && curr.action != null) {
            path.addFirst(curr.action);
            curr = curr.parent;
        }
        return path;
    }
}
