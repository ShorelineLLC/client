package net.shoreline.server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class Database
{
    private final HikariDataSource dataSource;

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

    public DataSource getDataSource()
    {
        return dataSource;
    }
}
