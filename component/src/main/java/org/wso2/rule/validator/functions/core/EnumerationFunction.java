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

/**
 * Enumeration function to check whether a value is contained in a set of values
 */
@FunctionName("enumeration")
public class EnumerationFunction extends LintFunction {

    public EnumerationFunction(Map<String, Object> options) {
        super(options);
    }

    @Override
    public List<String> validateFunctionOptions() {
        List<String> errors = new ArrayList<>();

        if (options == null) {
            errors.add("Enumeration function requires the set of values.");
            return errors;
        }

        if (!options.containsKey(Constants.RULESET_ENUMERATION_VALUES)) {
            errors.add("Enumeration function requires the set of values.");
            return errors;
        }

        if (!(options.get(Constants.RULESET_ENUMERATION_VALUES) instanceof List)) {
            errors.add("Enumeration function values options needs to be a list");
            return errors;
        }

        List<Object> values = (List<Object>) options.get(Constants.RULESET_ENUMERATION_VALUES);
        if (values.size() == 0) {
            errors.add("Enumeration function values options cannot be empty");
            return errors;
        }

        for (String option : options.keySet()) {
            if (!option.equals(Constants.RULESET_ENUMERATION_VALUES)) {
                errors.add("Enumeration function does not support option: " + option);
            }
        }

        return errors;
    }

    public FunctionResult executeFunction(LintTarget target) {
        List<String> values = (List<String>) options.get(Constants.RULESET_ENUMERATION_VALUES);

        if (!(target.value instanceof Integer) && !(target.value instanceof String)) {
            return new FunctionResult(true, null);
        }

        for (String value : values) {
            if (target.value.equals(value)) {
                return new FunctionResult(true, null);
            }
        }
        return new FunctionResult(false, "Value '" + target.value + "' is not in the enumeration.");
    }
}
