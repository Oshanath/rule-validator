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

import org.wso2.rule.validator.document.LintTarget;
import org.wso2.rule.validator.functions.FunctionName;
import org.wso2.rule.validator.functions.FunctionResult;
import org.wso2.rule.validator.functions.LintFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Falsy function implementation
 */
@FunctionName("falsy")
public class FalsyFunction extends LintFunction {

    public FalsyFunction(Map<String, Object> options) {
        super(options);
    }

    @Override
    public List<String> validateFunctionOptions() {
        List<String> errors = new ArrayList<>();

        if (options != null && !options.isEmpty()) {
            errors.add("Falsy function does not accept any options.");
        }

        return errors;
    }

    public FunctionResult executeFunction(LintTarget target) {
        if (target.value instanceof String) {
            if (((String) target.value).isEmpty()) {
                return new FunctionResult(true, "property \"" + target.getTargetName() + "\" must be falsy");
            } else {
                return new FunctionResult(false, null);
            }
        } else if (target.value instanceof List) {
            if (((List) target.value).isEmpty()) {
                return new FunctionResult(true, "property \"" + target.getTargetName() + "\" must be falsy");
            } else {
                return new FunctionResult(false, null);
            }
        } else if (target.value instanceof Map) {
            if (((Map) target.value).isEmpty()) {
                return new FunctionResult(true, null);
            } else {
                return new FunctionResult(false, "property \"" + target.getTargetName() + "\" must be falsy");
            }
        } else if (target.value instanceof Boolean) {
            if (!(Boolean) target.value) {
                return new FunctionResult(true, "property \"" + target.getTargetName() + "\" must be falsy");
            } else {
                return new FunctionResult(false, null);
            }
        } else if (target.value instanceof Integer) {
            if ((Integer) target.value == 0) {
                return new FunctionResult(true, "property \"" + target.getTargetName() + "\" must be falsy");
            } else {
                return new FunctionResult(false, null);
            }
        } else if (target.value instanceof Double) {
            if ((Double) target.value == 0.0) {
                return new FunctionResult(true, "property \"" + target.getTargetName() + "\" must be falsy");
            } else {
                return new FunctionResult(false, null);
            }
        } else if (target.value instanceof Float) {
            if ((Float) target.value == 0.0f) {
                return new FunctionResult(true, "property \"" + target.getTargetName() + "\" must be falsy");
            } else {
                return new FunctionResult(false, null);
            }
        } else {
            if (target.value == null) {
                return new FunctionResult(true, "property \"" + target.getTargetName() + "\" must be falsy");
            } else {
                return new FunctionResult(false, null);
            }
        }
    }
}
