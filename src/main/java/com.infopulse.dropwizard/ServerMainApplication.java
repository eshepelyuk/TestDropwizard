package com.infopulse.dropwizard;


import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class ServerMainApplication extends Application<ServerMainConfiguration> {
    public static void main(String[] args) throws Exception {
        new ServerMainApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<ServerMainConfiguration> bootstrap) {
        super.initialize(bootstrap);
        bootstrap.addBundle(new MigrationsBundle<ServerMainConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(ServerMainConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(ServerMainConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(HelloResource.class);
    }
}
