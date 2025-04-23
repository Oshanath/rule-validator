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

import org.junit.jupiter.api.Test;
import org.wso2.rule.validator.Constants;
import org.wso2.rule.validator.InvalidRulesetException;
import org.wso2.rule.validator.document.LintTarget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AlphabeticalFunctionTest {
    @Test
    public void testFalsyTargetShouldReturnNoError() {
        AlphabeticalFunction function = new AlphabeticalFunction(null);
        try {
            assertTrue(function.execute(new LintTarget(new ArrayList<>(), false)).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void testSingleElementTargetShouldReturnNoError() {
        AlphabeticalFunction function = new AlphabeticalFunction(null);
        try {
            assertTrue(function.execute(new LintTarget(new ArrayList<>(), "a")).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void testObjectKeysNotInOrderShouldReturnError() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("c", 2);
        input.put("b", "xz");

        AlphabeticalFunction function = new AlphabeticalFunction(null);
        try {
            assertFalse(function.execute(new LintTarget(new ArrayList<>(), input)).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void testUnsortedPropertiesWithNumericKeysShouldReturnError() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("400", Map.of("description", ""));
        input.put("200", Map.of("description", ""));

        AlphabeticalFunction function = new AlphabeticalFunction(null);
        try {
            assertFalse(function.execute(new LintTarget(new ArrayList<>(), input)).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void testUnsortedArrayOfStringsShouldReturnError() {
        List<Object> input = new ArrayList<>();
        input.add("b");
        input.add("a");

        AlphabeticalFunction function = new AlphabeticalFunction(null);
        try {
            assertFalse(function.execute(new LintTarget(new ArrayList<>(), input)).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void testSortedArrayOfStringsShouldReturnNoError() {
        List<Object> input = new ArrayList<>();
        input.add("a");
        input.add("ab");

        AlphabeticalFunction function = new AlphabeticalFunction(null);
        try {
            assertTrue(function.execute(new LintTarget(new ArrayList<>(), input)).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void testUnsortedArrayOfNumbersShouldReturnError() {
        List<Object> input = new ArrayList<>();
        input.add(10);
        input.add(1);

        AlphabeticalFunction function = new AlphabeticalFunction(null);
        try {
            assertFalse(function.execute(new LintTarget(new ArrayList<>(), input)).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void testArrayOfObjectsShouldReturnError() throws InvalidRulesetException {
        List<Object> input = new ArrayList<>();
        input.add(Map.of("a", "10"));
        input.add(Map.of("b", "1"));

        AlphabeticalFunction function = new AlphabeticalFunction(null);
        try {
            assertFalse(function.execute(new LintTarget(new ArrayList<>(), input)).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    void testArrayContainingInvalidValuesShouldReturnError() {
        List<Object> input = new ArrayList<>();
        input.add(false);
        input.add("a");
        input.add(null);

        AlphabeticalFunction function = new AlphabeticalFunction(null);
        try {
            assertFalse(function.execute(new LintTarget(new ArrayList<>(), input)).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    void testKeyedByWithUnsortedPropValuesShouldReturnError() {
        Map<String, Object> options = new HashMap<>();
        options.put(Constants.RULESET_ALPHABETICAL_KEYED_BY, "a");

        List<Object> input = new ArrayList<>();
        input.add(Map.of("a", "10"));
        input.add(Map.of("a", "1"));

        AlphabeticalFunction function = new AlphabeticalFunction(options);
        try {
            assertFalse(function.execute(new LintTarget(new ArrayList<>(), input)).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    void testKeyedByWithSortedPropValuesShouldReturnNoError() {
        Map<String, Object> options = new HashMap<>();
        options.put(Constants.RULESET_ALPHABETICAL_KEYED_BY, "a");

        List<Object> input = new ArrayList<>();
        input.add(Map.of("a", "1"));
        input.add(Map.of("a", "2"));
        input.add(Map.of("a", "2"));

        AlphabeticalFunction function = new AlphabeticalFunction(options);
        try {
            assertTrue(function.execute(new LintTarget(new ArrayList<>(), input)).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    void testKeyedByWithArrayOfPrimitivesShouldReturnError() {
        Map<String, Object> options = new HashMap<>();
        options.put(Constants.RULESET_ALPHABETICAL_KEYED_BY, "a");

        List<Object> input = new ArrayList<>();
        input.add(100);
        input.add(1);

        AlphabeticalFunction function = new AlphabeticalFunction(options);
        try {
            assertFalse(function.execute(new LintTarget(new ArrayList<>(), Arrays.asList(100, 1))).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    void testValidFunctionOptionsShouldNotThrow() {
        Map<String, Object>[] validOptions = new Map[] { null, Map.of(),
                Map.of(Constants.RULESET_ALPHABETICAL_KEYED_BY, "bar"), };

        for (Map<String, Object> validOption : validOptions) {
            AlphabeticalFunction function = new AlphabeticalFunction(validOption);
            assertDoesNotThrow(() -> function.execute(new LintTarget(new ArrayList<>(), List.of())));
        }
    }

    @Test
    void testInvalidFunctionOptionsShouldNotThrow() {
        Map<String, Object>[] invalidOptions = new Map[] { Map.of("foo", true),
                Map.of(Constants.RULESET_ALPHABETICAL_KEYED_BY, 2), };

        for (Map<String, Object> invalidOption : invalidOptions) {
            AlphabeticalFunction function = new AlphabeticalFunction(invalidOption);
            assertThrows(InvalidRulesetException.class,
                    () -> function.execute(new LintTarget(new ArrayList<>(), List.of())),
                    "Expected InvalidRulesetException for invalid options.");
        }
    }
}
