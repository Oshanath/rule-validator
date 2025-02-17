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

import org.wso2.rule.validator.Constants;
import org.wso2.rule.validator.document.LintTarget;
import org.wso2.rule.validator.functions.FunctionName;
import org.wso2.rule.validator.functions.FunctionResult;
import org.wso2.rule.validator.functions.LintFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Function to check the casing of a string
 */
@FunctionName("casing")
public class CasingFunction extends LintFunction {

    private static final String flat = "[a-z][a-z{__DIGITS__}]*";
    private static final String camel = "[a-z][a-z{__DIGITS__}]*(?:[A-Z{__DIGITS__}](?:[a-z{__DIGITS__}]+|$))*";
    private static final String pascal = "[A-Z][a-z{__DIGITS__}]*(?:[A-Z{__DIGITS__}](?:[a-z{__DIGITS__}]+|$))*";
    private static final String kebab = "[a-z][a-z{__DIGITS__}]*(?:-[a-z{__DIGITS__}]+)*";
    private static final String cobol = "[A-Z][A-Z{__DIGITS__}]*(?:-[A-Z{__DIGITS__}]+)*";
    private static final String snake = "[a-z][a-z{__DIGITS__}]*(?:_[a-z{__DIGITS__}]+)*";
    private static final String macro = "[A-Z][A-Z{__DIGITS__}]*(?:_[A-Z{__DIGITS__}]+)*";

    private static final String digitPattern = "0-9";

    public CasingFunction(Map<String, Object> options) {
        super(options);
    }

    @Override
    public List<String> validateFunctionOptions() {
        List<String> errors = new ArrayList<>();

        if (options == null) {
            errors.add("At least the casing type should be specified in functionOptions in the 'casing' function.");
            return errors;
        }

        // required options
        if (!options.containsKey(Constants.RULESET_CASING_TYPE)) {
            errors.add("The '" + Constants.RULESET_CASING_TYPE + "' option is required for the 'casing' function.");
        }

        // optional options
        for (Map.Entry<String, Object> entry : options.entrySet()) {
            if (entry.getKey().equals(Constants.RULESET_CASING_DISALLOW_DIGITS)) {
                if (!(options.get(entry.getKey()) instanceof Boolean)) {
                    errors.add("The '" + Constants.RULESET_CASING_DISALLOW_DIGITS + "' option should be a boolean.");
                }
            } else if (entry.getKey().equals(Constants.RULESET_CASING_SEPARATOR)) {
                if (!(options.get(entry.getKey()) instanceof Map)) {
                    errors.add("The '" + Constants.RULESET_CASING_SEPARATOR + "' option should be a map.");
                    continue;
                }

                Map<String, Object> separator = (Map<String, Object>) options.get(entry.getKey());

                if (!separator.containsKey(Constants.RULESET_CASING_SEPARATOR_CHAR) && !separator.containsKey(Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING)) {
                    errors.add("The separator object should not be empty if it is defined.");
                }

                if (separator.containsKey(Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING) && !separator.containsKey(Constants.RULESET_CASING_SEPARATOR_CHAR)) {
                    errors.add("Separator char must be present if allowLeading is specified.");
                }

                if (separator.containsKey(Constants.RULESET_CASING_SEPARATOR_CHAR) &&
                        !(separator.get(Constants.RULESET_CASING_SEPARATOR_CHAR) instanceof String)) {
                    errors.add("The '" + Constants.RULESET_CASING_SEPARATOR_CHAR +
                            "' key in the '" + Constants.RULESET_CASING_SEPARATOR + "' option should be a string.");
                }
                if (separator.containsKey(Constants.RULESET_CASING_SEPARATOR_CHAR) && separator.get(Constants.RULESET_CASING_SEPARATOR_CHAR) == null) {
                    errors.add("Separator char should not be null");
                    return errors;
                }
                if (separator.containsKey(Constants.RULESET_CASING_SEPARATOR_CHAR) && ((String)separator.get(Constants.RULESET_CASING_SEPARATOR_CHAR)).length() > 1) {
                    errors.add("Separator char is not a single character.");
                }
                if (separator.containsKey(Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING) &&
                        !(separator.get(Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING) instanceof Boolean)) {
                    errors.add("The '" + Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING +
                            "' key in the '" + Constants.RULESET_CASING_SEPARATOR + "' option should be a boolean.");
                }

                for (String separatorKey : separator.keySet()) {
                    if (!separatorKey.equals(Constants.RULESET_CASING_SEPARATOR_CHAR) && !separatorKey.equals(Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING)) {
                        errors.add("Invalid option in separator object: " + separatorKey);
                    }
                }
            } else if (!entry.getKey().equals(Constants.RULESET_CASING_TYPE) && !entry.getKey().equals(Constants.RULESET_CASING_DISALLOW_DIGITS)){
                errors.add("Invalid function option for the casing function: " + entry.getKey());
            }
        }

        return errors;
    }

