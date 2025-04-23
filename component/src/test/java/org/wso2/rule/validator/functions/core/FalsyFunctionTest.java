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

public class FalsyFunctionTest {
    private final FalsyFunction falsy = new FalsyFunction(null);

    @Test
    public void givenFalsyInputsShouldReturnNoErrorMessage() {
        List<Object> falsyInputs = new ArrayList<>();
        falsyInputs.add(false);
        falsyInputs.add(null);
        falsyInputs.add(0);
        falsyInputs.add("");

        for (Object input : falsyInputs) {
            LintTarget target = new LintTarget(new ArrayList<>(), input);
            try {
                FunctionResult result = falsy.execute(target);
                assertTrue(result.passed, "Expected falsy input: " + input + " to be falsy");
            } catch (InvalidRulesetException e) {
                fail("Exception thrown for falsy input: " + e.getMessage());
            }
        }
    }

    @Test
    public void givenTruthyInputsShouldReturnErrorMessage() {
        List<Object> truthyInputs = List.of(true, 1, new ArrayList<>(List.of(1)),
                new HashMap<>(Map.of("key", "value")));

        for (Object input : truthyInputs) {
            LintTarget target = new LintTarget(new ArrayList<>(), input);
            try {
                FunctionResult result = falsy.execute(target);
                assertFalse(result.passed, "Expected truthy input: " + input + " to be truthy");
            } catch (InvalidRulesetException e) {
                fail("Exception thrown for truthy input: " + e.getMessage());
            }
        }
    }

    @Test
    public void validationTestForInvalidOptions() {
        Map<String, Object> invalidOption = Map.of("unsupportedKey", true);

        FalsyFunction function = new FalsyFunction(invalidOption);
        LintTarget target = new LintTarget(new ArrayList<>(), null);

        assertThrows(InvalidRulesetException.class, () -> function.execute(target),
                "Expected InvalidRulesetException for invalid option: " + invalidOption);
    }
}
