package com.dbms.analyzer.model;

import java.util.TreeSet;
import java.util.Set;

public class NormalFormResult {
    public enum NormalForm {
        FIRST_NF, SECOND_NF, THIRD_NF, BCNF
    }

    private NormalForm highestNormalForm;
    private Set<String> violations;
    private String explanation;

    public NormalFormResult(NormalForm highestNormalForm, Set<String> violations, String explanation) {
        this.highestNormalForm = highestNormalForm;
        this.violations = new TreeSet<>(violations);
        this.explanation = explanation;
    }

    public NormalForm getHighestNormalForm() {
        return highestNormalForm;
    }

    public Set<String> getViolations() {
        return new TreeSet<>(violations);
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public String toString() {
        return "Highest Normal Form: " + highestNormalForm.name();
    }
}
