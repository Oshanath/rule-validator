package org.wso2.rule.validator.functions.core;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.wso2.rule.validator.InvalidRulesetException;
import org.wso2.rule.validator.document.LintTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CasingFunctionTest {
    @Test
    void givenNonStringInputShouldReturnNoErrorMessage() {
        CasingFunction casingFunction = new CasingFunction(Map.of("type", "camel"));

        try {
            boolean result1 = casingFunction.execute(new LintTarget(new ArrayList<>(), false));
            assertTrue(result1, "Boolean input should return true (no error).");

            boolean result2 = casingFunction.execute(new LintTarget(new ArrayList<>(), 1));
            assertTrue(result2, "Integer input should return true (no error).");
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception for non-string inputs.");
        }
    }

    @Test
    void givenEmptyStringInputShouldReturnNoErrorMessage() {
        CasingFunction casingFunction = new CasingFunction(Map.of("type", "camel"));

        try {
            boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), ""));
            assertTrue(result, "Empty string input should return true (no error).");

        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception for empty string input.");
        }
    }

    @Nested
    class CasingTypeFlat {

        private final List<String> invalidInputs = List.of("foo_test", "Foo", "123", "1d", "foo-bar");
        private final List<String> validInputs = List.of("foo", "foobar");
        private final List<String> validWithDigits = List.of("foo9bar", "foo24baz", "foo1");

        @Test
        void shouldRecognizeInvalidInputForFlatCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "sdf"));

            for (String input : invalidInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertFalse(result, "Expected invalid input '" + input + "' to fail flat casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        void givenValidFlatCaseInputShouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "flat"));

            for (String input : validInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result, "Expected valid input '" + input + "' to pass flat casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result,
                            "Expected valid input '" + input + "' to pass flat casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        class WhenDigitsAreDisallowed {

            @Test
            void shouldRecognizeInvalidInputForFlatCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "flat", "disallowDigits", true));

                for (String input : invalidInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected invalid input '" + input + "' to fail flat casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            void givenValidFlatCaseInputWithoutDigitsShouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "flat", "disallowDigits", true));

                for (String input : validInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertTrue(result,
                                "Expected valid input '" + input + "' to pass flat casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    @Nested
    class CasingTypeCamel {

        private final List<String> invalidInputs = List.of("foo_test", "Foo", "1fooBarBaz", "123", "foo-bar");
        private final List<String> validInputs = List.of("foo", "fooBar", "fooBarBaz", "coordinateX");
        private final List<String> validWithDigits = List.of("foo1", "foo24Bar", "fooBar0Baz323");

        @Test
        void shouldRecognizeInvalidInputForCamelCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "camel"));

            for (String input : invalidInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertFalse(result, "Expected invalid input '" + input + "' to fail camel casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        void givenValidCamelCaseInputShouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "camel"));

            for (String input : validInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result, "Expected valid input '" + input + "' to pass camel casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result,
                            "Expected valid input '" + input + "' to pass camel casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        class WhenDigitsAreDisallowed {

            @Test
            void shouldRecognizeInvalidInputForCamelCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "camel", "disallowDigits", true));

                for (String input : invalidInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected invalid input '" + input + "' to fail camel casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            void givenValidCamelCaseInputWithoutDigitsShouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "camel", "disallowDigits", true));

                for (String input : validInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertTrue(result,
                                "Expected valid input '" + input + "' to pass camel casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    @Nested
    class CasingTypePascal {

        private final List<String> invalidInputs = List.of("foo_test", "123", "1fooBarBaz", "fooBarBaz1", "fooBar",
                "foo1", "foo-bar");
        private final List<String> validInputs = List.of("Foo", "FooBar", "FooBarBaz", "CoordinateZ");
        private final List<String> validWithDigits = List.of("Foo1", "FooBarBaz1");

        @Test
        void shouldRecognizeInvalidInputForPascalCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "pascal"));

            for (String input : invalidInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertFalse(result, "Expected invalid input '" + input + "' to fail Pascal casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        void givenValidPascalCaseInputShouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "pascal"));

            for (String input : validInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result, "Expected valid input '" + input + "' to pass Pascal casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result,
                            "Expected valid input '" + input + "' to pass Pascal casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        class WhenDigitsAreDisallowed {

            @Test
            void shouldRecognizeInvalidInputForPascalCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "pascal", "disallowDigits", true));

                for (String input : invalidInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected invalid input '" + input + "' to fail Pascal casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            void givenValidPascalCaseInputWithoutDigits_shouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "pascal", "disallowDigits", true));

                for (String input : validInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertTrue(result,
                                "Expected valid input '" + input
                                        + "' to pass Pascal casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    @Nested
    class CasingTypeKebab {

        private final List<String> invalidInputs = List.of("foo_test", "Foo1", "123", "fooBarBaz1", "fooBar", "foO",
                "foo-baR", "1foo-bar", "foo--bar", "foo-", "-foo");

        private final List<String> validInputs = List.of("foo", "foo-bar", "foo-bar-baz");
        private final List<String> validWithDigits = List.of("foo-bar1", "foo1-2bar");

        @Test
        void shouldRecognizeInvalidInputForKebabCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "kebab"));

            for (String input : invalidInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertFalse(result, "Expected invalid input '" + input + "' to fail Kebab casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        void givenValidKebabCaseInputShouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "kebab"));

            for (String input : validInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result, "Expected valid input '" + input + "' to pass Kebab casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result,
                            "Expected valid input '" + input + "' to pass Kebab casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        class WhenDigitsAreDisallowed {

            @Test
            void shouldRecognizeInvalidInputForKebabCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "kebab", "disallowDigits", true));

                for (String input : invalidInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected invalid input '" + input + "' to fail Kebab casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            void givenValidKebabCaseInputWithoutDigitsShouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "kebab", "disallowDigits", true));

                for (String input : validInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertTrue(result,
                                "Expected valid input '" + input + "' to pass Kebab casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    @Nested
    class CasingTypeCobol {

        private final List<String> invalidInputs = List.of("foo_test", "Foo1", "123", "fooBarBaz1", "FOo", "FOO-BAr",
                "FOO--BAR", "FOO-", "-FOO");

        private final List<String> validInputs = List.of("FOO", "FOO-BAR", "FOO-BAR-BAZ");
        private final List<String> validWithDigits = List.of("FOO-BAR1", "FOO2-3BAR1");

        @Test
        void shouldRecognizeInvalidInputForCobolCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "cobol"));

            for (String input : invalidInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertFalse(result, "Expected invalid input '" + input + "' to fail COBOL casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        void givenValidCobolCaseInput_shouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "cobol"));

            for (String input : validInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result, "Expected valid input '" + input + "' to pass COBOL casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result,
                            "Expected valid input '" + input + "' to pass COBOL casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        class WhenDigitsAreDisallowed {

            @Test
            void shouldRecognizeInvalidInputForCobolCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "cobol", "disallowDigits", true));

                for (String input : invalidInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected invalid input '" + input + "' to fail COBOL casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            void givenValidCobolCaseInputWithoutDigitsShouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "cobol", "disallowDigits", true));

                for (String input : validInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertTrue(result,
                                "Expected valid input '" + input + "' to pass COBOL casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    @Nested
    class CasingTypeSnake {

        private final List<String> invalidInputs = List.of("Foo1", "123", "fooBarBaz1", "FOo", "FOO-BAR", "foo__bar",
                "1foo_bar1", "foo_", "_foo");

        private final List<String> validInputs = List.of("foo", "foo_bar", "foo_bar_baz");
        private final List<String> validWithDigits = List.of("foo_bar1", "foo2_4bar1");

        @Test
        void shouldRecognizeInvalidInputForSnakeCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "snake"));

            for (String input : invalidInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertFalse(result, "Expected invalid input '" + input + "' to fail snake casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        void givenValidSnakeCaseInputShouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "snake"));

            for (String input : validInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result, "Expected valid input '" + input + "' to pass snake casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result,
                            "Expected valid input '" + input + "' to pass snake casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        class WhenDigitsAreDisallowed {

            @Test
            void shouldRecognizeInvalidInputForSnakeCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "snake", "disallowDigits", true));

                for (String input : invalidInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected invalid input '" + input + "' to fail snake casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            void givenValidSnakeCaseInputWithoutDigitsShouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "snake", "disallowDigits", true));

                for (String input : validInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertTrue(result,
                                "Expected valid input '" + input + "' to pass snake casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    @Nested
    class CasingTypeMacro {

        private final List<String> invalidInputs = List.of("foo_test", "Foo1", "123", "fooBarBaz1", "FOo", "FOO-BAR",
                "FO__BAR", "1FOO_BAR1", "FOO___BAR1", "FOO_", "_FOO");

        private final List<String> validInputs = List.of("FOO", "FOO_BAR", "FOO_BAR_BAZ");
        private final List<String> validWithDigits = List.of("FOO_BAR1", "FOO2_4BAR1", "FOO2_4_2");

        @Test
        void shouldRecognizeInvalidInputForMacroCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "macro"));

            for (String input : invalidInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertFalse(result, "Expected invalid input '" + input + "' to fail macro casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        void givenValidMacroCaseInputShouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of("type", "macro"));

            for (String input : validInputs) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result, "Expected valid input '" + input + "' to pass macro casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                    assertTrue(result,
                            "Expected valid input '" + input + "' to pass macro casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        class WhenDigitsAreDisallowed {

            @Test
            void shouldRecognizeInvalidInputForMacroCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "macro", "disallowDigits", true));

                for (String input : invalidInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected invalid input '" + input + "' to fail macro casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertFalse(result, "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            void givenValidMacroCaseInputWithoutDigitsShouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(Map.of("type", "macro", "disallowDigits", true));

                for (String input : validInputs) {
                    try {
                        boolean result = casingFunction.execute(new LintTarget(new ArrayList<>(), input));
                        assertTrue(result,
                                "Expected valid input '" + input + "' to pass macro casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    private static Stream<Object[]> provideTestCases() {
        List<Object[]> testCases = new ArrayList<>();
        List<Object[]> baseData = List.of(new Object[][] { new Object[] { "flat", "flat", "flat01", "Nope" } });

        for (Object[] data : baseData) {
            for (String charSeparator : List.of("/", "*", "-")) {
                for (boolean allowLeading : List.of(true, false)) {
                    for (boolean disallowDigits : List.of(true, false)) {
                        testCases.add(
                                new Object[] { data[0], disallowDigits, charSeparator, allowLeading, data[1], data[2],
                                        data[3] });
                    }
                }
            }
        }
        return testCases.stream();
    }

    @Nested
    class ValidCasingDetection {

        @ParameterizedTest
        @MethodSource("org.wso2.rule.validator.functions.core.CasingFunctionTest#provideTestCases")
        void shouldProperlyDetectValidCases(String type, boolean disallowDigits, String charSeparator,
                boolean allowLeading, String simple, String withDigits, String invalid) {
            Map<String, Object> options = Map.of("type", type, "disallowDigits", disallowDigits, "separator",
                    Map.of("char", charSeparator, "allowLeading", allowLeading));
            CasingFunction casingFunction = new CasingFunction(options);

            try {
                assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), simple + charSeparator + simple)));

                assertFalse(
                        casingFunction.execute(new LintTarget(new ArrayList<>(), simple + charSeparator + invalid)));

                boolean digitsResult = casingFunction.execute(
                        new LintTarget(new ArrayList<>(), withDigits + charSeparator + simple));
                if (!disallowDigits) {
                    assertTrue(digitsResult);
                } else {
                    assertFalse(digitsResult);
                }

                boolean leadingSepResult = casingFunction.execute(
                        new LintTarget(new ArrayList<>(), charSeparator + simple + charSeparator + simple));
                if (allowLeading) {
                    assertTrue(leadingSepResult);
                } else {
                    assertFalse(leadingSepResult);
                }

            } catch (InvalidRulesetException e) {
                fail("Execution should not throw an exception for casing validation.");
            }
        }
    }

    @ParameterizedTest
    @CsvSource({ "flat", "camel", "pascal", "snake", "macro" })
    void shouldProperlyDetectLeadingCharForCasingType(String type) {
        Map<String, Object> options = Map.of("type", type, "disallowDigits", true, "separator",
                Map.of("allowLeading", true, "char", "/"));
        CasingFunction casingFunction = new CasingFunction(options);

        try {
            assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), "/")));
            assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), "//")));
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception for leading separator validation.");
        }
    }

    @Test
    void shouldAllowAdvancedScenarios() {
        Map<String, Object> pascalCaseOptions = Map.of("type", "pascal", "disallowDigits", true, "separator",
                Map.of("char", "-"));
        CasingFunction pascalCasingFunction = new CasingFunction(pascalCaseOptions);

        try {
            assertTrue(pascalCasingFunction.execute(new LintTarget(new ArrayList<>(), "X-MyAmazing-Header")));
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception for Pascal case validation.");
        }

        Map<String, Object> camelCaseOptions = Map.of("type", "camel", "disallowDigits", true, "separator",
                Map.of("char", "/", "allowLeading", true));
        CasingFunction camelCasingFunction = new CasingFunction(camelCaseOptions);

        try {
            assertTrue(camelCasingFunction.execute(new LintTarget(new ArrayList<>(), "/path/to/myResource")));
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception for Camel case validation.");
        }
    }

    @Nested
    class ValidInvalidOptions {

        @Test
        void shouldNotThrowExceptionForValidOptions() {
            Map<String, Object>[] validOptions = new Map[] { Map.of("type", "cobol"),
                    Map.of("type", "macro", "disallowDigits", true),
                    Map.of("type", "snake", "disallowDigits", true, "separator", Map.of("char", "a")),
                    Map.of("type", "pascal", "disallowDigits", false, "separator",
                            Map.of("char", "b", "allowLeading", true)), };

            for (Map<String, Object> validOption : validOptions) {
                CasingFunction function = new CasingFunction(validOption);
                try {
                    // Validate that no exception is thrown during execution
                    boolean result = function.execute(new LintTarget(new ArrayList<>(), "foo"));
                } catch (InvalidRulesetException e) {
                    // Handle exception, optionally log
                    fail("Exception thrown: " + e.getMessage());
                }
            }
        }
    }

    @Test
    void shouldThrowExceptionForInvalidOptions() {
        Map<String, Object>[] invalidOptions = new Map[] { Map.of("type", "macro", "foo", true), // Invalid option foo
                Map.of("type", "pascal", "disallowDigits", false, "separator", Map.of()),
                // Missing separator char option
                Map.of("type", "pascal", "disallowDigits", false, "separator", Map.of("allowLeading", true)),
                // Missing separator char option
                Map.of("type", "snake", "separator", Map.of("char", "a", "foo", true)), // Invalid separator option foo
                Map.of("type", "pascal", "separator", Map.of("char", "fo")), // Invalid char option value
                new HashMap<String, Object>() {{
                    put("type", "pascal");
                    put("separator", new HashMap<String, Object>() {{
                        put("char", null); // Invalid char option value (null)
                    }});
                }} };

        for (Map<String, Object> invalidOption : invalidOptions) {
            CasingFunction function = new CasingFunction(invalidOption);
            assertThrows(InvalidRulesetException.class,
                    () -> function.execute(new LintTarget(new ArrayList<>(), "foo")),
                    "Expected InvalidRulesetException for invalid options.");
        }
    }

}
