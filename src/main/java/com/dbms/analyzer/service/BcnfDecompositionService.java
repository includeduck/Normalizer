package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.BcnfDecomposer;
import com.dbms.analyzer.model.DecompositionStep;
import com.dbms.analyzer.model.Relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class BcnfDecompositionService {

    private final RelationService relationService;
    private final BcnfDecomposer bcnfDecomposer;

    public BcnfDecompositionService(
            RelationService relationService,
            BcnfDecomposer bcnfDecomposer) {
        this.relationService = relationService;
        this.bcnfDecomposer = bcnfDecomposer;
    }

    /**
     * Decomposes the current relation to BCNF.
     */
    public Set<Relation> decomposeToBcnf() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        Relation current = relationService.getCurrentRelation();
        return bcnfDecomposer.decomposeToBcnf(current);
    }

    /**
     * Decomposes and collects step-by-step decomposition records.
     */
    public Set<Relation> decomposeToBcnfWithSteps(List<DecompositionStep> stepLog) {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        Relation current = relationService.getCurrentRelation();
        return bcnfDecomposer.decomposeToBcnfWithSteps(current, stepLog);
    }

    /**
     * Produces a readable step-by-step explanation of the BCNF decomposition.
     */
    public List<String> decomposeWithExplanation() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        Relation current = relationService.getCurrentRelation();
        return bcnfDecomposer.decomposeWithExplanation(current);
    }

    /**
     * Checks if a relation is already in BCNF.
     */
    public boolean isAlreadyBcnf() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        return decomposeToBcnf().size() == 1;
    }

    /**
     * Gets decomposition steps.
     */
    public List<Set<Relation>> getDecompositionSteps() {
        Set<Relation> result = decomposeToBcnf();
        List<Set<Relation>> steps = new ArrayList<>();
        steps.add(result);
        return steps;
    }
}
