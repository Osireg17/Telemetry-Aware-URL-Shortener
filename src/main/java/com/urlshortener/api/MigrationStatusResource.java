package com.urlshortener.api;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

@Path("/migration-status")
@Produces(MediaType.APPLICATION_JSON)
public class MigrationStatusResource {

    private final DataSource dataSource;

    public MigrationStatusResource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GET
    public Response getMigrationStatus() {

        Map<String, Object> status = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase("migrations.xml",
                    new ClassLoaderResourceAccessor(), database);

            int unrunChanges = liquibase.listUnrunChangeSets(new Contexts(), new LabelExpression()).size();

            status.put("status", unrunChanges == 0 ? "UP_TO_DATE" : "PENDING_MIGRATIONS");
            status.put("unrunChanges", unrunChanges);
            status.put("databaseUrl", connection.getMetaData().getURL());

            return Response.ok(status).build();

        } catch (Exception e) {
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(status).build();
        }
    }
}
