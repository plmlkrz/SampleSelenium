package com.sampleselenium.drills.d06_java_core;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DRILL 06 — PRACTICE FILE  (no browser — fastest drill in the project)
 *
 * 1. Read SourceD06JavaCoreDrills.java. Close it — no peeking.
 * 2. Delete one @Disabled line, write the body AND any helper method from memory.
 * 3. Run:  mvn test -Dtest=PracticeD06JavaCoreDrills
 * 4. Compare. Repeat. These are the ones to over-practice — they are asked on a
 *    shared notepad with the interviewer watching you type.
 *
 * SECTIONS TO REPRODUCE:
 *   1. Reverse a string: StringBuilder one-liner AND the manual for-loop
 *   2. Reverse preserving whitespace: two-pointer swap that skips spaces  ("ab cd" -> "dc ba")
 *   3. Palindrome: two-pointer, case-insensitive
 *   4. Find duplicates in a list: HashSet.add returns false trick
 *   5. Count char occurrences: HashMap + merge
 *   6. String immutable vs StringBuilder mutable (assert both)
 *   7. LinkedHashMap insertion order vs TreeMap sorted order
 *   8. List vs Set duplicate behavior
 *   9. Comparable (compareTo in class) vs Comparator (external, comparing/reversed)
 *  10. Overloading vs overriding (and WHICH binds at compile vs runtime)
 *  11. try/catch/finally execution order with an unchecked exception
 */
class PracticeD06JavaCoreDrills {


    @Test
    void reverseAString() {
        String input = "Peter";
        StringBuilder manual = new StringBuilder();
        for (int i = input.length() - 1; i >= 0; i--) {
            manual.append(input.charAt(i));
        }
        assertEquals("reteP", manual.toString());
    }


    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void reverseStringPreservingWhitespacePositions() {
        // TODO — write reverseKeepingSpaces() from scratch; test "ab cd" -> "dc ba"
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void palindromeCheck() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void findDuplicatesWithASet() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void countCharacterOccurrencesWithAMap() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void stringIsImmutableStringBuilderIsNot() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void mapOrderingHashVsLinkedVsTree() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void listAllowsDuplicatesSetDoesNot() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void comparableVsComparator() {
        // TODO — include the little TestResult class with compareTo
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void overloadingVsOverriding() {
        // TODO
    }

    @Disabled("TODO: re-type from memory, then delete this line")
    @Test
    void exceptionsAndFinallyOrder() {
        // TODO
    }

    @Test
    void printEveryLetterName() {
        String name = "peter";
        for (int i = 0; i < name.length(); i++);
        System.out.println(name.charAt(0));
    }

}
