package com.dbms.analyzer.javafx;

import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.NormalFormResult;
import com.dbms.analyzer.model.Relation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ResultFormatterTest {

    @Test
    public void testCandidateKeysReportListsKeysInCanonicalOrder() {
        String report = ResultFormatter.candidateKeysReport(
            Set.of(new CandidateKey(Set.of("B", "C")), new CandidateKey(Set.of("A"))),
            Set.of("A", "B", "C"), Set.of("D"));

        assertEquals("Candidate Keys\n- {A}\n- {B,C}\n\n"
            + "Prime Attributes: {A, B, C}\nNon-Prime Attributes: {D}", report);
    }

    @Test
    public void testNormalFormReportDerivesEachFormFromTheHighest() {
        NormalFormResult result = new NormalFormResult(
            NormalFormResult.NormalForm.THIRD_NF,
            Set.of("BCNF violation: {C} is not a superkey for C -> B"),
            "BCNF violation detected");

        String report = ResultFormatter.normalFormReport(result);

        assertTrue(report.contains("Highest Normal Form: THIRD_NF"), report);
        assertTrue(report.contains("1NF: assumed"), report);
        assertTrue(report.contains("2NF: true\n3NF: true\nBCNF: false"), report);
        assertTrue(report.contains("- BCNF violation: {C} is not a superkey"), report);
    }

    @Test
    public void testDecompositionSummary() {
        assertEquals("No decomposition run", ResultFormatter.decompositionSummary(null));
        assertEquals("2 relations: R_1(A,B); R_2(B,C)",
            ResultFormatter.decompositionSummary(Set.of(
                new Relation("R_2", Set.of("B", "C")),
                new Relation("R_1", Set.of("A", "B")))));
    }

    @Test
    public void testClosureReportAndCounts() {
        assertEquals("Closure Input: A\n\nInitial closure: {A}\nFinal closure: {A}",
            ResultFormatter.closureReport("A",
                List.of("Initial closure: {A}", "Final closure: {A}")));
        assertEquals("1 dependency", ResultFormatter.formatCount(1, "dependency", "dependencies"));
        assertEquals("None", ResultFormatter.formatKeys(Set.of()));
    }

    @Test
    public void testTutorialTextIsLoadedFromResources() {
        String tutorial = ResultFormatter.tutorialText();
        assertTrue(tutorial.startsWith("App Tutorial"), tutorial);
        assertTrue(tutorial.contains("Check Result tab"), tutorial);
    }

    @Test
    public void testEverySampleRelationIsValid() {
        // Guards the "Load Example" button: every built-in sample must load
        for (SampleRelations.Sample sample : SampleRelations.ALL) {
            var relationService = new com.dbms.analyzer.service.RelationService();
            var fdService = new com.dbms.analyzer.service.FdService(relationService);
            Relation loaded = fdService.loadRelation(sample.schema(), sample.dependencies());
            assertEquals(sample.dependencies().size(),
                loaded.getFunctionalDependencies().size(), sample.name());
        }
        assertEquals(SampleRelations.ALL.get(0), SampleRelations.byName("no such sample"));
    }
}
