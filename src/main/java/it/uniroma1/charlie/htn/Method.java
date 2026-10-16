package it.uniroma1.charlie.htn;

import java.util.List;
import java.util.Optional;

/**
 * Metodo di decomposizione per un task composto in un pianificatore HTN.
 * Un metodo analizza lo stato corrente e, se applicabile, scompone un task di
 * alto livello
 * in una sequenza ordinata di sotto-task (che possono essere a loro volta
 * primitivi o composti).
 */
public interface Method {

    /**
     * @return Il nome del task composto a cui si applica questo metodo (es.
     *         "achieve_banana").
     */
    String getTaskName();

    /**
     * @return Un nome descrittivo della strategia adottata da questo metodo.
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Verifica se le condizioni di applicabilità (precondizioni del metodo) sono
     * soddisfatte.
     *
     * @param state Lo stato corrente del mondo.
     * @param args  Gli argomenti associati al task composto.
     * @return true se il metodo può decomporre il task nello stato corrente, false
     *         altrimenti.
     */
    boolean canApply(State state, List<Object> args);

    /**
     * Decompone il task composto in una sequenza ordinata di sotto-task.
     *
     * @param state Lo stato corrente del mondo.
     * @param args  Gli argomenti del task composto.
     * @return La lista ordinata di sotto-task da soddisfare.
     */
    List<Task> decompose(State state, List<Object> args);

    /**
     * Tenta la decomposizione se applicabile.
     *
     * @param state Lo stato corrente.
     * @param args  Gli argomenti del task.
     * @return Optional con la lista dei sotto-task se applicabile, Optional.empty()
     *         altrimenti.
     */
    default Optional<List<Task>> tryDecompose(State state, List<Object> args) {
        if (!canApply(state, args)) {
            return Optional.empty();
        }
        return Optional.ofNullable(decompose(state, args));
    }
}
