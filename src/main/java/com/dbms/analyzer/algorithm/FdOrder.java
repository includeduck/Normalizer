package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.FunctionalDependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Canonical ordering used wherever an algorithm has to choose between
 * equally valid options (which FD to apply or drop first, which key to
 * add), so results depend only on the FDs themselves, never on hash-set
 * iteration order.
 *
 * Attribute sets are ordered by size, then alphabetically by their sorted
 * members; FDs by left side, then right side.
 */
public final class FdOrder {

    public static final Comparator<Set<String>> ATTRIBUTE_SETS =
        Comparator.<Set<String>>comparingInt(Set::size)
            .thenComparing(FdOrder::compareSortedMembers);

    public static final Comparator<FunctionalDependency> CANONICAL =
        Comparator.comparing(FunctionalDependency::getLeftSide, ATTRIBUTE_SETS)
            .thenComparing(FunctionalDependency::getRightSide, ATTRIBUTE_SETS);

    public static final Comparator<CandidateKey> KEYS =
        Comparator.comparing(CandidateKey::getAttributes, ATTRIBUTE_SETS);

    private FdOrder() {
    }

    public static List<FunctionalDependency> sorted(
            Collection<FunctionalDependency> fds) {
        List<FunctionalDependency> sorted = new ArrayList<>(fds);
        sorted.sort(CANONICAL);
        return sorted;
    }

    public static List<CandidateKey> sortedKeys(Collection<CandidateKey> keys) {
        List<CandidateKey> sorted = new ArrayList<>(keys);
        sorted.sort(KEYS);
        return sorted;
    }

    private static int compareSortedMembers(Set<String> left, Set<String> right) {
        Iterator<String> l = new TreeSet<>(left).iterator();
        Iterator<String> r = new TreeSet<>(right).iterator();
        while (l.hasNext() && r.hasNext()) {
            int comparison = l.next().compareTo(r.next());
            if (comparison != 0) {
                return comparison;
            }
        }
        return Boolean.compare(l.hasNext(), r.hasNext());
    }
}
