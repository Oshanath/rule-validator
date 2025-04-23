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

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class to create LintFunction instances
 */
public class FunctionFactory {
    private static final Map<String, Class<? extends LintFunction>> functionRegistry = new HashMap<>();

    // Static block to initialize the registry
    static {
        registerFunctions("org.wso2.rule.validator.functions.core"); // Replace with your package
    }

    private static void registerFunctions(String packageName) {
        Reflections reflections = new Reflections(packageName);
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(FunctionName.class)) {
            FunctionName annotation = clazz.getAnnotation(FunctionName.class);
            functionRegistry.put(annotation.value(), clazz.asSubclass(LintFunction.class));
        }
    }

    public static LintFunction getFunction(String functionName, Map<String, Object> functionOptions) throws
            InvalidCoreFunctionException {
        Class<? extends LintFunction> functionClass = functionRegistry.get(StringUtils.toRootLowerCase(functionName));
        if (functionClass == null) {
            throw new InvalidCoreFunctionException("Unknown function: " + functionName);
        }
        try {
            return (LintFunction) functionClass.getDeclaredConstructors()[0].newInstance(functionOptions);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new InvalidCoreFunctionException("Error creating function instance: " + e.getMessage());
        }
    }

    public static boolean isFunction(String functionName) {
        return functionRegistry.containsKey(StringUtils.toRootLowerCase(functionName));
    }
}
