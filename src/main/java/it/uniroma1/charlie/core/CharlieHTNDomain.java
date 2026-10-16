package it.uniroma1.charlie.core;

import it.uniroma1.charlie.domain.CharlieDomain;
import it.uniroma1.charlie.domain.CharlieMethods;
import it.uniroma1.charlie.htn.Domain;

/**
 * Registrazione centrale e configurazione del dominio HTN (Hierarchical Task
 * Network) per Charlie.
 * Fornisce accesso agli operatori atomici primitivi e ai metodi composti
 * ricorsivi.
 */
public final class CharlieHTNDomain {

    public static final String ROOT_TASK = CharlieMethods.SOLVE_CHARLIE;
    public static final String TASK_REACH_POSITION = CharlieMethods.REACH_POSITION;
    public static final String TASK_RELOCATE_LADDER = CharlieMethods.RELOCATE_LADDER;
    public static final String TASK_PUSH_BOX_SEQUENCE = CharlieMethods.PUSH_BOX_SEQUENCE;
    public static final String TASK_ADJUST_HEIGHT = CharlieMethods.ADJUST_HEIGHT_BANANA;

    private CharlieHTNDomain() {
    }

    /**
     * Crea un'istanza del Dominio HTN con tutti gli operatori e metodi registrati.
     */
    public static Domain createDomain() {
        return CharlieDomain.createDomain();
    }
}
