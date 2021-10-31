package com.parkit.parkingsystem.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class handling the connection to the DataBase.
 *
 */
public class DataBaseConfig {

  private static final Logger logger = LogManager.getLogger("DataBaseConfig");

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
    return DriverManager.getConnection("jdbc:mysql://localhost:3306/prod?serverTimezone=UTC",
            identification.get("User"), identification.get("Password"));
  }

  /**
   * Try to close the Connection if not null.
   * 

   * @param con Connection to close
   */
  public void closeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
        logger.info("Closing DB connection");
      } catch (SQLException e) {
        logger.error("Error while closing connection", e);
      }
    }
  }

  /**
   * Try to close the PreparedStatement if not null.
   * 

   * @param ps PreparedStatement to close
   */

  public void closePreparedStatement(PreparedStatement ps) {
    if (ps != null) {
      try {
        ps.close();
        logger.info("Closing Prepared Statement");
      } catch (SQLException e) {
        logger.error("Error while closing prepared statement", e);
      }
    }
  }

  /**
   * Try to close the ResultSet if not null.
   * 

   * @param rs ResultSet to close
   */
  public void closeResultSet(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
        logger.info("Closing Result Set");
      } catch (SQLException e) {
        logger.error("Error while closing result set", e);
      }
    }
  }

  /**
   * Return a Map containing login credentials for the DataBase fetched from a
   * configuration file. User key return user login. Password key return password.
   * 

   * @param path of the configuration file
   * @return map containing login credentials
   */
  public Map<String, String> getIdentification(String path) {

    Properties properties = new Properties();
    Map<String, String> identifiant = new HashMap<>();

    try (InputStream input = DataBaseConfig.class.getResourceAsStream(path)) {
      properties.load(input);
      identifiant.put("User", properties.getProperty("dbuser"));
      identifiant.put("Password", properties.getProperty("dbpassword"));
      return identifiant;
    } catch (IOException e) {
      logger.error("Error while fetching DataBase indentification config", e);
    }

    return identifiant;
  }
}
