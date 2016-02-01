package com.infopulse.dropwizard;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloResourceTest {
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new HelloResource())
            .build();

    @Test
    public void shouldSayHello() throws Exception {
        assertThat(resources.client().target("/hello").queryParam("name", "qwe").request().get().readEntity(String.class))
                .isEqualTo("Hello qwe");
    }
}