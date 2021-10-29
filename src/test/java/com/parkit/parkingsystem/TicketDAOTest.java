package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

import nl.altindag.log.LogCaptor;

@ExtendWith(MockitoExtension.class)
class TicketDAOTest {

  private static TicketDAO ticketDAO;
  private LogCaptor logCaptor;
  private Ticket ticket;

  @Mock
  private static DataBaseConfig dataBaseConfig;
  @Mock
  private static Connection connection;
  @Mock
  private static PreparedStatement ps;
  @Mock
  private static ResultSet rs;

  @BeforeEach
  private void setUpPerTest() throws Exception {
    ticket = new Ticket();
    ticketDAO = new TicketDAO();
    ticketDAO.dataBaseConfig = dataBaseConfig;
    when(dataBaseConfig.getConnection()).thenReturn(connection);
  }

  @Test
  void saveTicketTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
    ticket.setVehicleRegNumber("ABCDEF");
    ticket.setInTime(LocalDateTime.now());
    // A ticket ready to be saved in DataBase

    // WHEN
    boolean result = ticketDAO.saveTicket(ticket);

    // THEN
    assertThat(result).isTrue();
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @Test
  void saveNullTicketTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    logCaptor = LogCaptor.forName("TicketDAO");
    logCaptor.setLogLevelToInfo();

    // WHEN
    boolean result = ticketDAO.saveTicket(ticket);

    // THEN
    assertThat(result).isFalse();
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error saving ticket info");
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @Test
  void saveTicketWithFailedQueryTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    logCaptor = LogCaptor.forName("TicketDAO");
    logCaptor.setLogLevelToInfo();

    // WHEN
    boolean result = ticketDAO.saveTicket(ticket);

    // THEN
    assertThat(result).isFalse();
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error saving ticket info");
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @Test
  void getTicketTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
      when(ps.executeQuery()).thenReturn(rs);
      when(rs.next()).thenReturn(true);
      when(rs.getString(6)).thenReturn("CAR");
      when(rs.getTimestamp(4)).thenReturn(new Timestamp(System.currentTimeMillis()));
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    Ticket ticket = ticketDAO.getTicket("ABCDEF");

    // THEN
    assertThat(ticket).isNotNull();
    verify(dataBaseConfig, times(1)).closeResultSet(rs);
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @Test
  void getTicketWhenNoTicketFoundTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
      when(ps.executeQuery()).thenReturn(rs);
      when(rs.next()).thenReturn(false);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    Ticket ticket = ticketDAO.getTicket("ABCDEF");

    // THEN
    assertThat(ticket).isNull();
    verify(dataBaseConfig, times(1)).closeResultSet(rs);
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @Test
  void getTicketWithFailedQueryTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    logCaptor = LogCaptor.forName("TicketDAO");
    logCaptor.setLogLevelToInfo();

    // WHEN
    Ticket ticket = ticketDAO.getTicket("ABCDEF");

    // THEN
    assertThat(ticket).isNull();
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error fetching ticket info");
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @Test
  void updateTicketTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
    ticket.setVehicleRegNumber("ABCDEF");
    ticket.setInTime(LocalDateTime.now().minusHours(1));
    ticket.setOutTime(LocalDateTime.now());
    // A ticket ready to be update

    // WHEN
    boolean result = ticketDAO.updateTicket(ticket);

    // THEN
    assertThat(result).isTrue();
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @Test
  void updateNullTicketTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    logCaptor = LogCaptor.forName("TicketDAO");
    logCaptor.setLogLevelToInfo();

    // WHEN
    boolean result = ticketDAO.updateTicket(ticket);

    // THEN
    assertThat(result).isFalse();
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error updating ticket info");
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @Test
  void updateTicketWithFailedQueryTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    logCaptor = LogCaptor.forName("TicketDAO");
    logCaptor.setLogLevelToInfo();

    // WHEN
    boolean result = ticketDAO.updateTicket(ticket);

    // THEN
    assertThat(result).isFalse();
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error updating ticket info");
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }
}
