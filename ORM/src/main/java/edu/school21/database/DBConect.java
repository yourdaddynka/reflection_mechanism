package edu.school21.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class DBConect {
    private static final String propertyFilePatch = "src/main/resources/hikari.properties";
    private static Properties properties;
    private static DBConect instance;

    public static DBConect getInstance() {
        if (instance == null) {
            instance = new DBConect();
        }
        return instance;
    }

    private DBConect() {
        setProperties();
    }


    private static void setProperties() {
        try {
            properties = new Properties();
            properties.load(new FileReader(propertyFilePatch));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public DataSource createDatasource() {
        setProperties();
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(properties.getProperty("URL"));
            hikariConfig.setUsername(properties.getProperty("USER"));
            hikariConfig.setPassword(properties.getProperty("PASSWORD"));
            return new HikariDataSource(hikariConfig);
        } catch (HikariPool.PoolInitializationException | IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
