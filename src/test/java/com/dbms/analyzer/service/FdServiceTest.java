package com.dbms.analyzer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import com.dbms.analyzer.model.FunctionalDependency;
import java.util.Set;

public class FdServiceTest {

    private FdService fdService;
    private RelationService relationService;

    @BeforeEach
    public void setUp() {
        relationService = new RelationService();
        fdService = new FdService(relationService);

        relationService.createRelation("R(A,B,C)");
    }

    @Test
    public void testAddValidFD() {
        fdService.addFunctionalDependency("A -> B");
        assertEquals(1, fdService.getAllDependencies().size());
    }

    @Test
    public void testAddMultiCharacterFD() {
        relationService.createRelation(
            "Enrollment(student_id,course_id,grade)");

        fdService.addFunctionalDependency("student_id, course_id -> grade");

        assertTrue(fdService.getAllDependencies().contains(
            new FunctionalDependency(
                Set.of("student_id", "course_id"),
                Set.of("grade"))));
    }

    @Test
    public void testAddFDForCompactMultiCharacterAttribute() {
        relationService.createRelation("R(AB,C)");

        fdService.addFunctionalDependency("AB -> C");

        assertTrue(fdService.getAllDependencies().contains(
            new FunctionalDependency(Set.of("AB"), Set.of("C"))));
    }

    @Test
    public void testAddFDKeepsSingleCharacterShorthand() {
        fdService.addFunctionalDependency("AB -> C");

        assertTrue(fdService.getAllDependencies().contains(
            new FunctionalDependency(Set.of("A", "B"), Set.of("C"))));
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
    public void testUnknownAttributeErrorNamesTheAttribute() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> fdService.addFunctionalDependency("A -> D"));
        assertTrue(error.getMessage().contains("'D'"), error.getMessage());
    }

    @Test
    public void testLoadRelationIsAllOrNothing() {
        // BUG-016: a failing FD used to leave a half-built relation behind
        var before = relationService.getCurrentRelation();

        assertThrows(IllegalArgumentException.class, () ->
            fdService.loadRelation("S(X,Y)", java.util.List.of("X -> Y", "X -> Z")));
        assertSame(before, relationService.getCurrentRelation());

        var loaded = fdService.loadRelation("S(X,Y)", java.util.List.of("X -> Y"));
        assertEquals(loaded, relationService.getCurrentRelation());
        assertEquals(Set.of(new FunctionalDependency(Set.of("X"), Set.of("Y"))),
            fdService.getAllDependencies());
    }
}
