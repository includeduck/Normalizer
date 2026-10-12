package com.dbms.analyzer.service;

import com.dbms.analyzer.algorithm.AnalysisLimits;
import com.dbms.analyzer.algorithm.CandidateKeyFinder;
import com.dbms.analyzer.algorithm.NormalFormChecker;
import com.dbms.analyzer.model.NormalFormResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class NormalFormServiceTest {

    private RelationService relationService;
    private FdService fdService;
    private CandidateKeyService candidateKeyService;
    private NormalFormService normalFormService;

    @BeforeEach
    public void setUp() {
        relationService = new RelationService();
        fdService = new FdService(relationService);
        candidateKeyService = new CandidateKeyService(
            relationService, fdService, new CandidateKeyFinder());
        normalFormService = new NormalFormService(
            relationService, fdService, candidateKeyService,
            new NormalFormChecker());
    }

    @Test
    public void testNormalFormClickAtAttributeLimitIsFast() {
        // BUG-004: one "Normal Form" click with 16 attributes used to take 13 s
        StringBuilder schema = new StringBuilder("R(");
        for (int i = 0; i < AnalysisLimits.MAX_ATTRIBUTES; i++) {
            schema.append(i == 0 ? "" : ",").append("x").append(i);
        }
        relationService.createRelation(schema.append(")").toString());
        fdService.addFunctionalDependency("x0 -> x1");
        fdService.addFunctionalDependency("x2 -> x3");

        // Replays the service calls MainController makes for one click
        NormalFormResult result = assertTimeoutPreemptively(
            Duration.ofSeconds(5), () -> {
                NormalFormResult analysis = normalFormService.analyzeNormalForm();
                normalFormService.is1NF();
                normalFormService.is2NF();
                normalFormService.is3NF();
                normalFormService.isBCNF();
                candidateKeyService.findAllCandidateKeys();
                normalFormService.analyzeNormalForm();
                return analysis;
            });

        assertEquals(NormalFormResult.NormalForm.FIRST_NF,
            result.getHighestNormalForm());
    }

    @Test
    public void testKeysRecomputedAfterFdChange() {
        relationService.createRelation("R(A,B,C)");
        assertEquals(1, candidateKeyService.findAllCandidateKeys().size());
        assertEquals(3,
            candidateKeyService.findAllCandidateKeys().iterator().next()
                .getAttributes().size());

        fdService.addFunctionalDependency("A -> B,C");
        assertEquals(java.util.Set.of("A"),
            candidateKeyService.findAllCandidateKeys().iterator().next()
                .getAttributes());
    }
}
