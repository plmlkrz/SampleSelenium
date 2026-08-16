package com.sampleselenium.drills.d07_sql;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DRILL 07 — SQL VALIDATION  [SOURCE — read, close, reproduce in Practice file]
 *
 * NO BROWSER, NO EXTERNAL DATABASE — H2 runs in memory:
 *   mvn test -Dtest=SourceD07SqlDrills
 *
 * Covers:
 *  - Deloitte R1 Q3: second-largest salary  (BOTH classic solutions below)
 *  - Deloitte R1 Q4 / Infosys SQL Q4-5: joins and their types
 *  - Infosys SQL Q8: find duplicate records, and the "now delete them" follow-up
 *  - Infosys SQL Q6/Q7: WHERE vs HAVING, GROUP BY
 *  - The ANTI-JOIN (LEFT JOIN + WHERE right IS NULL) — orphans and no-match rows
 *
 * One-line definitions to say while these run:
 *  - PRIMARY KEY: uniquely identifies a row; no nulls, no duplicates.
 *  - FOREIGN KEY: a column referencing another table's primary key — enforces referential
 *    integrity (employees.dept_id below references departments).
 *  - INDEX: a lookup structure that speeds reads at the cost of slower writes/extra storage.
 *  - NORMALIZATION: organizing tables to remove redundancy (1NF atomic values, 2NF/3NF
 *    remove partial and transitive dependencies).
 *  - As an SDET my SQL story is VALIDATION: expected-vs-actual reconciliation, duplicate
 *    detection, and proving whether a defect is UI, API, or data.
 */
class SourceD07SqlDrills {

    private Connection connection;

