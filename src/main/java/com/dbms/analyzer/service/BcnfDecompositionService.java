package com.dbms.analyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.BcnfDecomposer;
import com.dbms.analyzer.model.Relation;
import java.util.Set;

@Service
public class BcnfDecompositionService {

    @Autowired
    private RelationService relationService;

    private BcnfDecomposer bcnfDecomposer;

    public BcnfDecompositionService() {
        this.bcnfDecomposer = new BcnfDecomposer();
    }

    /**
     * Decomposes the current relation to BCNF
     */
    public Set<Relation> decomposeToBcnf() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        Relation current = relationService.getCurrentRelation();
        return bcnfDecomposer.decomposeToBcnf(current);
    }

    /**
     * Checks if a relation is already in BCNF
     */
    public boolean isAlreadyBcnf() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        return decomposeToBcnf().size() == 1;
    }

    /**
     * Gets decomposition steps (for future enhancement)
     */
    public java.util.List<java.util.Set<Relation>> getDecompositionSteps() {
        Set<Relation> result = decomposeToBcnf();
        java.util.List<java.util.Set<Relation>> steps = 
            new java.util.ArrayList<>();
        steps.add(result);
        return steps;
    }
}
