package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.ClosureComputer;
import com.dbms.analyzer.algorithm.FdUtil;
import java.util.Set;
import java.util.List;

@Service
public class ClosureService {

    private final RelationService relationService;
    private final FdService fdService;
    private final ClosureComputer closureComputer;

    public ClosureService(
            RelationService relationService,
            FdService fdService,
            ClosureComputer closureComputer) {
        this.relationService = relationService;
        this.fdService = fdService;
        this.closureComputer = closureComputer;
    }

    /**
     * Computes the closure of a set of attributes
     * @param attributeString A string like "ABC"
     * @return The closure as a set
     */
    public Set<String> computeClosure(String attributeString) 
            throws IllegalArgumentException {
        
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        Set<String> attributeSet = FdUtil.stringToAttributes(attributeString);
        
        // Validate attributes exist
        Set<String> relationAttrs = relationService.getAttributes();
        if (!relationAttrs.containsAll(attributeSet)) {
            throw new IllegalArgumentException(
                "Unknown attributes in input");
        }

        return closureComputer.computeClosure(
            attributeSet, fdService.getAllDependencies());
    }

    /**
     * Computes closure with step-by-step explanation
     */
    public List<String> computeClosureWithSteps(String attributeString) 
            throws IllegalArgumentException {
        
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        Set<String> attributeSet = FdUtil.stringToAttributes(attributeString);
        
        // Validate attributes exist
        Set<String> relationAttrs = relationService.getAttributes();
        if (!relationAttrs.containsAll(attributeSet)) {
            throw new IllegalArgumentException("Unknown attributes in input");
        }

        return closureComputer.computeClosureWithSteps(
            attributeSet, fdService.getAllDependencies());
    }

    /**
     * Checks if a set of attributes forms a superkey
     */
    public boolean isSuperKey(String attributeString) {
        Set<String> closure = computeClosure(attributeString);
        return closure.equals(relationService.getAttributes());
    }
}
