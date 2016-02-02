package com.infopulse.dropwizard.news;

import com.infopulse.dropwizard.ServerMainApplication;
import com.infopulse.dropwizard.ServerMainConfiguration;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.util.Collection;
import java.util.Date;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

public class NewsIntegrationTest {

    @ClassRule
    public static final DropwizardAppRule<ServerMainConfiguration> RULE =
            new DropwizardAppRule<>(ServerMainApplication.class, ResourceHelpers.resourceFilePath("test.yml"));

    static DBI jdbi = null;

    static ManagedDataSource DS = null;

    @BeforeClass
    public static void setUpClass() throws Exception {
        DBIFactory factory = new DBIFactory();
        jdbi = factory.build(RULE.getEnvironment(), RULE.getConfiguration().getDataSourceFactory(), "h2");

        DS = RULE.getConfiguration().getDataSourceFactory().build(RULE.getEnvironment().metrics(), "migrations");
        DS.start();

        try (Connection connection = DS.getConnection()) {
            new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(connection)).update("");
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        DS.stop();
    }

    public WebTarget NEWS = new JerseyClientBuilder().build().target(format("http://localhost:%d/news", RULE.getLocalPort()));

    @Test
    public void whenNewsPostedThenItAppearsInAllItemsList() {
        //when posting news item
        Response postNewsResp = NEWS.request(APPLICATION_JSON_TYPE).post(json(new NewsItem("title1", "author1", "content1", new Date())));
        Long insertedId = postNewsResp.readEntity(Long.class);

        //then postNewsResp is OK, DB populated with corresponding item
        assertThat(postNewsResp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat((Long) (jdbi.withHandle(handle -> (Long) handle.select("select count(*) as cnt from news_item where id = :id", insertedId).get(0).get("cnt")))).isEqualTo(1L);

        //when getting list of items
        Collection<NewsItem> items = NEWS.request(APPLICATION_JSON_TYPE).get(new GenericType<Collection<NewsItem>>() {
        });

        //then only one item is present, and id matches
        assertThat(items.size()).isEqualTo(1);
        assertThat(items.iterator().next().getId()).isEqualTo(insertedId);
    }

    @Test
    public void whenNewsPostedThenItCanBeRetrievedSeparately() {
        //given
        NewsItem originalItem = new NewsItem("title2", "author2", "content2", new Date());

        //when posting news item
        Response postNewsResp = NEWS.request(APPLICATION_JSON_TYPE).post(json(originalItem));
        Long insertedId = postNewsResp.readEntity(Long.class);

        //then postNewsResp is OK, DB populated with corresponding item
        assertThat(postNewsResp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat((Long) (jdbi.withHandle(handle -> (Long) handle.select("select count(*) as cnt from news_item where id = :id", insertedId).get(0).get("cnt")))).isEqualTo(1L);

        //when getting single item
        NewsItem retrievedItem = NEWS.path("/" + insertedId).request(APPLICATION_JSON_TYPE).get(NewsItem.class);

        //then only one item is present, and all field match
        assertThat(retrievedItem.getTitle()).isEqualTo(originalItem.getTitle());
        assertThat(retrievedItem.getAuthor()).isEqualTo(originalItem.getAuthor());
        assertThat(retrievedItem.getContent()).isEqualTo(originalItem.getContent());
        assertThat(retrievedItem.getPublishDate()).isEqualTo(originalItem.getPublishDate());
    }
}
