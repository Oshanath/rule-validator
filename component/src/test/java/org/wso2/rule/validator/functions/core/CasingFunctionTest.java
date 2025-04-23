/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.rule.validator.functions.core;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.wso2.rule.validator.Constants;
import org.wso2.rule.validator.InvalidRulesetException;
import org.wso2.rule.validator.document.LintTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CasingFunctionTest {
    @Test
    public void givenNonStringInputShouldReturnNoErrorMessage() {
        CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "camel"));

        try {
            assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), false)).passed,
                    "Boolean input should return true (no error).");
            assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), 1)).passed,
                    "Integer input should return true (no error).");
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception for non-string inputs.");
        }
    }

    @Test
    public void givenEmptyStringInputShouldReturnNoErrorMessage() {
        CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "camel"));

        try {
            assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), "")).passed,
                    "Empty string input should return true (no error).");
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception for empty string input.");
        }
    }

    @Nested
    public class CasingTypeFlat {

        private final List<String> invalidInputs = List.of("foo_test", "Foo", "123", "1d", "foo-bar");
        private final List<String> validInputs = List.of("foo", "foobar");
        private final List<String> validWithDigits = List.of("foo9bar", "foo24baz", "foo1");

        @Test
        public void shouldRecognizeInvalidInputForFlatCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "sdf"));

            for (String input : invalidInputs) {
                try {
                    assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected invalid input '" + input + "' to fail flat casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        public void givenValidFlatCaseInputShouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "flat"));

            for (String input : validInputs) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass flat casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass flat casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        public class WhenDigitsAreDisallowed {

            @Test
            public void shouldRecognizeInvalidInputForFlatCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "flat", Constants.RULESET_CASING_DISALLOW_DIGITS, true));

                for (String input : invalidInputs) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected invalid input '" + input + "' to fail flat casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            public void givenValidFlatCaseInputWithoutDigitsShouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "flat", Constants.RULESET_CASING_DISALLOW_DIGITS, true));

                for (String input : validInputs) {
                    try {
                        assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected valid input '" + input + "' to pass flat casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    @Nested
    public class CasingTypeCamel {

        private final List<String> invalidInputs = List.of("foo_test", "Foo", "1fooBarBaz", "123", "foo-bar");
        private final List<String> validInputs = List.of("foo", "fooBar", "fooBarBaz", "coordinateX");
        private final List<String> validWithDigits = List.of("foo1", "foo24Bar", "fooBar0Baz323");

        @Test
        public void shouldRecognizeInvalidInputForCamelCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "camel"));

            for (String input : invalidInputs) {
                try {
                    assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected invalid input '" + input + "' to fail camel casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        public void givenValidCamelCaseInputShouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "camel"));

            for (String input : validInputs) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass camel casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass camel casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        public class WhenDigitsAreDisallowed {

            @Test
            public void shouldRecognizeInvalidInputForCamelCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "camel", Constants.RULESET_CASING_DISALLOW_DIGITS, true));

                for (String input : invalidInputs) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected invalid input '" + input + "' to fail camel casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            public void givenValidCamelCaseInputWithoutDigitsShouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "camel", Constants.RULESET_CASING_DISALLOW_DIGITS, true));

                for (String input : validInputs) {
                    try {
                        assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected valid input '" + input + "' to pass camel casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    @Nested
    public class CasingTypePascal {

        private final List<String> invalidInputs = List.of("foo_test", "123", "1fooBarBaz", "fooBarBaz1", "fooBar",
                "foo1", "foo-bar");
        private final List<String> validInputs = List.of("Foo", "FooBar", "FooBarBaz", "CoordinateZ");
        private final List<String> validWithDigits = List.of("Foo1", "FooBarBaz1");

        @Test
        public void shouldRecognizeInvalidInputForPascalCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "pascal"));

            for (String input : invalidInputs) {
                try {
                    assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected invalid input '" + input + "' to fail Pascal casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        public void givenValidPascalCaseInputShouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "pascal"));

            for (String input : validInputs) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass Pascal casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass Pascal casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        public class WhenDigitsAreDisallowed {

            @Test
            public void shouldRecognizeInvalidInputForPascalCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "pascal", Constants.RULESET_CASING_DISALLOW_DIGITS,
                                true));

                for (String input : invalidInputs) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected invalid input '" + input + "' to fail Pascal casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            public void givenValidPascalCaseInputWithoutDigits_shouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "pascal", Constants.RULESET_CASING_DISALLOW_DIGITS,
                                true));

                for (String input : validInputs) {
                    try {
                        assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected valid input '" + input +
                                        "' to pass Pascal casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    @Nested
    public class CasingTypeKebab {

        private final List<String> invalidInputs = List.of("foo_test", "Foo1", "123", "fooBarBaz1", "fooBar", "foO",
                "foo-baR", "1foo-bar", "foo--bar", "foo-", "-foo");

        private final List<String> validInputs = List.of("foo", "foo-bar", "foo-bar-baz");
        private final List<String> validWithDigits = List.of("foo-bar1", "foo1-2bar");

        @Test
        public void shouldRecognizeInvalidInputForKebabCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "kebab"));

            for (String input : invalidInputs) {
                try {
                    assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected invalid input '" + input + "' to fail Kebab casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        public void givenValidKebabCaseInputShouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "kebab"));

            for (String input : validInputs) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass Kebab casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass Kebab casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        public class WhenDigitsAreDisallowed {

            @Test
            public void shouldRecognizeInvalidInputForKebabCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "kebab", Constants.RULESET_CASING_DISALLOW_DIGITS, true));

                for (String input : invalidInputs) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected invalid input '" + input + "' to fail Kebab casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            public void givenValidKebabCaseInputWithoutDigitsShouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "kebab", Constants.RULESET_CASING_DISALLOW_DIGITS, true));

                for (String input : validInputs) {
                    try {
                        assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected valid input '" + input + "' to pass Kebab casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    @Nested
    public class CasingTypeCobol {

        private final List<String> invalidInputs = List.of("foo_test", "Foo1", "123", "fooBarBaz1", "FOo", "FOO-BAr",
                "FOO--BAR", "FOO-", "-FOO");

        private final List<String> validInputs = List.of("FOO", "FOO-BAR", "FOO-BAR-BAZ");
        private final List<String> validWithDigits = List.of("FOO-BAR1", "FOO2-3BAR1");

        @Test
        public void shouldRecognizeInvalidInputForCobolCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "cobol"));

            for (String input : invalidInputs) {
                try {
                    assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected invalid input '" + input + "' to fail COBOL casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        public void givenValidCobolCaseInput_shouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "cobol"));

            for (String input : validInputs) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass COBOL casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass COBOL casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        public class WhenDigitsAreDisallowed {

            @Test
            public void shouldRecognizeInvalidInputForCobolCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "cobol", Constants.RULESET_CASING_DISALLOW_DIGITS, true));

                for (String input : invalidInputs) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected invalid input '" + input + "' to fail COBOL casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            public void givenValidCobolCaseInputWithoutDigitsShouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "cobol", Constants.RULESET_CASING_DISALLOW_DIGITS, true));

                for (String input : validInputs) {
                    try {
                        assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected valid input '" + input + "' to pass COBOL casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    @Nested
    public class CasingTypeSnake {

        private final List<String> invalidInputs = List.of("Foo1", "123", "fooBarBaz1", "FOo", "FOO-BAR", "foo__bar",
                "1foo_bar1", "foo_", "_foo");

        private final List<String> validInputs = List.of("foo", "foo_bar", "foo_bar_baz");
        private final List<String> validWithDigits = List.of("foo_bar1", "foo2_4bar1");

        @Test
        public void shouldRecognizeInvalidInputForSnakeCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "snake"));

            for (String input : invalidInputs) {
                try {
                    assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected invalid input '" + input + "' to fail snake casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        public void givenValidSnakeCaseInputShouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "snake"));

            for (String input : validInputs) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass snake casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass snake casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        public class WhenDigitsAreDisallowed {

            @Test
            public void shouldRecognizeInvalidInputForSnakeCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "snake", Constants.RULESET_CASING_DISALLOW_DIGITS, true));

                for (String input : invalidInputs) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected invalid input '" + input + "' to fail snake casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            public void givenValidSnakeCaseInputWithoutDigitsShouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "snake", Constants.RULESET_CASING_DISALLOW_DIGITS, true));

                for (String input : validInputs) {
                    try {
                        assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected valid input '" + input + "' to pass snake casing validation without digits.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }
        }
    }

    @Nested
    public class CasingTypeMacro {

        private final List<String> invalidInputs = List.of("foo_test", "Foo1", "123", "fooBarBaz1", "FOo", "FOO-BAR",
                "FO__BAR", "1FOO_BAR1", "FOO___BAR1", "FOO_", "_FOO");

        private final List<String> validInputs = List.of("FOO", "FOO_BAR", "FOO_BAR_BAZ");
        private final List<String> validWithDigits = List.of("FOO_BAR1", "FOO2_4BAR1", "FOO2_4_2");

        @Test
        public void shouldRecognizeInvalidInputForMacroCase() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "macro"));

            for (String input : invalidInputs) {
                try {
                    assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected invalid input '" + input + "' to fail macro casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Test
        public void givenValidMacroCaseInputShouldReturnNoErrorMessage() {
            CasingFunction casingFunction = new CasingFunction(Map.of(Constants.RULESET_CASING_TYPE, "macro"));

            for (String input : validInputs) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass macro casing validation.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }

            for (String input : validWithDigits) {
                try {
                    assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                            "Expected valid input '" + input + "' to pass macro casing validation with digits.");
                } catch (InvalidRulesetException e) {
                    fail("Execution should not throw an exception for input: " + input);
                }
            }
        }

        @Nested
        public class WhenDigitsAreDisallowed {

            @Test
            public void shouldRecognizeInvalidInputForMacroCaseWhenDigitsAreDisallowed() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "macro", Constants.RULESET_CASING_DISALLOW_DIGITS, true));

                for (String input : invalidInputs) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected invalid input '" + input + "' to fail macro casing validation.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }

                for (String input : validWithDigits) {
                    try {
                        assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
                                "Expected input '" + input + "' to fail due to digit restriction.");
                    } catch (InvalidRulesetException e) {
                        fail("Execution should not throw an exception for input: " + input);
                    }
                }
            }

            @Test
            public void givenValidMacroCaseInputWithoutDigitsShouldReturnNoErrorMessage() {
                CasingFunction casingFunction = new CasingFunction(
                        Map.of(Constants.RULESET_CASING_TYPE, "macro", Constants.RULESET_CASING_DISALLOW_DIGITS, true));

                for (String input : validInputs) {
                    try {
                        assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), input)).passed,
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
    public class ValidCasingDetection {

        @ParameterizedTest
        @MethodSource("org.wso2.rule.validator.functions.core.CasingFunctionTest#provideTestCases")
        public void shouldProperlyDetectValidCases(String type, boolean disallowDigits, String charSeparator,
                boolean allowLeading, String simple, String withDigits, String invalid) {
            Map<String, Object> options = Map.of(Constants.RULESET_CASING_TYPE, type,
                    Constants.RULESET_CASING_DISALLOW_DIGITS, disallowDigits, Constants.RULESET_CASING_SEPARATOR,
                    Map.of(Constants.RULESET_CASING_SEPARATOR_CHAR, charSeparator,
                            Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING, allowLeading));
            CasingFunction casingFunction = new CasingFunction(options);

            try {
                assertTrue(casingFunction.execute(
                        new LintTarget(new ArrayList<>(), simple + charSeparator + simple)).passed);

                assertFalse(casingFunction.execute(
                        new LintTarget(new ArrayList<>(), simple + charSeparator + invalid)).passed);

                boolean digitsResult = casingFunction.execute(
                        new LintTarget(new ArrayList<>(), withDigits + charSeparator + simple)).passed;
                if (!disallowDigits) {
                    assertTrue(digitsResult);
                } else {
                    assertFalse(digitsResult);
                }

                boolean leadingSepResult = casingFunction.execute(
                        new LintTarget(new ArrayList<>(), charSeparator + simple + charSeparator + simple)).passed;
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
    public void shouldProperlyDetectLeadingCharForCasingType(String type) {
        Map<String, Object> options = Map.of(Constants.RULESET_CASING_TYPE, type,
                Constants.RULESET_CASING_DISALLOW_DIGITS, true, Constants.RULESET_CASING_SEPARATOR,
                Map.of(Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING, true, Constants.RULESET_CASING_SEPARATOR_CHAR,
                        "/"));
        CasingFunction casingFunction = new CasingFunction(options);

        try {
            assertTrue(casingFunction.execute(new LintTarget(new ArrayList<>(), "/")).passed);
            assertFalse(casingFunction.execute(new LintTarget(new ArrayList<>(), "//")).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception for leading separator validation.");
        }
    }

    @Test
    public void shouldAllowAdvancedScenarios() {
        Map<String, Object> pascalCaseOptions = Map.of(Constants.RULESET_CASING_TYPE, "pascal",
                Constants.RULESET_CASING_DISALLOW_DIGITS, true, Constants.RULESET_CASING_SEPARATOR,
                Map.of(Constants.RULESET_CASING_SEPARATOR_CHAR, "-"));
        CasingFunction pascalCasingFunction = new CasingFunction(pascalCaseOptions);

        try {
            assertTrue(pascalCasingFunction.execute(new LintTarget(new ArrayList<>(), "X-MyAmazing-Header")).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception for Pascal case validation.");
        }

        Map<String, Object> camelCaseOptions = Map.of(Constants.RULESET_CASING_TYPE, "camel",
                Constants.RULESET_CASING_DISALLOW_DIGITS, true, Constants.RULESET_CASING_SEPARATOR,
                Map.of(Constants.RULESET_CASING_SEPARATOR_CHAR, "/", Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING,
                        true));
        CasingFunction camelCasingFunction = new CasingFunction(camelCaseOptions);

        try {
            assertTrue(camelCasingFunction.execute(new LintTarget(new ArrayList<>(), "/path/to/myResource")).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception for Camel case validation.");
        }
    }

    @Nested
    public class ValidInvalidOptions {

        @Test
        public void shouldNotThrowExceptionForValidOptions() {
            Map<String, Object>[] validOptions = new Map[] { Map.of(Constants.RULESET_CASING_TYPE, "cobol"),
                    Map.of(Constants.RULESET_CASING_TYPE, "macro", Constants.RULESET_CASING_DISALLOW_DIGITS, true),
                    Map.of(Constants.RULESET_CASING_TYPE, "snake", Constants.RULESET_CASING_DISALLOW_DIGITS, true,
                            Constants.RULESET_CASING_SEPARATOR, Map.of(Constants.RULESET_CASING_SEPARATOR_CHAR, "a")),
                    Map.of(Constants.RULESET_CASING_TYPE, "pascal", Constants.RULESET_CASING_DISALLOW_DIGITS, false,
                            Constants.RULESET_CASING_SEPARATOR, Map.of(Constants.RULESET_CASING_SEPARATOR_CHAR, "b",
                                    Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING, true)), };

            for (Map<String, Object> validOption : validOptions) {
                CasingFunction function = new CasingFunction(validOption);
                assertDoesNotThrow(() -> function.execute(new LintTarget(new ArrayList<>(), "foo")));
            }
        }
    }

    @Test
    public void shouldThrowExceptionForInvalidOptions() {
        Map<String, Object>[] invalidOptions = new Map[] { Map.of(Constants.RULESET_CASING_TYPE, "macro", "foo", true),
                Map.of(Constants.RULESET_CASING_TYPE, "pascal", Constants.RULESET_CASING_DISALLOW_DIGITS, false,
                        Constants.RULESET_CASING_SEPARATOR, Map.of()),
                Map.of(Constants.RULESET_CASING_TYPE, "pascal", Constants.RULESET_CASING_DISALLOW_DIGITS, false,
                        Constants.RULESET_CASING_SEPARATOR,
                        Map.of(Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING, true)),
                Map.of(Constants.RULESET_CASING_TYPE, "snake", Constants.RULESET_CASING_SEPARATOR,
                        Map.of(Constants.RULESET_CASING_SEPARATOR_CHAR, "a", "foo", true)),
                Map.of(Constants.RULESET_CASING_TYPE, "pascal", Constants.RULESET_CASING_SEPARATOR,
                        Map.of(Constants.RULESET_CASING_SEPARATOR_CHAR, "fo")), new HashMap<String, Object>() {{
            put(Constants.RULESET_CASING_TYPE, "pascal");
            put(Constants.RULESET_CASING_SEPARATOR, new HashMap<String, Object>() {{
                put(Constants.RULESET_CASING_SEPARATOR_CHAR, null);
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
