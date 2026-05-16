package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.FunctionalDependency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        Set<String> attrs = FdUtil.stringToAttributes("A1B");
        assertEquals(Set.of("A", "1", "B"), attrs);
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
    public void testHasDuplicatesTrue() {
        assertTrue(FdUtil.hasDuplicates("ABA"));
    }

    @Test
    public void testHasDuplicatesFalse() {
        assertFalse(FdUtil.hasDuplicates("ABC"));
    }

    @Test
    public void testHasDuplicatesWithMultiCharacterAttributes() {
        assertTrue(FdUtil.hasDuplicates("student_id,course_id,student_id"));
    }
}
