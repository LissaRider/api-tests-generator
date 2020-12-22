package com.consol.citrus.generate;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.generate.javadsl.SwaggerJavaModelGenerator;
import com.consol.citrus.utils.CleanupUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Collections;

public class SwaggerJavaModelGeneratorTest {
    private String testDir = CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "java/com/consol/citrus/model";

    private final CleanupUtils cleanupUtils = new CleanupUtils();

    @AfterMethod
    public void cleanUp(){
       cleanupUtils.deleteFiles(testDir, Collections.singleton("*"));
    }

    @Test
    public void testCreateModelAsClient() {
        SwaggerJavaModelGenerator modelGenerator = new SwaggerJavaModelGenerator();

        modelGenerator.setDirectory("src/test/java");
        modelGenerator.setPackageName("com.consol.citrus");
        modelGenerator.setSwaggerResource("swagger/petstore.json");

        modelGenerator.create();
    }
}
