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
import org.wso2.rule.validator.InvalidRulesetException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ruleset class represents a set of rules that can be applied to a document.
 */
public class Ruleset {
    public final Map<String, Rule> rules;
    public final Map<String, RulesetAliasDefinition> aliases;
    private boolean hasComplexAliases;
    public List<Format> formats;
    private List<Ruleset> extendsRulesets;
    private boolean initialized = true;
    private String initializationErrorMessage = "";

    public Ruleset(Map<String, Object> datamap) {
        this.rules = new HashMap<>();
        this.aliases = new HashMap<>();
        this.hasComplexAliases = false;
        this.formats = new ArrayList<>();

        if (datamap == null) {
            return;
        }

        Map<String, Object> ruleMap = (Map<String, Object>) datamap.get(Constants.RULESET_RULES);

        // Read formats
        if (datamap.containsKey(Constants.RULESET_FORMATS)) {
            this.formats = Format.getFormatListFromObject((List<String>) datamap.get(Constants.RULESET_FORMATS));
        }

        // Read aliases
        if (datamap.containsKey(Constants.RULESET_ALIASES)) {
            Map<String, Object> aliases = (Map<String, Object>) datamap.get(Constants.RULESET_ALIASES);
            for (Map.Entry<String, Object> entry : aliases.entrySet()) {
                RulesetAliasDefinition alias = new RulesetAliasDefinition(entry.getKey(), entry.getValue());
                this.aliases.put(entry.getKey(), alias);
                if (alias.isComplexAlias()) {
                    this.hasComplexAliases = true;
                }
            }
            try {
                RulesetAliasDefinition.resolveAliasesInAliases(this.aliases);
            } catch (InvalidRulesetException e) {
                // Ignore. Should be caught in the ruleset validation
            }
        }

        // Read rules
        for (Map.Entry<String, Object> entry : ruleMap.entrySet()) {
            String ruleName = entry.getKey();
            Rule rule = new Rule(ruleName, (Map<String, Object>) entry.getValue(), this.aliases, this.formats);
            if (rule.isInitialized()) {
                this.rules.put(ruleName, rule);
            } else {
                this.initialized = false;
                this.initializationErrorMessage = rule.getInitializationErrorMessage();
                return;
            }
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getInitializationErrorMessage() {
        return initializationErrorMessage;
    }
}
