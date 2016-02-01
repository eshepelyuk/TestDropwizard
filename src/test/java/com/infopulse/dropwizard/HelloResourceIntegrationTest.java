package com.infopulse.dropwizard;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloResourceIntegrationTest {
    @ClassRule
    public static final DropwizardAppRule<ServerMainConfiguration> RULE =
            new DropwizardAppRule<ServerMainConfiguration>(ServerMainApplication.class);

    @Test
    public void loginHandlerRedirectsAfterPost() {
        Client client = new JerseyClientBuilder().build();

        String response = client.target(
                String.format("http://localhost:%d/hello?name=%s", RULE.getLocalPort(), "qwe"))
                .request().get().readEntity(String.class);

        assertThat(response).isEqualTo("Hello qwe");
    }
}