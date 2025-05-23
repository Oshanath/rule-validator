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
package org.wso2.rule.validator.functions;

import org.wso2.rule.validator.InvalidRulesetException;
import org.wso2.rule.validator.document.LintTarget;

import java.util.List;
import java.util.Map;

/**
 * Abstract class to represent a lint function. All lint functions should extend this class and implement the execute
 * method.
 */
public abstract class LintFunction {

    public Map<String, Object> options;

    public LintFunction(Map<String, Object> options) {
        this.options = options;
        processFunctionOptions(options);
    }

    public FunctionResult execute(LintTarget target) throws InvalidRulesetException {
        List<String> errors = validateFunctionOptions();
        if (!errors.isEmpty()) {
            throw new InvalidRulesetException("Function options are invalid: " + errors);
        }
        return executeFunction(target);
    }

    protected abstract FunctionResult executeFunction(LintTarget target);

    public abstract List<String> validateFunctionOptions();

    public void processFunctionOptions(Map<String, Object> options) {}
}
