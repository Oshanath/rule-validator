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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class EnumerationFunctionTest {
    @Test
    public void givenValidInputShouldReturnTrue() {
        Map<String, Object> options = new HashMap<>();
        options.put(Constants.RULESET_ENUMERATION_VALUES, List.of(new String[] { "x", "y", "z" }));

        EnumerationFunction function = new EnumerationFunction(options);
        LintTarget target = new LintTarget(new ArrayList<>(), "x");

        try {
            assertTrue(function.execute(target).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void givenInvalidInputShouldReturnFalse() {
        Map<String, Object> options = new HashMap<>();
        options.put(Constants.RULESET_ENUMERATION_VALUES, List.of(new String[] { "y", "z" }));

        EnumerationFunction function = new EnumerationFunction(options);
        LintTarget target = new LintTarget(new ArrayList<>(), "x");

        try {
            assertFalse(function.execute(target).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void givenNonPrimitiveValueShouldReturnTrue() {
        Map<String, Object> options = new HashMap<>();
        options.put(Constants.RULESET_ENUMERATION_VALUES, List.of(new String[] { "test" }));

        EnumerationFunction function = new EnumerationFunction(options);
        LintTarget target = new LintTarget(new ArrayList<>(), new Object());

        try {
            assertTrue(function.execute(target).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void givenValidOptionsShouldNotThrow() {
        Map<String, Object> options = new HashMap<>();
        options.put(Constants.RULESET_ENUMERATION_VALUES, List.of(new String[] { "foo", "2" }));

        EnumerationFunction function = new EnumerationFunction(options);
        LintTarget target = new LintTarget(new ArrayList<>(), "foo");

        assertDoesNotThrow(() -> function.execute(target));
    }

    @Test
    public void givenInvalidOptionsShouldThrowInvalidRulesetException() {
        List<Map<String, Object>> invalidOptionsList = new ArrayList<>(
                List.of(Map.of(Constants.RULESET_ENUMERATION_VALUES, List.of(new String[] { "foo", "2" }), "foo", true),
                        Map.of(Constants.RULESET_ENUMERATION_VALUES, List.of(new Object[] {})),
                        Map.of(Constants.RULESET_ENUMERATION_VALUES, 2)));

        invalidOptionsList.add(null);

        for (Map<String, Object> options : invalidOptionsList) {
            EnumerationFunction function = new EnumerationFunction(options);
            LintTarget target = new LintTarget(new ArrayList<>(), "foo");
            assertThrows(InvalidRulesetException.class, () -> function.execute(target));
        }
    }
}
