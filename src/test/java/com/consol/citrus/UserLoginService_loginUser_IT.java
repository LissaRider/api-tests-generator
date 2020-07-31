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
public class UserLoginService_loginUser_IT extends TestNGCitrusSupport {
    @CitrusTest
    @Test
    @Parameters("runner")
    public void userLoginService_loginUser_IT(@CitrusResource @Optional TestCaseRunner runner) {
        runner.run(echo("TODO: Code the test UserLoginService_loginUser_IT"));

        runner.run(http().server("server")
            .receive()
            .get("@assertThat(matchesPath(/user/login))@")
            .contentType("application/json")
            .queryParam("password", "@notEmpty()@")
            .queryParam("username", "@notEmpty()@")
        );

        runner.run(http().server("server")
            .send()
            .response(HttpStatus.NOT_FOUND)
            .header("X-Rate-Limit", "citrus:randomNumber(10)")
            .header("X-Expires-After", "citrus:currentDate('yyyy-MM-dd'T'hh:mm:ss')")
            .payload("\"citrus:randomString(10)\"")
        );

    }
}
