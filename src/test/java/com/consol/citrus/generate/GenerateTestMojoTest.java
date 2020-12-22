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

package com.consol.citrus.generate;

import com.consol.citrus.GenerateTestMojo;
import com.consol.citrus.config.tests.SwaggerConfiguration;
import com.consol.citrus.config.tests.TestConfiguration;
import com.consol.citrus.generate.javadsl.JavaDslTestGenerator;
import com.consol.citrus.generate.javadsl.SwaggerJavaModelGenerator;
import com.consol.citrus.generate.javadsl.SwaggerJavaTestGenerator;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class GenerateTestMojoTest {

    private JavaDslTestGenerator javaTestGenerator = Mockito.mock(JavaDslTestGenerator.class);
    private SwaggerJavaTestGenerator swaggerJavaTestGenerator = Mockito.mock(SwaggerJavaTestGenerator.class);
    private SwaggerJavaModelGenerator swaggerJavaModelGenerator = Mockito.mock(SwaggerJavaModelGenerator.class);
    private XmlGenerator xmlGenerator = Mockito.mock(XmlGenerator.class);

    private GenerateTestMojo mojo;
    
    @BeforeMethod
    public void setup() {
        mojo = new GenerateTestMojo(javaTestGenerator,
                                    swaggerJavaTestGenerator,
                                    swaggerJavaModelGenerator,
                                    xmlGenerator);
    }
    
    @Test
    public void testCreate() throws PrompterException, MojoExecutionException, MojoFailureException {
        reset(javaTestGenerator);

        TestConfiguration configuration = new TestConfiguration();
        configuration.setName("FooTest");
        configuration.setAuthor("UnknownAuthor");
        configuration.setDescription("TODO");
        configuration.setPackageName("com.consol.citrus.foo");

        when(javaTestGenerator.withFramework(UnitFramework.TESTNG)).thenReturn(javaTestGenerator);
        when(javaTestGenerator.withDisabled(false)).thenReturn(javaTestGenerator);
        when(javaTestGenerator.withAuthor("UnknownAuthor")).thenReturn(javaTestGenerator);
        when(javaTestGenerator.withDescription("TODO")).thenReturn(javaTestGenerator);
        when(javaTestGenerator.usePackage("com.consol.citrus.foo")).thenReturn(javaTestGenerator);
        when(javaTestGenerator.withName("FooTest")).thenReturn(javaTestGenerator);
        when(javaTestGenerator.useSrcDirectory("target/generated/citrus")).thenReturn(javaTestGenerator);

        mojo.setTests(Collections.singletonList(configuration));

        mojo.execute();

        verify(javaTestGenerator).create();
    }
    
    @Test
    public void testSuiteFromSwagger() throws MojoExecutionException, PrompterException, MojoFailureException {
        reset(swaggerJavaTestGenerator);

        TestConfiguration configuration = new TestConfiguration();
        configuration.setName("UserLoginService");
        configuration.setAuthor("UnknownAuthor");
        configuration.setDescription("TODO");
        configuration.setPackageName("com.consol.citrus.swagger");
        configuration.setSuffix("_IT");

        SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
        swaggerConfiguration.setFile("classpath:swagger/petstore.json");
        configuration.setSwagger(swaggerConfiguration);

        when(swaggerJavaTestGenerator.withFramework(UnitFramework.TESTNG)).thenReturn(swaggerJavaTestGenerator);
        when(swaggerJavaTestGenerator.withDisabled(false)).thenReturn(swaggerJavaTestGenerator);
        when(swaggerJavaTestGenerator.withAuthor("UnknownAuthor")).thenReturn(swaggerJavaTestGenerator);
        when(swaggerJavaTestGenerator.withDescription("TODO")).thenReturn(swaggerJavaTestGenerator);
        when(swaggerJavaTestGenerator.usePackage("com.consol.citrus.swagger")).thenReturn(swaggerJavaTestGenerator);

        when(swaggerJavaTestGenerator.withSpec("classpath:swagger/petstore.json")).thenReturn(swaggerJavaTestGenerator);
        when(swaggerJavaTestGenerator.withNameSuffix("_Test")).thenReturn(swaggerJavaTestGenerator);

        when(swaggerJavaTestGenerator.withName("UserLoginService")).thenReturn(swaggerJavaTestGenerator);
        when(swaggerJavaTestGenerator.useSrcDirectory("target/generated/citrus")).thenReturn(swaggerJavaTestGenerator);

        mojo.setTests(Collections.singletonList(configuration));

        mojo.execute();

        verify(swaggerJavaTestGenerator).create();
        verify(swaggerJavaTestGenerator).withSpec("classpath:swagger/petstore.json");
        verify(swaggerJavaTestGenerator).withNameSuffix("_IT");
    }
}
