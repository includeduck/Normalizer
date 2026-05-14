package com.dbms.analyzer.algorithm;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import com.dbms.analyzer.model.FunctionalDependency;

public class FdUtil {

    private static final Pattern FD_PATTERN = Pattern.compile(
        "^([A-Za-z0-9]+)\\s*->\\s*([A-Za-z0-9]+)$");

    /**
     * Parses a functional dependency string (e.g., "AB -> CD")
     * @param fdString The FD string to parse
     * @return A FunctionalDependency object
     * @throws IllegalArgumentException if the string is invalid
     */
    public static FunctionalDependency parseFD(String fdString) {
        String[] parts = fdString.split("->");
        
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                "Invalid FD format. Use 'AB -> CD'");
        }

        String leftStr = parts[0].trim();
        String rightStr = parts[1].trim();

        if (leftStr.isEmpty() || rightStr.isEmpty()) {
            throw new IllegalArgumentException(
                "Both sides of FD must be non-empty");
        }

        Set<String> leftSide = stringToAttributes(leftStr);
        Set<String> rightSide = stringToAttributes(rightStr);

        return new FunctionalDependency(leftSide, rightSide);
    }

    /**
     * Converts a string like "ABC" to a set {"A", "B", "C"}
     */
    public static Set<String> stringToAttributes(String str) {
        Set<String> attributes = new HashSet<>();
        for (char c : str.toCharArray()) {
            if (Character.isLetter(c) || Character.isDigit(c)) {
                attributes.add(String.valueOf(c));
            }
        }
        return attributes;
    }

    /**
     * Validates if all attributes in FD exist in the relation
     */
    public static boolean validateFdAttributes(
            FunctionalDependency fd,
            Set<String> relationAttributes) {
        
        Set<String> fdAttributes = new HashSet<>();
        fdAttributes.addAll(fd.getLeftSide());
        fdAttributes.addAll(fd.getRightSide());

        return relationAttributes.containsAll(fdAttributes);
    }

    /**
     * Checks for duplicate attributes within a set
     */
    public static boolean hasDuplicates(String attributeString) {
        Set<String> seen = new HashSet<>();
        for (char c : attributeString.toCharArray()) {
            if (!seen.add(String.valueOf(c))) {
                return true;
            }
        }
        return false;
    }
}
