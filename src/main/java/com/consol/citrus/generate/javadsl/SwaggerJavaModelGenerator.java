package com.consol.citrus.generate.javadsl;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SwaggerJavaModelGenerator {
    private String swaggerResource;
    private String packageName;
    private String directory;

    public void create() {
        OpenAPI openAPI;

        try {
            openAPI = new OpenAPIV3Parser().readContents(FileUtils.readToString(new PathMatchingResourcePatternResolver()
                    .getResource(swaggerResource)), null, null).getOpenAPI();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to parse Swagger Open API specification: " + swaggerResource, e);
        }
        openAPI.getComponents().getSchemas();

        for (Map.Entry<String, Schema> schema : openAPI.getComponents().getSchemas().entrySet()) {
            TypeSpec classBuilder = TypeSpec.classBuilder(schema.getKey())
                    .addModifiers(Modifier.PUBLIC)
                    .build();

            JavaFile javaFile = JavaFile.builder("com.consol.citrus.model", classBuilder)
                    .build();

            try {
                javaFile.writeTo(new File(directory));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getSwaggerResource() {
        return swaggerResource;
    }

    public SwaggerJavaModelGenerator setSwaggerResource(String swaggerResource) {
        this.swaggerResource = swaggerResource;
        return this;
    }

    public String getPackageName() {
        return packageName;
    }

    public SwaggerJavaModelGenerator setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public String getDirectory() {
        return directory;
    }

    public SwaggerJavaModelGenerator setDirectory(String directory) {
        this.directory = directory;
        return this;
    }
}
