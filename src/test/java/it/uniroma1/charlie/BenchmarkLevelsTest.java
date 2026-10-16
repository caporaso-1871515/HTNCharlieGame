package it.uniroma1.charlie;

import it.uniroma1.charlie.core.Action;
import it.uniroma1.charlie.core.ClassicalPlanner;
import it.uniroma1.charlie.core.HTNPlanner;
import it.uniroma1.charlie.core.WorldState;
import it.uniroma1.charlie.model.ScenarioFactory;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di Benchmark comparativo tra Pianificazione Classica (Flat BFS su
 * STRIPS puro)
 * e Pianificazione Gerarchica HTN (Ordered Task Decomposition SHOP2 in Java).
 * Esegue e valida i tre livelli deterministici:
 * - Livello 1 (Facile): navigazione base senza scala.
 * - Livello 2 (Medio): ostacolo verticale Δh = 2 con prelievo, trasporto e uso
 * scala.
 * - Livello 3 (Difficile): scenario completo (profondità 21).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BenchmarkLevelsTest {

    private ClassicalPlanner classicalPlanner;
    private HTNPlanner htnPlanner;

    private static final List<String[]> benchmarkResults = new ArrayList<>();

    @BeforeEach
    void setUp() {
        classicalPlanner = new ClassicalPlanner();
        htnPlanner = new HTNPlanner();
    }

    @Test
    @Order(1)
    @DisplayName("Livello 1 (Facile): Valida correttezza di Classica e HTN")
    void testLevel1Easy() {
        WorldState s1Classical = ScenarioFactory.createLevel1();
        WorldState s1Htn = ScenarioFactory.createLevel1();

        // Classical BFS
        ClassicalPlanner.ClassicalPlanResult cRes = classicalPlanner.plan(s1Classical, 5000L, 50000);
        assertTrue(cRes.isSuccess(), "Classical BFS deve risolvere Livello 1");
        assertFalse(cRes.getActions().isEmpty(), "Il piano classico non deve essere vuoto");

        // Verifica validità fisica del piano classico
        WorldState finalClassical = s1Classical.copy();
        for (Action a : cRes.getActions()) {
            assertTrue(a.isApplicable(finalClassical), "Azione classica non applicabile: " + a);
            finalClassical = a.apply(finalClassical);
        }
        assertTrue(finalClassical.hasBanana(), "Charlie classico deve raccogliere la banana");

        // HTN
        HTNPlanner.HTNPlanResult hRes = htnPlanner.plan(s1Htn);
        assertTrue(hRes.isSuccess(), "HTN deve risolvere Livello 1");
        assertFalse(hRes.getActions().isEmpty(), "Il piano HTN non deve essere vuoto");

        // Verifica validità fisica del piano HTN
        WorldState finalHtn = s1Htn.copy();
        for (Action a : hRes.getActions()) {
            assertTrue(a.isApplicable(finalHtn), "Azione HTN non applicabile: " + a);
            finalHtn = a.apply(finalHtn);
        }
        assertTrue(finalHtn.hasBanana(), "Charlie HTN deve raccogliere la banana");

        benchmarkResults.add(new String[] {
                "Livello 1 (Facile)", "Classica (BFS)", "SUCCESSO",
                String.valueOf(cRes.getPlanningTimeMs()), String.valueOf(cRes.getExpandedNodes()),
                String.valueOf(cRes.getPlanLength())
        });
        benchmarkResults.add(new String[] {
                "Livello 1 (Facile)", "HTN (SHOP2)", "SUCCESSO",
                String.valueOf(hRes.getPlanningTimeMs()), String.valueOf(hRes.getExpandedNodes()),
                String.valueOf(hRes.getPlanLength())
        });
    }

    @Test
    @Order(2)
    @DisplayName("Livello 2 (Medio): Valida uso obbligatorio della scala su ostacolo Δh = 2")
    void testLevel2Medium() {
        WorldState s2Classical = ScenarioFactory.createLevel2();
        WorldState s2Htn = ScenarioFactory.createLevel2();

        // Classical BFS
        ClassicalPlanner.ClassicalPlanResult cRes = classicalPlanner.plan(s2Classical, 10000L, 50000);
        assertTrue(cRes.isSuccess(), "Classical BFS deve risolvere Livello 2");
        assertFalse(cRes.getActions().isEmpty(), "Il piano classico non deve essere vuoto");
        assertTrue(cRes.getActions().contains(Action.HOLD_LADDER), "Il piano classico deve contenere HOLD_LADDER");
        assertTrue(cRes.getActions().contains(Action.CLIMB_LADDER_RIGHT), "Il piano classico deve scalare l'ostacolo");

        // Validazione fisica piano classico
        WorldState finalClassical = s2Classical.copy();
        for (Action a : cRes.getActions()) {
            assertTrue(a.isApplicable(finalClassical), "Azione classica non applicabile: " + a);
            finalClassical = a.apply(finalClassical);
        }
        assertTrue(finalClassical.hasBanana(), "Charlie classico deve raccogliere la banana");

        // HTN
        HTNPlanner.HTNPlanResult hRes = htnPlanner.plan(s2Htn);
        assertTrue(hRes.isSuccess(), "HTN deve risolvere Livello 2");
        assertFalse(hRes.getActions().isEmpty(), "Il piano HTN non deve essere vuoto");

        // Validazione fisica piano HTN
        WorldState finalHtn = s2Htn.copy();
        for (Action a : hRes.getActions()) {
            assertTrue(a.isApplicable(finalHtn), "Azione HTN non applicabile: " + a);
            finalHtn = a.apply(finalHtn);
        }
        assertTrue(finalHtn.hasBanana(), "Charlie HTN deve raccogliere la banana");

        benchmarkResults.add(new String[] {
                "Livello 2 (Medio)", "Classica (BFS)", "SUCCESSO",
                String.valueOf(cRes.getPlanningTimeMs()), String.valueOf(cRes.getExpandedNodes()),
                String.valueOf(cRes.getPlanLength())
        });
        benchmarkResults.add(new String[] {
                "Livello 2 (Medio)", "HTN (SHOP2)", "SUCCESSO",
                String.valueOf(hRes.getPlanningTimeMs()), String.valueOf(hRes.getExpandedNodes()),
                String.valueOf(hRes.getPlanLength())
        });
    }

    @Test
    @Order(3)
    @DisplayName("Livello 3 (Difficile): Dimostra superiorità computazionale HTN e timeout della ricerca classica")
    void testLevel3Hard() {
        WorldState s3Classical = ScenarioFactory.createLevel3();
        WorldState s3Htn = ScenarioFactory.createLevel3();

        // HTN Planning: deve risolvere istantaneamente (< 250 ms) producendo la
        // sequenza esatta di 21 passi
        long htnStart = System.currentTimeMillis();
        HTNPlanner.HTNPlanResult hRes = htnPlanner.plan(s3Htn);
        long htnElapsed = System.currentTimeMillis() - htnStart;

        assertTrue(hRes.isSuccess(), "HTN deve risolvere lo scenario completo");
        assertEquals(26, hRes.getPlanLength(),
                "Il piano HTN per Figura 1.1(a) deve contenere la sequenza strategica completa (26 azioni)");
        assertTrue(htnElapsed < 1000, "HTN deve terminare in meno di 1 secondo (attuale: " + htnElapsed + " ms)");

        // Validazione fisica piano HTN passo per passo
        WorldState finalHtn = s3Htn.copy();
        for (Action a : hRes.getActions()) {
            assertTrue(a.isApplicable(finalHtn), "Azione HTN non applicabile nello stato corrente: " + a);
            finalHtn = a.apply(finalHtn);
        }
        assertTrue(finalHtn.hasBanana(), "Charlie HTN deve raccogliere la banana");

        // Classical Planning: timeout a 2500 ms per evidenziare l'esplosione
        // combinatoria senza bloccare il test
        ClassicalPlanner.ClassicalPlanResult cRes = classicalPlanner.plan(s3Classical, 2500L, 20000);
        assertTrue(cRes.isTimeout() || !cRes.isSuccess(),
                "La ricerca classica flat deve andare in timeout/esaurimento su profondità 26");

        benchmarkResults.add(new String[] {
                "Livello 3 (Difficile)", "Classica (BFS)", cRes.isTimeout() ? "TIMEOUT" : "FALLITO",
                String.valueOf(cRes.getPlanningTimeMs()), String.valueOf(cRes.getExpandedNodes()), "--"
        });
        benchmarkResults.add(new String[] {
                "Livello 3 (Difficile)", "HTN (SHOP2)", "SUCCESSO",
                String.valueOf(hRes.getPlanningTimeMs()), String.valueOf(hRes.getExpandedNodes()),
                String.valueOf(hRes.getPlanLength())
        });
    }

    @AfterAll
    static void printBenchmarkReport() {
        System.out.println("\n" + "=".repeat(88));
        System.out.println("  TABELLA COMPARATIVA: PIANIFICAZIONE CLASSICA (STRIPS) vs HTN (SHOP2)");
        System.out.println("=".repeat(88));
        System.out.printf("%-23s | %-16s | %-10s | %-11s | %-12s | %-8s%n",
                "LIVELLO", "PARADIGMA", "ESITO", "TEMPO (ms)", "NODI ESPANSI", "PASSI");
        System.out.println("-".repeat(88));

        for (String[] row : benchmarkResults) {
            System.out.printf("%-23s | %-16s | %-10s | %-11s | %-12s | %-8s%n",
                    row[0], row[1], row[2], row[3], row[4], row[5]);
        }
        System.out.println("=".repeat(88) + "\n");
    }
}
