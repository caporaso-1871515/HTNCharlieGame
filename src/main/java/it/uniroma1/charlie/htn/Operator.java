package it.uniroma1.charlie.htn;

import java.util.List;
import java.util.Optional;

/**
 * Operatore primitivo del pianificatore HTN.
 * Definisce un'azione elementare modificando direttamente lo stato del mondo
 * se le precondizioni sono verificate.
 */
public interface Operator {

    /**
     * @return Il nome univoco dell'operatore (es. "move_left", "push_right").
     */
    String getName();

    /**
     * Verifica se le precondizioni dell'operatore sono soddisfatte nello stato dato.
     *
     * @param state Lo stato corrente del mondo.
     * @param args  Gli argomenti associati al task primitivo.
     * @return true se l'operatore può essere applicato, false altrimenti.
     */
    boolean canApply(State state, List<Object> args);

    /**
     * Applica l'operatore modificando lo stato (o restituendo un nuovo stato trasformato).
     * Nota: questo metodo viene tipicamente invocato su una copia dello stato.
     *
     * @param state Lo stato corrente (tipicamente già copiato).
     * @param args  Gli argomenti associati all'operatore.
     * @return Lo stato modificato dopo l'applicazione degli effetti.
     */
    State apply(State state, List<Object> args);

    /**
     * Metodo di convenienza che verifica le precondizioni ed esegue l'operatore su una copia protetta.
     *
     * @param state Lo stato di partenza.
     * @param args  Gli argomenti del task.
     * @return Optional contenente il nuovo stato se applicabile, Optional.empty() altrimenti.
     */
    default Optional<State> execute(State state, List<Object> args) {
        if (!canApply(state, args)) {
            return Optional.empty();
        }
        State nextState = state.copy();
        return Optional.of(apply(nextState, args));
    }
}
