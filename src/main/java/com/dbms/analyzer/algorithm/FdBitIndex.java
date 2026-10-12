package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.FunctionalDependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Bitmask view of an attribute universe and an FD set. Used by algorithms
 * that compute closures for many attribute subsets, where set-based
 * closures would allocate far too much.
 *
 * Bit i corresponds to the i-th attribute in sorted order.
 */
final class FdBitIndex {

    private final List<String> attributes;
    private final Map<String, Integer> positions = new HashMap<>();
    private final long[] lhs;
    private final long[] rhs;

    FdBitIndex(Collection<String> universe,
               Collection<FunctionalDependency> functionalDependencies) {
        Set<String> all = new TreeSet<>(universe);
        for (FunctionalDependency fd : functionalDependencies) {
            all.addAll(fd.getLeftSide());
            all.addAll(fd.getRightSide());
        }
        if (all.size() > Long.SIZE - 1) {
            throw new IllegalArgumentException(
                "Too many attributes for analysis: " + all.size());
        }

        this.attributes = new ArrayList<>(all);
        for (int i = 0; i < attributes.size(); i++) {
            positions.put(attributes.get(i), i);
        }

        this.lhs = new long[functionalDependencies.size()];
        this.rhs = new long[functionalDependencies.size()];
        int i = 0;
        for (FunctionalDependency fd : functionalDependencies) {
            lhs[i] = mask(fd.getLeftSide());
            rhs[i] = mask(fd.getRightSide());
            i++;
        }
    }

    long mask(Collection<String> attributeSet) {
        long mask = 0L;
        for (String attribute : attributeSet) {
            Integer position = positions.get(attribute);
            if (position == null) {
                throw new IllegalArgumentException(
                    "Unknown attribute: " + attribute);
            }
            mask |= 1L << position;
        }
        return mask;
    }

    Set<String> toSet(long mask) {
        Set<String> result = new TreeSet<>();
        for (int i = 0; i < attributes.size(); i++) {
            if ((mask & (1L << i)) != 0) {
                result.add(attributes.get(i));
            }
        }
        return result;
    }

    int fdCount() {
        return lhs.length;
    }

    long lhs(int fdIndex) {
        return lhs[fdIndex];
    }

    long rhs(int fdIndex) {
        return rhs[fdIndex];
    }

    /** Fixed-point attribute closure of {@code start}. */
    long closure(long start) {
        long closure = start;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < lhs.length; i++) {
                if ((lhs[i] & ~closure) == 0 && (rhs[i] & ~closure) != 0) {
                    closure |= rhs[i];
                    changed = true;
                }
            }
        }
        return closure;
    }

    static boolean isSubset(long subset, long superset) {
        return (subset & ~superset) == 0;
    }

    /** Positions of the set bits of {@code mask}, ascending. */
    static List<Integer> bitsOf(long mask) {
        List<Integer> bits = new ArrayList<>();
        for (int bit = 0; bit < Long.SIZE - 1; bit++) {
            if ((mask & (1L << bit)) != 0) {
                bits.add(bit);
            }
        }
        return bits;
    }

    /**
     * Maps a combination over {@code bits.size()} positions back onto the
     * attribute bits it selects.
     */
    static long expand(long combination, List<Integer> bits) {
        long mask = 0L;
        for (int i = 0; i < bits.size(); i++) {
            if ((combination & (1L << i)) != 0) {
                mask |= 1L << bits.get(i);
            }
        }
        return mask;
    }

    /** Gosper's hack: next integer with the same number of set bits. */
    static long nextCombination(long combination) {
        long lowest = combination & -combination;
        long ripple = combination + lowest;
        return ripple | (((ripple ^ combination) >>> 2) / lowest);
    }
}
