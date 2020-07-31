package com.consol.citrus;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * TODO: Description
 *
 * @author Unknown
 * @since 2020-07-31
 */
public class SampleIT extends TestNGCitrusSupport {
    @CitrusXmlTest(
            name = "SampleIT"
    )
    @Test
    @Parameters("runner")
    public void sampleIT(@CitrusResource @Optional TestCaseRunner runner) {
    }
}
