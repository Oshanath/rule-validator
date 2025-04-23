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

package org.wso2.rule.validator.validator.ruleset;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import org.wso2.rule.validator.Constants;
import org.wso2.rule.validator.InvalidRulesetException;
import org.wso2.rule.validator.functions.FunctionFactory;
import org.wso2.rule.validator.functions.InvalidCoreFunctionException;
import org.wso2.rule.validator.functions.LintFunction;
import org.wso2.rule.validator.ruleset.Format;
import org.wso2.rule.validator.ruleset.RulesetAliasDefinition;
import org.wso2.rule.validator.ruleset.RulesetAliasTarget;
import org.wso2.rule.validator.validator.RulesetValidationError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements the actual ruleset validation logic
 */
public abstract class RulesetValidator {
    protected static List<RulesetValidationError> validate(Map<String, Object> ruleset) {

        List<RulesetValidationError> errors = new ArrayList<>();

        if (ruleset == null) {
            errors.add(new RulesetValidationError("", "Ruleset is empty."));
            return errors;
        }

        // Validate aliases
        if (ruleset.containsKey("aliases")) {
            if (!(ruleset.get("aliases") instanceof Map)) {
                errors.add(new RulesetValidationError("", "Aliases object should be a map"));
            } else {
                Map<String, Object> aliases = (Map<String, Object>) ruleset.get("aliases");
                errors.addAll(validateAliases(aliases));
            }
        }

        // Validate rules
        if (!ruleset.containsKey(Constants.RULESET_RULES)) {
            errors.add(new RulesetValidationError("", "Ruleset does not contain any rules."));
            return errors;
        }

        Object ruleMap = ruleset.get(Constants.RULESET_RULES);
        if (ruleMap == null) {
            errors.add(new RulesetValidationError("", "Ruleset contains a null value for rules."));
            return errors;
        } else if (!(ruleMap instanceof Map)) {
            errors.add(new RulesetValidationError("", "Ruleset contains an invalid value for rules."));
            return errors;
        } else {
            errors.addAll(validateRules((Map<String, Object>) ruleMap, ruleset.get(Constants.RULESET_ALIASES)));
        }

        // Validate extends
        if (ruleset.containsKey(Constants.RULESET_EXTENDS)) {
            if (!(ruleset.get(Constants.RULESET_EXTENDS) instanceof List) ||
                    !(ruleset.get(Constants.RULESET_EXTENDS) instanceof String)) {
                errors.add(new RulesetValidationError("",
                        "'extends' field of a ruleset should be a list or a string."));
            } else if (ruleset.get(Constants.RULESET_EXTENDS) instanceof List) {
                List<Object> extendsList = (List<Object>) ruleset.get(Constants.RULESET_EXTENDS);
                for (int i = 0; i < extendsList.size(); i++) {
                    if (!(extendsList.get(i) instanceof String)) {
                        errors.add(new RulesetValidationError("",
                                "Invalid '" + Constants.RULESET_EXTENDS + "' object in index " + i));
                    }
                }
            }
        }

        // Validate Formats
        errors.addAll(validateFormats("", ruleset));

        return errors;
    }

    private static List<RulesetValidationError> validateAliases(Map<String, Object> aliasMap) {

        List<RulesetValidationError> errors = new ArrayList<>();
        Map<String, RulesetAliasDefinition> aliases = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : aliasMap.entrySet()) {
            String aliasName = entry.getKey();
            Object alias = entry.getValue();
            if (!(alias instanceof List) && !(alias instanceof Map)) {
                errors.add(new RulesetValidationError(aliasName, "Invalid alias object."));
                return errors;
            }

            aliases.put(aliasName, new RulesetAliasDefinition(aliasName, alias));
        }

        // Check alias resolution and circular dependencies
        try {
            RulesetAliasDefinition.resolveAliasesInAliases(aliases);
        } catch (InvalidRulesetException e) {
            errors.add(new RulesetValidationError("", e.getMessage()));
            return errors;
        }
        if (!RulesetAliasDefinition.allAliasesResolved(aliases)) {
            errors.add(new RulesetValidationError("", "Circular alias dependency detected."));
            return errors;
        }

        // Check all resolved given for unsupported json paths
        for (Map.Entry<String, RulesetAliasDefinition> entry : aliases.entrySet()) {
            for (RulesetAliasTarget target : entry.getValue().targets) {
                for (String given : target.given) {
                    if (!validateJsonPath(given)) {
                        errors.add(new RulesetValidationError(entry.getKey(), "Invalid json path in resolved alias"));
                    }
                }
            }

            for (String given : entry.getValue().getGiven()) {
                if (!validateJsonPath(given)) {
                    errors.add(new RulesetValidationError(entry.getKey(), "Invalid json path in resolved alias"));
                }
            }
        }

