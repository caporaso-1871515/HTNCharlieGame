package it.uniroma1.charlie.core;

import it.uniroma1.charlie.htn.State;
import it.uniroma1.charlie.model.WorldConstants;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Rappresentazione formale dello stato del mondo per il problema di Charlie
 */
public class WorldState implements State, Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    /** Coordinata x (colonna) di Charlie nella griglia. */
    protected int monkeyX;

    /**
     * Quota dei piedi di Charlie (coincidente con l'altezza della colonna
     * calpestata).
     */
    protected int monkeyH;

    /**
     * true se e solo se Charlie sta trasportando la scala a mani impegnate.
     */
    protected boolean holdingLadder;

    /**
     * Coordinata x della scala (solidale a Charlie se holdingLadder == true).
     */
    protected int ladderX;

    /** Quota della base della scala al suolo. */
    protected int ladderH;

    /** Coordinata x immutabile della banana appesa. */
    protected int bananaX;

    /** Quota verticale immutabile della banana appesa al soffitto. */
    protected int bananaH;

    /**
     * true se la banana è stata raccolta (condizione di goal terminale).
     */
    protected boolean hasBanana;

    /**
     * Vettore delle altezze calpestabili per ciascuna colonna x da 0 a width - 1.
     * Modificato dinamicamente dalle azioni di spinta (push) e scavo (dig).
     */
    protected final int[] columnHeights;

    /**
     * Costruisce una nuova istanza esplicita dello stato del mondo.
     *
     * @param monkeyX       coordinata x iniziale di Charlie
     * @param monkeyH       quota iniziale dei piedi di Charlie
     * @param holdingLadder stato iniziale di possesso della scala
     * @param ladderX       coordinata x iniziale della scala
     * @param ladderH       quota iniziale della scala
     * @param bananaX       coordinata x della banana
     * @param bananaH       quota della banana
     * @param hasBanana     stato iniziale di raccolta della banana
     * @param columnHeights profilo iniziale delle altezze delle colonne
     */
    public WorldState(int monkeyX,
            int monkeyH,
            boolean holdingLadder,
            int ladderX,
            int ladderH,
            int bananaX,
            int bananaH,
            boolean hasBanana,
            int[] columnHeights) {
        this.monkeyX = monkeyX;
        this.monkeyH = monkeyH;
        this.holdingLadder = holdingLadder;
        this.ladderX = ladderX;
        this.ladderH = ladderH;
        this.bananaX = bananaX;
        this.bananaH = bananaH;
        this.hasBanana = hasBanana;
        this.columnHeights = columnHeights != null
                ? Arrays.copyOf(columnHeights, columnHeights.length)
                : new int[WorldConstants.GRID_WIDTH];
    }

    /**
     * Costruttore di copia profonda (Deep Copy).
     * Garantisce l'indipendenza strutturale del vettore delle altezze tra stati
     * successivi.
     *
     * @param other lo stato sorgente da duplicare
     */
    public WorldState(WorldState other) {
        this(other.monkeyX, other.monkeyH, other.holdingLadder,
                other.ladderX, other.ladderH,
                other.bananaX, other.bananaH, other.hasBanana,
                other.columnHeights);
    }

    /**
     * Crea e restituisce una copia profonda dello stato corrente.
     *
     * @return un nuovo {@link WorldState} identico ma indipendente
     */
    @Override
    public WorldState copy() {
        return new WorldState(this);
    }

    @Override
    public WorldState clone() {
        return copy();
    }

    /**
     * Verifica la consistenza fisica e topologica dello stato:
     * Charlie e scala si trovano entro i confini validi della griglia;
     * La quota dei piedi di Charlie corrisponde esattamente all'altezza della
     * colonna occupata;
     * Se la scala è in mano, le sue coordinate coincidono rigidamente con
     * quelle di Charlie.
     *
     * @return true se lo stato rispetta tutti gli invarianti fisici del dominio
     */
    @Override
    public boolean isConsistent() {
        if (monkeyX < 0 || monkeyX >= columnHeights.length)
            return false;
        if (monkeyH < 0 || monkeyH > WorldConstants.MAX_HEIGHT)
            return false;
        if (monkeyH != columnHeights[monkeyX])
            return false;
        if (holdingLadder) {
            if (ladderX != monkeyX || ladderH != monkeyH)
                return false;
        } else {
            if (ladderX < 0 || ladderX >= columnHeights.length)
                return false;
            if (ladderH < 0 || ladderH > WorldConstants.MAX_HEIGHT)
                return false;
        }
        return true;
    }

    /**
     * Predicato obiettivo terminale della pianificazione.
     * Conforme alla specifica dell'Esercitazione B.2.8: soddisfatto se e solo se la
     * banana è raccolta.
     *
     * @return true se Charlie ha afferrato la banana
     */
    public boolean isGoal() {
        return hasBanana;
    }

    /**
     * Valuta se Charlie si trova direttamente sotto la banana ed è idoneo ad
     * afferrarla.
     * Condizioni fisiche necessarie:
     * Allineamento orizzontale perfetto: monkeyX == bananaX;
     * Quota dei piedi esattamente una quota sotto la banana: monkeyH == bananaH -
     * 1;
     * Mani completamente libere senza scala: !holdingLadder;
     *
     * @return true se Charlie può eseguire l'operazione atomica !grab_banana
     */
    public boolean isDirectlyUnderBanana() {
        return !holdingLadder && monkeyX == bananaX && monkeyH == (bananaH - 1);
    }

    /**
     * Valuta se Charlie si trova nella medesima coordinata e quota della scala a
     * terra.
     * Condizione necessaria per l'operatore primitivo !hold_ladder.
     *
     * @return true se Charlie può raccogliere la scala al suolo
     */
    public boolean isAtLadderPosition() {
        return !holdingLadder && monkeyX == ladderX && monkeyH == ladderH;
    }

    // --- Getters e Setters ---

    public int getWidth() {
        return columnHeights.length;
    }

    public int getMonkeyX() {
        return monkeyX;
    }

    public void setMonkeyX(int monkeyX) {
        this.monkeyX = monkeyX;
    }

    public int getMonkeyH() {
        return monkeyH;
    }

    public void setMonkeyH(int monkeyH) {
        this.monkeyH = monkeyH;
    }

    public boolean isHoldingLadder() {
        return holdingLadder;
    }

    public void setHoldingLadder(boolean holdingLadder) {
        this.holdingLadder = holdingLadder;
        if (holdingLadder) {
            this.ladderX = this.monkeyX;
            this.ladderH = this.monkeyH;
        }
    }

    public int getLadderX() {
        return holdingLadder ? monkeyX : ladderX;
    }

    public void setLadderX(int ladderX) {
        this.ladderX = ladderX;
    }

    public int getLadderH() {
        return holdingLadder ? monkeyH : ladderH;
    }

    public void setLadderH(int ladderH) {
        this.ladderH = ladderH;
    }

    public int getBananaX() {
        return bananaX;
    }

    public void setBananaX(int bananaX) {
        this.bananaX = bananaX;
    }

    public int getBananaH() {
        return bananaH;
    }

    public void setBananaH(int bananaH) {
        this.bananaH = bananaH;
    }

    public boolean hasBanana() {
        return hasBanana;
    }

    public void setHasBanana(boolean hasBanana) {
        this.hasBanana = hasBanana;
    }

    public boolean isBananaGrabbed() {
        return hasBanana;
    }

    public void setBananaGrabbed(boolean bananaGrabbed) {
        this.hasBanana = bananaGrabbed;
    }

    public boolean hasLadder() {
        return holdingLadder;
    }

    public void setHasLadder(boolean hasLadder) {
        setHoldingLadder(hasLadder);
    }

    public int[] getHeights() {
        return Arrays.copyOf(columnHeights, columnHeights.length);
    }

    public int[] getColumnHeights() {
        return Arrays.copyOf(columnHeights, columnHeights.length);
    }

    public int getHeightAt(int x) {
        if (x < 0 || x >= columnHeights.length) {
            return -1;
        }
        return columnHeights[x];
    }

    public void setHeightAt(int x, int height) {
        if (x >= 0 && x < columnHeights.length) {
            columnHeights[x] = Math.max(0, Math.min(WorldConstants.MAX_HEIGHT, height));
        }
    }

    public void incrementHeightAt(int x) {
        if (x >= 0 && x < columnHeights.length && columnHeights[x] < WorldConstants.MAX_HEIGHT) {
            columnHeights[x]++;
        }
    }

    public void decrementHeightAt(int x) {
        if (x >= 0 && x < columnHeights.length && columnHeights[x] > 0) {
            columnHeights[x]--;
        }
    }

    /**
     * Valuta l'uguaglianza profonda tra due stati del mondo.
     * Due stati sono considerati identici ai fini della pianificazione se e solo
     * se:
     * Charlie si trova nella stessa colonna e quota;
     * Lo stato di possesso e la posizione della scala sono identici;
     * La banana ha le stesse coordinate ed è nello stesso stato di raccolta;
     * Fondamentale: l'intero vettore columnHeights[] è identico elemento per
     * elemento.
     * Questa verifica previene la fusione errata di nodi con configurazioni del
     * terreno differenti
     * all'interno della Closed List della BFS e del rilevamento cicli dell'HTN.
     *
     * @param o l'oggetto con cui confrontare lo stato corrente
     * @return true se i due stati condividono identiche proprietà fisiche e
     *         topologiche
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WorldState that))
            return false;
        return monkeyX == that.monkeyX &&
                monkeyH == that.monkeyH &&
                holdingLadder == that.holdingLadder &&
                getLadderX() == that.getLadderX() &&
                getLadderH() == that.getLadderH() &&
                bananaX == that.bananaX &&
                bananaH == that.bananaH &&
                hasBanana == that.hasBanana &&
                Arrays.equals(columnHeights, that.columnHeights);
    }

    /**
     * Calcola il valore di hash canonico dello stato.
     *
     * @return il valore hash intero
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(monkeyX, monkeyH, holdingLadder, getLadderX(), getLadderH(), bananaX, bananaH,
                hasBanana);
        result = 31 * result + Arrays.hashCode(columnHeights);
        return result;
    }

    @Override
    public String toString() {
        return String.format("WorldState[Charlie=(%d,%d), Scala=(%d,%d, inMano=%b), Banana=(%d,%d, presa=%b)]",
                monkeyX, monkeyH, getLadderX(), getLadderH(), holdingLadder, bananaX, bananaH, hasBanana);
    }
}
