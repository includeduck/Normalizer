package com.dbms.analyzer.javafx;

import java.util.List;

/** The built-in examples offered by "Load Example". */
public final class SampleRelations {

    public record Sample(
            String name,
            String schema,
            List<String> dependencies,
            String closureSeed) {
    }

    public static final List<Sample> ALL = List.of(
        new Sample(
            "Student Course Grades",
            "Enrollment(student_id,course_id,instructor,advisor,grade)",
            List.of(
                "student_id,course_id -> grade",
                "course_id -> instructor",
                "student_id -> advisor"),
            "student_id,course_id"),
        new Sample(
            "Employee Departments",
            "Employee(employee_id,employee_name,department_id,department_name,manager_id)",
            List.of(
                "employee_id -> employee_name,department_id",
                "department_id -> department_name,manager_id"),
            "employee_id"),
        new Sample(
            "Library Loans",
            "Loan(member_id,isbn,loan_date,member_name,book_title,due_date)",
            List.of(
                "member_id -> member_name",
                "isbn -> book_title",
                "member_id,isbn,loan_date -> due_date"),
            "member_id,isbn,loan_date"));

    private SampleRelations() {
    }

    /** The sample with this name, or the first sample if none matches. */
    public static Sample byName(String name) {
        return ALL.stream()
            .filter(sample -> sample.name().equals(name))
            .findFirst()
            .orElse(ALL.get(0));
    }
}
