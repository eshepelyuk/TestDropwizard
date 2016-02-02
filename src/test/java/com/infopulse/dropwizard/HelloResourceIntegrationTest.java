package com.infopulse.dropwizard;

import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import liquibase.Liquibase;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.client.Client;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloResourceIntegrationTest {
    @ClassRule
    public static final DropwizardAppRule<ServerMainConfiguration> RULE =
            new DropwizardAppRule<>(ServerMainApplication.class, ResourceHelpers.resourceFilePath("main.yml"));

    static DBI jdbi = null;

    static ManagedDataSource DS = null;

    @BeforeClass
    public static void setUpClass() throws Exception {
        DBIFactory factory = new DBIFactory();
        jdbi = factory.build(RULE.getEnvironment(), RULE.getConfiguration().getDataSourceFactory(), "h2");

        DS = RULE.getConfiguration().getDataSourceFactory().build(RULE.getEnvironment().metrics(), "migrations");
        DS.start();

        try (Connection connection = DS.getConnection()) {
            Liquibase migrator = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new liquibase.database.jvm.JdbcConnection(connection));
            migrator.update("");
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        DS.stop();
    }

    @Test
    public void shouldSayHello() {
        Client client = new JerseyClientBuilder().build();

        String response = client.target(
                String.format("http://localhost:%d/hello?name=%s", RULE.getLocalPort(), "qwe"))
                .request().get().readEntity(String.class);

        assertThat(response).isEqualTo("Hello qwe");
        assertThat((Long) (jdbi.withHandle(handle -> (Long) handle.select("select count(*) as cnt from public.people").get(0).get("cnt")))).isEqualTo(0L);
    }
}