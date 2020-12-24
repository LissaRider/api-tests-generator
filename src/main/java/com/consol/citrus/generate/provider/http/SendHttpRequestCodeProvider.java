/*
 * Copyright 2006-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.generate.provider.http;

import com.consol.citrus.generate.provider.CodeProvider;
import com.consol.citrus.http.message.HttpMessage;
import com.squareup.javapoet.CodeBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.7.4
 */
public class SendHttpRequestCodeProvider implements CodeProvider<HttpMessage>{

    private HttpCodeProvider httpCodeProvider = new HttpCodeProvider();

    @Override
    public CodeBlock getCode(final String endpoint, final HttpMessage message) {
        final CodeBlock.Builder code = CodeBlock.builder();
        final String formatPath = message.getPath().replaceAll("\\{", "\\${");
        message.path(formatPath);

        List<String> pathParams = getPathParams(formatPath);
        pathParams.forEach(s -> code.add("variable($S, null);\n", s));
        if (!pathParams.isEmpty()) {
            code.add("\n");
        }


        code.add("runner.run(http().client($S)\n", endpoint);
        code.indent();
        code.add(".send()\n");

        httpCodeProvider.provideRequestConfiguration(code, message);
        code.unindent();
        code.add(");");

        return code.build();
    }

    private static List<String> getPathParams(String formatPath) {
        List<String> pathParams = new ArrayList<>();
        String[] str = formatPath.split("/");
        for (String path : str) {
            String result;
            if (path.startsWith("$")) {
                result = path.substring(2, path.length() - 1);
                pathParams.add(result);
            }
        }

        return pathParams;
    }
}
