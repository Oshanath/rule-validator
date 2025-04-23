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

import net.minidev.json.JSONArray;
import org.wso2.rule.validator.InvalidContentTypeException;
import org.wso2.rule.validator.InvalidRulesetException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scenario {
    public final String name;
    public final String command;
    public final String stdout;
    public final Map<String, String> assets;

    private static final String SECTION_PATTERN = "====([^=]+)====";

    public Scenario(String content) {

        Map<String, String> sections = extractSections(content);
        try {
            this.name = sections.get("test");
            sections.remove("test");

            this.command = sections.get("command");
            sections.remove("command");

            this.stdout = sections.get("stdout");
            sections.remove("stdout");

            this.assets = sections;
        } catch (Exception e) {
            throw new IllegalArgumentException("Scenario name not found");
        }
    }

    public boolean runScenario() {
        List<String> commandSplit = List.of(this.command.split(" "));

        String documentKey = commandSplit.get(2);
        documentKey = documentKey.substring(1, documentKey.length() - 1);
        String document = this.assets.get(documentKey);

        String rulesetKey = commandSplit.get(4);
        rulesetKey = rulesetKey.substring(2, rulesetKey.length() - 2);
        String ruleset = processRuleset(rulesetKey);

        try {
            String result = Validator.validateDocument(document, ruleset);
            return matchResults(result);
        } catch (InvalidRulesetException e) {
            throw new RuntimeException(e);
        } catch (InvalidContentTypeException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean matchResults(String resultString) {
        JSONArray result = JsonPath.parse(resultString).json();

        List<String> expectedSplit = List.of(this.stdout.split("\n"));
        List<String> expectedErrors = expectedSplit.subList(1, expectedSplit.size() - 2);

        for (String error : expectedErrors) {
            List<String> errorSplit = List.of(error.split(" "));
            String severity = errorSplit.get(3);
            String ruleName = errorSplit.get(5);
            String path = errorSplit.get(errorSplit.size() - 1);
            String message = String.join(" ", errorSplit.subList(7, errorSplit.size() - 2));

            boolean found = false;
            for (Object errorObject : result) {
                Map<String, String> errorMap = (Map<String, String>) errorObject;
                if (errorMap.get("ruleName").equals(ruleName) &&
                        errorMap.get("path").equals(convertToBracketNotation(path))) {
                    found = true;
                    result.remove(errorObject);
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }

    private static String convertToBracketNotation(String jsonPath) {
        String result = jsonPath.replaceAll("\\.([0-9]+)", "[$1]");
        result = result.replaceAll("\\.([A-Za-z_$][A-Za-z0-9_$]*)", "[$1]");
        return result;
    }

    private String processRuleset(String rulesetKey) {
        String jsRuleset = this.assets.get(rulesetKey);
        String rulesetPrefix = "module.exports = ";
        int rulesetPrefixIndex = jsRuleset.indexOf(rulesetPrefix);
        if (rulesetPrefixIndex > 0) {
            return jsRuleset.substring(rulesetPrefixIndex + rulesetPrefix.length());
        } else {
            return jsRuleset;
        }
    }

    private static Map<String, String> extractSections(String content) {

        Pattern delimiterPattern = Pattern.compile(SECTION_PATTERN);
        Matcher matcher = delimiterPattern.matcher(content);

        Map<String, String> delimiterToContent = new LinkedHashMap<>();

        String currentDelimiter = null;
        int lastEnd = 0;

        while (matcher.find()) {
            if (currentDelimiter != null) {
                String subContent = content.substring(lastEnd, matcher.start());
                delimiterToContent.put(currentDelimiter.replaceAll("=", ""), subContent.trim());
            }
            currentDelimiter = matcher.group();
            lastEnd = matcher.end();
        }

        // After the loop, there might be content following the last delimiter.
        if (currentDelimiter != null) {
            String subContent = content.substring(lastEnd);
            delimiterToContent.put(currentDelimiter.replaceAll("=", ""), subContent.trim());
        }

        return delimiterToContent;
    }
}
