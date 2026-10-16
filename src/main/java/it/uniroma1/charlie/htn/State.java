package it.uniroma1.charlie.htn;

/**
 * Interfaccia comune per lo stato del mondo in un pianificatore HTN.
 *
 * Lo stato deve essere:
 *
 * Clonabile in modo profondo tramite copy(), per consentire
 * il branching e il backtracking durante la ricerca HTN senza effetti
 * collaterali tra rami distinti dell'albero di decomposizione.
 * Confrontabile per eguaglianza semantica tramite Object.equals(Object)
 * e Object.hashCode(), per permettere il rilevamento di cicli.
 *
 * Corrispondenza con SHOP2 (Nau et al., 2003):
 * Questo tipo corrisponde al concetto di current state usato nell'algoritmo
 * Seek-Plan. Ogni applicazione di un operatore primitivo produce una nuova
 * istanza
 * dello stato tramite copy() + mutazione, senza toccare il ramo padre.
 */
public interface State {

    /**
     * Crea una copia profonda e indipendente di questo stato.
     *
     * Invariante: per qualsiasi stato s, deve valere
     * s.copy().equals(s) e s.copy() != s.
     *
     * @return Una copia profonda e semanticamente equivalente dello stato.
     */
    State copy();

    /**
     * Verifica la coerenza interna dello stato (usato nei test e nel debug).
     * L'implementazione di default restituisce sempre true.
     * Le sottoclassi possono fare override per aggiungere invarianti di dominio.
     *
     * @return true se lo stato è consistente, false altrimenti.
     */
    default boolean isConsistent() {
        return true;
    }
}
