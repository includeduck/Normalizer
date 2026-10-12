package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.NormalFormResult;
import com.dbms.analyzer.model.Relation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Standard textbook exercises with exact expected answers. */
public class TextbookExamplesTest {

    private final CandidateKeyFinder keyFinder = new CandidateKeyFinder();
    private final NormalFormChecker normalFormChecker = new NormalFormChecker();
    private final BcnfDecomposer bcnfDecomposer = new BcnfDecomposer();
    private final ThreeNfSynthesizer threeNfSynthesizer = new ThreeNfSynthesizer();
    private final DependencyPreservationChecker preservationChecker =
        new DependencyPreservationChecker();
    private final LosslessJoinChecker losslessJoinChecker = new LosslessJoinChecker();

    @Test
    public void testThreeOverlappingKeysAllAttributesPrime() {
        // R(A,B,C,D,E), A -> B, BC -> E, ED -> A
        // Keys ACD, BCD, CDE; every attribute is prime, so 3NF but not BCNF.
        Set<FunctionalDependency> fds = Set.of(
            FdUtil.parseFD("A -> B"), FdUtil.parseFD("BC -> E"), FdUtil.parseFD("ED -> A"));
        Set<String> attributes = Set.of("A", "B", "C", "D", "E");

        Set<CandidateKey> keys = keyFinder.findAllCandidateKeys(attributes, fds);
        assertEquals(Set.of(
            new CandidateKey(Set.of("A", "C", "D")),
            new CandidateKey(Set.of("B", "C", "D")),
            new CandidateKey(Set.of("C", "D", "E"))), keys);

        assertEquals(NormalFormResult.NormalForm.THIRD_NF,
            normalFormChecker.determineHighestNormalForm(attributes, fds, keys)
                .getHighestNormalForm());
    }

    @Test
    public void testTransitiveChain() {
        // R(A,B,C,D), A -> B, B -> C, C -> D: key A, 2NF but not 3NF
        Relation relation = new Relation("R", Set.of("A", "B", "C", "D"), Set.of(
            FdUtil.parseFD("A -> B"), FdUtil.parseFD("B -> C"), FdUtil.parseFD("C -> D")));
        Set<CandidateKey> keys = keyFinder.findAllCandidateKeys(
            relation.getAttributes(), relation.getFunctionalDependencies());

        assertEquals(Set.of(new CandidateKey(Set.of("A"))), keys);
        assertEquals(NormalFormResult.NormalForm.SECOND_NF,
            normalFormChecker.determineHighestNormalForm(relation.getAttributes(),
                relation.getFunctionalDependencies(), keys).getHighestNormalForm());

        assertEquals(List.of("R_1_1(C,D)", "R_1_2(B,C)", "R_2(A,B)"),
            names(bcnfDecomposer.decomposeToBcnf(relation)));
        assertEquals(List.of("R_3NF_1(A,B)", "R_3NF_2(B,C)", "R_3NF_3(C,D)"),
            names(threeNfSynthesizer.synthesize(relation)));
    }

    @Test
    public void testBcnfCannotPreserveEveryDependency() {
        // R(A,B,C), AB -> C, C -> B: keys AB and AC; 3NF but not BCNF.
        // BCNF splits into (B,C) and (A,C) and loses AB -> C; 3NF keeps R.
        Relation relation = new Relation("R", Set.of("A", "B", "C"), Set.of(
            FdUtil.parseFD("AB -> C"), FdUtil.parseFD("C -> B")));
        Set<FunctionalDependency> fds = relation.getFunctionalDependencies();
        Set<CandidateKey> keys = keyFinder.findAllCandidateKeys(
            relation.getAttributes(), fds);

        assertEquals(Set.of(new CandidateKey(Set.of("A", "B")),
            new CandidateKey(Set.of("A", "C"))), keys);
        assertEquals(NormalFormResult.NormalForm.THIRD_NF,
            normalFormChecker.determineHighestNormalForm(
                relation.getAttributes(), fds, keys).getHighestNormalForm());

        Set<Relation> bcnf = bcnfDecomposer.decomposeToBcnf(relation);
        assertEquals(Set.of(Set.of("B", "C"), Set.of("A", "C")),
            bcnf.stream().map(Relation::getAttributes).collect(Collectors.toSet()));
        assertEquals(List.of(FdUtil.parseFD("AB -> C")),
            preservationChecker.findLostDependencies(fds, bcnf));
        assertTrue(losslessJoinChecker.isLossless(relation.getAttributes(), fds, bcnf));

        assertEquals(List.of("R_3NF_1(A,B,C)"),
            names(threeNfSynthesizer.synthesize(relation)));
    }

    private static List<String> names(Set<Relation> relations) {
        return relations.stream().map(Relation::toString).sorted().toList();
    }
}
