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

package org.wso2.rule.validator.ruleset;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.wso2.rule.validator.InvalidContentTypeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wso2.rule.validator.validator.Validator.validateRuleset;

/**
 * Ruleset test
 * should test following:
 * 1. Both Yaml and json rulesets
 * 2. Nested aliases
 * 3. With formats
 * 4. Invalid aliases
 */

public class RulesetTest {
    @Test
    public void validJsonOrYamlAliases() {
        Path jsonRulesetPath = Paths.get("src/test/resources/rulesets/valid-json.ruleset");
        Path yamlRulesetPath = Paths.get("src/test/resources/rulesets/valid-yaml.ruleset");
        try {
            String jsonRulesetContent = Files.readString(jsonRulesetPath);
            String yamlRulesetContent = Files.readString(yamlRulesetPath);
            Map<String, Object> jsonRuleset = JsonPath.parse(jsonRulesetContent).json();
            Map<String, Object> yamlRuleset = (Map<String, Object>) (new Load(
                    LoadSettings.builder().build())).loadFromString(yamlRulesetContent);

            Ruleset json = new Ruleset(jsonRuleset);
            Ruleset yaml = new Ruleset(yamlRuleset);

            if (!json.isInitialized()) {
                throw new RuntimeException("Ruleset initialization failed: " + json.getInitializationErrorMessage());
            }
            if (!yaml.isInitialized()) {
                throw new RuntimeException("Ruleset initialization failed: " + yaml.getInitializationErrorMessage());
            }

            assertTrue(json.aliases.containsKey("HeaderNames") && Objects.equals(
                    json.aliases.get("HeaderNames").getGiven().get(0), "$..parameters.[?(@.in === 'header')].name"));
            assertTrue(json.aliases.containsKey("Info") && Objects.equals(json.aliases.get("Info").getGiven().get(0),
                    "$..info"));
            assertTrue(json.aliases.containsKey("Paths") && Objects.equals(json.aliases.get("Paths").getGiven().get(0),
                    "$.paths[*]~"));

            assertTrue(yaml.aliases.containsKey("HeaderNames") && Objects.equals(
                    yaml.aliases.get("HeaderNames").getGiven().get(0), "$..parameters.[?(@.in === 'header')].name"));
            assertTrue(yaml.aliases.containsKey("Info") && Objects.equals(yaml.aliases.get("Info").getGiven().get(0),
                    "$..info"));
            assertTrue(yaml.aliases.containsKey("Paths") && Objects.equals(yaml.aliases.get("Paths").getGiven().get(0),
                    "$.paths[*]~"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void validJsonOrYamlNestedAliases() {
        Path jsonRulesetPath = Paths.get("src/test/resources/rulesets/valid-json-nested.ruleset");
        Path yamlRulesetPath = Paths.get("src/test/resources/rulesets/valid-yaml-nested.ruleset");
        try {
            String jsonRulesetContent = Files.readString(jsonRulesetPath);
            String yamlRulesetContent = Files.readString(yamlRulesetPath);
            Map<String, Object> jsonRuleset = JsonPath.parse(jsonRulesetContent).json();
            Map<String, Object> yamlRuleset = (Map<String, Object>) (new Load(
                    LoadSettings.builder().build())).loadFromString(yamlRulesetContent);

            Ruleset json = new Ruleset(jsonRuleset);
            Ruleset yaml = new Ruleset(yamlRuleset);

            if (!json.isInitialized()) {
                throw new RuntimeException("Ruleset initialization failed: " + json.getInitializationErrorMessage());
            }
            if (!yaml.isInitialized()) {
                throw new RuntimeException("Ruleset initialization failed: " + yaml.getInitializationErrorMessage());
            }

            assertTrue(json.aliases.containsKey("InfoDescription") && Objects.equals(
                    json.aliases.get("InfoDescription").getGiven().get(0), "$..info.description"));
            assertTrue(json.aliases.containsKey("InfoContact") && Objects.equals(
                    json.aliases.get("InfoContact").getGiven().get(0), "$..info.contact"));

            assertTrue(yaml.aliases.containsKey("InfoDescription") && Objects.equals(
                    yaml.aliases.get("InfoDescription").getGiven().get(0), "$..info.description"));
            assertTrue(yaml.aliases.containsKey("InfoContact") && Objects.equals(
                    yaml.aliases.get("InfoContact").getGiven().get(0), "$..info.contact"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void validJsonOrYamlAliasesWithFormats() {
        Path jsonRulesetPath = Paths.get("src/test/resources/rulesets/valid-json-with-formats.ruleset");
        Path yamlRulesetPath = Paths.get("src/test/resources/rulesets/valid-yaml-with-formats.ruleset");
        try {
            String jsonRulesetContent = Files.readString(jsonRulesetPath);
            String yamlRulesetContent = Files.readString(yamlRulesetPath);
            Map<String, Object> jsonRuleset = JsonPath.parse(jsonRulesetContent).json();
            Map<String, Object> yamlRuleset = (Map<String, Object>) (new Load(
                    LoadSettings.builder().build())).loadFromString(yamlRulesetContent);

            Ruleset json = new Ruleset(jsonRuleset);
            Ruleset yaml = new Ruleset(yamlRuleset);

            if (!json.isInitialized()) {
                throw new RuntimeException("Ruleset initialization failed: " + json.getInitializationErrorMessage());
            }
            if (!yaml.isInitialized()) {
                throw new RuntimeException("Ruleset initialization failed: " + yaml.getInitializationErrorMessage());
            }

            List<Object> expectedGivenOAS = List.of(
                    "$.paths[*].parameters[*]",
                    "$.paths[*][get,put,post,delete,options,head,patch,trace].parameters[*]",
                    "$.parameters[*]"
            );

            assertTrue(
                    Objects.equals(json.aliases.get("ParameterObject").targets.get(0).formats.get(0).name(), "OAS2"));
            assertEquals(expectedGivenOAS, json.aliases.get("ParameterObject").targets.get(0).given);

            assertTrue(
                    Objects.equals(yaml.aliases.get("ParameterObject").targets.get(0).formats.get(0).name(), "OAS2"));
            assertEquals(expectedGivenOAS, yaml.aliases.get("ParameterObject").targets.get(0).given);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void invalidCircularAliases() {
        Path jsonRulesetPath = Paths.get("src/test/resources/rulesets/invalid-circular-json.ruleset");
        Path yamlRulesetPath = Paths.get("src/test/resources/rulesets/invalid-circular-yaml.ruleset");
        try {
            String jsonRulesetContent = Files.readString(jsonRulesetPath);
            String yamlRulesetContent = Files.readString(yamlRulesetPath);

            String validationResultJson = validateRuleset(jsonRulesetContent);
            String validationResultYaml = validateRuleset(yamlRulesetContent);

            assertTrue(validationResultJson.contains("Circular alias dependency detected."));
            assertTrue(validationResultYaml.contains("Circular alias dependency detected."));
        } catch (IOException | InvalidContentTypeException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void invalidUndefinedAliases() {
        Path jsonRulesetPath = Paths.get("src/test/resources/rulesets/invalid-undefined-json.ruleset");
        Path yamlRulesetPath = Paths.get("src/test/resources/rulesets/invalid-undefined-yaml.ruleset");
        try {
            String jsonRulesetContent = Files.readString(jsonRulesetPath);
            String yamlRulesetContent = Files.readString(yamlRulesetPath);

            String validationResultJson = validateRuleset(jsonRulesetContent);
            String validationResultYaml = validateRuleset(yamlRulesetContent);

            assertTrue(validationResultJson.contains("Alias #Name not found"));
            assertTrue(validationResultYaml.contains("Alias #Name not found"));
        } catch (IOException | InvalidContentTypeException e) {
            throw new RuntimeException(e);
        }
    }
}
