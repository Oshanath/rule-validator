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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to represent a ruleset alias definition.
 * An alias definition can be a simple alias or a complex alias.
 * A simple alias is a list of givens
 * A complex alias is a list of targets and a description
 */
public class RulesetAliasDefinition {
    private final String name;
    private String description;
    public List<RulesetAliasTarget> targets;
    private boolean isComplexAlias;
    private List<String> given;

    public RulesetAliasDefinition(String name, Object aliasObject) {
        this.name = name;
        this.given = new ArrayList<>();
        this.targets = new ArrayList<>();

        if (aliasObject instanceof List) {
            isComplexAlias = false;
            this.given = new ArrayList<>((List<String>) aliasObject);
        } else if (aliasObject instanceof Map) {
            isComplexAlias = true;
            Map<String, Object> aliasMap = (Map<String, Object>) aliasObject;
            this.description = (String) aliasMap.get(Constants.DESCRIPTION);
            this.targets = new ArrayList<>();
            List<Object> targets = (List<Object>) aliasMap.get(Constants.RULESET_ALIAS_TARGETS);
            if (targets != null) {
                for (Object target : targets) {
                    RulesetAliasTarget aliasTarget = new RulesetAliasTarget(target);
                    this.targets.add(aliasTarget);
                }
            }
        }
    }

    public boolean isComplexAlias() {
        return isComplexAlias;
    }

    public static List<String> resolveAliasGiven(String given, Map<String, RulesetAliasDefinition> aliases,
                                                      List<Format> formats) throws InvalidRulesetException {

        List<String> resolved = new ArrayList<>();

        String aliasExtractionRegex = Constants.RULESET_ALIAS_EXTRACTION_REGEX;
        Pattern pattern = Pattern.compile(aliasExtractionRegex);
        Matcher matcher = pattern.matcher(given);
        if (!matcher.find()) {
            resolved.add(given);
            return resolved;
        }
        String aliasName = matcher.group(0);
        RulesetAliasDefinition alias = aliases.get(aliasName.substring(1));
        if (alias == null) {
            throw new InvalidRulesetException("Alias " + aliasName + " not found");
        }

        if (alias.isComplexAlias()) {
            for (RulesetAliasTarget target : alias.targets) {
                if (!Format.matchFormat(target.formats, formats)) {
                    continue;
                }
                for (String g : target.given) {
                    resolved.add(given.replaceFirst(aliasExtractionRegex, "\\" + g));
                }
            }
        } else {
            for (String g : alias.given) {
                resolved.add(given.replaceFirst(aliasExtractionRegex, "\\" + g));
            }
        }

        return resolved;
    }

    public static void resolveAliasesInAliases(Map<String, RulesetAliasDefinition> aliases)
            throws InvalidRulesetException {
        for (int i = 0; i < aliases.size(); i++) {
            if (!allAliasesResolved(aliases)) {
                for (RulesetAliasDefinition alias : aliases.values()) {
                    if (!alias.isComplexAlias()) {
                        List<String> resolvedGiven = new ArrayList<>();
                        for (String given : alias.given) {
                            if (given.startsWith(Constants.ALIAS_PREFIX)) {
                                resolvedGiven.addAll(RulesetAliasDefinition
                                        .resolveAliasGiven(given, aliases, null));
                            } else {
                                resolvedGiven.add(given);
                            }
                        }
                        alias.given = resolvedGiven;
                    } else {
                        for (RulesetAliasTarget target: alias.targets) {
                            List<String> resolvedGiven = new ArrayList<>();
                            for (String given: target.given) {
                                if (given.startsWith(Constants.ALIAS_PREFIX)) {
                                    resolvedGiven.addAll(RulesetAliasDefinition.resolveAliasGiven(
                                            given, aliases, target.formats));
                                } else {
                                    resolvedGiven.add(given);
                                }
                            }
                            target.given = resolvedGiven;
                        }
                    }
                }
            } else {
                break;
            }
        }
    }

    public static boolean allAliasesResolved(Map<String, RulesetAliasDefinition> aliases) {
        for (RulesetAliasDefinition alias : aliases.values()) {
            if (!alias.isComplexAlias()) {
                for (String given : alias.given) {
                    if (given.startsWith(Constants.ALIAS_PREFIX)) {
                        return false;
                    }
                }
            } else {
                for (RulesetAliasTarget target: alias.targets) {
                    for (String given: target.given) {
                        if (given.startsWith(Constants.ALIAS_PREFIX)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public List<String> getGiven() {
        return new ArrayList<>(given);
    }
}

