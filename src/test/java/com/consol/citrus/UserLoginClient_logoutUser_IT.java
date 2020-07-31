package com.consol.citrus;

import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This is a sample test
 *
 * @author Christoph
 * @since 2020-07-31
 */
public class UserLoginClient_logoutUser_IT extends TestNGCitrusSupport {
    @CitrusTest
    @Test
    @Parameters("runner")
    public void userLoginClient_logoutUser_IT(@CitrusResource @Optional TestCaseRunner runner) {
        runner.run(echo("TODO: Code the test UserLoginClient_logoutUser_IT"));

        runner.run(http().client("client")
            .send()
            .get("/v1/user/logout")
            .contentType("application/json")
        );

        runner.run(http().client("client")
            .receive()
            .response(HttpStatus.NOT_FOUND)
        );

    }
}