    public FunctionResult executeFunction(LintTarget target) {
        if (!(target.value instanceof String)) {
            // This passes according to spec and tests
            return new FunctionResult(true, null);
        }

        String targetString = (String) target.value;
        if (targetString.isEmpty()) {
            // This passes according to spec and tests
            return new FunctionResult(true, null);
        }

        boolean allowLeading = Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING_DEFAULT;
        String separatorChar = "";
        if (options.containsKey(Constants.RULESET_CASING_SEPARATOR)) {
            Map<String, Object> separator = (Map<String, Object>) options.get(Constants.RULESET_CASING_SEPARATOR);
            if (separator.containsKey(Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING)) {
                allowLeading = (boolean) separator.get(Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING);
            }
            if (separator.containsKey(Constants.RULESET_CASING_SEPARATOR_CHAR)) {
                separatorChar = separator.get(Constants.RULESET_CASING_SEPARATOR_CHAR).toString();
            }
        }

        if (targetString.length() == 1 && options.containsKey(Constants.RULESET_CASING_SEPARATOR) && allowLeading &&
                targetString.equals(separatorChar)) {
            return new FunctionResult(true, null);
        }

        if (targetString.matches(getPattern(options))) {
            return new FunctionResult(true, null);
        } else {
            return new FunctionResult(false, target.getTargetName() + " does not match the specified casing pattern.");
        }
    }

    private String getPattern(Map<String, Object> options) {
        String baseCase = (String) options.get(Constants.RULESET_CASING_TYPE);

        boolean allowdigits = true;
        if (options.containsKey(Constants.RULESET_CASING_DISALLOW_DIGITS)) {
            allowdigits = !(boolean) options.get(Constants.RULESET_CASING_DISALLOW_DIGITS);
        }

        String basePattern;

        if (baseCase.equals("flat")) {
            basePattern = flat;
        } else if (baseCase.equals("camel")) {
            basePattern = camel;
        } else if (baseCase.equals("pascal")) {
            basePattern = pascal;
        } else if (baseCase.equals("kebab")) {
            basePattern = kebab;
        } else if (baseCase.equals("cobol")) {
            basePattern = cobol;
        } else if (baseCase.equals("snake")) {
            basePattern = snake;
        } else if (baseCase.equals("macro")) {
            basePattern = macro;
        } else {
            basePattern = flat;
        }

        String pattern = basePattern.replace("{__DIGITS__}", allowdigits ? digitPattern : "");

        if (!options.containsKey(Constants.RULESET_CASING_SEPARATOR)) {
            return "^" + pattern + Constants.JSON_PATH_ROOT;
        }

        Map<String, Object> separator = (Map<String, Object>) options.get(Constants.RULESET_CASING_SEPARATOR);
        String separatorChar = null;
        if (separator.containsKey(Constants.RULESET_CASING_SEPARATOR_CHAR)) {
            separatorChar = separator.get(Constants.RULESET_CASING_SEPARATOR_CHAR).toString();
        }
        boolean allowLeading = false;
        if (separator.containsKey(Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING)) {
            allowLeading = (boolean) separator.get(Constants.RULESET_CASING_SEPARATOR_ALLOW_LEADING);
        }

        String separatorPattern = separatorChar != null ? "[" + Pattern.quote(separatorChar) + "]" : "";
        String leadingSeparatorPattern = allowLeading ? separatorPattern + "?" : "";

        return "^" + leadingSeparatorPattern + pattern + "(?:" + separatorPattern + pattern + ")*$";
    }
}
