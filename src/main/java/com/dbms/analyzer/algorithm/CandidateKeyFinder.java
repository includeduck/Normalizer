package com.dbms.analyzer.algorithm;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.FunctionalDependency;

public class CandidateKeyFinder {

    private ClosureComputer closureComputer;

    public CandidateKeyFinder() {
        this.closureComputer = new ClosureComputer();
    }

    /**
     * Finds all candidate keys for a relation
     * @param attributes All attributes of the relation
     * @param functionalDependencies All FDs of the relation
     * @return Set of all candidate keys
     */
    public Set<CandidateKey> findAllCandidateKeys(
            Set<String> attributes,
            Set<FunctionalDependency> functionalDependencies) {
        
        Set<CandidateKey> candidateKeys = new HashSet<>();
        Set<Set<String>> superKeys = new HashSet<>();

        // Generate all subsets and check which are superkeys
        Set<Set<String>> allSubsets = generateAllSubsets(attributes);
        
        for (Set<String> subset : allSubsets) {
            Set<String> closure = closureComputer.computeClosure(
                subset, functionalDependencies);
            
            if (closure.equals(attributes)) {
                superKeys.add(new HashSet<>(subset));
            }
        }

        // Filter out non-minimal superkeys to get candidate keys
        for (Set<String> superKey : superKeys) {
            boolean isMinimal = true;
            for (Set<String> other : superKeys) {
                if (!superKey.equals(other) && superKey.containsAll(other)) {
                    isMinimal = false;
                    break;
                }
            }
            if (isMinimal) {
                candidateKeys.add(new CandidateKey(superKey));
            }
        }

        return candidateKeys;
    }

    /**
     * Generates all subsets of a given set
     */
    private Set<Set<String>> generateAllSubsets(Set<String> set) {
        Set<Set<String>> subsets = new HashSet<>();
        List<String> list = new ArrayList<>(set);
        int n = list.size();
        
        for (int i = 0; i < (1 << n); i++) {
            Set<String> subset = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    subset.add(list.get(j));
                }
            }
            subsets.add(subset);
        }
        
        return subsets;
    }
}
