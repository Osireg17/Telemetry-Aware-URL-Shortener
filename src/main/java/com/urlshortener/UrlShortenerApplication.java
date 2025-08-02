package com.urlshortener;

import com.urlshortener.api.LinksResource;
import com.urlshortener.api.MigrationStatusResource;
import com.urlshortener.api.RedirectResource;
import com.urlshortener.core.Base62Service;
import com.urlshortener.db.LinkDAO;
import com.urlshortener.health.BasicHealthCheck;
import com.urlshortener.health.DatabaseHealthCheck;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import javax.sql.DataSource;

public class UrlShortenerApplication extends Application<UrlShortenerConfiguration> {

    private Jdbi jdbi;

    public static void main(final String[] args) throws Exception {
        new UrlShortenerApplication().run(args);
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    @Override
    public String getName() {
        return "UrlShortener";
    }

    @Override
    public void initialize(final Bootstrap<UrlShortenerConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)));
        
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

        // Use configured version instead of hardcoded value
        UrlShortenerConfiguration.ApplicationConfiguration appConfig = configuration.getApplication();
        environment.healthChecks().register("application",
                new BasicHealthCheck(appConfig.getVersion(), appConfig.getName()));

        DataSource dataSource = configuration.getDataSourceFactory().build(environment.metrics(), "database");
        environment.healthChecks().register("database", new DatabaseHealthCheck(dataSource));

        this.jdbi = Jdbi.create(dataSource);
        this.jdbi.installPlugin(new SqlObjectPlugin());

        LinkDAO linkDAO = this.jdbi.onDemand(LinkDAO.class);
        Base62Service base62Service = new Base62Service();

        // Pass application configuration to resources
        LinksResource linksResource = new LinksResource(linkDAO, base62Service, appConfig);
        RedirectResource redirectResource = new RedirectResource(linkDAO);
        MigrationStatusResource migrationStatusResource = new MigrationStatusResource(dataSource);

        environment.jersey().register(linksResource);
        environment.jersey().register(redirectResource);
        environment.jersey().register(migrationStatusResource);
    }
}