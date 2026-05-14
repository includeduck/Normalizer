package com.dbms.analyzer.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.NormalFormResult;

public class NormalFormChecker {

    private final ClosureComputer closureComputer;

    public NormalFormChecker() {
        this.closureComputer = new ClosureComputer();
    }

    /**
     * Checks if relation satisfies 1NF
     * Simplified: Always true for our purposes (atomic attributes)
     */
    public boolean is1NF(Set<String> attributes, 
                        Set<FunctionalDependency> functionalDependencies) {
        return true;
    }

    /**
     * Checks if relation satisfies 2NF
     * No non-prime attribute is partially dependent on a candidate key
     */
    public boolean is2NF(Set<String> attributes,
                        Set<FunctionalDependency> functionalDependencies,
                        Set<CandidateKey> candidateKeys) {
        return find2NfViolations(
            attributes, functionalDependencies, candidateKeys).isEmpty();
    }

    /**
     * Checks if relation satisfies 3NF.
     * For every non-trivial FD X -> A, X must be a superkey or A must be prime.
     */
    public boolean is3NF(Set<String> attributes,
                        Set<FunctionalDependency> functionalDependencies,
                        Set<CandidateKey> candidateKeys) {
        return is2NF(attributes, functionalDependencies, candidateKeys)
            && find3NfViolations(
                attributes, functionalDependencies, candidateKeys).isEmpty();
    }

    /**
     * Checks if relation satisfies BCNF
     * For every FD X -> Y, X must be a superkey
     */
    public boolean isBCNF(Set<String> attributes,
                         Set<FunctionalDependency> functionalDependencies,
                         Set<CandidateKey> candidateKeys) {

        for (FunctionalDependency fd : functionalDependencies) {
            if (!isTrivial(fd)
                    && !isSuperkey(fd.getLeftSide(), attributes,
                        functionalDependencies)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines the highest normal form satisfied
     */
    public NormalFormResult determineHighestNormalForm(
            Set<String> attributes,
            Set<FunctionalDependency> functionalDependencies,
            Set<CandidateKey> candidateKeys) {
        
        Set<String> violations = new HashSet<>();
        String explanation = "";

        Set<String> twoNfViolations = find2NfViolations(
            attributes, functionalDependencies, candidateKeys);
        if (!twoNfViolations.isEmpty()) {
            violations.addAll(twoNfViolations);
            explanation = "2NF violation detected";
            return new NormalFormResult(
                NormalFormResult.NormalForm.FIRST_NF, violations, explanation);
        }

        Set<String> threeNfViolations = find3NfViolations(
            attributes, functionalDependencies, candidateKeys);
        if (!threeNfViolations.isEmpty()) {
            violations.addAll(threeNfViolations);
            explanation = "3NF violation detected";
            return new NormalFormResult(
                NormalFormResult.NormalForm.SECOND_NF, violations, explanation);
        }

        Set<String> bcnfViolations = findBcnfViolations(
            attributes, functionalDependencies);
        if (!bcnfViolations.isEmpty()) {
            violations.addAll(bcnfViolations);
            explanation = "BCNF violation detected";
            return new NormalFormResult(
                NormalFormResult.NormalForm.THIRD_NF, violations, explanation);
        }

        return new NormalFormResult(
            NormalFormResult.NormalForm.BCNF, new HashSet<>(), 
            "Relation satisfies BCNF");
    }

    private Set<String> find2NfViolations(
            Set<String> attributes,
            Set<FunctionalDependency> functionalDependencies,
            Set<CandidateKey> candidateKeys) {

        Set<String> violations = new HashSet<>();
        Set<String> nonPrimeAttributes = new HashSet<>(attributes);
        nonPrimeAttributes.removeAll(getPrimeAttributes(candidateKeys));

        if (nonPrimeAttributes.isEmpty()) {
            return violations;
        }

        for (CandidateKey candidateKey : candidateKeys) {
            Set<String> keyAttributes = candidateKey.getAttributes();
            if (keyAttributes.size() <= 1) {
                continue;
            }

            for (Set<String> subset : properNonEmptySubsets(keyAttributes)) {
                Set<String> closure = closureComputer.computeClosure(
                    subset, functionalDependencies);

                for (String nonPrimeAttribute : nonPrimeAttributes) {
                    if (closure.contains(nonPrimeAttribute)
                            && !subset.contains(nonPrimeAttribute)) {
                        violations.add("2NF violation: "
                            + formatSet(subset)
                            + " partially determines non-prime attribute "
                            + nonPrimeAttribute
                            + " from candidate key "
                            + formatSet(keyAttributes));
                    }
                }
            }
        }

        return violations;
    }

    private Set<String> find3NfViolations(
            Set<String> attributes,
            Set<FunctionalDependency> functionalDependencies,
            Set<CandidateKey> candidateKeys) {

        Set<String> violations = new HashSet<>();
        Set<String> primeAttributes = getPrimeAttributes(candidateKeys);

        for (FunctionalDependency fd : functionalDependencies) {
            if (isTrivial(fd)
                    || isSuperkey(fd.getLeftSide(), attributes,
                        functionalDependencies)) {
                continue;
            }

            for (String rightAttribute : fd.getRightSide()) {
                if (!fd.getLeftSide().contains(rightAttribute)
                        && !primeAttributes.contains(rightAttribute)) {
                    violations.add("3NF violation: "
                        + formatSet(fd.getLeftSide())
                        + " determines non-prime attribute "
                        + rightAttribute
                        + " without being a superkey");
                }
            }
        }

        return violations;
    }

    private Set<String> findBcnfViolations(
            Set<String> attributes,
            Set<FunctionalDependency> functionalDependencies) {

        Set<String> violations = new HashSet<>();
        for (FunctionalDependency fd : functionalDependencies) {
            if (!isTrivial(fd)
                    && !isSuperkey(fd.getLeftSide(), attributes,
                        functionalDependencies)) {
                violations.add("BCNF violation: "
                    + formatSet(fd.getLeftSide())
                    + " is not a superkey for "
                    + fd);
            }
        }
        return violations;
    }

    private boolean isSuperkey(
            Set<String> determinant,
            Set<String> attributes,
            Set<FunctionalDependency> functionalDependencies) {
        return closureComputer.computeClosure(
            determinant, functionalDependencies).containsAll(attributes);
    }

    private Set<String> getPrimeAttributes(Set<CandidateKey> candidateKeys) {
        Set<String> primeAttributes = new HashSet<>();
        for (CandidateKey candidateKey : candidateKeys) {
            primeAttributes.addAll(candidateKey.getAttributes());
        }
        return primeAttributes;
    }

    private boolean isTrivial(FunctionalDependency fd) {
        return fd.getLeftSide().containsAll(fd.getRightSide());
    }

    private Set<Set<String>> properNonEmptySubsets(Set<String> attributes) {
        List<String> orderedAttributes = new ArrayList<>(attributes);
        orderedAttributes.sort(String::compareTo);

        Set<Set<String>> subsets = new HashSet<>();
        int subsetCount = 1 << orderedAttributes.size();
        for (int mask = 1; mask < subsetCount - 1; mask++) {
            Set<String> subset = new HashSet<>();
            for (int index = 0; index < orderedAttributes.size(); index++) {
                if ((mask & (1 << index)) != 0) {
                    subset.add(orderedAttributes.get(index));
                }
            }
            subsets.add(subset);
        }
        return subsets;
    }

    private String formatSet(Set<String> values) {
        return values.stream()
            .sorted()
            .reduce("{", (result, value) -> {
                if (result.length() == 1) {
                    return result + value;
                }
                return result + "," + value;
            }) + "}";
    }
}
