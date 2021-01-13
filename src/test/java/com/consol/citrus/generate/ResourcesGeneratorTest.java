package com.consol.citrus.generate;

import org.testng.annotations.Test;

import java.nio.file.Paths;

public class ResourcesGeneratorTest {
    private String testDir = Paths.get(".").toAbsolutePath().normalize().toString();

    //@Test
    public void testCreateResources() {
        ResourcesGenerator resourcesGenerator = new ResourcesGenerator();

        resourcesGenerator.setDirectory(testDir + "/src/test/resources/");

        resourcesGenerator.create();
    }
}
