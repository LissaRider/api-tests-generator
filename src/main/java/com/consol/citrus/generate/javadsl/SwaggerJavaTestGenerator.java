package com.consol.citrus.generate.javadsl;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.generate.SwaggerTestGenerator;
import com.consol.citrus.http.actions.HttpActionBuilder;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.variable.dictionary.json.JsonPathMappingDataDictionary;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SwaggerJavaTestGenerator extends MessagingJavaTestGenerator<SwaggerJavaTestGenerator> implements SwaggerTestGenerator<SwaggerJavaTestGenerator> {
    /** Loop counter for recursion */
    private Map<String, Integer> control;

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
        OpenAPI openAPI;

        try {
            openAPI = new OpenAPIV3Parser().readContents(FileUtils.readToString(new PathMatchingResourcePatternResolver()
                    .getResource(swaggerResource)), null, null).getOpenAPI();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to parse Swagger Open API specification: " + swaggerResource, e);
        }

        if (!StringUtils.hasText(namePrefix)) {
            String title = openAPI.getInfo().getTitle();
            if (title != null) {
                title = title.replaceAll("[^A-Za-z0-9]", "");
                if (title.matches("^[A-Za-z]+")) {
                    title = title.substring(0, 1).toUpperCase() + title.substring(1);
                } else {
                    title = null;
                }
            }
            withNamePrefix(StringUtils.trimAllWhitespace(Optional.ofNullable(title).orElse("Swagger")) + "_");
        }

        for (Map.Entry<String, PathItem> path : openAPI.getPaths().entrySet()) {
            for (Map.Entry<PathItem.HttpMethod, Operation> operation : path.getValue().readOperationsMap().entrySet()) {

                ApiResponses responses = operation.getValue().getResponses();

                if (responses.containsKey("200") || responses.containsKey("default")) {

                    // Now generate it

                    if (operation.getValue().getOperationId() != null) {
                        withName(namePrefix + operation.getValue().getOperationId()  + nameSuffix);
                    } else {
                        String endpointName = getEndpointName(path.getKey());
                        withName(String.format("%s%s_%s%s", namePrefix, operation.getKey().name(), endpointName, nameSuffix));
                    }

                    HttpMessage requestMessage = new HttpMessage();

                    requestMessage.path(Optional.ofNullable(contextPath).orElse("") + Optional
                            .ofNullable(openAPI.getServers().get(0).getUrl())
                            .filter(basePath -> !basePath.equals("/")).orElse("") + path.getKey());

                    requestMessage.method(org.springframework.http.HttpMethod.valueOf(operation.getKey().name()));


                    if (operation.getValue().getParameters() != null) {

                        operation.getValue().getParameters().stream()
                                .filter(p -> p instanceof HeaderParameter)
                                .filter(Parameter::getRequired)
                                .forEach(p -> requestMessage.setHeader(p.getName(), null));

                        operation.getValue().getParameters().stream()
                                .filter(param -> param instanceof QueryParameter)
                                .filter(Parameter::getRequired)
                                .forEach(param -> requestMessage.queryParam(param.getName(),null));

                        if (isCoverage) {
                            operation.getValue().getParameters().stream()
                                    .filter(p -> p instanceof PathParameter)
                                    .filter(Parameter::getRequired)
                                    .forEach(p -> requestMessage.setHeader("{" + p.getName() + "}", null));
                        }
                    }

                    RequestBody requestBody = operation.getValue().getRequestBody();

                    //TODO: Add JsonParser
                    if (requestBody != null) {
                        requestMessage.setPayload("");
                    }

                    withRequest(requestMessage);

                    HttpMessage responseMessage = new HttpMessage();
                    ApiResponse response = operation.getValue().getResponses().get("200");

                    if (response == null) {
                        response = operation.getValue().getResponses().get("default");
                    }

                    if (response != null) {
                        responseMessage.status(HttpStatus.OK);
                        
                        if (response.getHeaders() != null) {
                            for (Map.Entry<String, Header> header : response.getHeaders().entrySet()) {
                                responseMessage.setHeader(header.getKey(), createValidationHeader(header.getValue()));
                            }
                        }

                        if (response.getContent() != null) {
                            Schema responseSchema = response.getContent().get("application/json").getSchema();
                            control = new HashMap<>();
                            responseMessage.setPayload(createValidationExpression(responseSchema, openAPI.getComponents().getSchemas()));
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
     * Create test name from endpoint.
     */
    private String getEndpointName(String endpoint) {
        StringBuilder sb = new StringBuilder();
        String[] str = Arrays.stream(endpoint.split("/"))
                .filter(s -> !s.contains("{")).toArray(String[]::new);

        for (String s : str) {
            if (s.length() > 0 && Character.isAlphabetic(s.charAt(0))) {
                char upper = Character.toUpperCase(s.charAt(0));
                sb.append(upper).append(s.substring(1));
            } else {
                sb.append(s);
            }
        }

        return sb.toString();
    }

    /**
     * Create validation expression using functions according to parameter type and format.
     * property - Property.
     * definitions - Map<String, Model>.
     * @return validation JSON schema.
     */
    private String createValidationExpression(Schema schema, Map<String, Schema> schemas) {
        StringBuilder payload = new StringBuilder();
        String type = schema.getType();
        String format = "null";

        if (schema.getFormat() != null) {
            format = schema.getFormat();
        }

        boolean permit = true;

        if (type == null) {
            String[] str = schema.get$ref().split("/");
            String ref = str[str.length - 1];

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

                Schema object = schemas.get(ref);

                payload.append("{");
                if (object.getProperties() != null) {
                    Map<String, Schema> map = object.getProperties();
                    for (Map.Entry<String, Schema> entry : map.entrySet()) {
                        payload.append("\"").append(entry.getKey()).append("\": ").append(createValidationExpression(entry.getValue(), schemas)).append(",");
                    }
                }

                control.put(ref, control.get(ref) - 1);

                if (payload.toString().endsWith(",")) {
                    payload.replace(payload.length() - 1, payload.length(), "");
                }

                payload.append("}");
            }
        } else if (type.equals("array")) {
            payload.append("\"@ignore@\"");
        } else if (type.equals("object") && schema.getAdditionalProperties() != null) {
            payload.append("\"@ignore@\"");
        } else if (type.equals("string") && format.equals("date")) {
            payload.append("\"@matchesDatePattern('yyyy-MM-dd')@\"");
        } else if (type.equals("string") && format.equals("date-time")) {
            payload.append("\"@matchesDatePattern('yyyy-MM-dd'T'hh:mm:ss')@\"");
        } else if (type.equals("string")) {
            if (!CollectionUtils.isEmpty(schema.getEnum())) {
                payload.append("\"@matches(").append(schema.getEnum().stream().collect(Collectors.joining("|"))).append(")@\"");
            } else {
                payload.append("\"@notEmpty()@\"");
            }
        } else if (type.equals("integer") || type.equals("number")) {
            payload.append("\"@isNumber()@\"");
        } else if (type.equals("boolean")) {
            payload.append("\"@matches(true|false)@\"");
        } else {
            payload.append("\"@ignore@\"");
        }

        return payload.toString();
    }

    /**
     * Create validation expression using functions according to parameter type and format.
     * @param header
     * @return validation parameter.
     */
    private String createValidationHeader(Header header) {
        switch (header.getSchema().getType()) {
            case "integer":
            case "number":
                return "@isNumber()@";
            case "string":
                if (header.getSchema().getFormat() != null && header.getSchema().getFormat().equals("date")) {
                    return "\"@matchesDatePattern('yyyy-MM-dd')@\"";
                } else if (header.getSchema().getFormat() != null && header.getSchema().getFormat().equals("date-time")) {
                    return "\"@matchesDatePattern('yyyy-MM-dd'T'hh:mm:ss')@\"";
                } else if (StringUtils.hasText(header.getSchema().getPattern())) {
                    return "\"@matches(" + header.getSchema().getPattern() + ")@\"";
                } else if (!CollectionUtils.isEmpty(header.getSchema().getEnum())) {
                    return "\"@matches(" + (header.getSchema().getEnum().stream().collect(Collectors.joining("|"))) + ")@\"";
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
