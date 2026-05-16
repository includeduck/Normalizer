package com.dbms.analyzer.service;

import com.dbms.analyzer.algorithm.ClosureComputer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClosureServiceTest {

    private RelationService relationService;
    private FdService fdService;
    private ClosureService closureService;

    @BeforeEach
    public void setUp() {
        relationService = new RelationService();
        fdService = new FdService(relationService);
        closureService = new ClosureService(
            relationService,
            fdService,
            new ClosureComputer());
    }

    @Test
    public void testComputeClosureWithMultiCharacterAttributes() {
        relationService.createRelation(
            "Enrollment(student_id,course_id,grade)");
        fdService.addFunctionalDependency("student_id, course_id -> grade");

        Set<String> closure =
            closureService.computeClosure("student_id, course_id");

        assertEquals(Set.of("student_id", "course_id", "grade"), closure);
    }

    @Test
    public void testComputeClosureForCompactMultiCharacterAttribute() {
        relationService.createRelation("R(AB,C)");
        fdService.addFunctionalDependency("AB -> C");

        Set<String> closure = closureService.computeClosure("AB");

        assertTrue(closure.containsAll(Set.of("AB", "C")));
        assertEquals(2, closure.size());
    }
}
