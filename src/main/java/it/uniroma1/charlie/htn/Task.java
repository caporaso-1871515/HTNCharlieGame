package it.uniroma1.charlie.htn;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Rappresenta un Task (compito) all'interno di una rete HTN.
 * Può essere:
 * - Primitivo: corrisponde a un'azione elementare eseguibile tramite un
 * Operator.
 * - Composto: un obiettivo o piano ad alto livello decomponibile tramite un
 * Method.
 */
public final class Task implements Serializable {

    private final String name;
    private final List<Object> args;
    private final boolean primitive;

    public Task(String name, List<Object> args, boolean primitive) {
        this.name = Objects.requireNonNull(name, "task name cannot be null");
        this.args = args != null ? List.copyOf(args) : Collections.emptyList();
        this.primitive = primitive;
    }

    public static Task primitive(String name, Object... args) {
        return new Task(name, Arrays.asList(args), true);
    }

    public static Task compound(String name, Object... args) {
        return new Task(name, Arrays.asList(args), false);
    }

    public String getName() {
        return name;
    }

    public List<Object> getArgs() {
        return args;
    }

    @SuppressWarnings("unchecked")
    public <T> T getArg(int index) {
        return (T) args.get(index);
    }

    public boolean isPrimitive() {
        return primitive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Task task))
            return false;
        return primitive == task.primitive && Objects.equals(name, task.name) && Objects.equals(args, task.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, args, primitive);
    }

    @Override
    public String toString() {
        String typePrefix = primitive ? "!" : "";
        if (args.isEmpty()) {
            return typePrefix + name;
        }
        return typePrefix + name + args;
    }
}
