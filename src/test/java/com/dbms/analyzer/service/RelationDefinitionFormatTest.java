package com.dbms.analyzer.service;

import com.dbms.analyzer.algorithm.FdUtil;
import com.dbms.analyzer.model.Relation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RelationDefinitionFormatTest {

    private RelationService relationService;
    private FdService fdService;

    @BeforeEach
    public void setUp() {
        relationService = new RelationService();
        fdService = new FdService(relationService);
    }

    @Test
    public void testParsesTheDocumentedExample() {
        String text = """
            # Lines starting with # are comments
            relation = Enrollment(student_id, course_id, instructor, grade)

            FD = {
                student_id, course_id -> grade
                course_id -> instructor     # trailing comment
            }

            closure = student_id, course_id
            """;

        RelationDefinitionFormat.Definition definition = RelationDefinitionFormat.parse(text);

        assertEquals(new RelationDefinitionFormat.Line(2,
            "Enrollment(student_id, course_id, instructor, grade)"), definition.relation());
        assertEquals(List.of(
            new RelationDefinitionFormat.Line(5, "student_id, course_id -> grade"),
            new RelationDefinitionFormat.Line(6, "course_id -> instructor")),
            definition.dependencies());
        assertEquals("student_id, course_id", definition.closure().text());

        Relation relation = fdService.loadDefinition(definition);
        assertEquals(Set.of("student_id", "course_id", "instructor", "grade"),
            relation.getAttributes());
        assertEquals(Set.of(
            FdUtil.parseFD("student_id, course_id -> grade"),
            FdUtil.parseFD("course_id -> instructor")),
            relation.getFunctionalDependencies());
        assertSame(relation, relationService.getCurrentRelation());
    }

    @Test
    public void testOneLineBlockAndSemicolonsAndCaseInsensitiveKeys() {
        RelationDefinitionFormat.Definition definition = RelationDefinitionFormat.parse(
            "Relation = R(A,B,C)\nFDs = { A -> B; B -> C }\r\n");

        assertEquals(List.of(
            new RelationDefinitionFormat.Line(2, "A -> B"),
            new RelationDefinitionFormat.Line(2, "B -> C")), definition.dependencies());
        assertNull(definition.closure());
    }

    @Test
    public void testBlockMayOpenAndCloseOnLinesWithFds() {
        RelationDefinitionFormat.Definition definition = RelationDefinitionFormat.parse("""
            relation = R(A,B,C)
            FD = { A -> B
                B -> C }
            """);

        assertEquals(List.of(
            new RelationDefinitionFormat.Line(2, "A -> B"),
            new RelationDefinitionFormat.Line(3, "B -> C")), definition.dependencies());
    }

    @Test
    public void testRelationWithoutFdsIsAllowed() {
        Relation relation = fdService.loadDefinition(
            RelationDefinitionFormat.parse("relation = R(A,B)"));
        assertTrue(relation.getFunctionalDependencies().isEmpty());
    }

    @Test
    public void testStructuralErrorsNameTheLine() {
        assertMessage("Line 2: the FD block opened here is never closed",
            "relation = R(A,B)\nFD = {\n A -> B\n");
        assertMessage("Line 2: expected '{' after 'FD ='", "relation = R(A,B)\nFD = A -> B");
        assertMessage("Line 1: unknown setting 'relations'", "relations = R(A)");
        assertMessage("Line 2: the relation is already defined on line 1",
            "relation = R(A)\nrelation = S(B)");
        assertMessage("Line 1: expected 'relation = ...'", "A -> B");
        assertMessage("Line 3: expected an FD like A -> B but found 'relation = S(A)'",
            "relation = R(A,B)\nFD = {\nrelation = S(A)\n}");
        assertMessage("Line 2: unexpected text after '}'", "relation = R(A)\nFD = { } extra");
        assertMessage("No relation defined", "FD = { A -> B }");
        assertMessage("The definition is empty", "   ");
        assertMessage("No relation defined", "# only a comment\n");
    }

    @Test
    public void testValidationErrorsNameTheLineAndKeepTheCurrentRelation() {
        relationService.createRelation("Before(X,Y)");
        Relation before = relationService.getCurrentRelation();

        assertLoadMessage("Line 1: Invalid attribute name 'B$'", "relation = R(A,B$)");
        assertLoadMessage("Line 4: Unknown attribute 'D'",
            "relation = R(A,B,C)\nFD = {\n  A -> B\n  A -> D\n}");
        assertLoadMessage("Line 3: Unknown attribute 'Z'",
            "relation = R(A,B)\nFD = { A -> B }\nclosure = Z");

        assertSame(before, relationService.getCurrentRelation());
    }

    @Test
    public void testFormatRoundTrips() {
        Relation original = fdService.loadRelation("R(A,B,C,D)",
            List.of("A,B -> C", "C -> D", "D -> A"));

        String text = RelationDefinitionFormat.format(original, "A, B");

        assertEquals("""
            relation = R(A, B, C, D)

            FD = {
                C -> D
                D -> A
                A, B -> C
            }

            closure = A, B
            """, text);

        RelationDefinitionFormat.Definition reparsed = RelationDefinitionFormat.parse(text);
        assertEquals(original, fdService.loadDefinition(reparsed));
        assertEquals("A, B", reparsed.closure().text());
    }

    @Test
    public void testFormatWithoutClosureOrFds() {
        assertEquals("relation = R(A)\n\nFD = {\n}\n",
            RelationDefinitionFormat.format(new Relation("R", Set.of("A")), "  "));
    }

    @Test
    public void testEveryExampleFileLoads() throws java.io.IOException {
        // Guards the files shipped in examples/ against format or rule changes
        java.util.List<java.nio.file.Path> files;
        try (var stream = java.nio.file.Files.list(java.nio.file.Path.of("examples"))) {
            files = stream.filter(path -> path.toString().endsWith(".txt")).sorted().toList();
        }
        assertFalse(files.isEmpty(), "no example files found");

        for (java.nio.file.Path file : files) {
            String text = java.nio.file.Files.readString(file);
            Relation relation = fdService.loadDefinition(RelationDefinitionFormat.parse(text));
            assertFalse(relation.getFunctionalDependencies().isEmpty(), file.toString());
        }
    }

    private static void assertMessage(String expectedStart, String text) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> RelationDefinitionFormat.parse(text));
        assertTrue(error.getMessage().startsWith(expectedStart), error.getMessage());
    }

    private void assertLoadMessage(String expectedStart, String text) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> fdService.loadDefinition(RelationDefinitionFormat.parse(text)));
        assertTrue(error.getMessage().startsWith(expectedStart), error.getMessage());
    }
}
