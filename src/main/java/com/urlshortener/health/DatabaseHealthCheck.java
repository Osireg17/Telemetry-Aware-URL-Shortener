package com.urlshortener.health;

import com.codahale.metrics.health.HealthCheck;
import javax.sql.DataSource;
import java.sql.Connection;

public class DatabaseHealthCheck extends HealthCheck {
	private final DataSource dataSource;

	public DatabaseHealthCheck(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	protected Result check() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			if (connection != null && !connection.isClosed()) {
				try (var statement = connection.createStatement()) {
					statement.execute("SELECT 1");
					return Result.healthy("Database is healthy and connected.");
				}
			} else {
				return Result.unhealthy("Database connection is closed.");
			}
		} catch (Exception e) {
			return Result.unhealthy("Database connection failed: " + e.getMessage());
		}
	}
}
