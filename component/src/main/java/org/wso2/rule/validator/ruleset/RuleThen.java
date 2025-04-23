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

import org.wso2.rule.validator.Constants;
import org.wso2.rule.validator.functions.FunctionFactory;
import org.wso2.rule.validator.functions.InvalidCoreFunctionException;
import org.wso2.rule.validator.functions.LintFunction;

import java.util.Map;

/**
 * Represents the "then" part of a rule. Applies to each target identified by the "given" of the rule.
 */
public class RuleThen {
    public String field;
    private String function;
    private Map<String, Object> functionOptions;
    public LintFunction lintFunction;
    private boolean initialized = true;
    private String initializationErrorMessage = "";

    public RuleThen(Map<String, Object> ruleThenData) {
        this.field = (String) ruleThenData.get(Constants.RULESET_FIELD);
        this.function = (String) ruleThenData.get(Constants.RULESET_FUNCTION);
        this.functionOptions = (Map<String, Object>) ruleThenData.get(Constants.RULESET_FUNCTION_OPTIONS);
        try {
            this.lintFunction = FunctionFactory.getFunction(this.function, this.functionOptions);
        } catch (InvalidCoreFunctionException e) {
            initialized = false;
            initializationErrorMessage = e.getMessage();
            return;
        }
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getInitializationErrorMessage() {
        return initializationErrorMessage;
    }
}
