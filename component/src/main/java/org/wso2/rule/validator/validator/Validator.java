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
import org.wso2.rule.validator.InvalidContentTypeException;
import org.wso2.rule.validator.InvalidRulesetException;
import org.wso2.rule.validator.document.Document;
import org.wso2.rule.validator.functions.LintResult;
import org.wso2.rule.validator.ruleset.Ruleset;
import org.wso2.rule.validator.ruleset.RulesetType;
import org.wso2.rule.validator.ruleset.file.type.JsonRuleset;
import org.wso2.rule.validator.ruleset.file.type.YamlRuleset;
import org.wso2.rule.validator.utils.Util;
import org.wso2.rule.validator.validator.ruleset.RulesetValidationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator class to validate documents and rulesets.
 */
public class Validator {
    public static String validateDocument(String documentFile, String rulesetFile)
            throws InvalidRulesetException, InvalidContentTypeException {

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

        if (!ruleset.isInitialized()) {
            throw new InvalidRulesetException(ruleset.getInitializationErrorMessage());
        }

        Document document = new Document(documentFile);
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        List<DocumentValidationResult> results = new ArrayList<>();
        if (!document.isNull()) {
            List<LintResult> lintResults = document.lint(ruleset);
            for (LintResult lintResult : lintResults) {
                if (lintResult.passed) {
                    continue;
                }
                results.add(new DocumentValidationResult(lintResult.path, lintResult.message,
                        lintResult.rule.name, lintResult.rule.severity));
            }
        } else {
            throw new InvalidContentTypeException("Document is empty.");
        }
        return gson.toJson(results);
    }

    private static List<RulesetValidationError> getRulesetValidationErrors(String rulesetString)
            throws InvalidContentTypeException {
        RulesetType type = findRulesetType(rulesetString);
        List<RulesetValidationError> errors;
        if (type == RulesetType.YAML) {
            errors = YamlRulesetValidator.validateRuleset(rulesetString);
        } else {
            errors = JsonRulesetValidator.validateRuleset(rulesetString);
        }
        return errors;
    }

    public static String validateRuleset(String rulesetString) throws InvalidContentTypeException {
        List<RulesetValidationError> errors = getRulesetValidationErrors(rulesetString);
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        // Create a string with all the error strings
        StringBuilder errorString = new StringBuilder();
        for (RulesetValidationError error : errors) {
            errorString.append(error.toString()).append("\n");
        }
        RulesetValidationResult result = new RulesetValidationResult(errors.isEmpty(), errorString.toString());
        return gson.toJson(result);
    }

    private static RulesetType findRulesetType(String ruleset) throws InvalidContentTypeException {
        try {
            String trimmedRuleset = ruleset.trim();
            if (trimmedRuleset.startsWith("{") || trimmedRuleset.startsWith("[")) {
                JsonPath.parse(ruleset);
                return RulesetType.JSON;
            } else {
                Util.loadYaml(ruleset);
                return RulesetType.YAML;
            }
        } catch (Exception e) {
            throw new InvalidContentTypeException(e.getMessage());
        }
    }
}
