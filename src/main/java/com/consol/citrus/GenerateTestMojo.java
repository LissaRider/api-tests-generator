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
import com.consol.citrus.generate.TestGenerator;
import com.consol.citrus.generate.ResourcesGenerator;
import com.consol.citrus.generate.javadsl.SwaggerJavaModelGenerator;
import com.consol.citrus.generate.javadsl.SwaggerJavaTestGenerator;
import com.consol.citrus.generate.javadsl.MessageListenerGenerator;
import com.consol.citrus.generate.provider.http.HttpCodeProvider;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

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

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void doExecute() {
        if (skipGenerateTest) {
            return;
        }

        HttpCodeProvider.setCoverage(isCoverage);
        SwaggerJavaTestGenerator.setCoverage(isCoverage);

        for (TestConfiguration test : getTests()) {
            if (test.getSwagger() != null) {
                //Create object models
                SwaggerJavaModelGenerator swaggerJavaModelGenerator = new SwaggerJavaModelGenerator();
                swaggerJavaModelGenerator.setBaseDir(project.getBasedir().getAbsolutePath());
                swaggerJavaModelGenerator.setPackageName(test.getPackageName());
                swaggerJavaModelGenerator.setSwaggerResource(test.getSwagger().getFile());

                swaggerJavaModelGenerator.create();

                //Create MessageListener
                MessageListenerGenerator messageListenerGenerator = new MessageListenerGenerator();
                messageListenerGenerator.setBaseDir(mainDirectory);
                messageListenerGenerator.setPackageName(test.getPackageName());

                messageListenerGenerator.create();

                //Create citrus-context.xml and log4j2.xml
                ResourcesGenerator resourcesGenerator = new ResourcesGenerator();
                resourcesGenerator.setDirectory(resourcesDirectory + "/");
                resourcesGenerator.setPackageName(test.getPackageName());
                resourcesGenerator.setEndpoint(test.getEndpoint());

                resourcesGenerator.create();

                //Create tests
                SwaggerJavaTestGenerator generator = new SwaggerJavaTestGenerator();

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
            }
        }
    }
}
