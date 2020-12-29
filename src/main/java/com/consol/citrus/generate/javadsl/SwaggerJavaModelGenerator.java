package com.consol.citrus.generate.javadsl;

import com.consol.citrus.Generator;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.lang.reflect.Field;

public class SwaggerJavaModelGenerator extends Generator {

    @Override
    public void create() {
        CodegenConfigurator config = new CodegenConfigurator();
        DefaultGenerator generator = new DefaultGenerator();
        Class<?> clazz = DefaultGenerator.class;

        try {
            config.setInputSpec(FileUtils.readToString(new PathMatchingResourcePatternResolver()
                    .getResource(swaggerResource)));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to parse Swagger Open API specification: " + swaggerResource, e);
        }

        config.setLang("java");
        config.setOutputDir(baseDir);
        config.setModelPackage(packageName + ".models");

        ClientOptInput input = config.toClientOptInput();

        generator.opts(input);

        System.setProperty("generateApis", "false");
        System.setProperty("generateModels", "true");
        System.setProperty("supportingFiles", "false");
        System.setProperty("modelTests", "false");
        System.setProperty("modelDocs", "false");
        System.setProperty("apiTests", "false");
        System.setProperty("apiDocs", "false");

        try {
            Field field = clazz.getDeclaredField("generateSwaggerMetadata");
            field.setAccessible(true);
            field.set(generator, false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        generator.generate();
    }

}
