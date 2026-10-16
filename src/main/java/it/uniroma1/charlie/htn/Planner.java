package it.uniroma1.charlie.htn;

import java.util.Collections;
import java.util.List;

/**
 * Interfaccia comune per un pianificatore HTN.
 */
public interface Planner {

    /**
     * Calcola un piano di azioni primitive per realizzare la lista di task specificata
     * a partire dallo stato iniziale dato.
     *
     * @param initialState Lo stato iniziale del mondo.
     * @param tasks        La lista ordinata di task da compiere (primitivi o composti).
     * @return Il piano calcolato (con stato di successo o fallimento).
     */
    Plan plan(State initialState, List<Task> tasks);

    /**
     * Metodo di convenienza per pianificare un singolo task principale (es. "achieve_banana").
     *
     * @param initialState Lo stato iniziale del mondo.
     * @param rootTask     Il task radice da realizzare.
     * @return Il piano calcolato.
     */
    default Plan plan(State initialState, Task rootTask) {
        return plan(initialState, Collections.singletonList(rootTask));
    }
}
