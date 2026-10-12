package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.FdOrder;
import com.dbms.analyzer.algorithm.NormalFormChecker;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class ExplanationService {

    private final RelationService relationService;
    private final FdService fdService;
    private final CandidateKeyService candidateKeyService;
    private final NormalFormService normalFormService;

    public ExplanationService(
            RelationService relationService,
            FdService fdService,
            CandidateKeyService candidateKeyService,
            NormalFormService normalFormService) {
        this.relationService = relationService;
        this.fdService = fdService;
        this.candidateKeyService = candidateKeyService;
        this.normalFormService = normalFormService;
    }

    /**
     * Generates explanation for candidate key finding
     */
    public List<String> explainCandidateKeys() {
        List<String> explanation = new ArrayList<>();
        explanation.add("Finding all candidate keys...");
        explanation.add("Candidate keys are minimal superkeys.");
        
        FdOrder.sortedKeys(candidateKeyService.findAllCandidateKeys()).forEach(key ->
            explanation.add("  Candidate Key: " + key));

        explanation.add("");
        explanation.add("Prime attributes: "
            + formatSet(candidateKeyService.getPrimeAttributes()));
        explanation.add("Non-prime attributes: "
            + formatSet(candidateKeyService.getNonPrimeAttributes()));

        return explanation;
    }

    /**
     * Generates explanation for normal form analysis
     */
    public List<String> explainNormalForm() {
        List<String> explanation = new ArrayList<>();
        var result = normalFormService.analyzeNormalForm();
        
        explanation.add("Normal Form Analysis:");
        explanation.add("  1NF: " + NormalFormChecker.FIRST_NF_ASSUMPTION);
        explanation.add("  2NF satisfied: " + normalFormService.is2NF());
        explanation.add("  3NF satisfied: " + normalFormService.is3NF());
        explanation.add("  BCNF satisfied: " + normalFormService.isBCNF());
        explanation.add("\nHighest Normal Form: " + 
            result.getHighestNormalForm());
        
        if (!result.getViolations().isEmpty()) {
            explanation.add("Violations:");
            result.getViolations().forEach(v ->
                explanation.add("  - " + v));
        }

        return explanation;
    }

    /**
     * Generates general explanation about the relation
     */
    public List<String> explainRelation() {
        List<String> explanation = new ArrayList<>();
        
        if (!relationService.hasRelation()) {
            explanation.add("No relation defined.");
            return explanation;
        }

        explanation.add("Relation: " + 
            relationService.getCurrentRelation());
        explanation.add("Attributes: " + formatSet(relationService.getAttributes()));
        explanation.add("Functional Dependencies:");
        
        fdService.getAllDependencies().stream()
            .map(Object::toString)
            .sorted()
            .forEach(fd -> explanation.add("  " + fd));

        return explanation;
    }

    private String formatSet(Set<String> values) {
        return "{" + String.join(", ", new TreeSet<>(values)) + "}";
    }
}
