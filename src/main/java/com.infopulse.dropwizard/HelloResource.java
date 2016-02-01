package com.infopulse.dropwizard;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/hello")
public class HelloResource {
    @GET
    public String sayHello(@QueryParam("name") String name) {
        return "Hello " + name;
    }
}
