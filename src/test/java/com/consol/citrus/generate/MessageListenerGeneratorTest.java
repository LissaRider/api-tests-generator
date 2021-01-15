package com.consol.citrus.generate;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.generate.javadsl.MessageListenerGenerator;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.utils.CleanupUtils;
import org.springframework.core.io.FileSystemResource;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class MessageListenerGeneratorTest {
    private String testDir = CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "java";

    private final CleanupUtils cleanupUtils = new CleanupUtils();

    @AfterMethod
    public void cleanUp(){
       cleanupUtils.deleteFiles(testDir, Collections.singleton("MessageListener.java"));
    }

    @Test
    public void testCreateMessageListener() throws IOException {
        MessageListenerGenerator messageListenerGenerator = new MessageListenerGenerator();

        messageListenerGenerator.setBaseDir(testDir);
        messageListenerGenerator.setPackageName("com.consol.citrus");

        messageListenerGenerator.create();

        verifyListener("MessageListener");
    }

    private void verifyListener(String name) throws IOException {
        File javaFile = new File(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "java/com/consol/citrus/" + name + ".java");
        Assert.assertTrue(javaFile.exists());

        String javaContent = FileUtils.readToString(new FileSystemResource(javaFile));
        Assert.assertTrue(javaContent.contains("public class " + name));
        Assert.assertTrue(javaContent.contains("protected static Logger log = LogManager.getLogger(com.consol.citrus.report.MessageListener.class.getSimpleName());"));
        Assert.assertTrue(javaContent.contains("public static MessageTracingTestListener messageTracingTestListener()"));
        Assert.assertTrue(javaContent.contains("private static class CustomMessageListener extends MessageTracingTestListener"));
        Assert.assertTrue(javaContent.contains("public void onInboundMessage(Message message, TestContext context)"));
        Assert.assertTrue(javaContent.contains("public void onOutboundMessage(Message message, TestContext context)"));
        Assert.assertTrue(javaContent.contains("public void onTestFinish(TestCase test)"));
        Assert.assertTrue(javaContent.contains("private String separator()"));
        Assert.assertTrue(javaContent.contains("public void afterPropertiesSet() throws Exception"));
    }
}
