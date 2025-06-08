package com.urlshortener;

import com.urlshortener.health.BasicHealthCheck;
import com.urlshortener.health.DatabaseHealthCheck;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;

import javax.sql.DataSource;

public class
UrlShortenerApplication extends Application<UrlShortenerConfiguration> {

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
    }

}
