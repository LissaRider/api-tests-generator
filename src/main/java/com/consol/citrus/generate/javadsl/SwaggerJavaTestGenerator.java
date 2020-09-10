/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.generate.javadsl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.generate.SwaggerTestGenerator;
import com.consol.citrus.http.actions.HttpActionBuilder;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.variable.dictionary.json.JsonPathMappingDataDictionary;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.swagger.models.*;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.*;
import io.swagger.parser.SwaggerParser;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Test generator creates one to many test cases based on operations defined in a XML schema XSD.
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class SwaggerJavaTestGenerator extends MessagingJavaTestGenerator<SwaggerJavaTestGenerator> implements SwaggerTestGenerator<SwaggerJavaTestGenerator> {
    /** Loop counter for recursion */
    private Map<String, Integer> control = new HashMap<>();

    private static boolean isCoverage;

    private String swaggerResource;

    private String contextPath;
    private String operation;

    private String namePrefix;
    private String nameSuffix = "_IT";

    private JsonPathMappingDataDictionary inboundDataDictionary = new JsonPathMappingDataDictionary();
    private JsonPathMappingDataDictionary outboundDataDictionary = new JsonPathMappingDataDictionary();

    public static void setCoverage(boolean isCoverage) {
        SwaggerJavaTestGenerator.isCoverage = isCoverage;
    }

    @Override
    protected JavaFile.Builder createJavaFileBuilder(TypeSpec.Builder testTypeBuilder) {
        return super.createJavaFileBuilder(testTypeBuilder)
                .addStaticImport(HttpActionBuilder.class, "http");
    }

    @Override
    public void create() {
        Swagger swagger;

        try {
            swagger = new SwaggerParser().parse(FileUtils.readToString(new PathMatchingResourcePatternResolver().getResource(swaggerResource)));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to parse Swagger Open API specification: " + swaggerResource, e);
        }

        if (!StringUtils.hasText(namePrefix)) {
            String title = swagger.getInfo().getTitle();
            if (title != null) {
                title = title.replaceAll("[^A-Za-z0-9]", "");
                if (title.matches("^[A-Za-z]+")) {
                    title = title.substring(0,1).toUpperCase() + title.substring(1);
                } else {
                    title = null;
                }
            }
            withNamePrefix(StringUtils.trimAllWhitespace(Optional.ofNullable(title).orElse("Swagger")) + "_");
        }

        for (Map.Entry<String, Path> path : swagger.getPaths().entrySet()) {
            for (Map.Entry<HttpMethod, Operation> operation : path.getValue().getOperationMap().entrySet()) {

                Map<String, Response> responses = operation.getValue().getResponses();

                if (responses.containsKey("200") || responses.containsKey("default")) {

                    // Now generate it
                    withName(namePrefix + operation.getValue().getOperationId()  + nameSuffix);

                    HttpMessage requestMessage = new HttpMessage();

                    if (getMode().equals(GeneratorMode.CLIENT) && !isCoverage) {
                        String randomizedPath = path.getKey();
                        if (operation.getValue().getParameters() != null) {
                            List<PathParameter> pathParams = operation.getValue().getParameters().stream()
                                    .filter(p -> p instanceof PathParameter)
                                    .map(PathParameter.class::cast)
                                    .collect(Collectors.toList());

                            for (PathParameter parameter : pathParams) {
                                randomizedPath = randomizedPath.replaceAll("\\{" + parameter.getName() + "\\}", createRandomValueExpression(parameter));
                            }
                        }

                        requestMessage.path(Optional.ofNullable(contextPath).orElse("") + Optional.ofNullable(swagger.getBasePath()).filter(basePath -> !basePath.equals("/")).orElse("") + randomizedPath);
                    } else if (getMode().equals(GeneratorMode.CLIENT) && isCoverage) {
                        requestMessage.path(Optional.ofNullable(contextPath).orElse("") + Optional.ofNullable(swagger.getBasePath()).filter(basePath -> !basePath.equals("/")).orElse("") + path.getKey());
                    } else {
                        requestMessage.path("@assertThat(matchesPath(" + path.getKey() + "))@");
                    }

                    requestMessage.method(org.springframework.http.HttpMethod.valueOf(operation.getKey().name()));


                    if (operation.getValue().getParameters() != null) {

                        operation.getValue().getParameters().stream()
                                .filter(p -> p instanceof HeaderParameter)
                                .filter(Parameter::getRequired)
                                .forEach(p -> requestMessage.setHeader(p.getName(), getMode().equals(GeneratorMode.CLIENT) ? createRandomValueExpression((HeaderParameter) p) : createValidationExpression((HeaderParameter) p)));

                        if (isCoverage) {
                            operation.getValue().getParameters().stream()
                                    .filter(p -> p instanceof PathParameter)
                                    .filter(Parameter::getRequired)
                                    .forEach(p -> requestMessage.setHeader("{" + p.getName() + "}", getMode().equals(GeneratorMode.CLIENT) ? createRandomValueExpression((PathParameter) p) : createValidationExpression((PathParameter) p)));
                        }

                        operation.getValue().getParameters().stream()
                                .filter(param -> param instanceof QueryParameter)
                                .filter(Parameter::getRequired)
                                .forEach(param -> requestMessage.queryParam(param.getName(), getMode().equals(GeneratorMode.CLIENT) ? createRandomValueExpression((QueryParameter) param) : createValidationExpression((QueryParameter) param)));

                        operation.getValue().getParameters().stream()
                                .filter(p -> p instanceof BodyParameter)
                                .filter(Parameter::getRequired)
                                .findFirst()
                                .ifPresent(p -> {control = new HashMap<>();
                                                requestMessage.setPayload(getMode().equals(GeneratorMode.CLIENT) ? createOutboundPayload(((BodyParameter) p).getSchema(), swagger.getDefinitions()) : createInboundPayload(((BodyParameter) p).getSchema(), swagger.getDefinitions()));});
                    }
                    withRequest(requestMessage);

                    HttpMessage responseMessage = new HttpMessage();
                        Response response = operation.getValue().getResponses().get("200");
                        if (response == null) {
                            response = operation.getValue().getResponses().get("default");
                        }

                        if (response != null) {
                            responseMessage.status(HttpStatus.OK);

                            if (response.getHeaders() != null) {
                                for (Map.Entry<String, Property> header : response.getHeaders().entrySet()) {
                                    responseMessage.setHeader(header.getKey(), getMode().equals(GeneratorMode.CLIENT) ? createValidationExpression(header.getValue(), swagger.getDefinitions(), false) : createRandomValueExpression(header.getValue(), swagger.getDefinitions(), false));
                                }
                            }

                            if (response.getSchema() != null) {
                                control = new HashMap<>();
                                responseMessage.setPayload(getMode().equals(GeneratorMode.CLIENT) ? createInboundPayload(response.getSchema(), swagger.getDefinitions()): createOutboundPayload(response.getSchema(), swagger.getDefinitions()));
                            }
                        }
                    withResponse(responseMessage);

                    super.create();

                    log.info("Successfully created new test case " + getTargetPackage() + "." + getName());
                }
            }
        }
    }

    /**
     * Creates payload from schema for outbound message.
     * @param model
     * @param definitions
     * @return
     */
    private String createOutboundPayload(Model model, Map<String, Model> definitions) {
        StringBuilder payload = new StringBuilder();

        if (model instanceof RefModel) {
            model = definitions.get(((RefModel) model).getSimpleRef());
        }

        if (model instanceof ArrayModel) {
            payload.append("[");
            payload.append(createOutboundPayload(((ArrayModel) model).getItems(), definitions));
            payload.append("]");
        } else {
            payload.append("{");
            if (model.getProperties() != null) {
                for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
                    payload.append("\"").append(entry.getKey()).append("\": ").append(createOutboundPayload(entry.getValue(), definitions)).append(",");
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        }

        return payload.toString();
    }

    /**
     * Creates payload from property for outbound message.
     * @param property
     * @param definitions
     * @return
     */
    private String createOutboundPayload(Property property, Map<String, Model> definitions) {
        StringBuilder payload = new StringBuilder();
        boolean permit = true;

        if (property instanceof RefProperty) {
            String ref = ((RefProperty) property).getSimpleRef();


            if (control.containsKey(ref)) {
                if (control.get(ref) > 1) {
                    permit = false;
                    payload.append("{}");
                } else {
                    control.put(ref, control.get(ref) + 1);
                }
            } else {
                control.put(ref, 1);
            }

            if (permit) {
                Model model = definitions.get(((RefProperty) property).getSimpleRef());
                payload.append("{");

                if (model.getProperties() != null) {
                    for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
                        payload.append("\"").append(entry.getKey()).append("\": ").append(createRandomValueExpression(entry.getValue(), definitions, true)).append(",");
                    }
                }
                if (control != null) {
                    control.put(ref, control.get(ref) - 1);
                }

                if (payload.toString().endsWith(",")) {
                    payload.replace(payload.length() - 1, payload.length(), "");
                }

                payload.append("}");
            }
        } else if (property instanceof ArrayProperty) {
            payload.append("[");
            payload.append(createRandomValueExpression(((ArrayProperty) property).getItems(), definitions, true));
            payload.append("]");
        } else if (property instanceof MapProperty) {
            payload.append("{");
            payload.append("citrus:randomString(10): ");
            payload.append(createRandomValueExpression(((MapProperty) property).getAdditionalProperties(), definitions, true));
            payload.append("}");
        } else {
            payload.append(createRandomValueExpression(property, definitions, true));
        }

        return payload.toString();
    }

    /**
     * Create payload from schema with random values.
     * @param property
     * @param definitions
     * @param quotes
     * @return
     */
    private String createRandomValueExpression(Property property, Map<String, Model> definitions, boolean quotes) {
        StringBuilder payload = new StringBuilder();

        if (property instanceof RefProperty) {
            payload.append(createOutboundPayload(property, definitions));
        } else if (property instanceof ArrayProperty) {
            payload.append(createOutboundPayload(property, definitions));
        } else if (property instanceof StringProperty || property instanceof DateProperty || property instanceof DateTimeProperty) {
            if (quotes) {
                payload.append("\"");
            }

            if (property instanceof DateProperty) {
                payload.append("citrus:currentDate()");
            } else if (property instanceof DateTimeProperty) {
                payload.append("citrus:currentDate('yyyy-MM-dd'T'hh:mm:ss')");
            } else if (!CollectionUtils.isEmpty(((StringProperty) property).getEnum())) {
                payload.append("citrus:randomEnumValue(").append(((StringProperty) property).getEnum().stream().map(value -> "'" + value + "'").collect(Collectors.joining(","))).append(")");
            } else if (Optional.ofNullable(property.getFormat()).orElse("").equalsIgnoreCase("uuid")) {
                payload.append("citrus:randomUUID()");
            } else {
                payload.append("citrus:randomString(").append(((StringProperty) property).getMaxLength() != null && ((StringProperty) property).getMaxLength() > 0 ? ((StringProperty) property).getMaxLength() : (((StringProperty) property).getMinLength() != null && ((StringProperty) property).getMinLength() > 0 ? ((StringProperty) property).getMinLength() : 10)).append(")");
            }

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof IntegerProperty || property instanceof LongProperty) {
            payload.append("citrus:randomNumber(9)");
        } else if (property instanceof DecimalProperty) {
            payload.append("citrus:randomNumber(9)");
        } else if (property instanceof BooleanProperty) {
            payload.append("citrus:randomEnumValue('true', 'false')");
        } else {
            if (quotes) {
                payload.append("\"\"");
            } else {
                payload.append("");
            }
        }

        return payload.toString();
    }

    /**
     * Creates control payload from property for validation.
     * @param property
     * @param definitions
     * @return
     */
    private String createInboundPayload(Property property, Map<String, Model> definitions) {
        StringBuilder payload = new StringBuilder();

        if (property instanceof RefProperty) {
            Model model = definitions.get(((RefProperty) property).getSimpleRef());
            payload.append("{");

            if (model.getProperties() != null) {
                for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
                    payload.append("\"").append(entry.getKey()).append("\": ").append(createValidationExpression(entry.getValue(), definitions, true)).append(",");
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        } else if (property instanceof ArrayProperty) {
            if (((ArrayProperty) property).getItems() instanceof RefProperty) {
                payload.append("[");
                payload.append(createValidationExpression(((ArrayProperty) property).getItems(), definitions, true));
                payload.append("]");
            } else {
                payload.append("\"@ignore@\"");
            }
        } else if (property instanceof MapProperty) {
            payload.append("");
        } else {
            payload.append(createValidationExpression(property, definitions, false));
        }

        return payload.toString();
    }

    /**
     * Creates control payload from schema for validation.
     * @param model
     * @param definitions
     * @return
     */
    private String createInboundPayload(Model model, Map<String, Model> definitions) {
        StringBuilder payload = new StringBuilder();

        if (model instanceof RefModel) {
            model = definitions.get(((RefModel) model).getSimpleRef());
        }

        if (model instanceof ArrayModel) {
            payload.append("[");
            payload.append(createValidationExpression(((ArrayModel) model).getItems(), definitions, true));
            payload.append("]");
        } else {
            payload.append("{");

            if (model.getProperties() != null) {
                for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
                    payload.append("\"").append(entry.getKey()).append("\": ").append(createValidationExpression(entry.getValue(), definitions, true)).append(",");
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        }

        return payload.toString();
    }

    /**
     * Create validation expression using functions according to parameter type and format.
     * @param property
     * @param definitions
     * @param quotes
     * @return
     */
    private String createValidationExpression(Property property, Map<String, Model> definitions, boolean quotes) {
        StringBuilder payload = new StringBuilder();
        boolean permit = true;

        if (property instanceof RefProperty) {
            String ref = ((RefProperty) property).getSimpleRef();

            if (control.containsKey(ref)) {
                if (control.get(ref) > 1) {
                    permit = false;
                    payload.append("\"@ignore@\"");
                } else {
                    control.put(ref, control.get(ref) + 1);
                }
            } else {
                control.put(ref, 1);
            }

            if (permit) {
                Model model = definitions.get(((RefProperty) property).getSimpleRef());
                payload.append("{");
                if (model.getProperties() != null) {
                    for (Map.Entry<String, Property> entry : model.getProperties().entrySet()) {
                        payload.append("\"").append(entry.getKey()).append("\": ").append(createValidationExpression(entry.getValue(), definitions, quotes)).append(",");
                    }
                }

                control.put(ref, control.get(ref) - 1);

                if (payload.toString().endsWith(",")) {
                    payload.replace(payload.length() - 1, payload.length(), "");
                }

                payload.append("}");
            }
        } else if (property instanceof ArrayProperty) {
            if (quotes) {
                payload.append("\"");
            }
            payload.append("@ignore@");

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof MapProperty) {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@ignore@");

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof StringProperty) {
            if (quotes) {
                payload.append("\"");
            }

            if (!CollectionUtils.isEmpty(((StringProperty) property).getEnum())) {
                payload.append("@matches(").append(((StringProperty) property).getEnum().stream().collect(Collectors.joining("|"))).append(")@");
            } else {
                payload.append("@notEmpty()@");
            }

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof DateProperty) {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@matchesDatePattern('yyyy-MM-dd')@");

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof DateTimeProperty) {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@matchesDatePattern('yyyy-MM-dd'T'hh:mm:ss')@");

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof IntegerProperty || property instanceof LongProperty) {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@isNumber()@");

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof DecimalProperty) {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@isNumber()@");

            if (quotes) {
                payload.append("\"");
            }
        } else if (property instanceof BooleanProperty) {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@matches(true|false)@");

            if (quotes) {
                payload.append("\"");
            }
        } else {
            if (quotes) {
                payload.append("\"");
            }

            payload.append("@ignore@");

            if (quotes) {
                payload.append("\"");
            }
        }

        return payload.toString();
    }

    /**
     * Create validation expression using functions according to parameter type and format.
     * @param parameter
     * @return
     */
    private String createValidationExpression(AbstractSerializableParameter parameter) {
        switch (parameter.getType()) {
            case "integer":
            case "number":
                return "@isNumber()@";
            case "string":
                if (parameter.getFormat() != null && parameter.getFormat().equals("date")) {
                    return "\"@matchesDatePattern('yyyy-MM-dd')@\"";
                } else if (parameter.getFormat() != null && parameter.getFormat().equals("date-time")) {
                    return "\"@matchesDatePattern('yyyy-MM-dd'T'hh:mm:ss')@\"";
                } else if (StringUtils.hasText(parameter.getPattern())) {
                    return "\"@matches(" + parameter.getPattern() + ")@\"";
                } else if (!CollectionUtils.isEmpty(parameter.getEnum())) {
                    return "\"@matches(" + (parameter.getEnum().stream().collect(Collectors.joining("|"))) + ")@\"";
                } else {
                    return "@notEmpty()@";
                }
            case "boolean":
                return "@matches(true|false)@";
            default:
                return "@ignore@";
        }
    }

    /**
     * Create random value expression using functions according to parameter type and format.
     * @param parameter
     * @return
     */
    private String createRandomValueExpression(AbstractSerializableParameter parameter) {
        String quotes = "\"";
        if (parameter instanceof QueryParameter || parameter instanceof PathParameter) {
            quotes = "";
        }

        String type = parameter.getType();
        String format = parameter.getFormat();
        if (type.equals("array")) {
            type = parameter.getItems().getType();
            format = parameter.getItems().getFormat();
        }

        switch (type) {
            case "integer":
            case "number":
                return "citrus:randomNumber(9)";
            case "string":
                if (parameter.getFormat() != null && format.equals("date")) {
                    return quotes + "citrus:currentDate('yyyy-MM-dd')" + quotes;
                } else if (parameter.getFormat() != null && format.equals("date-time")) {
                    return quotes + "citrus:currentDate('yyyy-MM-dd'T'hh:mm:ss')" + quotes;
                } else if (StringUtils.hasText(parameter.getPattern())) {
                    return quotes + "citrus:randomValue(" + parameter.getPattern() + ")" + quotes;
                } else if (!CollectionUtils.isEmpty(parameter.getEnum())) {
                    return quotes + "citrus:randomEnumValue(" + (parameter.getEnum().stream().collect(Collectors.joining(","))) + ")" + quotes;
                } else if (Optional.ofNullable(format).orElse("").equalsIgnoreCase("uuid")){
                    return "citrus:randomUUID()";
                } else {
                    return "citrus:randomString(10)";
                }
            case "boolean":
                return "true";
            default:
                return "";
        }
    }

    /**
     * Set the swagger Open API resource to use.
     * @param swaggerResource
     * @return
     */
    public SwaggerJavaTestGenerator withSpec(String swaggerResource) {
        this.swaggerResource = swaggerResource;
        return this;
    }

    /**
     * Set the server context path to use.
     * @param contextPath
     * @return
     */
    public SwaggerJavaTestGenerator withContextPath(String contextPath) {
        this.nameSuffix = contextPath;
        return this;
    }

    /**
     * Set the test name prefix to use.
     * @param prefix
     * @return
     */
    public SwaggerJavaTestGenerator withNamePrefix(String prefix) {
        this.namePrefix = prefix;
        return this;
    }

    /**
     * Set the test name suffix to use.
     * @param suffix
     * @return
     */
    public SwaggerJavaTestGenerator withNameSuffix(String suffix) {
        this.nameSuffix = suffix;
        return this;
    }

    /**
     * Set the swagger operation to use.
     * @param operation
     * @return
     */
    public SwaggerJavaTestGenerator withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Add inbound JsonPath expression mappings to manipulate inbound message content.
     * @param mappings
     * @return
     */
    public SwaggerJavaTestGenerator withInboundMappings(Map<String, String> mappings) {
        this.inboundDataDictionary.getMappings().putAll(mappings);
        return this;
    }

    /**
     * Add outbound JsonPath expression mappings to manipulate outbound message content.
     * @param mappings
     * @return
     */
    public SwaggerJavaTestGenerator withOutboundMappings(Map<String, String> mappings) {
        this.outboundDataDictionary.getMappings().putAll(mappings);
        return this;
    }

    /**
     * Add inbound JsonPath expression mappings file to manipulate inbound message content.
     * @param mappingFile
     * @return
     */
    public SwaggerJavaTestGenerator withInboundMappingFile(String mappingFile) {
        this.inboundDataDictionary.setMappingFile(new PathMatchingResourcePatternResolver().getResource(mappingFile));
        try {
            this.inboundDataDictionary.afterPropertiesSet();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to read mapping file", e);
        }
        return this;
    }

    /**
     * Add outbound JsonPath expression mappings file to manipulate outbound message content.
     * @param mappingFile
     * @return
     */
    public SwaggerJavaTestGenerator withOutboundMappingFile(String mappingFile) {
        this.outboundDataDictionary.setMappingFile(new PathMatchingResourcePatternResolver().getResource(mappingFile));
        try {
            this.outboundDataDictionary.afterPropertiesSet();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to read mapping file", e);
        }
        return this;
    }

    /**
     * Gets the swaggerResource.
     *
     * @return
     */
    public String getSwaggerResource() {
        return swaggerResource;
    }

    /**
     * Sets the swaggerResource.
     *
     * @param swaggerResource
     */
    public void setSwaggerResource(String swaggerResource) {
        this.swaggerResource = swaggerResource;
    }

    /**
     * Gets the contextPath.
     *
     * @return
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the contextPath.
     *
     * @param contextPath
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Sets the nameSuffix.
     *
     * @param nameSuffix
     */
    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    /**
     * Gets the nameSuffix.
     *
     * @return
     */
    public String getNameSuffix() {
        return nameSuffix;
    }

    /**
     * Sets the namePrefix.
     *
     * @param namePrefix
     */
    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    /**
     * Gets the namePrefix.
     *
     * @return
     */
    public String getNamePrefix() {
        return namePrefix;
    }

    /**
     * Sets the operation.
     *
     * @param operation
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Gets the operation.
     *
     * @return
     */
    public String getOperation() {
        return operation;
    }
}
