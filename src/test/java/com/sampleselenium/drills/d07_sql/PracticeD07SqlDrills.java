package com.sampleselenium.drills.d07_sql;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * DRILL 07 — PRACTICE FILE  (no browser, no external DB — H2 in memory)
 *
 * 1. Read SourceD07SqlDrills.java. Close it — no peeking.
 * 2. Delete one @Disabled line. You'll need to write the @BeforeEach schema/seed
 *    setup from memory too — that IS part of the drill (CREATE TABLE with a
 *    PRIMARY KEY and a FOREIGN KEY is an interview question by itself).
 * 3. Run:  mvn test -Dtest=PracticeD07SqlDrills
 *
 * QUERIES TO REPRODUCE FROM MEMORY (write them on paper first, then type them):
 *   1. Second-highest salary — MAX WHERE salary < (SELECT MAX...)
 *   2. Second-highest salary — SELECT DISTINCT ... ORDER BY DESC LIMIT 1 OFFSET 1
 *      (why does forgetting DISTINCT break it when the top salary is tied?)
 *   3. Duplicates — GROUP BY name HAVING COUNT(*) > 1
 *   4. Delete all but one — DELETE ... WHERE id NOT IN (SELECT MIN(id) ... GROUP BY name),
 *      then re-run query 3 as the assertion. executeUpdate, not executeQuery.
 *   5. INNER vs LEFT JOIN row counts (who disappears and why?)
 *   6. Departments with zero employees — LEFT JOIN + COUNT(e.emp_id), not COUNT(*)
 *   7. ANTI-JOIN — LEFT JOIN + WHERE right_key IS NULL, in BOTH directions
 *      (orphaned employee, then empty department). Why is NOT IN a trap here?
 *   8. WHERE (rows, before grouping) vs HAVING (groups, after aggregation)
 */
class PracticeD07SqlDrills {

    // TODO: @BeforeEach — open jdbc:h2:mem: connection, CREATE TABLE departments + employees
    //       (foreign key!), seed: tied top salary, one duplicate name, one NULL dept, one
    //       empty department. @AfterEach — close the connection.

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void secondHighestSalaryWithMaxSubquery() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void secondHighestSalaryWithLimitOffset() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void findDuplicateRecords() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void deleteDuplicatesKeepingTheLowestId() {
        // TODO — SELECT the doomed rows first, then DELETE, then assert no duplicates remain
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void innerJoinVsLeftJoin() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void leftJoinRevealsDepartmentWithNoEmployees() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void antiJoinFindsRowsWithNoMatchOnTheOtherSide() {
        // TODO — both directions: orphaned employee (dana), then empty department (HR)
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void whereFiltersRowsHavingFiltersGroups() {
        // TODO
    }
}
