package com.dbms.analyzer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

public class RelationServiceTest {

    private RelationService relationService;

    @BeforeEach
    public void setUp() {
        relationService = new RelationService();
    }

    @Test
    public void testRelationAboveAttributeLimitIsRejected() {
        StringBuilder schema = new StringBuilder("R(");
        for (int i = 0; i <= com.dbms.analyzer.algorithm.AnalysisLimits.MAX_ATTRIBUTES; i++) {
            schema.append(i == 0 ? "" : ",").append("a").append(i);
        }
        schema.append(")");

        assertThrows(IllegalArgumentException.class,
            () -> relationService.createRelation(schema.toString()));
        assertFalse(relationService.hasRelation());
    }

    @Test
    public void testCreateValidRelation() {
        relationService.createRelation("R(A,B,C)");
        assertTrue(relationService.hasRelation());
        assertEquals(3, relationService.getAttributes().size());
    }

    @Test
    public void testCreateRelationWithMultiCharacterAttributes() {
        relationService.createRelation(
            "Enrollment(student_id,course_id,grade)");

        assertEquals(
            Set.of("student_id", "course_id", "grade"),
            relationService.getAttributes());
    }

    @Test
    public void testCreateRelationInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () ->
            relationService.createRelation("R[A,B,C]"));
    }

    @Test
    public void testCreateRelationDuplicateAttributes() {
        assertThrows(IllegalArgumentException.class, () ->
            relationService.createRelation("R(A,B,A)"));
    }

    @Test
    public void testCreateRelationEmptyName() {
        assertThrows(IllegalArgumentException.class, () ->
            relationService.createRelation("(A,B,C)"));
    }

    @Test
    public void testCreateRelationRejectsWhitespaceInAttributeName() {
        assertThrows(IllegalArgumentException.class, () ->
            relationService.createRelation("R(student id,course_id)"));
    }

    @Test
    public void testNoRelationInitially() {
        assertFalse(relationService.hasRelation());
    }

    @Test
    public void testClearRelation() {
        relationService.createRelation("R(A,B,C)");
        relationService.clear();
        assertFalse(relationService.hasRelation());
    }

    @Test
    public void testAttributeNamesFollowTheFdGrammar() {
        // BUG-011: "(B" and "C->D" were accepted but could never be used in an FD
        assertThrows(IllegalArgumentException.class,
            () -> relationService.createRelation("R(A,(B,C->D)"));
        assertThrows(IllegalArgumentException.class,
            () -> relationService.createRelation("R(A,B$)"));
        assertThrows(IllegalArgumentException.class,
            () -> relationService.createRelation("My-Relation(A,B)"));

        relationService.createRelation("Order_2(id_1,_note)");
        assertEquals(Set.of("id_1", "_note"), relationService.getAttributes());
    }
}
