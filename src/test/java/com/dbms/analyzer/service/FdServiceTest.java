package com.dbms.analyzer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;

public class FdServiceTest {

    private FdService fdService;
    private RelationService relationService;

    @BeforeEach
    public void setUp() {
        relationService = new RelationService();
        fdService = new FdService();
        fdService.relationService = relationService;
        
        relationService.createRelation("R(A,B,C)");
    }

    @Test
    public void testAddValidFD() {
        fdService.addFunctionalDependency("A -> B");
        assertEquals(1, fdService.getAllDependencies().size());
    }

    @Test
    public void testAddFDWithoutRelation() {
        relationService.clear();
        assertThrows(IllegalStateException.class, () ->
            fdService.addFunctionalDependency("A -> B"));
    }

    @Test
    public void testAddFDInvalidAttribute() {
        assertThrows(IllegalArgumentException.class, () ->
            fdService.addFunctionalDependency("A -> D"));
    }

    @Test
    public void testValidateFdString() {
        assertTrue(fdService.validateFdString("A -> B"));
        assertFalse(fdService.validateFdString("A -> D"));
    }

    @Test
    public void testClearDependencies() {
        fdService.addFunctionalDependency("A -> B");
        fdService.clearDependencies();
        assertEquals(0, fdService.getAllDependencies().size());
    }
}
