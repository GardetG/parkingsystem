package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParkingSpotDAOTest {

  private ParkingSpotDAO parkingSpotDAO;
  private LogCaptor logCaptor;

  @Mock
  private static DataBaseConfig dataBaseConfig;
  @Mock
  private static Connection connection;
  @Mock
  private static PreparedStatement ps;
  @Mock
  private static ResultSet rs;

  @BeforeEach
  void setUp() throws Exception {
    parkingSpotDAO = new ParkingSpotDAO(dataBaseConfig);
    when(dataBaseConfig.getConnection()).thenReturn(connection);
    logCaptor = LogCaptor.forName("ParkingSpotDAO");
    logCaptor.setLogLevelToInfo();
  }

  @DisplayName("Succesful update parking spot should return true")
  @Test
  void updateParkingTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
      when(ps.executeUpdate()).thenReturn(1);
      // Mock ps update one line in DB
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

    // WHEN
    boolean result = parkingSpotDAO.updateParking(parkingSpot);

    // THEN
    assertThat(result).isTrue();
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @DisplayName("Failed update parking spot should return false")
  @Test
  void updateParkingWhenParkingSpotNotFoundTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
      when(ps.executeUpdate()).thenReturn(0);
      // Mock ps update 0 line in DB
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

    // WHEN
    boolean result = parkingSpotDAO.updateParking(parkingSpot);

    // THEN
    assertThat(result).isFalse();
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @DisplayName("Update null parking spot should return false and log error")
  @Test
  void updateWithNullParkingTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    ParkingSpot parkingSpot = null;

    // WHEN
    boolean result = parkingSpotDAO.updateParking(parkingSpot);

    // THEN
    assertThat(result).isFalse();
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error updating parking info");
  }

  @DisplayName("Update when SQLException occured should return false and log error")
  @Test
  void updateParkingWithFailedQueryTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

    // WHEN
    boolean result = parkingSpotDAO.updateParking(parkingSpot);

    // THEN
    assertThat(result).isFalse();
    verify(dataBaseConfig, times(1)).closePreparedStatement(any());
    verify(dataBaseConfig, times(1)).closeConnection(connection);
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error updating parking info");
  }

  @DisplayName("Get next available slot successfully")
  @Test
  void getNextAvailableSlotTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
      when(ps.executeQuery()).thenReturn(rs);
      when(rs.next()).thenReturn(true);
      when(rs.getInt(anyInt())).thenReturn(1);
      // Mock rs return slot 1
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

    // THEN
    assertThat(result).isEqualTo(1);
    verify(dataBaseConfig, times(1)).closeResultSet(rs);
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @DisplayName("Get next available slot when no availble slot found should return -1")
  @Test
  void getNextAvailableSlotWhenNoAvailableSlotTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
      when(ps.executeQuery()).thenReturn(rs);
      when(rs.next()).thenReturn(false);
      // Mock rs empty
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

    // THEN
    assertThat(result).isEqualTo(-1);
    verify(dataBaseConfig, times(1)).closeResultSet(rs);
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

  @DisplayName("Get next available slot when SQLException occured should return -1 and log error")
  @Test
  void getNextAvailableSlotWithFailedQueryTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    logCaptor = LogCaptor.forName("ParkingSpotDAO");
    logCaptor.setLogLevelToInfo();

    // WHEN
    int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

    // THEN
    assertThat(result).isEqualTo(-1);
    verify(dataBaseConfig, times(1)).closeResultSet(any());
    verify(dataBaseConfig, times(1)).closePreparedStatement(any());
    verify(dataBaseConfig, times(1)).closeConnection(connection);
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error fetching next available slot");
  }
}