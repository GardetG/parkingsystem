package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    ticketDAO = new TicketDAO(dataBaseConfig);
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
      when(dataBaseConfig.getConnection()).thenReturn(connection);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    logCaptor = LogCaptor.forName("TicketDAO");
    logCaptor.setLogLevelToInfo();
  }

  @Nested
  class SaveAndUpdateATicket {

    @BeforeEach
    private void setUpPerTest() throws Exception {
      ticket.setId(1);
      ticket.setInTime(LocalDateTime.now().minusMinutes(60));
      ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
      ticket.setPrice(Fare.CAR_RATE_PER_HOUR);
      ticket.setVehicleRegNumber("ABCDEF");
      // Prepare a filled ticket
    }

    @DisplayName("Save a ticket with out time not null")
    @Test
    void saveTicketWithOutTimeTest() {
      // GIVEN
      ticket.setOutTime(LocalDateTime.now());

      // WHEN
      boolean result = ticketDAO.saveTicket(ticket);

      // THEN
      assertThat(result).isTrue();
      verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
      verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @DisplayName("Save a ticket with out time null")
    @Test
    void saveTicketWithNullOutTmeTest() {
      // GIVEN
      ticket.setOutTime(null);

      // WHEN
      boolean result = ticketDAO.saveTicket(ticket);

      // THEN
      assertThat(result).isTrue();
      verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
      verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @DisplayName("Update a ticket")
    @Test
    void updateTicketTest() {
      // GIVEN
      try {
        when(ps.executeUpdate()).thenReturn(1);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to set up test mock objects");
      }
      ticket.setOutTime(LocalDateTime.now());

      // WHEN
      boolean result = ticketDAO.updateTicket(ticket);

      // THEN
      assertThat(result).isTrue();
      verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
      verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @DisplayName("Update a ticket not found should return false")
    @Test
    void updateTicketNotFoundTest() {
      // GIVEN
      try {
        when(ps.executeUpdate()).thenReturn(0);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to set up test mock objects");
      }
      ticket.setOutTime(LocalDateTime.now());

      // WHEN
      boolean result = ticketDAO.updateTicket(ticket);

      // THEN
      assertThat(result).isFalse();
      verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
      verify(dataBaseConfig, times(1)).closeConnection(connection);
    }
  }

  @DisplayName("Save a null ticket should return false and log error")
  @Test
  void saveNullTicketTest() {
    // GIVEN
    ticket = null;

    // WHEN
    boolean result = ticketDAO.saveTicket(ticket);

    // THEN
    assertThat(result).isFalse();
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error saving ticket info");
  }

  @DisplayName("Save a ticket with SQLException should return false and log error")
  @Test
  void saveTicketWithFailedQueryTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    boolean result = ticketDAO.saveTicket(ticket);

    // THEN
    assertThat(result).isFalse();
    verify(dataBaseConfig, times(1)).closePreparedStatement(any());
    verify(dataBaseConfig, times(1)).closeConnection(connection);
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error saving ticket info");
  }

  @DisplayName("Update a null ticket should return false and log error")
  @Test
  void updateNullTicketTest() {
    // GIVEN
    ticket = null;

    // WHEN
    boolean result = ticketDAO.updateTicket(ticket);

    // THEN
    assertThat(result).isFalse();
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error updating ticket info");
  }

  @DisplayName("Update a ticket with SQLException should return false and log error")
  @Test
  void updateTicketWithFailedQueryTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    boolean result = ticketDAO.updateTicket(ticket);

    // THEN
    assertThat(result).isFalse();
    verify(dataBaseConfig, times(1)).closePreparedStatement(any());
    verify(dataBaseConfig, times(1)).closeConnection(connection);
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error updating ticket info");
  }

  @Nested
  class GetTicketAndGetAllTicketTest {

    @BeforeEach
    private void setUpPerTest() throws Exception {
      try {
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.getString(anyInt())).then(invocation -> {
          String awnser = null;
          final int index = invocation.getArgument(0);
          switch (index) {
            case 1:
              awnser = "ABCDEF";
              break;
            case 6:
              awnser = "CAR";
              break;
            default:
          }
          return awnser;
        });
        when(rs.getInt(anyInt())).thenReturn(1);
        when(rs.getDouble(anyInt())).thenReturn(Fare.CAR_RATE_PER_HOUR);
        // Mock rs return ticket data
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to set up test mock objects");
      }
    }

    @DisplayName("Fetch a ticket with out time not null")
    @Test
    void getTicketWithOutTimeTest() {
      // GIVEN
      try {
        when(rs.next()).thenReturn(true);
        Timestamp inTime = new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000);
        Timestamp outTime = new Timestamp(System.currentTimeMillis());
        when(rs.getTimestamp(anyInt())).thenReturn(inTime).thenReturn(outTime);
        // Mock rs return a ticket with in and out time
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to set up test mock objects");
      }

      // WHEN
      Ticket actualTicket = ticketDAO.getTicket("ABCDEF");

      // THEN
      assertThat(actualTicket).isNotNull();
      verify(dataBaseConfig, times(1)).closeResultSet(rs);
      verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
      verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @DisplayName("Fetch a ticket with out time null")
    @Test
    void getTicketWithNullOutTimeTest() {
      // GIVEN
      try {
        when(rs.next()).thenReturn(true);
        Timestamp inTime = new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000);
        when(rs.getTimestamp(anyInt())).thenReturn(inTime).thenReturn(null);
        // Mock rs return a ticket with in time and null out time
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to set up test mock objects");
      }

      // WHEN
      Ticket actualTicket = ticketDAO.getTicket("ABCDEF");

      // THEN
      assertThat(actualTicket).isNotNull();
      verify(dataBaseConfig, times(1)).closeResultSet(rs);
      verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
      verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @DisplayName("Fetch all tickets with out time not null")
    @Test
    void getAllTicketsTest() {
      // GIVEN
      try {
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Timestamp inTime = new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000);
        Timestamp outTime = new Timestamp(System.currentTimeMillis());
        when(rs.getTimestamp(anyInt())).thenReturn(inTime).thenReturn(outTime).thenReturn(outTime)
                .thenReturn(inTime).thenReturn(outTime);
        // Mock rs return 2 ticket with in and out time
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to set up test mock objects");
      }

      // WHEN
      List<Ticket> allTickets = ticketDAO.getAllTickets("ABCDEF");

      // THEN
      assertThat(allTickets.size()).isEqualTo(2);
      verify(dataBaseConfig, times(1)).closeResultSet(rs);
      verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
      verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @DisplayName("Fetch all tickets with one with out time null")
    @Test
    void getAllTicketsWithOutTimeNullTest() {
      // GIVEN
      try {
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        Timestamp inTime = new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000);
        Timestamp outTime = new Timestamp(System.currentTimeMillis());
        when(rs.getTimestamp(anyInt())).thenReturn(inTime).thenReturn(outTime).thenReturn(outTime)
                .thenReturn(inTime).thenReturn(null);
        // Mock rs return 2 ticket, the second of which has a null out time
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to set up test mock objects");
      }

      // WHEN
      List<Ticket> allTickets = ticketDAO.getAllTickets("ABCDEF");

      // THEN
      assertThat(allTickets.size()).isEqualTo(2);
      verify(dataBaseConfig, times(1)).closeResultSet(rs);
      verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
      verify(dataBaseConfig, times(1)).closeConnection(connection);
    }
  }

  @DisplayName("Fetch ticket when no ticket found should return a null ticket")
  @Test
  void getTicketWhenNoTicketFoundTest() {
    // GIVEN
    try {
      when(ps.executeQuery()).thenReturn(rs);
      when(rs.next()).thenReturn(false);
      // Mock rs empty
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    Ticket actualTicket = ticketDAO.getTicket("ABCDEF");

    // THEN
    assertThat(actualTicket).isNull();
    verify(dataBaseConfig, times(1)).closeResultSet(rs);
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @DisplayName("Fetch ticket with SQLException should return null ticket and log error")
  @Test
  void getTicketWithFailedQueryTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    Ticket actualTicket = ticketDAO.getTicket("ABCDEF");

    // THEN
    assertThat(actualTicket).isNull();
    verify(dataBaseConfig, times(1)).closeResultSet(any());
    verify(dataBaseConfig, times(1)).closePreparedStatement(any());
    verify(dataBaseConfig, times(1)).closeConnection(connection);
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error fetching ticket info");
  }

  @DisplayName("Fetch all tickets when no ticket found should return an empty list")
  @Test
  void getAllTicketsWithNoTicketFoundTest() {
    // GIVEN
    try {
      when(ps.executeQuery()).thenReturn(rs);
      when(rs.next()).thenReturn(false);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    List<Ticket> allTickets = ticketDAO.getAllTickets("ABCDEF");

    // THEN
    assertThat(allTickets.isEmpty()).isTrue();
    verify(dataBaseConfig, times(1)).closeResultSet(rs);
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @DisplayName("Fetch all tickets with SQLException should return an empty list")
  @Test
  void getAllTicketsWithFailedQueryTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    List<Ticket> allTickets = ticketDAO.getAllTickets("ABCDEF");

    // THEN
    assertThat(allTickets.size()).isZero();
    verify(dataBaseConfig, times(1)).closeResultSet(any());
    verify(dataBaseConfig, times(1)).closePreparedStatement(any());
    verify(dataBaseConfig, times(1)).closeConnection(connection);
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error fetching all ticket info");
  }
}
