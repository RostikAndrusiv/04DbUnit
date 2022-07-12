package org.rostik.andrusiv.databases.db;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

@Slf4j
public class DbManager {
    private static DbManager instance = null;

//    private static final String DB_URL = loadURL();

    private static final String DB_URL = "jdbc:h2:mem:default;DB_CLOSE_DELAY=-1";

    private static final Logger logger = LoggerFactory.getLogger(DbManager.class.getName());

    private DbManager() {
    }

    private static String loadURL() {
        try (InputStream input = new FileInputStream("application.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty("db.url");
        } catch (IOException ex) {
            logger.info(String.format("IO Exception while loading properties: %s", ex.getMessage()));
        }
        return null;
    }

    public static synchronized DbManager getInstance() {
        if (instance == null) {
            instance = new DbManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
