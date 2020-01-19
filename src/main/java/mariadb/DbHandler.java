package mariadb;

import config.ConfigGetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DbHandler {
    private ConnectionStorage connectionStorage;
    private ConfigGetter configGetter;

    public DbHandler(ConnectionStorage connectionStorage, ConfigGetter configGetter)
    {
        this.connectionStorage = connectionStorage;
        this.configGetter = configGetter;
    }

    public void prepareDb() throws SQLException {
        var connection = this.connectionStorage.getConnection();
        var ps = connection.prepareStatement("DROP TABLE IF EXISTS `queue_test`");
        ps.execute();
        ps.close();

        ps = connection.prepareStatement(String.format("CREATE TABLE `queue_test` (\n" +
                "  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
                "  `locking_item_id` int(11) NOT NULL,\n" +
                "  `next_retry` datetime NOT NULL,\n" +
                "  `try_count` int(11) NOT NULL,\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `locking_item_id` (`locking_item_id`)\n" +
                ") ENGINE=%s DEFAULT CHARSET=latin1;", this.configGetter.getString("engine")));
        ps.execute();
        ps.close();

        connection.close();
    }

    public Integer getLockingItemId() throws SQLException {
        var connection = this.connectionStorage.getConnection();
        PreparedStatement ps = connection.prepareStatement(String.format("INSERT INTO `queue_test` (`locking_item_id`, `next_retry`, `try_count`) VALUES \n" +
                "(FLOOR(RAND()*%d), NOW(), 0) ON DUPLICATE KEY UPDATE \n" +
                "`id` = LAST_INSERT_ID(`id`), `try_count` = 0;", this.configGetter.getInteger("executions")));
        ps.execute();
        ps.close();

        connection.setAutoCommit(false);
        ps = connection.prepareStatement("UPDATE `queue_test` SET\n" +
                "`locking_item_id` = LAST_INSERT_ID(`locking_item_id`),\n" +
                "`next_retry` = DATE_ADD(NOW(), INTERVAL 120 SECOND),\n" +
                "`try_count` = `try_count` + 1\n" +
                "WHERE\n" +
                "`next_retry` = NOW() AND `try_count` < 5;", PreparedStatement.RETURN_GENERATED_KEYS);

        ps.executeUpdate();
        ps.close();
        ps = connection.prepareStatement("SELECT LAST_INSERT_ID()");
        var rs = ps.executeQuery();
        int result = 0;

        if(rs.next())
        {
            result = rs.getInt(1);
        }

        connection.setAutoCommit(true);
        ps.close();
        connection.close();

        if (result == 0) {
            throw new RuntimeException("queue is empty");
        }

        ps = connection.prepareStatement(String.format("DELETE FROM `queue_test` WHERE id = %d", result), PreparedStatement.RETURN_GENERATED_KEYS);
        ps.executeUpdate();
        ps.close();

        return result;
    }
}
