package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.FunctionalDependency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

public class FdUtilTest {

    @Test
    public void testValidParse() {
        FunctionalDependency fd = FdUtil.parseFD("AB -> CD");
        assertEquals(Set.of("A", "B"), fd.getLeftSide());
        assertEquals(Set.of("C", "D"), fd.getRightSide());
    }

    @Test
    public void testParseWithSpaces() {
        FunctionalDependency fd = FdUtil.parseFD("  A  ->  B  ");
        assertEquals(Set.of("A"), fd.getLeftSide());
        assertEquals(Set.of("B"), fd.getRightSide());
    }

    @Test
    public void testParseSingleAttributes() {
        FunctionalDependency fd = FdUtil.parseFD("A -> B");
        assertEquals(Set.of("A"), fd.getLeftSide());
        assertEquals(Set.of("B"), fd.getRightSide());
    }

    @Test
    public void testParseCommaSeparatedMultiCharacterAttributes() {
        FunctionalDependency fd =
                FdUtil.parseFD("student_id, course_id -> grade");

        assertEquals(Set.of("student_id", "course_id"), fd.getLeftSide());
        assertEquals(Set.of("grade"), fd.getRightSide());
    }

    @Test
    public void testParseContextPreservesCompactMultiCharacterAttribute() {
        FunctionalDependency fd =
                FdUtil.parseFD("AB -> C", Set.of("AB", "C"));

        assertEquals(Set.of("AB"), fd.getLeftSide());
        assertEquals(Set.of("C"), fd.getRightSide());
    }

    @Test
    public void testParseContextKeepsSingleCharacterShorthand() {
        FunctionalDependency fd =
                FdUtil.parseFD("AB -> C", Set.of("A", "B", "C"));

        assertEquals(Set.of("A", "B"), fd.getLeftSide());
        assertEquals(Set.of("C"), fd.getRightSide());
    }

    @Test
    public void testParseInvalidNoArrow() {
        assertThrows(IllegalArgumentException.class,
                () -> FdUtil.parseFD("AB CD"));
    }

    @Test
    public void testParseInvalidEmptyLeft() {
        assertThrows(IllegalArgumentException.class,
                () -> FdUtil.parseFD(" -> B"));
    }

    @Test
    public void testParseInvalidEmptyRight() {
        assertThrows(IllegalArgumentException.class,
                () -> FdUtil.parseFD("A -> "));
    }

    @Test
    public void testParseInvalidMultipleArrows() {
        assertThrows(IllegalArgumentException.class,
                () -> FdUtil.parseFD("A -> B -> C"));
    }

    @Test
    public void testStringToAttributes() {
        Set<String> attrs = FdUtil.stringToAttributes("ABC");
        assertEquals(Set.of("A", "B", "C"), attrs);
    }

    @Test
    public void testStringToAttributesWithDigits() {
        // Without a relation, only a run of uppercase letters is shorthand;
        // "1" is not a valid attribute name, so "A1B" is one name
        assertEquals(Set.of("A1B"), FdUtil.stringToAttributes("A1B"));
        assertEquals(Set.of("A1", "B"),
                FdUtil.stringToAttributes("A1B", Set.of("A1", "B", "C")));
    }

    @Test
    public void testStringToAttributesIgnoresSpecialChars() {
        Set<String> attrs = FdUtil.stringToAttributes("A,B C");
        assertEquals(Set.of("A", "B", "C"), attrs);
    }

    @Test
    public void testStringToAttributesSupportsSingleMultiCharacterName() {
        Set<String> attrs = FdUtil.stringToAttributes("studentId");
        assertEquals(Set.of("studentId"), attrs);
    }

    @Test
    public void testValidateFdAttributesValid() {
        FunctionalDependency fd = new FunctionalDependency(
                Set.of("A"), Set.of("B"));
        assertTrue(FdUtil.validateFdAttributes(fd, Set.of("A", "B", "C")));
    }

    @Test
    public void testValidateFdAttributesInvalid() {
        FunctionalDependency fd = new FunctionalDependency(
                Set.of("A"), Set.of("D"));
        assertFalse(FdUtil.validateFdAttributes(fd, Set.of("A", "B", "C")));
    }

    @Test
    public void testLowercaseCompactInputIsSplitLikeUppercase() {
        // BUG-012: "ab -> c" used to be read as the single attribute "ab"
        FunctionalDependency fd = FdUtil.parseFD("ab -> c", Set.of("a", "b", "c"));
        assertEquals(Set.of("a", "b"), fd.getLeftSide());
        assertEquals(Set.of("c"), fd.getRightSide());
    }

    @Test
    public void testCompactInputSplitsIntoMultiCharacterAttributes() {
        FunctionalDependency fd = FdUtil.parseFD(
                "A1B2 -> C", Set.of("A1", "B2", "C"));
        assertEquals(Set.of("A1", "B2"), fd.getLeftSide());
    }

    @Test
    public void testAmbiguousCompactInputIsRejected() {
        // ABC could be A,B,C or AB,C
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> FdUtil.parseFD("ABC -> D", Set.of("A", "B", "AB", "C", "D")));
        assertTrue(error.getMessage().contains("Ambiguous"), error.getMessage());
        assertTrue(error.getMessage().contains("commas"), error.getMessage());

        // Separators remove the ambiguity
        assertEquals(Set.of("AB", "C"), FdUtil.parseFD(
                "AB, C -> D", Set.of("A", "B", "AB", "C", "D")).getLeftSide());
    }

    @Test
    public void testExactAttributeNameWinsOverSplitting() {
        assertEquals(Set.of("AB"),
                FdUtil.stringToAttributes("AB", Set.of("A", "B", "AB")));
    }

    @Test
    public void testInvalidNamesAreRejectedEverywhere() {
        // BUG-011: the FD parser and the schema parser now share one grammar
        assertThrows(IllegalArgumentException.class,
                () -> FdUtil.stringToAttributes("A$", Set.of("A")));
        assertThrows(IllegalArgumentException.class,
                () -> FdUtil.stringToAttributes("A, B$", Set.of("A", "B")));
        assertThrows(IllegalArgumentException.class,
                () -> FdUtil.stringToAttributes("1st", null));
        assertTrue(FdUtil.isValidName("student_id2"));
        assertFalse(FdUtil.isValidName("C->D"));
    }

    @Test
    public void testSplittingWithManyDeadEndsIsFast() {
        String input = "A".repeat(60) + "B";
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(2), () ->
                FdUtil.stringToAttributes(input, Set.of("A", "AA", "AAA", "C")));
    }

    @Test
    public void testUnknownAttributeMessageNamesTheAttribute() {
        String message = FdUtil.unknownAttributesMessage(
                List.of("ab"), Set.of("a", "b"));
        assertTrue(message.contains("'ab'"), message);
        assertTrue(message.contains("a, b"), message);
    }
}
