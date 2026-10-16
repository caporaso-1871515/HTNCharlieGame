package it.uniroma1.charlie.domain;

import it.uniroma1.charlie.htn.Domain;

/**
 * Factory per il Dominio HTN completo di Charlie.
 */
public final class CharlieDomain {

    private CharlieDomain() {
    }

    /**
     * Crea e restituisce il dominio HTN completamente configurato per il problema
     * di Charlie.
     *
     * @return Il Domain pronto all'uso con l'HTNPlanner.
     */
    public static Domain createDomain() {
        Domain domain = new Domain();

        // Operatori primitivi
        CharlieOperators.registerAll(domain);

        // Metodi strategici composti
        CharlieMethods.registerAll(domain);

        return domain;
    }
}
