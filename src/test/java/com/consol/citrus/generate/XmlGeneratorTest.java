package com.consol.citrus.generate;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class XmlGeneratorTest {
    private String testDir = Paths.get(".").toAbsolutePath().normalize().toString();

    @Test(enabled = false)
    public void testCreateCitrusContext() throws IOException {
        XmlGenerator xmlGenerator = new XmlGenerator();

        xmlGenerator.setDirectory(testDir + "/src/test/resources/");

        xmlGenerator.create();
    }
}
