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

import com.jayway.jsonpath.JsonPath;
import org.wso2.rule.validator.validator.ruleset.RulesetValidator;

import java.util.List;
import java.util.Map;

/**
 * Ruleset validation for JSON files
 */
public class JsonRulesetValidator extends RulesetValidator {
    public static List<RulesetValidationError> validateRuleset(String rulesetString) {
        return RulesetValidator.validate((Map<String, Object>) JsonPath.parse(rulesetString).json());
    }
}
