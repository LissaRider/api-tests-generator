package com.consol.citrus.generate.javadsl;

import com.consol.citrus.Generator;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.squareup.javapoet.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SwaggerJavaModelGenerator extends Generator {

    @Override
    public void create() {
        OpenAPI openAPI;

        //TODO: дублировние парсинга, надо вынести
        try {
            openAPI = new OpenAPIV3Parser().readContents(FileUtils.readToString(new PathMatchingResourcePatternResolver()
                    .getResource(swaggerResource)), null, null).getOpenAPI();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to parse Swagger Open API specification: " + swaggerResource, e);
        }

        //Сомнительная идея
        for (Map.Entry<String, Schema> schema : openAPI.getComponents().getSchemas().entrySet()) {
            Map<String, Schema> properties = schema.getValue().getProperties();
            Iterable<FieldSpec> fieldSpecs;
            List<FieldSpec> list = new ArrayList<>();

            for (Map.Entry<String, Schema> property : properties.entrySet()) {
                String type = property.getValue().getType();
                TypeName typeName = TypeName.OBJECT;
                if (type != null) {
                    switch (type) {
                        case "integer":
                            typeName = property.getValue().getFormat().equals("int32") ?
                                    ClassName.get(Integer.class) : ClassName.get(Long.class);
                            break;
                        case "number":
                            typeName = ClassName.get(Double.class);
                            break;
                        case "string":
                            typeName = ClassName.get(String.class);
                            break;
                        case "boolean":
                            typeName = ClassName.get(Boolean.class);
                            break;
                        case "array":
                            typeName = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Object.class));
                            break;
                    }
                }
                list.add(FieldSpec.builder(typeName, property.getKey()).addModifiers(Modifier.PUBLIC).build());
            }

            fieldSpecs = list;

            TypeSpec classBuilder = TypeSpec.classBuilder(schema.getKey())
                    .addModifiers(Modifier.PUBLIC)
                    .addFields(fieldSpecs)
                    .addAnnotation(Data.class)
                    .addAnnotation(Builder.class)
                    .build();

            JavaFile javaFile = JavaFile.builder(packageName + ".model", classBuilder)
                    .build();

            try {
                javaFile.writeTo(new File(directory));
                log.info("Successfully created class: " + schema.getKey());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
