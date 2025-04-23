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
package org.wso2.rule.validator.document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import org.wso2.rule.validator.Constants;
import org.wso2.rule.validator.InvalidRulesetException;
import org.wso2.rule.validator.functions.FunctionResult;
import org.wso2.rule.validator.functions.LintResult;
import org.wso2.rule.validator.ruleset.Format;
import org.wso2.rule.validator.ruleset.Rule;
import org.wso2.rule.validator.ruleset.RuleThen;
import org.wso2.rule.validator.ruleset.Ruleset;
import org.wso2.rule.validator.ruleset.RulesetAliasDefinition;
import org.wso2.rule.validator.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Document class to represent a target document for rules to be applied.
 */
public class Document {

    private String documentString;
    private Object document = null;
    List<Format> formats;

    public Document(String documentString) {
        Object yamlData = Util.loadYaml(documentString);

        if (yamlData == null) {
            return;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        this.documentString = gson.toJson(yamlData);

        this.document = JsonPath.parse(this.documentString).json();
        resolveReferences();

        // Read format
        if (this.document instanceof Map) {
            this.formats = new ArrayList<>();
            Map<String, Object> documentMap = (Map<String, Object>) yamlData;
            if (documentMap.containsKey(Constants.OPENAPI_KEY)) {
                String oasVersion = (String) documentMap.get(Constants.OPENAPI_KEY);
                if (oasVersion.startsWith(Constants.OAS_3_1_VERSION)) {
                    this.formats.add(Format.OAS3_1);
                    this.formats.add(Format.OAS3);
                } else if (oasVersion.startsWith(Constants.OAS_3_0_VERSION)) {
                    this.formats.add(Format.OAS3_0);
                    this.formats.add(Format.OAS3);
                } else {
                    this.formats.add(Format.OAS3);
                    this.formats.add(Format.OAS3_0);
                    this.formats.add(Format.OAS3_1);
                }
            } else if (documentMap.containsKey(Constants.SWAGGER_KEY)) {
                this.formats.add(Format.OAS2);
            }
        }
    }

    public boolean isNull() {
        return this.document == null;
    }

    public List<LintResult> lint(Ruleset ruleset) throws InvalidRulesetException {

        for (Rule rule : ruleset.rules.values()) {
            // resolve given aliases
            List<String> resolvedGiven = new ArrayList<>();
            for (String given : rule.given) {
                if (given.startsWith(Constants.ALIAS_PREFIX)) {
                    List<Format> aliasFormats;
                    if (this.formats != null && !this.formats.isEmpty()) {
                        aliasFormats = this.formats;
                    } else {
                        aliasFormats = null;
                    }
                    resolvedGiven.addAll(RulesetAliasDefinition
                            .resolveAliasGiven(given, ruleset.aliases, aliasFormats));
                } else {
                    resolvedGiven.add(given);
                }
            }
            rule.given = resolvedGiven;
        }

        List<LintResult> results = new ArrayList<>();

        for (Rule rule : ruleset.rules.values()) {
            if (!matchFormat(ruleset, rule)) {
                continue;
            }
            for (String given : rule.given) {
                try {
                    Configuration config = Configuration.builder().options(Option.AS_PATH_LIST).build();
                    List<String> paths = JsonPath.using(config).parse(this.document).read(given);
                    for (String path : paths) {
                        results.addAll(lintNode(path, rule));
                    }
                    // log("Json Path resolved: " + given);
                } catch (PathNotFoundException e) {
                    // log("Json Path not found: " + given);
                } catch (InvalidPathException e) {
                    // log("Unsupported Json Path: " + given);
                }
            }
        }

        return results;
    }

    private boolean matchFormat(Ruleset ruleset, Rule rule) {
        if (!rule.formats.isEmpty()) {
            return  Format.matchFormat(rule.formats, this.formats);
        } else if (!ruleset.formats.isEmpty()) {
            return Format.matchFormat(ruleset.formats, this.formats);
        } else {
            return true;
        }
    }

    private void resolveReferences() {
        /**
         * A document Inventory maintains a graph (non-circular) pointing to other documents via refs. When a ref is in
         * a document, it adds a Node in the graph pointing to the document, and if there are refs within that ref, that
         * node will point to another node with a ref.
         *
         * When traversing the json paths for linting with a ruleset, it will get the closest path until the ref, then
         * traverse the rest on the ref doc, and this is done recursively.
         */
    }

    private List<LintResult> lintNode(String path, Rule rule) throws InvalidRulesetException {
        List<LintResult> results = new ArrayList<>();
        Object node;
        try {
            node = JsonPath.read(this.document, path);
        } catch (PathNotFoundException e) {
            return results;
        }
        for (RuleThen then : rule.then) {
            List<LintTarget> lintTargets = getLintTargets(node, then);

            for (LintTarget target : lintTargets) {
                List<String> parentPath = splitJsonPath(path);
                parentPath.addAll(target.jsonPath);
                String targetPath = LintTarget.getPathString(parentPath);
                target.jsonPath = parentPath;
                FunctionResult result = then.lintFunction.execute(target);
                results.add(new LintResult(result.passed, targetPath, rule,
                        rule.message == null ? result.message : rule.message));
            }
        }
        return results;
    }

    private List<LintTarget> getLintTargets(Object node, RuleThen then) {
        List<LintTarget> lintTargets = new ArrayList<>();

        if ((node instanceof List || node instanceof Map) && (then.field != null && !then.field.isEmpty())) {
            if (then.field.equals(Constants.RULESET_FIELD_KEY)) {
                if (node instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) node;
                    for (String key : map.keySet()) {
                        lintTargets.add(new LintTarget(new ArrayList<>(Arrays.asList(key)), key));
                    }
                } else if (node instanceof List) {
                    List<Object> list = (List<Object>) node;
                    for (int i = 0; i < list.size(); i++) {
                        lintTargets.add(new LintTarget(new ArrayList<>(Arrays.asList(String.valueOf(i))),
                                String.valueOf(i)));
                    }
                }
            } else if (then.field.startsWith(Constants.JSON_PATH_ROOT)) {
                Configuration config = Configuration.builder().options(Option.AS_PATH_LIST).build();
                List<String> paths;
                try {
                    paths = JsonPath.using(config).parse(node).read(then.field);
                } catch (PathNotFoundException e) {
                    return lintTargets;
                }

                for (String path : paths) {
                    List<String> splitPath = splitJsonPath(path);
                    Object value;
                    try {
                        value = JsonPath.read(node, path);
                        lintTargets.add(new LintTarget(splitPath, value));
                    } catch (PathNotFoundException ignored) {

                    }
                }
            } else {
                List<String> path = toPath(then.field);
                Object value;
                try {
                    value = JsonPath.read(node, then.field);
                    lintTargets.add(new LintTarget(path, value));
                } catch (PathNotFoundException e) {
                    lintTargets.add(new LintTarget(path, null));
                }
            }
        } else {
            lintTargets.add(new LintTarget(new ArrayList<String>(), node));
        }

        return lintTargets;
    }

    public static List<String> splitJsonPath(String jsonPath) {
        List<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean insideBrackets = false;

        for (char ch : jsonPath.toCharArray()) {
            if (ch == '[') {
                insideBrackets = true;
                continue; // Skip the opening bracket
            } else if (ch == ']') {
                insideBrackets = false;
                parts.add(currentPart.toString().replace("'", "").replace("\"", ""));
                currentPart.setLength(0); // Clear the current part
            } else if (insideBrackets) {
                currentPart.append(ch);
            }
        }

        return parts;
    }

    public static List<String> toPath(String path) {
        List<String> segments = new ArrayList<>();

        // Regex to match either dot-separated keys or bracket notation
        Pattern pattern = Pattern.compile(Constants.JSON_PATH_GROUPING_REGEX);
        Matcher matcher = pattern.matcher(path);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // Single-quoted key in brackets
                segments.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                // Double-quoted key in brackets
                segments.add(matcher.group(2));
            } else if (matcher.group(3) != null) {
                // Unquoted index or key in brackets
                segments.add(matcher.group(3));
            } else if (matcher.group(4) != null) {
                // Dot-separated key
                segments.add(matcher.group(4));
            }
        }

        return segments;
    }
}
