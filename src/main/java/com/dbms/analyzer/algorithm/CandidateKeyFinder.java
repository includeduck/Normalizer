package com.dbms.analyzer.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.FunctionalDependency;

public class CandidateKeyFinder {

    /**
     * Finds all candidate keys for a relation.
     *
     * Attributes are classified first:
     * <ul>
     *   <li>never on the non-trivial right side of an FD: cannot be derived,
     *       so they belong to every key (the core);</li>
     *   <li>on a right side but never on a left side: never in any key;</li>
     *   <li>the rest: may or may not be in a key.</li>
     * </ul>
     * Only subsets of the last group are searched, smallest first, skipping
     * supersets of keys already found, so every key found is minimal.
     *
     * @param attributes All attributes of the relation
     * @param functionalDependencies All FDs of the relation
     * @return Set of all candidate keys
     * @throws IllegalArgumentException if the relation exceeds
     *         {@link AnalysisLimits#MAX_ATTRIBUTES}
     */
    public Set<CandidateKey> findAllCandidateKeys(
            Set<String> attributes,
            Set<FunctionalDependency> functionalDependencies) {

        AnalysisLimits.requireWithinLimit(attributes.size());

        FdBitIndex index = new FdBitIndex(attributes, functionalDependencies);
        long all = index.mask(attributes);

        long derivable = 0L;
        long determinants = 0L;
        for (int i = 0; i < index.fdCount(); i++) {
            derivable |= index.rhs(i) & ~index.lhs(i);
            determinants |= index.lhs(i);
        }

        long core = all & ~derivable;
        long neverInKey = all & derivable & ~determinants;
        long optional = all & ~core & ~neverInKey;

        Set<CandidateKey> candidateKeys = new HashSet<>();
        if (FdBitIndex.isSubset(all, index.closure(core))) {
            candidateKeys.add(new CandidateKey(index.toSet(core)));
            return candidateKeys;
        }

        List<Integer> optionalBits = FdBitIndex.bitsOf(optional);

        int m = optionalBits.size();
        List<Long> foundKeys = new ArrayList<>();
        for (int size = 1; size <= m; size++) {
            // Gosper's hack: iterate all m-bit combinations with `size` bits set
            long combination = (1L << size) - 1;
            while (combination < (1L << m)) {
                long candidate = core | FdBitIndex.expand(combination, optionalBits);

                if (!containsAnyKey(candidate, foundKeys)
                        && FdBitIndex.isSubset(all, index.closure(candidate))) {
                    foundKeys.add(candidate);
                }

                combination = FdBitIndex.nextCombination(combination);
            }
        }

        for (long key : foundKeys) {
            candidateKeys.add(new CandidateKey(index.toSet(key)));
        }
        return candidateKeys;
    }

    private static boolean containsAnyKey(long candidate, List<Long> keys) {
        for (long key : keys) {
            if (FdBitIndex.isSubset(key, candidate)) {
                return true;
            }
        }
        return false;
    }
}
