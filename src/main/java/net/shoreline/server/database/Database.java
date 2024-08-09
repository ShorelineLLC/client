package net.shoreline.server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class Database
{
    private final HikariDataSource dataSource;
    private final ReentrantLock lock = new ReentrantLock();

    public Database(String jdbcUrl,
                    String username,
                    String password)
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        this.dataSource = new HikariDataSource(config);
    }

    public ReentrantLock getLock()
    {
        return this.lock;
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }
}
