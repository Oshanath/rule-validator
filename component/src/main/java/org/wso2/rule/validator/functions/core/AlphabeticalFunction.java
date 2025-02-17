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
 * Function to check if the given string is alphabetical
 */
@FunctionName("alphabetical")
public class AlphabeticalFunction extends LintFunction {

    public AlphabeticalFunction(Map<String, Object> options) {
        super(options);
    }

    @Override
    public List<String> validateFunctionOptions() {

        List<String> errors = new ArrayList<>();

        if (options == null) {
            return errors;
        }

        for (Map.Entry<String, Object> entry : options.entrySet()) {
            if (entry.getKey().equals(Constants.RULESET_ALPHABETICAL_KEYED_BY)) {
                if (!(options.get(entry.getKey()) instanceof String)) {
                    errors.add("The value of '" + Constants.RULESET_ALPHABETICAL_KEYED_BY + "' should be a string");
                }
            } else {
                errors.add("Unknown option '" + entry.getKey() + "' for alphabetical function.");
            }
        }

        return errors;

    }

    public FunctionResult executeFunction(LintTarget target) {

        Object value = target.value;

        if (!(value instanceof List) && !(value instanceof Map)) {
            // According to spec and tests
            return new FunctionResult(true, null);
        }
        if (value instanceof Map) {
            if (isAlphabetical((Map) value)) {
                return new FunctionResult(true, null);
            } else {
                return new FunctionResult(false, target.getTargetName() + " is not alphabetical");
            }
        }

        List<Object> list = (List) value;

        if (options != null && options.containsKey(Constants.RULESET_ALPHABETICAL_KEYED_BY)) {
            for (Object element : list) {
                if (!(element instanceof Map)) {
                    return new FunctionResult(false, target.getTargetName() + " Value is not a list of maps");
                }
                Map<String, Object> map = (Map) element;
                if (!map.containsKey(options.get(Constants.RULESET_ALPHABETICAL_KEYED_BY))) {
                    return new FunctionResult(false, target.getTargetName() + " Map does not contain key " +
                            options.get(Constants.RULESET_ALPHABETICAL_KEYED_BY));
                }
                Object valueToCheck = map.get(options.get(Constants.RULESET_ALPHABETICAL_KEYED_BY));
                if (!(valueToCheck instanceof String) && !(valueToCheck instanceof Integer) &&
                        !(valueToCheck instanceof Double)) {
                    return new FunctionResult(false, target.getTargetName() + " Value is not a list of strings");
                }
            }
            if (isAlphabetical(list, (String) options.get(Constants.RULESET_ALPHABETICAL_KEYED_BY))) {
                return new FunctionResult(true, null);
            } else {
                return new FunctionResult(false, target.getTargetName() + " is not alphabetical");
            }
        } else {
            for (Object element : list) {
                if (!(element instanceof String) && !(element instanceof Integer) && !(element instanceof Double)) {
                    return new FunctionResult(false, target.getTargetName() + " Value is not a list of strings");
                }
            }
            if (isAlphabetical(list)) {
                return new FunctionResult(true, null);
            } else {
                return new FunctionResult(false, target.getTargetName() + " is not alphabetical");
            }
        }
    }

    private boolean isAlphabetical(List<Object> objectList, String key) {
        List<Object> list = new ArrayList<>();
        for (Object obj : objectList) {
            Map<String, Object> map = (Map) obj;
            list.add(map.get(key));
        }
        return isAlphabetical(list);
    }

    private boolean isAlphabetical(Map<String, Object> map) {
        return isAlphabetical(new ArrayList<>(map.keySet()));
    }

    private boolean isAlphabetical(List<Object> list) {

        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).toString().compareTo(list.get(i + 1).toString()) > 0) {
                return false;
            }
        }
        return true;
    }

}
