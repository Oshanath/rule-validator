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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ValidatorTest {

    @Test
    public void testMain() {
        String packageName = "org.wso2.rule.validator.";  // Adjust your package path
        List<String> contents = null;
        try {
            contents = readFilesFromPackage(packageName);
        } catch (IOException e) {
            fail("Error occurred while reading files: " + e.getMessage());
        } catch (URISyntaxException e) {
            fail("Error occurred while converting URL to URI: " + e.getMessage());
        }

        for (String content : contents) {
            Scenario scenario = new Scenario(content);
            scenario.runScenario();
        }
    }

    private static List<String> readFilesFromPackage(String packageName) throws IOException, URISyntaxException {
        List<String> fileContents = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource("scenarios");

        if (resource == null) {
            throw new IllegalArgumentException("Package not found: " + packageName);
        }

        Path directoryPath = Paths.get(resource.toURI());

        // Read all files in package and sub-packages
        try (var paths = Files.walk(directoryPath)) {
            List<Path> files = paths.filter(Files::isRegularFile).collect(Collectors.toList());

            for (Path file : files) {
                String[] allowedScenarios = {
                        "aliases.scenario",
                        "alphabetical-responses-order.oas3.scenario",
                        "results-default.scenario"
                };
                List<String> allowedScenariosList = List.of(allowedScenarios);
                if (allowedScenariosList.contains(file.toString().split("scenarios/")[1])) {
                    fileContents.add(Files.readString(file));
                }
            }
        }

        return fileContents;
    }
}
