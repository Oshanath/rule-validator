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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wso2.rule.validator.Constants;
import org.wso2.rule.validator.InvalidRulesetException;
import org.wso2.rule.validator.document.LintTarget;
import org.wso2.rule.validator.functions.FunctionResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class LengthFunctionTest {

    private List<Object> inputs;
    private Map<String, Object> options;

    @BeforeEach
    public void setUp() {
        inputs = List.of(
                "123",
                3,
                List.of(1, 2, 3),
                Map.of("one", 1, "two", 2, "three", 3)
        );

        options = new HashMap<>();
    }

    private LengthFunction createFunction(Map<String, Object> customOptions) {
        return new LengthFunction(customOptions);
    }

    @Test
    public void testExceedsMaxLength() {
        options.put(Constants.RULESET_LENGTH_MAX, 2);

        for (Object input : inputs) {
            LintTarget target = new LintTarget(new ArrayList<>(), input);
            LengthFunction function = createFunction(options);

            try {
                FunctionResult result = function.execute(target);
                assertFalse(result.passed, "Expected input to exceed max length of 2");
            } catch (InvalidRulesetException e) {
                fail("Exception thrown: " + e.getMessage());
            }
        }
    }

    @Test
    public void testFallsBelowMinLength() {
        options.put(Constants.RULESET_LENGTH_MIN, 4);

        for (Object input : inputs) {
            LintTarget target = new LintTarget(new ArrayList<>(), input);
            LengthFunction function = createFunction(options);

            try {
                FunctionResult result = function.execute(target);
                assertFalse(result.passed, "Expected input to fall below min length of 4");
            } catch (InvalidRulesetException e) {
                fail("Exception thrown: " + e.getMessage());
            }
        }
    }

    @Test
    public void testWithinMinAndMaxLength() {
        options.put(Constants.RULESET_LENGTH_MIN, 3);
        options.put(Constants.RULESET_LENGTH_MAX, 3);

        for (Object input : inputs) {
            LintTarget target = new LintTarget(new ArrayList<>(), input);
            LengthFunction function = createFunction(options);

            try {
                FunctionResult result = function.execute(target);
                assertTrue(result.passed, "Expected input to be within min and max length of 3");
            } catch (InvalidRulesetException e) {
                fail("Exception thrown: " + e.getMessage());
            }
        }
    }

    @Test
    public void testValidOptions() {
        Map<String, Object>[] validOptions = new Map[]{
                Map.of(Constants.RULESET_LENGTH_MIN, 2),
                Map.of(Constants.RULESET_LENGTH_MAX, 4),
                Map.of(Constants.RULESET_LENGTH_MIN, 2, Constants.RULESET_LENGTH_MAX, 4)
        };

        for (Map<String, Object> validOption : validOptions) {
            LengthFunction function = createFunction(validOption);
            try {
                FunctionResult result = function.execute(new LintTarget(new ArrayList<>(), "123"));
                assertTrue(result.passed, "Expected no exception for valid options.");
            } catch (InvalidRulesetException e) {
                fail("Exception thrown: " + e.getMessage());
            }
        }
    }

    @Test
    public void testInvalidOptions() {
        Map<String, Object>[] invalidOptions = new Map[]{
                null,
                Map.of("foo", true),
                Map.of(Constants.RULESET_LENGTH_MIN, "2"),
                Map.of(Constants.RULESET_LENGTH_MAX, "2"),
                Map.of(Constants.RULESET_LENGTH_MIN, "4", Constants.RULESET_LENGTH_MAX, "2")
        };

        for (Map<String, Object> invalidOption : invalidOptions) {
            LengthFunction function = createFunction(invalidOption);
            assertThrows(InvalidRulesetException.class, () -> function.execute(new LintTarget(new ArrayList<>(), "")),
                    "Expected InvalidRulesetException for invalid options.");
        }
    }
}
