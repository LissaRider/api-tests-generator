package com.consol.citrus.generate;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.utils.CleanupUtils;
import org.springframework.core.io.FileSystemResource;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;

public class ResourcesGeneratorTest {
    private String testDir = Paths.get(".").toAbsolutePath().normalize().toString() + "/src/test/resources/";

    private final CleanupUtils cleanupUtils = new CleanupUtils();

    @AfterMethod
    public void cleanUp(){
        cleanupUtils.deleteFiles(testDir, Collections.singleton("log4j2.xml"));
        cleanupUtils.deleteFiles(testDir, Collections.singleton("citrus-context.xml"));
    }

    @Test
    public void testCreateResources() throws IOException {
        ResourcesGenerator resourcesGenerator = new ResourcesGenerator();

        resourcesGenerator.setDirectory(testDir);
        resourcesGenerator.setPackageName("com.consol.citrus");
        resourcesGenerator.setEndpoint("httpClient");

        resourcesGenerator.create();

        verifyResource("log4j2.xml");
        verifyResource("citrus-context.xml");
    }

    private void verifyResource(String name) throws IOException {
        File file = new File(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "resources/" + name);
        Assert.assertTrue(file.exists());

        String javaContent = FileUtils.readToString(new FileSystemResource(file));
        Assert.assertTrue(javaContent.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    }
}
