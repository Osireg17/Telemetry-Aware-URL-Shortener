package com.urlshortener;

import com.urlshortener.api.LinksResource;
import com.urlshortener.api.RedirectResource;
import com.urlshortener.core.Base62Service;
import com.urlshortener.db.LinkDAO;
import com.urlshortener.health.BasicHealthCheck;
import com.urlshortener.health.DatabaseHealthCheck;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import javax.sql.DataSource;

public class UrlShortenerApplication extends Application<UrlShortenerConfiguration> {

    public static void main(final String[] args) throws Exception {
        new UrlShortenerApplication().run(args);
    }

    @Override
    public String getName() {
        return "UrlShortener";
    }

    @Override
    public void initialize(final Bootstrap<UrlShortenerConfiguration> bootstrap) {
        bootstrap.addBundle(new MigrationsBundle<UrlShortenerConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(UrlShortenerConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }


    @Override
    public void run(final UrlShortenerConfiguration configuration,
            final Environment environment) {

        environment.healthChecks().register("application", new BasicHealthCheck("1.0.0"));

        DataSource dataSource = configuration.getDataSourceFactory().build(environment.metrics(), "database");
        environment.healthChecks().register("database", new DatabaseHealthCheck(dataSource));

        Jdbi jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());

        // Create tables if they don't exist
        jdbi.useHandle(handle -> {
            handle.execute("CREATE TABLE IF NOT EXISTS links (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "long_url TEXT NOT NULL, " +
                "short_code VARCHAR(12) NOT NULL UNIQUE, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        });

        LinkDAO linkDAO = jdbi.onDemand(LinkDAO.class);
        Base62Service base62Service = new Base62Service();

        LinksResource linksResource = new LinksResource(linkDAO, base62Service);
        RedirectResource redirectResource = new RedirectResource(linkDAO);
        
        environment.jersey().register(linksResource);
        environment.jersey().register(redirectResource);
    }
}
