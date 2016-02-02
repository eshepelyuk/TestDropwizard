package com.infopulse.dropwizard;


import com.infopulse.dropwizard.news.NewsItemDAO;
import com.infopulse.dropwizard.news.NewsResource;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;

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
        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "h2");
        final NewsItemDAO newsItemDAO = jdbi.onDemand(NewsItemDAO.class);

        environment.jersey().register(new NewsResource(newsItemDAO));
    }
}
