package com.swamisuite.common.tenancy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;

/**
 * Routes each Hibernate session to its tenant's Postgres schema via {@code
 * SET search_path}, per ADR 0001 (schema-per-tenant).
 *
 * <p>Phase 0 scaffolding: wired against a single {@link DataSource} bean supplied by
 * whichever service depends on this module. Tenant schema provisioning (creating the
 * schema and running Flyway against it) is Phase 1+ work — this class only switches
 * an already-provisioned connection's search_path.
 */
public class DefaultMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    public DefaultMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + sanitizeSchema(tenantIdentifier));
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + TenantContext.DEFAULT_SCHEMA);
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException("Cannot unwrap " + getClass().getName());
    }

    private static String sanitizeSchema(String schema) {
        if (schema == null || !schema.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid tenant schema identifier: " + schema);
        }
        return schema;
    }
}
