package com.dbms.analyzer.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import com.dbms.analyzer.model.FunctionalDependency;

/**
 * Parsing and validation for relation names, attribute names and FDs.
 *
 * <p>One grammar is used everywhere: a name starts with a letter or
 * underscore and contains only letters, digits and underscores.
 *
 * <p>Attribute lists may be separated by commas or spaces ({@code A, B},
 * {@code student_id course_id}). A list written without separators
 * ({@code AB}, {@code ab}, {@code A1B}) is resolved against the relation's
 * attributes: an exact attribute name wins; otherwise the input is split
 * into a sequence of the relation's attribute names, the same way for
 * upper- and lower-case names (matching itself is case-sensitive). If it
 * can be split in more than one way, it is rejected as ambiguous.
 */
public class FdUtil {

    private static final Pattern IDENTIFIER =
            Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Pattern UPPERCASE_LETTERS =
            Pattern.compile("[A-Z]+");

    /**
     * Parses a functional dependency string (e.g., "AB -> CD") without a
     * relation for context: a run of uppercase letters is single-letter
     * shorthand, anything else is one name.
     *
     * @param fdString The FD string to parse
     * @return A FunctionalDependency object
     * @throws IllegalArgumentException if the string is invalid
     */
    public static FunctionalDependency parseFD(String fdString) {
        return parseFD(fdString, null);
    }

    /**
     * Parses a functional dependency, resolving lists written without
     * separators against the relation's attributes (see class comment).
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
     * Converts a string like "ABC" to a set {"A", "B", "C"} without a
     * relation for context.
     */
    public static Set<String> stringToAttributes(String str) {
        return stringToAttributes(str, null);
    }

    /**
     * Converts user input to attributes, resolving lists written without
     * separators against the relation's attributes.
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
        return unknownAttributes(fd, relationAttributes).isEmpty();
    }

    /** Attributes of {@code fd} that are not in the relation, sorted. */
    public static List<String> unknownAttributes(
            FunctionalDependency fd,
            Set<String> relationAttributes) {
        Set<String> fdAttributes = new TreeSet<>(fd.getLeftSide());
        fdAttributes.addAll(fd.getRightSide());
        return unknownAttributes(fdAttributes, relationAttributes);
    }

    /** Members of {@code attributes} that are not in the relation, sorted. */
    public static List<String> unknownAttributes(
            Collection<String> attributes,
            Set<String> relationAttributes) {
        List<String> unknown = new ArrayList<>(new TreeSet<>(attributes));
        unknown.removeAll(relationAttributes);
        return unknown;
    }

    /** A user-facing message for attributes not in the relation. */
    public static String unknownAttributesMessage(
            List<String> unknown,
            Set<String> relationAttributes) {
        String names = "'" + String.join("', '", unknown) + "'";
        return (unknown.size() == 1 ? "Unknown attribute " : "Unknown attributes ")
            + names + ". The relation has: "
            + String.join(", ", new TreeSet<>(relationAttributes))
            + ". Separate attributes with commas, e.g. A,B.";
    }

    /** True if {@code name} is a valid relation or attribute name. */
    public static boolean isValidName(String name) {
        return name != null && IDENTIFIER.matcher(name).matches();
    }

    /**
     * @throws IllegalArgumentException naming {@code kind} and the bad value
     *         if {@code name} is not a valid name
     */
    public static void requireValidName(String name, String kind) {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Invalid " + kind + " name '"
                + name + "': names must start with a letter or underscore and"
                + " contain only letters, digits and underscores");
        }
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

        if (hasExplicitSeparator(input)) {
            return parseSeparatedAttributes(input);
        }

        requireValidName(input, "attribute");

        if (relationAttributes == null) {
            if (UPPERCASE_LETTERS.matcher(input).matches()) {
                List<String> attributes = new ArrayList<>();
                for (char c : input.toCharArray()) {
                    attributes.add(String.valueOf(c));
                }
                return attributes;
            }
            return List.of(input);
        }

        if (relationAttributes.contains(input)) {
            return List.of(input);
        }

        List<List<String>> splits = splitIntoAttributes(input, relationAttributes, 2);
        if (splits.size() == 1) {
            return splits.get(0);
        }
        if (splits.size() > 1) {
            throw new IllegalArgumentException("Ambiguous attribute list '"
                + input + "': it can be read as "
                + String.join(",", splits.get(0)) + " or "
                + String.join(",", splits.get(1))
                + ". Separate attributes with commas.");
        }
        // Not made of the relation's attributes; reported as unknown later
        return List.of(input);
    }

    /**
     * Finds up to {@code limit} ways to write {@code input} as a sequence
     * of the given attribute names. Positions from which no split exists
     * are remembered, so the search is polynomial.
     */
    private static List<List<String>> splitIntoAttributes(
            String input, Set<String> attributes, int limit) {
        List<String> names = new ArrayList<>(attributes);
        names.sort(Comparator.comparingInt(String::length).reversed()
            .thenComparing(Comparator.naturalOrder()));

        List<List<String>> results = new ArrayList<>();
        split(input, 0, names, new ArrayList<>(), results, limit,
            new boolean[input.length() + 1]);
        return results;
    }

    private static boolean split(String input, int position, List<String> names,
                                 List<String> current, List<List<String>> results,
                                 int limit, boolean[] deadEnd) {
        if (position == input.length()) {
            results.add(new ArrayList<>(current));
            return true;
        }
        if (deadEnd[position]) {
            return false;
        }

        boolean found = false;
        for (String name : names) {
            if (results.size() >= limit) {
                return true;
            }
            if (input.startsWith(name, position)) {
                current.add(name);
                found |= split(input, position + name.length(), names,
                    current, results, limit, deadEnd);
                current.remove(current.size() - 1);
            }
        }
        if (!found) {
            deadEnd[position] = true;
        }
        return found;
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
                    requireValidName(token, "attribute");
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
