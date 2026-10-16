package it.uniroma1.charlie.model;

/**
 * Costanti globali del dominio di gioco di Charlie (Esercitazione B.2.8).
 */
public final class WorldConstants {

    private WorldConstants() {
        // utility class
    }

    /** Dimensione orizzontale della griglia (posizioni da 0 a 20). */
    public static final int GRID_WIDTH = 21;

    /** Minima coordinata orizzontale. */
    public static final int MIN_X = 0;

    /** Massima coordinata orizzontale. */
    public static final int MAX_X = 20;

    /** Massima altezza consentita per le scatole (da 0 a 5). */
    public static final int MAX_HEIGHT = 5;

    /** Altezza standard del soffitto/banana. */
    public static final int CEILING_HEIGHT = 5;

    /** Massima differenza in altezza superabile da Charlie a piedi nudi (senza scala). */
    public static final int MAX_STEP_WITHOUT_LADDER = 1;

    /** Massima differenza in altezza superabile da Charlie con la scala. */
    public static final int MAX_STEP_WITH_LADDER = 2;
}
