package com.sampleselenium.drills.d06_java_core;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DRILL 06 — JAVA CORE  [SOURCE — read, close, reproduce in Practice file]
 *
 * NO BROWSER — runs in about a second:
 *   mvn test -Dtest=SourceD06JavaCoreDrills
 *
 * Covers the notepad-coding questions from the banks:
 *  - Deloitte R1 Q2: reverse a string PRESERVING WHITESPACE positions
 *  - Infosys Java Q15/16/17: reverse, palindrome, find duplicates
 *  - Deloitte R1 Q5: LinkedHashMap; R2 Q11: Comparable vs Comparator
 *  - Infosys Java Q7/8/12/19/20: Array vs ArrayList, List vs Set, String vs StringBuilder,
 *    HashMap ordering
 *
 * Definitions to say while these run (they're asserted below where possible):
 *  - String is IMMUTABLE — every concat makes a new object. StringBuilder mutates in place
 *    (fast, not thread-safe); StringBuffer is the synchronized/thread-safe legacy twin.
 *  - checked exceptions = compiler forces handling (IOException); unchecked = RuntimeException
 *    family (NullPointerException, ArithmeticException). An Error (OutOfMemoryError) is a JVM
 *    problem you don't catch.
 *  - final = keyword (constant / no override / no reassign); finally = block that always runs;
 *    finalize = deprecated GC hook. Three unrelated things sharing a syllable.
 *  - overloading = same name, different PARAMETERS, resolved at COMPILE time (static binding);
 *    overriding = subclass replaces a parent method, resolved at RUNTIME (dynamic binding).
 */
class SourceD06JavaCoreDrills {

    // ---------- STRING DRILLS ----------

    /** Plain reversal, two ways — know both, the manual loop is what they ask you to type. */
    @Test
    void reverseAString() {
        String input = "Selenium";

        // Way 1: the library one-liner
        String reversedBuilder = new StringBuilder(input).reverse().toString();

        // Way 2: the manual loop they actually want to see on a notepad
        StringBuilder manual = new StringBuilder();
        for (int i = input.length() - 1; i >= 0; i--) {
            manual.append(input.charAt(i));
        }

        assertEquals("muineleS", reversedBuilder);
        assertEquals("muineleS", manual.toString());
    }

    /**
     * DELOITTE R1 Q2 — reverse the characters but every space stays where it was.
     * Two-pointer swap that SKIPS spaces: "ab cd" -> "dc ba", "a b" -> "b a".
     */
    @Test
    void reverseStringPreservingWhitespacePositions() {
        assertEquals("dc ba", reverseKeepingSpaces("ab cd"));
        // All chars reverse, but the space STAYS at index 5 — this is NOT word-swapping:
        assertEquals("dlrow olleh", reverseKeepingSpaces("hello world"));
        assertEquals("cb a", reverseKeepingSpaces("ab c"));
    }

    private static String reverseKeepingSpaces(String input) {
        char[] chars = input.toCharArray();
        int left = 0;
        int right = chars.length - 1;
        while (left < right) {
            if (chars[left] == ' ') {
                left++;                       // space on the left: freeze it, move on
            } else if (chars[right] == ' ') {
                right--;                      // space on the right: freeze it, move on
            } else {
                char tmp = chars[left];       // both sides are letters: swap
                chars[left] = chars[right];
                chars[right] = tmp;
                left++;
                right--;
            }
        }
        return new String(chars);
    }

    /** Palindrome: two-pointer, case-insensitive. */
    @Test
    void palindromeCheck() {
        assertTrue(isPalindrome("Level"));
        assertTrue(isPalindrome("racecar"));
        assertFalse(isPalindrome("Selenium"));
    }

    private static boolean isPalindrome(String input) {
        String s = input.toLowerCase();
        int left = 0;
        int right = s.length() - 1;
        while (left < right) {
            if (s.charAt(left) != s.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    /** Duplicates in a collection: HashSet.add returns false for anything already present. */
    @Test
    void findDuplicatesWithASet() {
        List<String> testers = List.of("amy", "raj", "amy", "chen", "raj", "amy");

        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        for (String name : testers) {
            if (!seen.add(name)) {        // add() returning false == duplicate
                duplicates.add(name);
            }
        }

        assertEquals(Set.of("amy", "raj"), duplicates);
    }

    /** Character counts — the "find duplicate characters in a string" variant. */
    @Test
    void countCharacterOccurrencesWithAMap() {
        String word = "mississippi";

        Map<Character, Integer> counts = new HashMap<>();
        for (char c : word.toCharArray()) {
            counts.merge(c, 1, Integer::sum);   // merge = "insert 1 or add 1 to existing"
        }

        assertEquals(4, counts.get('s'));
        assertEquals(4, counts.get('i'));
        assertEquals(2, counts.get('p'));
        assertEquals(1, counts.get('m'));
    }

    /** String immutability vs StringBuilder — the assertion shows WHY the builder exists. */
    @Test
    void stringIsImmutableStringBuilderIsNot() {
        String original = "test";
        String upper = original.toUpperCase();   // returns a NEW string...
        assertEquals("test", original);          // ...the original never changed

        StringBuilder builder = new StringBuilder("test");
        builder.append("ng");                    // mutates in place, no new object per append
        assertEquals("testng", builder.toString());
    }

    // ---------- COLLECTIONS DRILLS ----------

    /**
     * DELOITTE R1 Q5 — LinkedHashMap: a HashMap that REMEMBERS INSERTION ORDER.
     * HashMap = no order guarantee; TreeMap = sorted by key; LinkedHashMap = insertion order.
     * QA use case: keeping form-field validation results in the order the fields appear.
     */
    @Test
    void mapOrderingHashVsLinkedVsTree() {
        Map<String, Integer> linked = new LinkedHashMap<>();
        linked.put("banana", 1);
        linked.put("apple", 2);
        linked.put("cherry", 3);
        assertEquals(List.of("banana", "apple", "cherry"), new ArrayList<>(linked.keySet()),
                "LinkedHashMap iterates in insertion order");

        Map<String, Integer> sorted = new TreeMap<>(linked);
        assertEquals(List.of("apple", "banana", "cherry"), new ArrayList<>(sorted.keySet()),
                "TreeMap iterates in sorted-key order");
        // HashMap ordering is not asserted because it guarantees NOTHING — that's the answer.
        // Related: HashMap allows one null key and is not synchronized; Hashtable is the
        // legacy synchronized version and allows no nulls.
    }

    /** List vs Set in one breath: List = ordered, duplicates OK; Set = unique elements. */
    @Test
    void listAllowsDuplicatesSetDoesNot() {
        List<String> list = new ArrayList<>(List.of("a", "b", "a"));
        Set<String> set = new HashSet<>(list);

        assertEquals(3, list.size());
        assertEquals(2, set.size());
        // Array vs ArrayList: array is FIXED SIZE and can hold primitives; ArrayList grows
        // dynamically, objects only, and gives you the Collections API.
    }

    /**
     * DELOITTE R2 Q11 — Comparable vs Comparator.
     * Comparable = the class's ONE natural order, compareTo inside the class.
     * Comparator = as many EXTERNAL orderings as you want, without touching the class.
     */
    @Test
    void comparableVsComparator() {
        List<TestResult> results = new ArrayList<>(List.of(
                new TestResult("checkout", 4200),
                new TestResult("login", 900),
                new TestResult("search", 1800)));

        // Comparable: natural order (duration, fastest first) — defined IN TestResult
        results.sort(null);   // null comparator = "use natural ordering"
        assertEquals("login", results.get(0).name);

        // Comparator: a different order, defined OUTSIDE the class, on the spot
        results.sort(Comparator.comparing((TestResult r) -> r.name));
        assertEquals("checkout", results.get(0).name);

        results.sort(Comparator.comparingInt((TestResult r) -> r.durationMs).reversed());
        assertEquals("checkout", results.get(0).name);
    }

    private static final class TestResult implements Comparable<TestResult> {
        final String name;
        final int durationMs;

        TestResult(String name, int durationMs) {
            this.name = name;
            this.durationMs = durationMs;
        }

        @Override
        public int compareTo(TestResult other) {
            return Integer.compare(this.durationMs, other.durationMs);
        }
    }

    // ---------- OOP DRILLS ----------

    /** Overloading (compile-time / static binding) vs overriding (runtime / dynamic binding). */
    @Test
    void overloadingVsOverriding() {
        // Overloading: same method name, different parameter lists — resolved at COMPILE time
        assertEquals("step: click login", describe("click login"));
        assertEquals("step 3: click login", describe("click login", 3));

        // Overriding: subclass replaces the parent implementation — resolved at RUNTIME.
        // The variable's declared type is Parent, but Child's method runs: dynamic binding.
        Reporter reporter = new HtmlReporter();
        assertEquals("writing HTML report", reporter.report());
    }

    private static String describe(String step) {
        return "step: " + step;
    }

    private static String describe(String step, int number) {
        return "step " + number + ": " + step;
    }

    private static class Reporter {
        String report() {
            return "writing plain report";
        }
    }

    private static class HtmlReporter extends Reporter {
        @Override
        String report() {
            return "writing HTML report";
        }
    }

    // ---------- EXCEPTION DRILLS ----------

    /** Unchecked exceptions surface at runtime; finally ALWAYS runs — even after a catch. */
    @Test
    void exceptionsAndFinallyOrder() {
        List<String> executionLog = new ArrayList<>();
        try {
            executionLog.add("try");
            int impossible = 10 / 0;              // ArithmeticException — unchecked
            executionLog.add("never reached");
        } catch (ArithmeticException e) {
            executionLog.add("catch");
        } finally {
            executionLog.add("finally");
        }

        assertEquals(List.of("try", "catch", "finally"), executionLog);
        // Exception vs Error: exceptions are recoverable app-level problems; Errors
        // (OutOfMemoryError, StackOverflowError) are JVM-level — you do not catch those.
    }
}
