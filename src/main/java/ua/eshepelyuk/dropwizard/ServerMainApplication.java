package ua.eshepelyuk.dropwizard;


import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.skife.jdbi.v2.DBI;
import ua.eshepelyuk.dropwizard.news.NewsItemDAO;
import ua.eshepelyuk.dropwizard.news.NewsResource;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

import static org.eclipse.jetty.servlets.CrossOriginFilter.*;

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

        FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, environment.getApplicationContext().getContextPath() + "*");

        filter.setInitParameter(ALLOWED_ORIGINS_PARAM, "*");
        filter.setInitParameter(ALLOWED_HEADERS_PARAM, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        filter.setInitParameter(ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");

        filter.setInitParameter(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
    }
}
