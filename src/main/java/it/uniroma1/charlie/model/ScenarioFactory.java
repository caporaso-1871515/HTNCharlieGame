package it.uniroma1.charlie.model;

/**
 * Factory per generare i 3 scenari deterministici richiesti:
 * 
 * Livello 1 (Facile): Navigazione base e gradini standard (|Δh| ≤ 1, nessuna
 * scala);
 * Livello 2 (Medio): Ostacolo verticale (Δh = 2) con prelievo, trasporto e uso
 * della scala;
 * Livello 3 (Difficile): Scenario completo (Esercitazione B.2.8).
 */
public final class ScenarioFactory {

    private ScenarioFactory() {
    }

    /**
     * Livello 4: Scenario Personalizzato (Custom Challenge)
     * Griglia a 15 colonne (x in [0, 14]), parete in x=5 (Δh=2), banana in x=10
     * (h=4).
     */
    public static CharlieState createLevel4() {
        int[] heights = new int[] {
                1, 2, 2, 2, 2, 4, 3, 2, 1, 1, 1, 1, 1, 1, 1
        };
        return new CharlieState(
                10, // monkeyX = 10
                heights[10], // monkeyH = 1
                false, // holdingLadder = false (mani libere)
                8, // ladderX = 8
                heights[8], // ladderH = 2
                6, // bananaX = 6
                5, // bananaH = 5 (raggiungibile ad altezza h = 4)
                false, // hasBanana = false
                heights);
    }

    /**
     * Livello 1: Facile (Navigazione Base - Gradini Standard)
     * - Griglia ridotta: x in [0, 8], quote h in [0, 3].
     * - Profilo altezze: [2, 2, 2, 3, 2, 2, 2, 1, 1].
     * - Entità: Charlie in x=0 (h=2), Banana in x=5 (h=3).
     * - Nessuna scala necessaria, dislivelli |Δh| ≤ 1.
     */
    public static CharlieState createLevel1() {
        int[] heights = new int[] { 2, 2, 2, 3, 2, 2, 2, 1, 1 };
        return new CharlieState(
                0, // monkeyX = 0
                heights[0], // monkeyH = 2
                false, // holdingLadder = false
                0, // ladderX = 0
                heights[0], // ladderH = 2
                5, // bananaX = 5
                3, // bananaH = 3 (raggiungibile ad altezza h=2)
                false, // hasBanana = false
                heights);
    }

    /**
     * Livello 2: Medio (Ostacolo Verticale - Uso della Scala)
     * - Griglia media: x in [0, 11] (12 colonne), quote h in [0, 4].
     * - Profilo altezze: [2, 2, 2, 4, 3, 2, 2, 2, 2, 2, 2, 2].
     * - Entità: Charlie in x=0 (h=2), Scala a terra in x=1 (h=2), Banana in x=6
     * (h=3).
     * - Vincolo: dislivello tra x=2 (h=2) e x=3 (h=4) è pari a Δh = 2.
     * Charlie deve raccogliere la scala in x=1, portarla in x=2, scalarla in x=3
     * e superare l'ostacolo fino alla banana in x=6.
     */
    public static CharlieState createLevel2() {
        int[] heights = new int[] { 2, 2, 2, 4, 3, 2, 2, 2, 2, 2, 2, 2 };
        return new CharlieState(
                0, // monkeyX = 0
                heights[0], // monkeyH = 2
                false, // holdingLadder = false
                1, // ladderX = 1
                heights[1], // ladderH = 2 (a terra)
                6, // bananaX = 6
                3, // bananaH = 3 (raggiungibile ad altezza h=2)
                false, // hasBanana = false
                heights);
    }

    /**
     * Livello 3: Difficile (Scenario Completo - Esercitazione B.2.8)
     * - Griglia completa: x in [0, 20], quote h in [0, 5].
     * - Profilo altezze: [2, 2, 2, 3, 2, 3, 2, 1, 2, 1, 1, 2, 4, 3, 4, 3, 4, 5, 4,
     * 3, 4].
     * - Entità: Charlie in x=13 (h=3), Scala in x=2 (h=2), Banana in x=8 (h=4).
     */
    public static CharlieState createLevel3() {
        int[] heights = new int[] {
                2, // x = 0
                2, // x = 1
                2, // x = 2 (scala poggiata qui)
                3, // x = 3
                2, // x = 4
                3, // x = 5
                2, // x = 6
                1, // x = 7
                2, // x = 8 (sotto la banana)
                1, // x = 9
                1, // x = 10
                2, // x = 11
                4, // x = 12
                3, // x = 13 (Charlie qui)
                4, // x = 14
                3, // x = 15
                4, // x = 16
                5, // x = 17
                4, // x = 18
                3, // x = 19
                4 // x = 20
        };

        return new CharlieState(
                13, // monkeyX = 13
                heights[13], // monkeyH = 3
                false, // holdingLadder = false
                2, // ladderX = 2
                heights[2], // ladderH = 2
                8, // bananaX = 8
                4, // bananaH = 4 (raggiungibile ad altezza h=3)
                false, // hasBanana = false
                heights);
    }

    public static CharlieState createFigure1Scenario() {
        return createLevel3();
    }

    /**
     * Micro-scenario di prova per test rapidi.
     */
    public static CharlieState createSimpleScenario() {
        return createLevel1();
    }
}
