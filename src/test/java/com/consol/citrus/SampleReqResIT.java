package com.consol.citrus;

import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This is a sample test
 *
 * @author Christoph
 * @since 2020-07-31
 */
public class SampleReqResIT extends TestNGCitrusSupport {
    @CitrusTest
    @Test
    @Parameters("runner")
    public void sampleReqResIT(@CitrusResource @Optional TestCaseRunner runner) {
        runner.run(echo("TODO: Code the test SampleReqResIT"));

        runner.run(send().endpoint("client")
            .payload("<TestRequest><Message>Citrus rocks!</Message></TestRequest>")
        );

        runner.run(receive().endpoint("client")
            .payload("<TestResponse><Message>Hell Ya!</Message></TestResponse>")
        );

    }
}
