package com.infopulse.dropwizard.news;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/news")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NewsResource {

    private NewsItemDAO newsItemDAO;

    public NewsResource(NewsItemDAO newsItemDAO) {
        this.newsItemDAO = newsItemDAO;
    }

    @POST
    public Long addItem(@Valid NewsItem newsItem) {
        return newsItemDAO.insert(newsItem);
    }

    @GET
    public Collection<NewsItem> listItems() {
        return newsItemDAO.findAll();
    }

    @GET
    @Path("{id}")
    public NewsItem findItemById(@PathParam("id") Long id) {
        return newsItemDAO.findById(id);
    }
}