        return errors;
    }

    private static List<RulesetValidationError> validateRules(Map<String, Object> rules, Object aliases) {
        List<RulesetValidationError> errors = new ArrayList<>();

        for (Map.Entry<String, Object> entry : rules.entrySet()) {
            String key = entry.getKey();
            Object ruleObject = rules.get(key);
            if (!(ruleObject instanceof Map)) {
                errors.add(new RulesetValidationError(key, "Rule is not a valid object."));
                continue;
            }
            Map<String, Object> rule = (Map<String, Object>) ruleObject;

            // Validate given
            errors.addAll(validateGiven(key, rule, aliases));

            // Validate then
            errors.addAll(validateThen(key, rule));

            // Validate description
            if (rule.containsKey(Constants.DESCRIPTION) && !(rule.get(Constants.DESCRIPTION) instanceof String)) {
                errors.add(new RulesetValidationError(key, "'" + Constants.DESCRIPTION +
                        "' field of a rule should be a string"));
            }
            // Validate message
            if (rule.containsKey(Constants.RULESET_MESSAGE) &&
                    !(rule.get(Constants.RULESET_MESSAGE) instanceof String)) {
                errors.add(new RulesetValidationError(key, "'" + Constants.RULESET_MESSAGE +
                        "' field of a rule should be a string"));
            }
            if (rule.get(Constants.RULESET_MESSAGE) instanceof String) {
                String message = (String) rule.get(Constants.RULESET_MESSAGE);
                if (rule.containsKey(Constants.RULESET_MESSAGE) && message.contains("{{")) {
                    String regex = "\\{\\{.*?}}";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(message);
                    while (matcher.find()) {
                        String match = matcher.group();
                        String placeholder = match.substring(2, match.length() - 2);
                        List<String> placeholders = Arrays.asList("error", "description", "path", "property", "value");
                        if (!placeholders.contains(placeholder)) {
                            errors.add(new RulesetValidationError(key, "Invalid placeholder: " + match));
                        }
                    }
                }
            }

            // Validate severity
            if (rule.containsKey(Constants.RULESET_SEVERITY) &&
                    !(rule.get(Constants.RULESET_SEVERITY) instanceof String)) {
                errors.add(new RulesetValidationError(key, "'severity' field of a rule should be a string"));
            } else if (rule.containsKey(Constants.RULESET_SEVERITY)) {
                String severity = (String) rule.get(Constants.RULESET_SEVERITY);
                List<String> severities = Arrays.asList("error", "warn", "info", "hint", "off");
                if (!severities.contains(severity)) {
                    errors.add(new RulesetValidationError(key, "Invalid severity: " + severity));
                }
            }

            // Validate formats
            errors.addAll(validateFormats(key, rule));

            // Validate recommended
            if (rule.containsKey(Constants.RULESET_RECOMMENDED) &&
                    !(rule.get(Constants.RULESET_RECOMMENDED) instanceof Boolean)) {
                errors.add(new RulesetValidationError(key, "'recommended' field of a rule should be a boolean"));
            }

            // Validate resolved
            if (rule.containsKey(Constants.RULESET_RESOLVED) &&
                    !(rule.get(Constants.RULESET_RESOLVED) instanceof Boolean)) {
                errors.add(new RulesetValidationError(key, "'resolved' field of a rule should be a boolean"));
            }
        }

        return errors;
    }

    private static List<RulesetValidationError> validateFormats(String ruleName, Map<String, Object> object) {
        List<RulesetValidationError> errors = new ArrayList<>();

        if (object.containsKey(Constants.RULESET_FORMATS) && !(object.get(Constants.RULESET_FORMATS) instanceof List)) {
            errors.add(new RulesetValidationError(ruleName, "'formats' field of a rule should be a list"));
            return errors;
        }
        if (object.containsKey(Constants.RULESET_FORMATS)) {
            List<Object> formatObjects = (List<Object>) object.get(Constants.RULESET_FORMATS);
            for (int i = 0; i < formatObjects.size(); i++) {
                if (!(formatObjects.get(i) instanceof String)) {
                    errors.add(new RulesetValidationError(ruleName, "Invalid format in index " + i));
                    continue;
                }
                Format format = Format.getFormat((String) formatObjects.get(i));
                if (format == null) {
                    errors.add(new RulesetValidationError(ruleName, "Unknown format: " + formatObjects.get(i)));
                }
            }
        }

        return errors;
    }

    private static List<RulesetValidationError> validateThen (String ruleName, Map<String, Object> rule) {
        List<RulesetValidationError> errors = new ArrayList<>();

        if (!rule.containsKey(Constants.RULESET_THEN)) {
            errors.add(new RulesetValidationError(ruleName, "Rule does not contain a 'then' field."));
        } else if (!(rule.get(Constants.RULESET_THEN) instanceof List) &&
                !(rule.get(Constants.RULESET_THEN) instanceof Map)) {
            errors.add(new RulesetValidationError(ruleName,
                    "'then' field of a rule should be an object or a list"));
        } else {
            if (rule.get(Constants.RULESET_THEN) instanceof List) {
                List<Object> thenList = (List<Object>) rule.get(Constants.RULESET_THEN);
                for (int i = 0; i < thenList.size(); i++) {
                    if (thenList.get(i) instanceof Map) {
                        errors.addAll(validateThenObject(ruleName, (Map<String, Object>) thenList.get(i)));
                    } else {
                        errors.add(new RulesetValidationError(ruleName, "Invalid 'then' object in index " + i));
                    }
                }
            } else {
                if (!(rule.get(Constants.RULESET_THEN) instanceof Map)) {
                    errors.add(new RulesetValidationError(ruleName, "Invalid 'then' object"));
                } else {
                    errors.addAll(validateThenObject(ruleName, (Map<String, Object>) rule.get(Constants.RULESET_THEN)));
                }
            }
        }

        return errors;
    }

    private static List<RulesetValidationError> validateThenObject(String ruleName, Map<String, Object> then) {
        List<RulesetValidationError> errors = new ArrayList<>();

        // Field can be undefined. If it is defined, it should be a string
        if (then.containsKey(Constants.RULESET_FIELD) && !(then.get(Constants.RULESET_FIELD) instanceof String)) {
            errors.add(new RulesetValidationError(ruleName, "'field' field of a then object should be a string"));
        }

        if (!then.containsKey(Constants.RULESET_FUNCTION)) {
            errors.add(new RulesetValidationError(ruleName, "Then object does not contain a 'function' field."));
            return errors;
        } else if (!(then.get(Constants.RULESET_FUNCTION) instanceof String)) {
            errors.add(new RulesetValidationError(ruleName, "'function' field of a then object should be a string"));
            return errors;
        } else {
            String function = (String) then.get(Constants.RULESET_FUNCTION);
            if (!FunctionFactory.isFunction(function)) {
                errors.add(new RulesetValidationError(ruleName, "Unknown function: " + function));
                return errors;
            }
        }

        // Match each function with the function options
        String function = (String) then.get(Constants.RULESET_FUNCTION);
        Map<String, Object> functionOptions = null;
        if (then.containsKey(Constants.RULESET_FUNCTION_OPTIONS)) {
            if (then.get(Constants.RULESET_FUNCTION_OPTIONS) instanceof Map) {
                functionOptions = (Map<String, Object>) then.get(Constants.RULESET_FUNCTION_OPTIONS);
            } else {
                errors.add(new RulesetValidationError(ruleName,
                        "'functionOptions' field of a then object should be an object"));
            }
        }
        LintFunction lintFunction;
        try {
            lintFunction = FunctionFactory.getFunction(function, functionOptions);
        } catch (InvalidCoreFunctionException e) {
            errors.add(new RulesetValidationError(ruleName, e.getMessage()));
            return errors;
        }
        List<String> functionErrors = lintFunction.validateFunctionOptions();
        for (String error : functionErrors) {
            errors.add(new RulesetValidationError(ruleName, error));
        }

        return errors;
    }

    private static List<RulesetValidationError> validateGiven (String ruleName, Map<String, Object> rule,
                                                               Object aliases) {

        List<RulesetValidationError> errors = new ArrayList<>();

        if (!rule.containsKey(Constants.RULESET_GIVEN)) {
            errors.add(new RulesetValidationError(ruleName, "Rule does not contain a 'given' field."));
        } else if (!(rule.get(Constants.RULESET_GIVEN) instanceof List) &&
                !(rule.get(Constants.RULESET_GIVEN) instanceof String)) {
            errors.add(new RulesetValidationError(ruleName, "'given' field of a rule should be a string or a list"));
        } else {
            // We only need to check whether the given alias exists because the alias itself has been validated prior.
            if (rule.get(Constants.RULESET_GIVEN) instanceof List) {
                List<String> givenList = (List<String>) rule.get(Constants.RULESET_GIVEN);
                for (String given : givenList) {
                    errors.addAll(validateGiven(ruleName, given, aliases));
                }
            } else {
                String given = (String) rule.get(Constants.RULESET_GIVEN);
                errors.addAll(validateGiven(ruleName, given, aliases));
            }
        }

        return errors;
    }

    private static List<RulesetValidationError> validateGiven(String ruleName, String given, Object aliases) {
        List<RulesetValidationError> errors = new ArrayList<>();

        if (given.startsWith(Constants.ALIAS_PREFIX)) {
            if (aliases == null) {
                errors.add(new RulesetValidationError(ruleName, "Rule uses an alias but no aliases are defined."));
            } else {
                if (!(aliases instanceof Map)) {
                    errors.add(new RulesetValidationError(ruleName, "Invalid aliases object."));
                } else {
                    Map<String, Object> aliasMap = (Map<String, Object>) aliases;
                    if (!aliasMap.containsKey(given.substring(1))) {
                        errors.add(new RulesetValidationError(ruleName, "Unknown alias: " + given.substring(1)));
                    }
                }
            }
        } else if (!validateJsonPath(given)) {
            errors.add(new RulesetValidationError(ruleName, "Invalid JSON path: " + given));
        }

        return errors;
    }

    private static boolean validateJsonPath(String jsonPath) {
        try {
            if (!jsonPath.startsWith(Constants.JSON_PATH_ROOT)) {
                return false;
            }

            JsonPath.compile(jsonPath);
            return true;
        } catch (InvalidPathException e) {
            return false;
        }
    }
}
