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

import com.consol.citrus.generate.provider.MessageCodeProvider;
import com.consol.citrus.http.message.HttpMessage;
import com.squareup.javapoet.CodeBlock;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Optional;
import java.util.stream.Stream;

public class HttpCodeProvider {
    private static boolean coverage;

    public static void setCoverage(boolean coverage) {
        HttpCodeProvider.coverage = coverage;
    }

    private MessageCodeProvider messageCodeProvider = new MessageCodeProvider();

    void provideRequestConfiguration(final CodeBlock.Builder code, final HttpMessage message) {
        final String method = Optional.ofNullable(message.getRequestMethod())
                .map(Object::toString)
                .orElse(RequestMethod.POST.name());

        providePath(code, message, method);
        provideContentType(code, message);
        messageCodeProvider.provideHeaderAndPayload(code, message);
        provideQueryParameter(code, message);
    }

    void provideResponseConfiguration(final CodeBlock.Builder code, final HttpMessage message) {
        code.add(".response($T.$L)\n", HttpStatus.class, message.getStatusCode().name());
        messageCodeProvider.provideHeaderAndPayload(code, message);
    }

    private void provideContentType(final CodeBlock.Builder code, final HttpMessage message) {
        code.add(".contentType($S)\n",
                Optional.ofNullable(message.getHeader("Content-Type"))
                        .orElse("application/json"));
    }

    private void providePath(final CodeBlock.Builder code, final HttpMessage message, final String method) {
        code.add(".$L(InterceptorHandler.getPath($L))\n",
                method.toLowerCase(),
                Optional.ofNullable(message.getPath())
                        .map(Object::toString)
                        .map(path -> "\"" + path + "\"")
                        .orElse(""));
    }

    private void provideQueryParameter(final CodeBlock.Builder code, final HttpMessage message) {
        if (!CollectionUtils.isEmpty(message.getQueryParams())) {
            message.getQueryParams()
                    .forEach((key, values) ->
                            values.forEach(value ->
                                    code.add(".queryParam($S, $S)\n", key, value))
                    );
        } else if (StringUtils.hasText(message.getQueryParamString())) {
            Stream.of(message.getQueryParamString()
                    .split(","))
                    .map(nameValuePair -> nameValuePair.split("="))
                    .forEach(param ->
                            code.add(".queryParam($S, $S)\n", param[0], param[1])
                    );
        }
    }
}
