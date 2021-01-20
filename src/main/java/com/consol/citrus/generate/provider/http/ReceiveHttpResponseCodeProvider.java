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

import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.generate.provider.CodeProvider;
import com.consol.citrus.http.message.HttpMessage;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class ReceiveHttpResponseCodeProvider implements CodeProvider<HttpMessage> {

    private HttpCodeProvider httpCodeProvider = new HttpCodeProvider();

    @Override
    public CodeBlock getCode(final String endpoint, final HttpMessage message) {
        final CodeBlock.Builder code = CodeBlock.builder();

        code.add("runner.run(http().client($S)\n", endpoint);
        code.indent();
        code.add(".receive()\n");

        httpCodeProvider.provideResponseConfiguration(code, message);
        code.unindent();
        code.add(");");

        if (StringUtils.hasText(message.getPayload(String.class))) {
            code.add(getValidation(message.getPayload(String.class)));
        }

        return code.build();
    }

    private CodeBlock getValidation(String message) {
        final CodeBlock.Builder code = CodeBlock.builder();

        String[] str = message.split(",");
        String packageName = str[0];
        String className = str[1];
        ClassName responseClass = ClassName.get(packageName, className);

        code.add("\n\n");
        code.add("$T response = objectMapper.readValue(context\n", responseClass);
        code.indent();
        code.add(".getMessageStore()\n");
        code.add(".getMessage(\"response\")\n");
        code.add(".getPayload($T.class), $T.class);\n\n", String.class, responseClass);
        code.unindent();
        code.add("$T<$T<$T>> violations = validator.validate(response);\n\n",
                Set.class,
                ConstraintViolation.class,
                responseClass);
        code.add("if (violations.size() > 0) {\n");
        code.indent();
        code.add("String message = \"\\n\";\n");
        code.add("for ($T<$T> violation : violations) {\n",
                ConstraintViolation.class,
                responseClass);
        code.indent();
        code.add("message = $T.format(\"%sПоле ['%s'] %s\\n\", message, violation.getPropertyPath(), violation.getMessage());\n",
                String.class);
        code.unindent();
        code.add("}\n");
        code.add("throw new $T(new Throwable(message));\n", TestCaseFailedException.class);
        code.unindent();
        code.add("}\n");

        return code.build();
    }
}
