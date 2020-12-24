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

package com.consol.citrus;

import com.consol.citrus.config.tests.TestConfiguration;
import com.consol.citrus.generate.SwaggerTestGenerator;
import com.consol.citrus.generate.TestGenerator;
import com.consol.citrus.generate.XmlGenerator;
import com.consol.citrus.generate.javadsl.JavaDslTestGenerator;
import com.consol.citrus.generate.javadsl.SwaggerJavaModelGenerator;
import com.consol.citrus.generate.javadsl.SwaggerJavaTestGenerator;
import com.consol.citrus.generate.provider.http.HttpCodeProvider;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
@Mojo( name = "generate-tests", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class GenerateTestMojo extends AbstractCitrusMojo {

    @Parameter(property = "citrus.skip.generate.test", defaultValue = "false")
    protected boolean skipGenerateTest;

    @Parameter(property = "citrus.build.directory", defaultValue= "${project.basedir}/src/main/java")
    protected String mainDirectory = "src/main/java";

    @Parameter(property = "citrus.build.directory", defaultValue= "${project.basedir}/src/test/resources")
    protected String resourcesDirectory = "src/test/resources";

    @Parameter(property = "citrus.build.directory", defaultValue= "${project.build.directory}/generated/citrus")
    protected String buildDirectory = "target/generated/citrus";

    @Parameter(property = "citrus.build.coverage", defaultValue = "false")
    protected boolean isCoverage;

    private final JavaDslTestGenerator javaTestGenerator;
    private final SwaggerJavaTestGenerator swaggerJavaTestGenerator;
    private final SwaggerJavaModelGenerator swaggerJavaModelGenerator;
    private final XmlGenerator xmlGenerator;

    /**
     * Default constructor.
     */
    public GenerateTestMojo() {
        this(new JavaDslTestGenerator(),
                new SwaggerJavaTestGenerator(),
                new SwaggerJavaModelGenerator(),
                new XmlGenerator());
    }

    /**
     * Constructor using final fields.
     * @param javaTestGenerator
     * @param swaggerJavaTestGenerator
     */
    public GenerateTestMojo(JavaDslTestGenerator javaTestGenerator,
                            SwaggerJavaTestGenerator swaggerJavaTestGenerator,
                            SwaggerJavaModelGenerator swaggerJavaModelGenerator,
                            XmlGenerator xmlGenerator) {
        this.javaTestGenerator = javaTestGenerator;
        this.swaggerJavaTestGenerator = swaggerJavaTestGenerator;
        this.swaggerJavaModelGenerator = swaggerJavaModelGenerator;
        this.xmlGenerator = xmlGenerator;
    }

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        if (skipGenerateTest) {
            return;
        }

        HttpCodeProvider.setCoverage(isCoverage);
        SwaggerJavaTestGenerator.setCoverage(isCoverage);

        for (TestConfiguration test : getTests()) {
            if (test.getSwagger() != null) {
                //Create object model
                SwaggerJavaModelGenerator modelGenerator = getSwaggerJavaModelGenerator();

                modelGenerator.setDirectory(mainDirectory);
                modelGenerator.setPackageName(test.getPackageName());
                modelGenerator.setSwaggerResource(test.getSwagger().getFile());

                //Create citrus-context.xml and pom.xml
                XmlGenerator xmlGenerator = new XmlGenerator();
                xmlGenerator.setDirectory(resourcesDirectory + "/");

                xmlGenerator.create();

                //Create tests
                SwaggerTestGenerator generator = getSwaggerTestGenerator();

                generator.withFramework(getFramework())
                        .withName(test.getName())
                        .withAuthor(test.getAuthor())
                        .withDescription(test.getDescription())
                        .usePackage(test.getPackageName())
                        .useSrcDirectory(buildDirectory);

                generator.withDisabled(test.isDisabled());
                generator.withMode(TestGenerator.GeneratorMode.valueOf(test.getSwagger().getMode()));
                generator.withSpec(test.getSwagger().getFile());
                generator.withOperation(test.getSwagger().getOperation());


                if (test.getSwagger().getMappings() != null) {
                    generator.withInboundMappings(test.getSwagger().getMappings().getInbound());
                    generator.withOutboundMappings(test.getSwagger().getMappings().getOutbound());
                    generator.withInboundMappingFile(test.getSwagger().getMappings().getInboundFile());
                    generator.withOutboundMappingFile(test.getSwagger().getMappings().getOutboundFile());
                }

                generator.withEndpoint(test.getEndpoint());

                generator.withNameSuffix(test.getSuffix());

                generator.create();

                modelGenerator.create();
            } else {
                if (!StringUtils.hasText(test.getName())) {
                    throw new MojoExecutionException("Please provide proper test name! Test name must not be empty starting with uppercase letter!");
                }

                if (getType().equals("java")) {
                    JavaDslTestGenerator generator = (JavaDslTestGenerator) getJavaTestGenerator()
                            .withDisabled(test.isDisabled())
                            .withFramework(getFramework())
                            .withName(test.getName())
                            .withAuthor(test.getAuthor())
                            .withDescription(test.getDescription())
                            .usePackage(test.getPackageName())
                            .useSrcDirectory(buildDirectory);

                    generator.create();
                }

                getLog().info("Successfully created new test case " + test.getPackageName() + "." + test.getName());
            }
        }
    }

    /**
     * Method provides test generator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized generator instance.
     * .
     * @return test generator.
     */
    public JavaDslTestGenerator getJavaTestGenerator() {
        return Optional.ofNullable(javaTestGenerator).orElse(new JavaDslTestGenerator());
    }

    /**
     * Method provides test generator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized generator instance.
     * .
     * @return test generator.
     */
    public SwaggerTestGenerator getSwaggerTestGenerator() {
        return Optional.ofNullable(swaggerJavaTestGenerator).orElse(new SwaggerJavaTestGenerator());
    }

    /**
     * Method provides model generator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized generator instance.
     * .
     * @return model generator.
     */
    public SwaggerJavaModelGenerator getSwaggerJavaModelGenerator() {
        return Optional.ofNullable(swaggerJavaModelGenerator).orElse(new SwaggerJavaModelGenerator());
    }

    public XmlGenerator getXmlGenerator() {
        return Optional.ofNullable(xmlGenerator).orElse(new XmlGenerator());
    }
}
