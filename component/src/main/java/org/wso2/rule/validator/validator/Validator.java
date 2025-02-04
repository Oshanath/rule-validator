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

package org.wso2.rule.validator.validator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.JsonPath;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.wso2.rule.validator.InvalidRulesetException;
import org.wso2.rule.validator.document.Document;
import org.wso2.rule.validator.ruleset.Ruleset;
import org.wso2.rule.validator.ruleset.RulesetType;
import org.wso2.rule.validator.ruleset.file.type.JsonRuleset;
import org.wso2.rule.validator.ruleset.file.type.YamlRuleset;

import java.io.IOException;
import java.util.List;

/**
 * Validator class to validate documents and rulesets.
 */
public class Validator {
    public static String validateDocument(String documentFile, String rulesetFile) throws InvalidRulesetException {

        List<RulesetValidationError> errors = getRulesetValidationErrors(rulesetFile);
        if (!errors.isEmpty()) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            throw new InvalidRulesetException(gson.toJson(errors));
        }

        RulesetType type = findRulesetType(rulesetFile);
        Ruleset ruleset;

        if (type == RulesetType.YAML) {
            ruleset = new YamlRuleset(rulesetFile);
        } else {
            ruleset = new JsonRuleset(rulesetFile);
        }

        Document document = new Document(documentFile);
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        return gson.toJson(document.lint(ruleset));
    }

    private static List<RulesetValidationError> getRulesetValidationErrors(String rulesetString) {
        RulesetType type = findRulesetType(rulesetString);
        List<RulesetValidationError> errors;
        if (type == RulesetType.YAML) {
            errors = YamlRulesetValidator.validateRuleset(rulesetString);
        } else {
            errors = JsonRulesetValidator.validateRuleset(rulesetString);
        }
        return errors;
    }

    public static String validateRuleset(String rulesetString) throws IOException {
        List<RulesetValidationError> errors = getRulesetValidationErrors(rulesetString);
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        return gson.toJson(errors);
    }

    private static RulesetType findRulesetType(String ruleset) {
        try {
            Load settings = new Load(LoadSettings.builder().build());
            settings.loadFromString(ruleset);
            return RulesetType.YAML;
        } catch (Exception e) {
            try {
                JsonPath.parse(ruleset);
                return RulesetType.JSON;
            } catch (Exception e1) {
                return RulesetType.INVALID;
            }
        }
    }
}
