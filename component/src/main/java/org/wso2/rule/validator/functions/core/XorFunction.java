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
 * Xor function implementation
 */
@FunctionName("xor")
public class XorFunction extends LintFunction {

    public XorFunction(Map<String, Object> options) {
        super(options);
    }

    @Override
    public List<String> validateFunctionOptions() {
        List<String> errors = new ArrayList<>();

        if (options == null) {
            errors.add("Xor function requires the list of properties.");
            return errors;
        }

        if (!options.containsKey(Constants.RULESET_XOR_PROPERTIES)) {
            errors.add("Xor function requires the list of properties.");
            return errors;
        }

        if (!(options.get(Constants.RULESET_XOR_PROPERTIES) instanceof List)) {
            errors.add("Xor function requires the list of properties.");
            return errors;
        }

        List<Object> properties = (List<Object>) options.get(Constants.RULESET_XOR_PROPERTIES);
        for (Object property : properties) {
            if (!(property instanceof String)) {
                errors.add("Xor function properties requires a list of Strings.");
                return errors;
            }
        }

        if (properties.size() <= 1) {
            errors.add("Xor functions requires more than 1 property.");
            return errors;
        }

        for (String key : options.keySet()) {
            if (!key.equals(Constants.RULESET_XOR_PROPERTIES)) {
                errors.add("Unknown option for the Xor function: " + key);
            }
        }

        return errors;
    }

    public FunctionResult executeFunction(LintTarget target) {
        List<String> properties = (List<String>) options.get(Constants.RULESET_XOR_PROPERTIES);
        int count = 0;
        for (String property : properties) {
            if (target.value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) target.value;
                if (map.containsKey(property)) {
                    count++;
                }
            } else {
                return new FunctionResult(true, "Xor function can only be applied to objects");
            }
        }

        if (count == 1) {
            return new FunctionResult(true, null);
        } else {
            return new FunctionResult(false, "Only one of the properties " + properties + " should be present");
        }
    }
}
