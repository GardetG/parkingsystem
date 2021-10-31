package com.parkit.parkingsystem.integration.config;

import com.parkit.parkingsystem.config.DataBaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Class handling the connection to the DataBase for testing.
 *
 */
public class DataBaseTestConfig extends DataBaseConfig {

  private static final Logger logger = LogManager.getLogger("DataBaseTestConfig");

  /**
   * Creates and return the DataBase connection. Throws an exception if jdbc driver
   * not found or if connection with DataBase can't be established.
   * 

   * @return Connection
   * @throws ClassNotFoundException if jdbc driver not found
   * @throws SQLException if cannot establish DataBase connection
   */
  public Connection getConnection() throws ClassNotFoundException, SQLException {
    logger.info("Create DB connection");
    Class.forName("com.mysql.cj.jdbc.Driver");
    Map<String, String> identification = getIdentification("/config.properties");
    return DriverManager.getConnection("jdbc:mysql://localhost:3306/test?serverTimezone=UTC",
            identification.get("User"), identification.get("Password"));
  }
}
