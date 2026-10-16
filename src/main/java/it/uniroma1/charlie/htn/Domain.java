package it.uniroma1.charlie.htn;

import java.util.*;

/**
 * Rappresenta il Dominio HTN, ovvero la collezione di tutti gli operatori
 * primitivi
 * e dei metodi di decomposizione definiti per il problema.
 */
public class Domain {

    private final Map<String, Operator> operators = new HashMap<>();
    private final Map<String, List<Method>> methods = new HashMap<>();

    public Domain() {
    }

    /**
     * Registra un operatore primitivo nel dominio.
     *
     * @param operator L'operatore primitivo.
     * @return Questa istanza
     */
    public Domain registerOperator(Operator operator) {
        Objects.requireNonNull(operator, "operator cannot be null");
        operators.put(operator.getName(), operator);
        return this;
    }

    /**
     * Registra un metodo di decomposizione per un task composto nel dominio.
     *
     * @param method Il metodo di decomposizione.
     * @return Questa istanza.
     */
    public Domain registerMethod(Method method) {
        Objects.requireNonNull(method, "method cannot be null");
        methods.computeIfAbsent(method.getTaskName(), k -> new ArrayList<>()).add(method);
        return this;
    }

    public boolean hasOperator(String name) {
        return operators.containsKey(name);
    }

    public Operator getOperator(String name) {
        return operators.get(name);
    }

    public boolean hasMethods(String taskName) {
        return methods.containsKey(taskName) && !methods.get(taskName).isEmpty();
    }

    public List<Method> getMethods(String taskName) {
        return methods.getOrDefault(taskName, Collections.emptyList());
    }

    public Set<String> getOperatorNames() {
        return Collections.unmodifiableSet(operators.keySet());
    }

    public Set<String> getCompoundTaskNames() {
        return Collections.unmodifiableSet(methods.keySet());
    }
}
