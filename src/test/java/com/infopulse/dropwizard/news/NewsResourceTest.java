package com.infopulse.dropwizard.news;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.skife.jdbi.v2.exceptions.DBIException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Date;

import static java.util.Collections.singletonList;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.client.Entity.text;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class NewsResourceTest {
    private static final NewsItemDAO itemsDao = mock(NewsItemDAO.class);

    @ClassRule
    public static final ResourceTestRule RESOURCE = ResourceTestRule.builder()
            .addResource(new NewsResource(itemsDao)).build();

    public WebTarget NEWS = RESOURCE.client().target("/news");

    @After
    public void tearDown() throws Exception {
        reset(itemsDao);
    }

    private static NewsItem createItem(Long id) {
        return new NewsItem(id, "title" + id, "author" + id, "content" + id, new Date());
    }

    private static NewsItem createItemWithoutId() {
        return new NewsItem("title", "author", "content", new Date());
    }

    @Test
    public void shouldUseDaoForFindAllNews() {
        // given
        Collection<NewsItem> items = singletonList(createItem(1L));
        when(itemsDao.findAll()).thenReturn(items);

        // when
        Collection<NewsItem> retrieved = NEWS.request(APPLICATION_JSON_TYPE).get().readEntity(new GenericType<Collection<NewsItem>>() {
        });

        //then DAO called and proper result returned
        verify(itemsDao).findAll();
        assertThat(retrieved.size()).isEqualTo(1);
        assertThat(retrieved.iterator().next().getId()).isEqualTo(1L);
    }

    @Test
    public void shouldAcceptOnlyJSONForFindAllNews() {
        // given
        Collection<NewsItem> items = singletonList(createItem(1L));
        when(itemsDao.findAll()).thenReturn(items);

        // when using unsupported content type
        Response response = NEWS.request(APPLICATION_ATOM_XML_TYPE).get();

        //then DAO not called and HTTP status 4XX returned
        verifyZeroInteractions(itemsDao);
        assertThat(response.getStatus()).isEqualTo(NOT_ACCEPTABLE.getStatusCode());
    }

    @Test
    public void shouldUseDaoForFindSingleNews() {
        // given
        NewsItem item = createItem(2L);
        when(itemsDao.findById(eq(222L))).thenReturn(item);

        // when
        NewsItem retrieved = NEWS.path("/222").request(APPLICATION_JSON_TYPE).get().readEntity(NewsItem.class);

        //then DAO called and proper result returned
        verify(itemsDao).findById(222L);
        assertThat(retrieved.getTitle()).isEqualTo(item.getTitle());
    }

    @Test
    public void shouldAcceptOnlyJSONForFindSingleNews() {
        // given
        NewsItem item = createItem(2L);
        when(itemsDao.findById(eq(222L))).thenReturn(item);

        // when using unsupported content type
        Response response = NEWS.path("/222").request(APPLICATION_ATOM_XML_TYPE).get();

        //then DAO not called and HTTP status 4XX returned
        verifyZeroInteractions(itemsDao);
        assertThat(response.getStatus()).isEqualTo(NOT_ACCEPTABLE.getStatusCode());
    }

    @Test
    public void shouldNotReturn200IfItemNotFound() {
        // given
        when(itemsDao.findById(eq(333L))).thenReturn(null);

        // when
        Response response = NEWS.path("/333").request(APPLICATION_JSON_TYPE).get();

        //then DAO called and proper result returned
        verify(itemsDao).findById(333L);
        assertThat(response.getStatus()).isNotEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test(expected = ProcessingException.class)
    public void shouldNotReturn200IfDbException() {
        // given DB throws exception
        when(itemsDao.findById(anyLong())).thenThrow(new DBIException("DB error") {
        });

        // when
        NEWS.path("/333").request(APPLICATION_JSON_TYPE).get();

        //then exception
    }

    @Test
    public void shouldUseDaoForAddingNews() {
        // given
        NewsItem item = createItemWithoutId();
        when(itemsDao.insert(any(NewsItem.class))).thenReturn(444L);

        // when
        Long insertedId = NEWS.request(APPLICATION_JSON_TYPE).post(json(item)).readEntity(Long.class);

        //then DAO called and proper result returned
        verify(itemsDao).insert(any(NewsItem.class));
        assertThat(insertedId).isEqualTo(444L);
    }

    @Test
    public void shouldAcceptOnlyJSONForAddingNews() {
        // given
        NewsItem item = createItemWithoutId();
        when(itemsDao.insert(any(NewsItem.class))).thenReturn(444L);

        // when using unsupported content type
        Response response = NEWS.request(APPLICATION_ATOM_XML).post(json(item));

        //then DAO not called and 4XX returned
        verifyZeroInteractions(itemsDao);
        assertThat(response.getStatus()).isEqualTo(NOT_ACCEPTABLE.getStatusCode());
    }

    @Test
    public void shouldAcceptOnlyProperJSONForAddingNews() {
        // given
        when(itemsDao.insert(any(NewsItem.class))).thenReturn(444L);

        // when posting arbitrary data instead of JSON
        Response response = NEWS.request(APPLICATION_JSON_TYPE).post(text("test string"));

        //then DAO not called and 4XX returned
        verifyZeroInteractions(itemsDao);
        assertThat(response.getStatus()).isEqualTo(UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    }

    @Test
    public void shouldValidateItemWhenAddingNews() {
        // given item violating validation
        NewsItem item = new NewsItem(null, "author5", "content5", new Date());
        when(itemsDao.insert(any(NewsItem.class))).thenReturn(555L);

        // when
        Response response = NEWS.request(APPLICATION_JSON_TYPE).post(json(item));

        //then DAO not called and 4XX returned
        verifyZeroInteractions(itemsDao);
        assertThat(response.getStatus()).isEqualTo(422);
    }
}