    @BeforeEach
    void createSchemaAndSeedData() throws SQLException {
        // A brand-new in-memory database for every test — perfect isolation, zero cleanup.
        connection = DriverManager.getConnection("jdbc:h2:mem:drills;DB_CLOSE_DELAY=-1");
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            statement.execute("""
                    CREATE TABLE departments (
                        dept_id   INT PRIMARY KEY,
                        dept_name VARCHAR(50) NOT NULL
                    )""");
            statement.execute("""
                    CREATE TABLE employees (
                        emp_id  INT PRIMARY KEY,
                        name    VARCHAR(50) NOT NULL,
                        salary  INT NOT NULL,
                        dept_id INT,
                        CONSTRAINT fk_dept FOREIGN KEY (dept_id) REFERENCES departments(dept_id)
                    )""");
            statement.execute("""
                    INSERT INTO departments VALUES
                        (1, 'QA'), (2, 'Dev'), (3, 'HR')""");
            // Note the traps baked into the data:
            //  - TWO people share the top salary of 95000 (second-highest must be 87000, not 95000)
            //  - 'sam' appears twice (duplicate-record drill)
            //  - dana has a NULL dept_id (inner vs left join drill)
            //  - HR has no employees at all (right/left join from the other side)
            statement.execute("""
                    INSERT INTO employees VALUES
                        (1, 'alice', 95000, 2),
                        (2, 'bob',   95000, 2),
                        (3, 'carol', 87000, 1),
                        (4, 'sam',   72000, 1),
                        (5, 'sam',   72000, 1),
                        (6, 'dana',  65000, NULL)""");
        }
    }

    @AfterEach
    void closeConnection() throws SQLException {
        connection.close();
    }

    /** DELOITTE R1 Q3 — second-largest salary, solution 1: MAX below MAX. */
    @Test
    void secondHighestSalaryWithMaxSubquery() throws SQLException {
        String sql = """
                SELECT MAX(salary) AS second_highest
                FROM employees
                WHERE salary < (SELECT MAX(salary) FROM employees)""";

        assertEquals(87000, singleInt(sql),
                "Two people share 95000 — the second-highest DISTINCT salary is 87000");
    }

    /** Second-largest salary, solution 2: DISTINCT + ORDER BY + LIMIT/OFFSET. Know both. */
    @Test
    void secondHighestSalaryWithLimitOffset() throws SQLException {
        String sql = """
                SELECT DISTINCT salary
                FROM employees
                ORDER BY salary DESC
                LIMIT 1 OFFSET 1""";
        // Forgetting DISTINCT is the classic trap: with a tie at the top,
        // plain ORDER BY DESC LIMIT 1 OFFSET 1 returns 95000 again.

        assertEquals(87000, singleInt(sql));
    }

    /** INFOSYS SQL Q8 — duplicate records: GROUP BY + HAVING COUNT(*) > 1. */
    @Test
    void findDuplicateRecords() throws SQLException {
        String sql = """
                SELECT name, COUNT(*) AS occurrences
                FROM employees
                GROUP BY name
                HAVING COUNT(*) > 1""";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            assertTrue(rs.next(), "There should be a duplicate");
            assertEquals("sam", rs.getString("name"));
            assertEquals(2, rs.getInt("occurrences"));
            assertFalse(rs.next(), "sam should be the only duplicate");
        }
    }

    /**
     * THE FOLLOW-UP TO THE DUPLICATE QUESTION — "now delete all but one of each."
     * Keep the row with the lowest key per group; delete the rest.
     * NOT IN (SELECT MIN(id) ... GROUP BY dupe_column) is the idiom to have ready.
     *
     * Say the QE caveat out loud, because it is the senior half of the answer: on a real
     * table this runs inside a transaction, after a SELECT of the same rows has been eyeballed,
     * and MIN() is only "the right one to keep" if the rows are genuinely identical — if they
     * differ in a column nobody checked, you are picking a winner arbitrarily.
     */
    @Test
    void deleteDuplicatesKeepingTheLowestId() throws SQLException {
        String preview = """
                SELECT emp_id, name
                FROM employees
                WHERE emp_id NOT IN (SELECT MIN(emp_id) FROM employees GROUP BY name)""";
        assertEquals(1, rowCount(preview), "Exactly one row (the second 'sam') should be doomed");

        String delete = """
                DELETE FROM employees
                WHERE emp_id NOT IN (SELECT MIN(emp_id) FROM employees GROUP BY name)""";

        int deleted;
        try (Statement statement = connection.createStatement()) {
            deleted = statement.executeUpdate(delete);   // executeUpdate, not executeQuery
        }

        assertEquals(1, deleted, "sam emp_id=5 removed; emp_id=4 survives");
        assertEquals(5, rowCount("SELECT * FROM employees"));
        assertEquals(0, rowCount("""
                SELECT name FROM employees GROUP BY name HAVING COUNT(*) > 1"""),
                "No duplicates left — always re-run the detection query as the assertion");
    }

    /**
     * JOINS — the four types in one breath:
     *   INNER: only rows with a match on both sides.
     *   LEFT:  every row from the left table; NULLs where the right side has no match.
     *   RIGHT: mirror image of LEFT.
     *   FULL:  everything from both sides, matched where possible.
     * The seeded data makes the difference VISIBLE: dana (NULL dept) drops out of the
     * inner join but survives the left join.
     */
    @Test
    void innerJoinVsLeftJoin() throws SQLException {
        String innerJoin = """
                SELECT e.name, d.dept_name
                FROM employees e
                INNER JOIN departments d ON e.dept_id = d.dept_id""";
        String leftJoin = """
                SELECT e.name, d.dept_name
                FROM employees e
                LEFT JOIN departments d ON e.dept_id = d.dept_id""";

        assertEquals(5, rowCount(innerJoin), "dana has no department -> excluded by INNER JOIN");
        assertEquals(6, rowCount(leftJoin), "LEFT JOIN keeps dana with a NULL dept_name");
    }

    /** LEFT JOIN from the departments side: HR exists with zero employees. */
    @Test
    void leftJoinRevealsDepartmentWithNoEmployees() throws SQLException {
        String sql = """
                SELECT d.dept_name, COUNT(e.emp_id) AS headcount
                FROM departments d
                LEFT JOIN employees e ON e.dept_id = d.dept_id
                GROUP BY d.dept_name
                ORDER BY d.dept_name""";

        List<String> rows = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(rs.getString("dept_name") + "=" + rs.getInt("headcount"));
            }
        }

        assertEquals(List.of("Dev=2", "HR=0", "QA=3"), rows,
                "COUNT(e.emp_id) counts 0 for HR — COUNT(*) would have counted the NULL row as 1");
    }

    /**
     * THE ANTI-JOIN — "find the rows on this side that have NO match on that side."
     * LEFT JOIN + WHERE right_key IS NULL. Learn this shape cold: it is the query behind
     * most data-integrity checks, and it is asked constantly.
     *
     * Note the difference from the COUNT(...)=0 drill above. Both find no-match rows, but
     * this one filters instead of aggregating, so it hands you the actual offending ROWS —
     * which is what you want when you are chasing orphans rather than reporting headcount.
     *
     * The QE translation, and this is the sentence to say in an interview: orphaned records,
     * users with no orders, an order whose customer was deleted, a payment with no matching
     * settlement. Every one of those is this query. The Credit Suisse reconciliation work is
     * exactly this shape — prove nothing fell through the gap between two systems.
     */
    @Test
    void antiJoinFindsRowsWithNoMatchOnTheOtherSide() throws SQLException {
        // Direction 1: employees with no valid department (dana, dept_id IS NULL).
        String orphanedEmployees = """
                SELECT e.name
                FROM employees e
                LEFT JOIN departments d ON e.dept_id = d.dept_id
                WHERE d.dept_id IS NULL""";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(orphanedEmployees)) {
            assertTrue(rs.next(), "dana has a NULL dept_id, so she has no matching department");
            assertEquals("dana", rs.getString("name"));
            assertFalse(rs.next(), "dana should be the only orphan");
        }

        // Direction 2: flip the tables — departments nobody belongs to (HR).
        String emptyDepartments = """
                SELECT d.dept_name
                FROM departments d
                LEFT JOIN employees e ON e.dept_id = d.dept_id
                WHERE e.emp_id IS NULL""";

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(emptyDepartments)) {
            assertTrue(rs.next());
            assertEquals("HR", rs.getString("dept_name"));
            assertFalse(rs.next());
        }
        // NOT EXISTS is the other way to write this and is often the better plan on big
        // tables. NOT IN is the trap: if the subquery returns even one NULL, NOT IN
        // returns no rows at all. Know that gotcha — it gets asked.
    }

    /**
     * INFOSYS SQL Q6 — WHERE vs HAVING:
     * WHERE filters ROWS before grouping; HAVING filters GROUPS after aggregation.
     * You physically cannot put an aggregate like AVG() in a WHERE clause.
     */
    @Test
    void whereFiltersRowsHavingFiltersGroups() throws SQLException {
        String sql = """
                SELECT dept_id, AVG(salary) AS avg_salary
                FROM employees
                WHERE dept_id IS NOT NULL        -- row filter, runs BEFORE grouping
                GROUP BY dept_id
                HAVING AVG(salary) > 80000       -- group filter, runs AFTER aggregation
                """;

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("dept_id"), "Only Dev (avg 95000) clears the HAVING bar");
            assertFalse(rs.next(), "QA's average (77000) is filtered out by HAVING");
        }
    }

    // ---------- helpers ----------

    private int singleInt(String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            assertTrue(rs.next(), "Query should return one row");
            return rs.getInt(1);
        }
    }

    private int rowCount(String sql) throws SQLException {
        int count = 0;
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                count++;
            }
        }
        return count;
    }
}
