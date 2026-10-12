package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.NormalFormChecker;
import com.dbms.analyzer.model.NormalFormResult;

@Service
public class NormalFormService {

    private final RelationService relationService;
    private final FdService fdService;
    private final CandidateKeyService candidateKeyService;
    private final NormalFormChecker normalFormChecker;

    public NormalFormService(
            RelationService relationService,
            FdService fdService,
            CandidateKeyService candidateKeyService,
            NormalFormChecker normalFormChecker) {
        this.relationService = relationService;
        this.fdService = fdService;
        this.candidateKeyService = candidateKeyService;
        this.normalFormChecker = normalFormChecker;
    }

    /**
     * Analyzes and returns the highest normal form satisfied
     */
    public NormalFormResult analyzeNormalForm() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        return normalFormChecker.determineHighestNormalForm(
            relationService.getAttributes(),
            fdService.getAllDependencies(),
            candidateKeyService.findAllCandidateKeys());
    }
}
