package com.consol.citrus.generate;

import com.consol.citrus.generate.javadsl.SwaggerJavaModelGenerator;
import org.testng.annotations.Test;

public class SwaggerJavaModelGeneratorTest {
    @Test
    public void testCreateModelAsClient() {
        SwaggerJavaModelGenerator modelGenerator = new SwaggerJavaModelGenerator();

        modelGenerator.setDirectory("src/test/java")
                .setSwaggerResource("swagger/petstore.json");

        modelGenerator.create();
    }
}
