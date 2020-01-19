package mariadb;

import config.ConfigGetter;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConnectionStorage {
    private MariaDbPoolDataSource connectionPool;

    public ConnectionStorage(ConfigGetter configGetter) throws SQLException {
        String dbname = configGetter.getString("dbname");

        createRequiredDb(configGetter, dbname);

        this.connectionPool = createPool(configGetter, dbname);
    }

    private void createRequiredDb(ConfigGetter configGetter, String dbName) throws SQLException {
        var schemaPool = createPool(configGetter, "INFORMATION_SCHEMA");

        Connection connection = schemaPool.getConnection();
        PreparedStatement ps = connection.prepareStatement(String.format("CREATE DATABASE IF NOT EXISTS `%s`", dbName));
        ps.execute();
        ps.close();
        connection.close();
        schemaPool.close();
    }

    private MariaDbPoolDataSource createPool(ConfigGetter configGetter, String dbName) throws SQLException {
         var result = new MariaDbPoolDataSource(
            configGetter.getString("dbhost"),
            configGetter.getInteger("dbport"),
            dbName
        );

        result.setUser(configGetter.getString("dbuser"));
        result.setPassword(configGetter.getString("dbpassword"));
        result.setMaxPoolSize(configGetter.getInteger("concurrecncy"));

        result.initialize();

        return result;
    }

    public MariaDbPoolDataSource getConnectionPool() {
        return connectionPool;
    }

    public Connection getConnection() throws SQLException {
        return this.getConnectionPool().getConnection();
    }
}
