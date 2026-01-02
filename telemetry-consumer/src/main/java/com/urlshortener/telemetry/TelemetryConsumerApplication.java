package com.urlshortener.telemetry;

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

public class TelemetryConsumerApplication extends Application<TelemetryConsumerConfiguration> {

    private Jdbi jdbi;

    public static void main(final String[] args) throws Exception {
        new TelemetryConsumerApplication().run(args);
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    @Override
    public String getName() {
        return "TelemetryConsumer";
    }

    @Override
    public void initialize(final Bootstrap<TelemetryConsumerConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)));

        bootstrap.addBundle(new MigrationsBundle<TelemetryConsumerConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(TelemetryConsumerConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(final TelemetryConsumerConfiguration configuration,
                    final Environment environment) {

        DataSource dataSource = configuration.getDataSourceFactory().build(environment.metrics(), "database");

        this.jdbi = Jdbi.create(dataSource);
        this.jdbi.installPlugin(new SqlObjectPlugin());
    }
}