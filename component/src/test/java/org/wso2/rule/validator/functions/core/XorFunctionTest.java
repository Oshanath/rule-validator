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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class XorFunctionTest {
    @Test
    public void givenNoPropertiesShouldReturnErrorMessage() {
        Map<String, Object> document = new HashMap<>();
        document.put("version", "1.0.0");
        document.put("title", "Swagger Petstore");
        document.put("termsOfService", "http://swagger.io/terms/");

        Map<String, Object> options = Map.of(Constants.RULESET_XOR_PROPERTIES,
                new ArrayList<>(List.of("yada-yada", "whatever", "foo")));
        XorFunction xorFunction = new XorFunction(options);
        LintTarget target = new LintTarget(new ArrayList<>(), document);

        try {
            assertFalse(xorFunction.execute(target).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void givenMultiplePropertiesThatDoNotMatchShouldReturnErrorMessage() {
        Map<String, Object> document = new HashMap<>();
        document.put("version", "1.0.0");
        document.put("title", "Swagger Petstore");
        document.put("termsOfService", "http://swagger.io/terms/");

        Map<String, Object> options = Map.of(Constants.RULESET_XOR_PROPERTIES,
                new ArrayList<>(List.of("yada-yada", "whatever", "foo")));
        XorFunction xorFunction = new XorFunction(options);
        LintTarget target = new LintTarget(new ArrayList<>(), document);

        try {
            assertFalse(xorFunction.execute(target).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void givenBothPropertiesShouldReturnErrorMessage() {
        Map<String, Object> document = new HashMap<>();
        document.put("version", "1.0.0");
        document.put("title", "Swagger Petstore");

        Map<String, Object> options = Map.of(Constants.RULESET_XOR_PROPERTIES,
                new ArrayList<>(List.of("version", "title")));
        XorFunction xorFunction = new XorFunction(options);
        LintTarget target = new LintTarget(new ArrayList<>(), document);

        try {
            assertFalse(xorFunction.execute(target).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void givenInvalidInputShouldReturnNoErrorMessage() {
        Map<String, Object> options = Map.of(Constants.RULESET_XOR_PROPERTIES,
                new ArrayList<>(List.of("version", "title")));
        XorFunction xorFunction = new XorFunction(options);
        LintTarget target = new LintTarget(new ArrayList<>(), null);

        try {
            assertTrue(xorFunction.execute(target).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void givenOnlyOnePropertyShouldReturnNoErrorMessage() {
        Map<String, Object> document = new HashMap<>();
        document.put("version", "1.0.0");
        document.put("title", "Swagger Petstore");
        document.put("termsOfService", "http://swagger.io/terms/");

        Map<String, Object> options = Map.of(Constants.RULESET_XOR_PROPERTIES,
                new ArrayList<>(List.of("something", "title")));
        XorFunction xorFunction = new XorFunction(options);
        LintTarget target = new LintTarget(new ArrayList<>(), document);

        try {
            assertTrue(xorFunction.execute(target).passed);
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void givenValidOptionsShouldNotThrow() {
        Map<String, Object> options = Map.of(Constants.RULESET_XOR_PROPERTIES, new ArrayList<>(List.of("foo", "bar")));
        XorFunction xorFunction = new XorFunction(options);
        LintTarget target = new LintTarget(new ArrayList<>(), new HashMap<>());

        assertDoesNotThrow(() -> xorFunction.execute(target));
    }

    @Test
    public void givenInvalidOptionsShouldThrow() {
        List<Object> invalidOptionsList = Arrays.asList(
                Map.of(Constants.RULESET_XOR_PROPERTIES, new ArrayList<>(List.of("foo"))),
                Map.of(Constants.RULESET_XOR_PROPERTIES, new ArrayList<>(List.of())),
                Map.of(Constants.RULESET_XOR_PROPERTIES, new ArrayList<>(List.of("foo", new HashMap<>()))),
                Map.of(Constants.RULESET_XOR_PROPERTIES, new ArrayList<>(List.of("foo", "bar")), "foo", true));

        assertThrows(InvalidRulesetException.class,
                () -> new XorFunction(null).execute(new LintTarget(new ArrayList<>(), new HashMap<>())));

        for (Object invalidOptions : invalidOptionsList) {
            XorFunction xorFunction = new XorFunction((Map<String, Object>) invalidOptions);
            LintTarget target = new LintTarget(new ArrayList<>(), new HashMap<>());

            assertThrows(InvalidRulesetException.class, () -> xorFunction.execute(target));
        }
    }
}
