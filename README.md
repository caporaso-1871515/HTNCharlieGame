# HTN Charlie Game — Classical (STRIPS) vs Hierarchical (HTN SHOP2) Planner

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36?style=flat&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-Academic-blue.svg)]()

> **Sapienza Università di Roma — Corso di Intelligenza Artificiale**  
> Progetto Formale di Pianificazione Automatica: confronto sperimentale tra **Pianificazione Classica nello Spazio degli Stati (STRIPS / Ricerca Cieca Ottima)** e **Pianificazione Gerarchica HTN (SHOP2 / Ordered Task Decomposition)** sul problema di ragionamento spaziale e manipolazione fisica _"Charlie, modelling"_ (Esercitazione B.2.8).

---

## 🏛️ Sommario Architetturale

Il sistema risolve il dominio di Charlie mediante due paradigmi complementari di Intelligenza Artificiale, integrati in un'unica suite software ad alte prestazioni sviluppata in **Java 17**, con motore grafico interattivo **Swing** (Split-Screen) e containerizzazione **Docker**:

1. **Modellazione Classica PDDL / STRIPS Puro**:
   - Assenza totale di aritmetica o numeri (sostituiti da relazioni topologiche discrete `NextX`, `NextH`, `WalkableDiff`, `LadderDiff`, `CanReceiveBox`).
   - Assenza di precondizioni negative (`:negative-preconditions`) tramite fluent affermativo `(HandsEmpty)`.
   - Sdoppiamento delle azioni per evitare effetti condizionali (`move-empty` vs `move-carrying`).
   - Risolutore BFS esatto con Closed List indicizzata su stato compatto per garantire completezza e ottimalità del cammino minimo.

2. **Pianificazione Gerarchica HTN (SHOP2 / Ordered Task Decomposition)**:
   - Scomposizione gerarchica dei task da metodi astratti (`:solve-charlie`, `:relocate-ladder`, `:fill-pits-with-boxes`, `:build-box-pyramid`) fino alle azioni atomiche primitive.
   - Pianificazione nello spazio degli stati: stato deterministico pienamente noto ad ogni espansione, consentendo potatura immediata dei rami infattibili e annullamento del branching combinatorio.

3. **Interfaccia Grafica e Simulazione (Swing)**:
   - Visualizzazione animata split-screen che mette a confronto fianco a fianco l'esplorazione esaustiva della Classica contro l'esecuzione mirata dell'HTN.
   - Controlli di playback (Play, Pausa, Step, Reset, Slider di velocità da 10ms a 1000ms).

---

## 📊 Benchmark Comparativo sui 3 Livelli Ufficiali

| Livello                     | Descrizione Scenario                                                        | Paradigma                             |            Esito             |       Tempo (ms)        |   Nodi Generati / Decomposti   |        Lunghezza Piano        |
| :-------------------------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------: | :---------------------: | :----------------------------: | :---------------------------: |
| **Livello 1** _(Facile)_    | Griglia 9 col, Charlie pos1, banana pos5 a quota h3                         | **Classica (BFS)**<br>**HTN (SHOP2)** | **SUCCESSO**<br>**SUCCESSO** |    23 ms<br>**2 ms**    |    29 stati<br>**14 task**     |      6 passi<br>6 passi       |
| **Livello 2** _(Medio)_     | Griglia 12 col, parete $\Delta h = 2$ a pos2, banana pos6                   | **Classica (BFS)**<br>**HTN (SHOP2)** | **SUCCESSO**<br>**SUCCESSO** |    9 ms<br>**2 ms**     |    90 stati<br>**20 task**     |      9 passi<br>9 passi       |
| **Livello 3** _(Difficile)_ | **Esercitazione B.2.8** (Fig. 1.1), griglia 21 col, fossa, rampa e piramide | **Classica (BFS)**<br>**HTN (SHOP2)** | **TIMEOUT**<br>**SUCCESSO**  | > 15.000 ms<br>**3 ms** | > 100.000 stati<br>**50 task** | _Non trovato_<br>**26 passi** |

> **Nota Teorica**: Nel Livello 3 lo spazio degli stati della ricerca classica esplode combinatoriamente ($O(b^d)$ con fattore di diramazione medio $b \approx 8 \div 10$ e profondità $d = 26 \implies \approx 10^{26}$ stati). Il pianificatore HTN, guidato da conoscenza procedurale di dominio, riduce il branching factor effettivo a $b_{eff} \approx 1.1$, risolvendo il problema in soli **3 millisecondi**.

---

## 🚀 Istruzioni di Avvio Rapido (Quickstart)

### Opzione 1: Esecuzione One-Line con Docker (GUI abilitata)

Il repository include configurazione multi-stage `Dockerfile` e `docker-compose.yml` con inoltro del display grafico X11:

```bash
# 1. Concedi i permessi al server grafico X11 locale (Linux / WSL2):
xhost +local:root

# 2. Compila l'immagine e avvia il container in un singolo comando:
docker compose up --build
```

_Per chiudere l'applicazione e deallocare il container:_

```bash
docker compose down
```

