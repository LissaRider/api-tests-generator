package com.consol.citrus.generate.javadsl;

import com.consol.citrus.Generator;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.DefaultGenerator;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import org.apache.maven.project.MavenProject;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.lang.reflect.Field;

public class SwaggerJavaModelGenerator extends Generator {
    private MavenProject project;
    private String baseDir = "C:/Users/Karpyuk/IdeaProjects/test-generators/generator-maven-plugin";

    @Override
    public void create() {
        CodegenConfigurator config = new CodegenConfigurator();
        DefaultGenerator generator = new DefaultGenerator();
        Class<?> clazz = DefaultGenerator.class;
        Field field;

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

        try {
            field = clazz.getDeclaredField("generateApis");
            field.setAccessible(true);
            field.set(generator, false);

            field = clazz.getDeclaredField("generateModels");
            field.setAccessible(true);
            field.set(generator, true);

            field = clazz.getDeclaredField("generateSupportingFiles");
            field.setAccessible(true);
            field.set(generator, false);

            field = clazz.getDeclaredField("generateApiTests");
            field.setAccessible(true);
            field.set(generator, false);

            field = clazz.getDeclaredField("generateApiDocumentation");
            field.setAccessible(true);
            field.set(generator, false);

            field = clazz.getDeclaredField("generateModelTests");
            field.setAccessible(true);
            field.set(generator, false);

            field = clazz.getDeclaredField("generateModelDocumentation");
            field.setAccessible(true);
            field.set(generator, false);

            field = clazz.getDeclaredField("generateSwaggerMetadata");
            field.setAccessible(true);
            field.set(generator, false);

            field = clazz.getDeclaredField("generateSwaggerMetadata");
            field.setAccessible(true);
            field.set(generator, false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

}
