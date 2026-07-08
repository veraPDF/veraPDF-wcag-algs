package org.verapdf.wcag.algorithms.entities;

public class ObjectKey {
    private int number;
    private int generation;

    public ObjectKey(int number, int generation) {
        this.number = number;
        this.generation = generation;
    }

    public int getNumber() {
        return number;
    }

    public int getGeneration() {
        return generation;
    }
}
