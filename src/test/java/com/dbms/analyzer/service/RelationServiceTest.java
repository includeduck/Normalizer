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
}
