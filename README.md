# WSO2 Rule Validator

---
- WSO2 Rule Validator is a JSON/YAML linter that is a Java implementation of Stoplight Spectral. [https://github.com/stoplightio/spectral](https://github.com/stoplightio/spectral)
- This validator is automatically called by the APIM Governance feature to validate different artifacts. (Open API Specifications, API Metadata files, etc.)
- Any valid JSON or YAML file can be validated by providing a valid Spectral ruleset.

## Features and Limitations
WSO2 Rule validator supports most features that Spectral itself does. With the exception of a few listed below.

1. [_**given**_](https://docs.stoplight.io/docs/spectral/d3482ff0ccae9-rules#rules-properties) path is a JSON Path. But currently WSO2 Rule Validator does not support [JSON Path Plus](https://github.com/JSONPath-Plus/JSONPath) features, even though Spectral does.
   - Object access should always be done inside single quotes \(paths\['/order'\]\)
2. All [core functions](https://docs.stoplight.io/docs/spectral/cb95cf0d26b83-core-functions) except [UnreferecedReusableObject](https://docs.stoplight.io/docs/spectral/cb95cf0d26b83-core-functions#unreferencedreusableobject) and [typedEnum](https://docs.stoplight.io/docs/spectral/cb95cf0d26b83-core-functions#typedenum) are supported.
3. Custom functions are not supported.
4. References ($ref) are not supported.
5. The core function "pattern" allows you to define regex patterns and check whether a certain lint target matches it or not.
   - _**In YAML rulesets, always define the regex pattern inside single quotes.**_
   - All regex should be valid regex Java regex, which is different from JavaScript regex used in Stoplight Spectral.
6. Extends and overrides are currently not supported
7. Parser options are not supported
8. Only Async API and Open API are supported (all versions)

## Usage

1. Build using `mvn clean install`.
2. Use the `.jar` file generated in `component/target`.
3. Use the following maven dependency
   ```xml
        <dependency>
            <groupId>org.wso2.carbon</groupId>
            <artifactId>org.wso2.rule.validator</artifactId>
            <version>${rule.validator.version}</version>
        </dependency>
    ```
4. Read the ruleset file into a string and call the `validateRuleset` method to validate the ruleset.
    ```java
        String document = new String(Files.readAllBytes(Paths.get("path/to/ruleset/ruleset.yaml")));
        String rulesetValidationResult = Validator.validateRuleset(ruleset);
    ```
5. Read the target document file and the ruleset into strings and call the `validateDocument` method to validate the document against the ruleset.
    ```java
        String document = new String(Files.readAllBytes(Paths.get("path/to/document/document.yaml")));
        String documentValidationResult = Validator.validateDocument(document, ruleset);
    ```