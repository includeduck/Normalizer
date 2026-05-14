package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.DependencyPreservationChecker;
import com.dbms.analyzer.algorithm.LosslessJoinChecker;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;

import java.util.List;
import java.util.Set;

/**
 * Service for verifying decomposition quality: dependency preservation
 * and lossless-join property.
 */
@Service
public class DecompositionAnalysisService {

    private final RelationService relationService;
    private final FdService fdService;
    private final DependencyPreservationChecker depChecker;
    private final LosslessJoinChecker joinChecker;

    public DecompositionAnalysisService(
            RelationService relationService,
            FdService fdService,
            DependencyPreservationChecker depChecker,
            LosslessJoinChecker joinChecker) {
        this.relationService = relationService;
        this.fdService = fdService;
        this.depChecker = depChecker;
        this.joinChecker = joinChecker;
    }

    /**
     * Checks if a decomposition preserves all FDs of the current relation.
     */
    public boolean isDependencyPreserving(Set<Relation> decomposition) {
        ensureRelation();
        return depChecker.isPreserved(
                fdService.getAllDependencies(), decomposition);
    }

    /**
     * Returns the FDs that are NOT preserved by the decomposition.
     */
    public List<FunctionalDependency> findLostDependencies(
            Set<Relation> decomposition) {
        ensureRelation();
        return depChecker.findLostDependencies(
                fdService.getAllDependencies(), decomposition);
    }

    /**
     * Step-by-step dependency preservation check.
     */
    public List<String> checkDependencyPreservationWithSteps(
            Set<Relation> decomposition) {
        ensureRelation();
        return depChecker.checkWithSteps(
                fdService.getAllDependencies(), decomposition);
    }

    /**
     * Checks if a decomposition is lossless-join.
     */
    public boolean isLosslessJoin(Set<Relation> decomposition) {
        ensureRelation();
        return joinChecker.isLossless(
                relationService.getAttributes(),
                fdService.getAllDependencies(),
                decomposition);
    }

    /**
     * Step-by-step lossless join check.
     */
    public List<String> checkLosslessJoinWithSteps(
            Set<Relation> decomposition) {
        ensureRelation();
        return joinChecker.checkWithSteps(
                relationService.getAttributes(),
                fdService.getAllDependencies(),
                decomposition);
    }

    private void ensureRelation() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }
    }
}
