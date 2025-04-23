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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class SchemaFunctionTest {
    @Test
    public void validatesDraft4() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("$schema", "http://json-schema.org/draft-04/schema#");
        schema.put("type", "number");
        schema.put("maximum", 2);
        schema.put("exclusiveMaximum", true);

        Map<String, Object> options = new HashMap<>();
        options.put(Constants.RULESET_SCHEMA_SCHEMA, schema);

        SchemaFunction schemaFunction = new SchemaFunction(options);

        LintTarget target = new LintTarget(new ArrayList<>(), 2);

        boolean result = false;
        try {
            assertFalse(schemaFunction.execute(target).passed,
                    "Validation should fail when value is 2 with exclusiveMaximum");
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }

        options.put(Constants.RULESET_SCHEMA_DIALECT, "auto");
        schemaFunction = new SchemaFunction(options);
        try {
            assertFalse(schemaFunction.execute(target).passed,
                    "Validation should fail when value is 2 with exclusiveMaximum");
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }

    @Test
    public void validatesDraft6() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("$schema", "http://json-schema.org/draft-06/schema#");
        schema.put("type", "string");

        Map<String, Object> options = new HashMap<>();
        options.put(Constants.RULESET_SCHEMA_SCHEMA, schema);

        SchemaFunction schemaFunction = new SchemaFunction(options);

        LintTarget target = new LintTarget(new ArrayList<>(), 2);

        assertFalse(schemaFunction.executeFunction(target).passed,
                "Validation should fail when value type is not string");

        options.put(Constants.RULESET_SCHEMA_DIALECT, "auto");
        schemaFunction = new SchemaFunction(options);
        assertFalse(schemaFunction.executeFunction(target).passed,
                "Validation should fail when value type is not string");
    }

    @Test
    public void allowsRedundantEscapes() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("$schema", "http://json-schema.org/draft-07/schema#");
        schema.put("type", "string");
        schema.put("pattern", "[\\-_]");

        Map<String, Object> options = new HashMap<>();
        options.put(Constants.RULESET_SCHEMA_SCHEMA, schema);

        SchemaFunction schemaFunction = new SchemaFunction(options);
        LintTarget target = new LintTarget(new ArrayList<>(), 2);

        try {
            assertFalse(schemaFunction.execute(target).passed, "Validation should fail as value type is not a string");
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }

        schema = new HashMap<>();
        schema.put("$schema", "http://json-schema.org/draft-06/schema#");
        schema.put("type", "string");
        schema.put("pattern", "[\\_-]");

        options = new HashMap<>();
        options.put(Constants.RULESET_SCHEMA_SCHEMA, schema);

        schemaFunction = new SchemaFunction(options);

        try {
            assertFalse(schemaFunction.execute(target).passed, "Validation should fail as value type is not a string");
        } catch (InvalidRulesetException e) {
            fail("Execution should not throw an exception.");
        }
    }
}

