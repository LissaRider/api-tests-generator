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
public class HelloIT extends TestNGCitrusSupport {
    @CitrusTest
    @Test
    @Parameters("runner")
    public void helloIT(@CitrusResource @Optional TestCaseRunner runner) {
        runner.run(echo("TODO: Code the test HelloIT"));

        runner.run(send().endpoint("client")
            .payload("<hel:Hello xmlns:hel=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">\n"
                    + "  <hel:Text>citrus:randomString(6)</hel:Text>\n"
                    + "</hel:Hello>")
        );

    }
}
