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

import org.wso2.rule.validator.ruleset.Rule;

/**
 * Represents the result of a function execution
 */
public class LintResult {
    public final boolean passed;
    public final String path;
    public final Rule rule;
    public final String message;

    public LintResult(boolean passed, String path, Rule rule, String message) {
        this.passed = passed;
        this.path = path;
        this.rule = rule;
        this.message = message;
    }

    public String toString() {
        return "Rule: " + this.rule.name + " {\n" +
                "\tpassed=" + passed +
                "\n\tpath='" + path + '\'' +
                "\n\tmessage='" + this.message + '\'' +
                "\n}";
    }
}
