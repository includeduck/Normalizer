package com.dbms.analyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExplanationService {

    @Autowired
    private RelationService relationService;

    @Autowired
    private FdService fdService;

    @Autowired
    private ClosureService closureService;

    @Autowired
    private CandidateKeyService candidateKeyService;

    @Autowired
    private NormalFormService normalFormService;

    /**
     * Generates explanation for closure computation
     */
    public List<String> explainClosure(String attributeString) {
        return closureService.computeClosureWithSteps(attributeString);
    }

    /**
     * Generates explanation for candidate key finding
     */
    public List<String> explainCandidateKeys() {
        List<String> explanation = new ArrayList<>();
        explanation.add("Finding all candidate keys...");
        explanation.add("Candidate keys are minimal superkeys.");
        
        candidateKeyService.findAllCandidateKeys().forEach(key ->
            explanation.add("  Candidate Key: " + key));

        explanation.add("\nPrime attributes: " + 
            candidateKeyService.getPrimeAttributes());
        explanation.add("Non-prime attributes: " + 
            candidateKeyService.getNonPrimeAttributes());

        return explanation;
    }

    /**
     * Generates explanation for normal form analysis
     */
    public List<String> explainNormalForm() {
        List<String> explanation = new ArrayList<>();
        var result = normalFormService.analyzeNormalForm();
        
        explanation.add("Normal Form Analysis:");
        explanation.add("  1NF satisfied: " + normalFormService.is1NF());
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
        explanation.add("Attributes: " + relationService.getAttributes());
        explanation.add("Functional Dependencies:");
        
        fdService.getAllDependencies().forEach(fd ->
            explanation.add("  " + fd));

        return explanation;
    }
}