> **Utenti macOS**: Installare [XQuartz](https://www.xquartz.org/), abilitare _"Allow connections from network clients"_ nelle preferenze di sicurezza ed eseguire:
>
> ```bash
> xhost + 127.0.0.1
> DISPLAY=host.docker.internal:0 docker compose up --build
> ```

---

### Opzione 2: Compilazione ed Esecuzione Locale con Maven

**Prerequisiti**: Java 17+ (JDK) e Apache Maven 3.8+.

#### 1. Esecuzione della Suite di Test Unitari e Benchmark

```bash
mvn clean test
```

#### 2. Esecuzione Diretta con Maven

```bash
mvn compile exec:java -Dexec.mainClass="it.uniroma1.charlie.Main"
```

#### 3. Creazione ed Esecuzione del Fat-JAR Standalone

Il progetto utilizza `maven-shade-plugin` per produrre un archivio JAR auto-contenuto con tutte le dipendenze:

```bash
# Creazione del bundle JAR (incluso target/htn-charlie-game-1.0.0.jar)
mvn clean package -DskipTests

# Avvio del Fat-JAR standalone
java -jar target/htn-charlie-game-1.0.0.jar
```

---

## 📂 Mappa della Struttura del Progetto

```
HTNCharlieGame/
├── pom.xml                                      # Configurazione Maven (Java 17, JUnit 5, Shade Plugin)
├── Dockerfile                                   # Multi-stage Docker build per JVM e supporto X11
├── docker-compose.yml                           # Configurazione X11 forwarding e bind-mounts
├── README.md                                    # Documentazione sintetica di repository
├── src/
│   ├── main/
│   │   ├── java/it/uniroma1/charlie/
│   │   │   ├── Main.java                        # Entry point applicativo e avvio GUI Swing
│   │   │   ├── core/                            # Nucleo di pianificazione e stato centrale
│   │   │   │   ├── Action.java                  # Enum delle 14 azioni primitive atomiche
│   │   │   │   ├── WorldState.java              # Stato immutabile, hash compatto e applicatore transizioni
│   │   │   │   ├── ClassicalPlanner.java        # Risolutore STRIPS BFS con Closed List e potatura cicli
│   │   │   │   ├── HTNPlanner.java              # Adapter/Facade unificato per motore HTN
│   │   │   │   └── CharlieHTNDomain.java        # Mapping costanti e task HTN di alto livello
│   │   │   ├── model/                           # Modello dati topologico e fabbrica scenari
│   │   │   │   ├── WorldConstants.java          # Dimensioni griglia (21x5) e costanti fisiche
│   │   │   │   ├── CharlieState.java            # Wrapper per compatibilità interfaccia State
│   │   │   │   └── ScenarioFactory.java         # Costruttore deterministico dei 3 Livelli di benchmark
│   │   │   ├── htn/                             # Motore algoritmico HTN indipendente (SHOP2 / Pyhop)
│   │   │   │   ├── State.java                   # Interfaccia di stato clonabile per backtracking
│   │   │   │   ├── Task.java                    # Rappresentazione task primitivi (!) e composti
│   │   │   │   ├── Operator.java                # Schema formale di operatore atomico
│   │   │   │   ├── Method.java                  # Schema formale di metodo di scomposizione gerarchica
│   │   │   │   ├── Domain.java                  # Registro e indice degli operatori e metodi
│   │   │   │   ├── Plan.java                    # Piano d'azione generato e statistiche di decomposizione
│   │   │   │   ├── Planner.java                 # Interfaccia generica di pianificazione
│   │   │   │   └── HTNPlanner.java              # Algoritmo ricorsivo Ordered Task Decomposition
│   │   │   ├── domain/                          # Specifiche formali del dominio di Charlie
│   │   │   │   ├── CharlieOperators.java        # I 14 operatori primitivi atomici conformi a STRIPS
│   │   │   │   ├── CharlieMethods.java          # I 24 metodi HTN e strategie gerarchiche di livello
│   │   │   │   └── CharlieDomain.java           # Factory di istanziazione e binding del dominio HTN
│   │   │   └── gui/                             # Interfaccia grafica Desktop (Java Swing)
│   │   │       ├── MainFrame.java               # Finestra principale Split-Screen e controller asincrono
│   │   │       └── GridCanvas.java              # Canvas 2D per rendering vettoriale e animazioni
│   │   └── resources/pddl/                      # File PDDL 1.2 ufficiali (STRIPS puro)
│   │       ├── charlie_domain.pddl              # Dominio assiomatizzato senza numeri né negation
│   │       ├── charlie-level1-easy.pddl         # Definizione PDDL Livello 1
│   │       ├── charlie-level2-medium.pddl       # Definizione PDDL Livello 2
│   │       └── charlie-level3-hard.pddl         # Definizione PDDL Livello 3 (Esercitazione B.2.8)
│   └── test/java/it/uniroma1/charlie/           # Suite di collaudo e benchmark (JUnit 5)
│       ├── BenchmarkLevelsTest.java             # Verifica comparativa delle prestazioni sui 3 livelli
│       ├── domain/OperatorTest.java             # Validazione fisica dei 14 operatori atomici
│       ├── htn/HTNPlannerTest.java              # Test isolato del motore SHOP2
│       ├── model/CharlieStateTest.java          # Test integrità dello stato e Closed List hashing
│       └── planning/CharliePlanningTest.java    # Test di consistenza delle azioni PDDL/Java
```

---

## 👥 Autori e Riferimenti

- **Studente**: Pierangelo Caporaso
- **Corso**: Intelligenza Artificiale — Sapienza Università di Roma
- **Riferimenti Bibliografici**:
  - S. Russell, P. Norvig, _Artificial Intelligence: A Modern Approach_, 4th Edition.
  - D. Nau et al., _SHOP2: An HTN Planning System_, Journal of Artificial Intelligence Research (JAIR), 2003.
