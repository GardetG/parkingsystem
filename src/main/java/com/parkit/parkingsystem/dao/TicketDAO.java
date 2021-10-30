package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

/**
 * DAO Class for Ticket.
 * 
 *
 */
public class TicketDAO {

  private static final Logger logger = LogManager.getLogger("TicketDAO");

  public DataBaseConfig dataBaseConfig = new DataBaseConfig();

  /**
   * Save the ticket as a new entry in the DataBase.
   * 

   * @param ticket to save
   * @return true if successfully saved
   */
  public boolean saveTicket(Ticket ticket) {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = dataBaseConfig.getConnection();
      ps = con.prepareStatement(DBConstants.SAVE_TICKET);
      // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
      ps.setInt(1, ticket.getParkingSpot().getId());
      ps.setString(2, ticket.getVehicleRegNumber());
      ps.setDouble(3, ticket.getPrice());
      ps.setTimestamp(4, Timestamp.valueOf(ticket.getInTime().truncatedTo(ChronoUnit.SECONDS)));
      ps.setTimestamp(5, (ticket.getOutTime() == null) ? null
              : (Timestamp.valueOf(ticket.getOutTime().truncatedTo(ChronoUnit.SECONDS))));
      ps.execute();
      return true;
    } catch (Exception ex) {
      logger.error("Error saving ticket info", ex);
    } finally {
      dataBaseConfig.closePreparedStatement(ps);
      dataBaseConfig.closeConnection(con);
    }
    return false;
  }

  /**
   * Get the last ticket saved in the DataBase for the provided vehicle
   * registration number. If no ticket found, return null.
   * 

   * @param vehicleRegNumber associated to the ticket
   * @return last ticket saved
   */
  public Ticket getTicket(String vehicleRegNumber) {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    Ticket ticket = null;
    try {
      con = dataBaseConfig.getConnection();
      ps = con.prepareStatement(DBConstants.GET_TICKET);
      // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
      ps.setString(1, vehicleRegNumber);
      rs = ps.executeQuery();
      if (rs.next()) {
        ticket = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1),
                ParkingType.valueOf(rs.getString(6)), false);
        ticket.setParkingSpot(parkingSpot);
        ticket.setId(rs.getInt(2));
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setPrice(rs.getDouble(3));
        ticket.setInTime(rs.getTimestamp(4).toLocalDateTime());
        ticket.setOutTime(
                (rs.getTimestamp(5) == null) ? null : rs.getTimestamp(5).toLocalDateTime());
      }

    } catch (Exception ex) {
      logger.error("Error fetching ticket info", ex);
    } finally {
      dataBaseConfig.closeResultSet(rs);
      dataBaseConfig.closePreparedStatement(ps);
      dataBaseConfig.closeConnection(con);
    }
    return ticket;
  }

  /**
   * Update the ticket in the DataBase.
   * 

   * @param ticket to update
   * @return true if successfully updated
   */
  public boolean updateTicket(Ticket ticket) {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = dataBaseConfig.getConnection();
      ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
      ps.setDouble(1, ticket.getPrice());
      ps.setTimestamp(2, Timestamp.valueOf(ticket.getOutTime().truncatedTo(ChronoUnit.SECONDS)));
      ps.setInt(3, ticket.getId());
      int updateRowCount = ps.executeUpdate();
      return (updateRowCount == 1);
    } catch (Exception ex) {
      logger.error("Error updating ticket info", ex);
    } finally {
      dataBaseConfig.closePreparedStatement(ps);
      dataBaseConfig.closeConnection(con);
    }
    return false;
  }

  /**
   * Get a list of all tickets saved in the DataBase for the provided vehicle
   * registration number. If not tickets found return an empty list.
   * 

   * @param vehicleRegNumber associated to the tickets
   * @return all tickets saved
   */
  public List<Ticket> getAllTickets(String vehicleRegNumber) {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    List<Ticket> allTickets = new ArrayList<>();
    try {
      con = dataBaseConfig.getConnection();
      ps = con.prepareStatement(DBConstants.GET_ALL_TICKET);
      // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
      ps.setString(1, vehicleRegNumber);
      rs = ps.executeQuery();
      while (rs.next()) {
        Ticket ticket = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1),
                ParkingType.valueOf(rs.getString(6)), false);
        ticket.setParkingSpot(parkingSpot);
        ticket.setId(rs.getInt(2));
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setPrice(rs.getDouble(3));
        ticket.setInTime(rs.getTimestamp(4).toLocalDateTime());
        ticket.setOutTime(
                (rs.getTimestamp(5) == null) ? null : rs.getTimestamp(5).toLocalDateTime());
        allTickets.add(ticket);
      }

    } catch (Exception ex) {
      logger.error("Error fetching all ticket info", ex);
    } finally {
      dataBaseConfig.closeResultSet(rs);
      dataBaseConfig.closePreparedStatement(ps);
      dataBaseConfig.closeConnection(con);
    }
    return allTickets;
  }
}
