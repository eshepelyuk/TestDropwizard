package com.infopulse.dropwizard;


import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class ServerMainApplication extends Application<ServerMainConfiguration> {
    public static void main(String[] args) throws Exception {
        new ServerMainApplication().run(args);
    }

    @Override
    public void run(ServerMainConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(HelloResource.class);
    }
}
