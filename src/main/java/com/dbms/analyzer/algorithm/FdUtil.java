package com.dbms.analyzer.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.dbms.analyzer.model.FunctionalDependency;

public class FdUtil {

    private static final Pattern COMPACT_ATTRIBUTE_SET =
            Pattern.compile("[A-Z0-9]+");
    private static final Pattern ATTRIBUTE_NAME =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*|[0-9]+");
    private static final Pattern ATTRIBUTE_TOKEN =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*|[0-9]+");

    /**
     * Parses a functional dependency string (e.g., "AB -> CD").
     *
     * Compact uppercase strings are treated as single-character shorthand,
     * while comma/space separated strings can contain multi-character names.
     *
     * @param fdString The FD string to parse
     * @return A FunctionalDependency object
     * @throws IllegalArgumentException if the string is invalid
     */
    public static FunctionalDependency parseFD(String fdString) {
        String[] parts = splitFd(fdString);

        Set<String> leftSide = stringToAttributes(parts[0]);
        Set<String> rightSide = stringToAttributes(parts[1]);

        return new FunctionalDependency(leftSide, rightSide);
    }

    /**
     * Parses a functional dependency using relation attributes to resolve
     * ambiguous compact input. For example, "AB" is parsed as the single
     * attribute "AB" when the relation contains it; otherwise it remains
     * compatible with the traditional shorthand "{A, B}".
     */
    public static FunctionalDependency parseFD(
            String fdString,
            Set<String> relationAttributes) {
        String[] parts = splitFd(fdString);

        Set<String> leftSide = stringToAttributes(parts[0], relationAttributes);
        Set<String> rightSide = stringToAttributes(parts[1], relationAttributes);

        return new FunctionalDependency(leftSide, rightSide);
    }

    /**
     * Converts a string like "ABC" to a set {"A", "B", "C"}.
     *
     * Multi-character attributes should be separated with commas or spaces,
     * or use lower-case/underscore names such as "student_id".
     */
    public static Set<String> stringToAttributes(String str) {
        return new HashSet<>(attributeTokens(str, null));
    }

    /**
     * Converts user input to attributes using relation attributes to resolve
     * ambiguous compact input.
     */
    public static Set<String> stringToAttributes(
            String str,
            Set<String> relationAttributes) {
        return new HashSet<>(attributeTokens(str, relationAttributes));
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
        for (String attribute : attributeTokens(attributeString, null)) {
            if (!seen.add(attribute)) {
                return true;
            }
        }
        return false;
    }

    private static String[] splitFd(String fdString) {
        if (fdString == null) {
            throw new IllegalArgumentException("FD must be non-empty");
        }

        String[] parts = fdString.trim().split("\\s*->\\s*", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                "Invalid FD format. Use 'A,B -> C'");
        }

        if (parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Both sides of FD must be non-empty");
        }

        return new String[] { parts[0].trim(), parts[1].trim() };
    }

    private static List<String> attributeTokens(
            String value,
            Set<String> relationAttributes) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute set must be non-empty");
        }

        String input = value.trim();

        if (!hasExplicitSeparator(input)
                && relationAttributes != null
                && relationAttributes.contains(input)) {
            return List.of(input);
        }

        if (hasExplicitSeparator(input)) {
            return parseSeparatedAttributes(input);
        }

        if (COMPACT_ATTRIBUTE_SET.matcher(input).matches()) {
            List<String> attributes = new ArrayList<>();
            for (char c : input.toCharArray()) {
                attributes.add(String.valueOf(c));
            }
            return attributes;
        }

        if (ATTRIBUTE_NAME.matcher(input).matches()) {
            return List.of(input);
        }

        List<String> attributes = new ArrayList<>();
        var matcher = ATTRIBUTE_TOKEN.matcher(input);
        while (matcher.find()) {
            attributes.add(matcher.group());
        }

        if (attributes.isEmpty()) {
            throw new IllegalArgumentException("Attribute set must be non-empty");
        }

        return attributes;
    }

    private static boolean hasExplicitSeparator(String input) {
        for (char c : input.toCharArray()) {
            if (c == ',' || Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> parseSeparatedAttributes(String input) {
        List<String> attributes = new ArrayList<>();
        for (String commaPart : input.split(",", -1)) {
            String part = commaPart.trim();
            if (part.isEmpty()) {
                throw new IllegalArgumentException("Empty attribute name");
            }

            for (String token : part.split("\\s+")) {
                if (!token.isEmpty()) {
                    attributes.add(token);
                }
            }
        }

        if (attributes.isEmpty()) {
            throw new IllegalArgumentException("Attribute set must be non-empty");
        }

        return attributes;
    }
}
