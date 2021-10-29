package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.model.ParkingSpot;

import nl.altindag.log.LogCaptor;

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
    parkingSpotDAO = new ParkingSpotDAO();
    parkingSpotDAO.dataBaseConfig = dataBaseConfig;
    when(dataBaseConfig.getConnection()).thenReturn(connection);
  }

  @Test
  void updateParkingTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
      when(ps.executeUpdate()).thenReturn(1);
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

  @Test
  void updateWithNullParkingTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    logCaptor = LogCaptor.forName("ParkingSpotDAO");
    logCaptor.setLogLevelToInfo();
    ParkingSpot parkingSpot = null;

    // WHEN
    boolean result = parkingSpotDAO.updateParking(parkingSpot);

    // THEN
    assertThat(result).isFalse();
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error updating parking info");
  }

  @Test
  void updateParkingWithFailedQueryTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }
    logCaptor = LogCaptor.forName("ParkingSpotDAO");
    logCaptor.setLogLevelToInfo();
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

    // WHEN
    boolean result = parkingSpotDAO.updateParking(parkingSpot);

    // THEN
    assertThat(result).isFalse();
    verify(dataBaseConfig, times(1)).closePreparedStatement(any());
    verify(dataBaseConfig, times(1)).closeConnection(connection);
    assertThat(logCaptor.getErrorLogs()).containsExactly("Error updating parking info");
  }

  @Test
  void getNextAvailableSlotTest() {
    // GIVEN
    try {
      when(connection.prepareStatement(anyString())).thenReturn(ps);
      when(ps.executeQuery()).thenReturn(rs);
      when(rs.next()).thenReturn(true);
      when(rs.getInt(1)).thenReturn(1);
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

  @Test
  void getNextAvailableSlotWhenNoAvailableSlotTest() {
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
    int result = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

    // THEN
    assertThat(result).isEqualTo(-1);
    verify(dataBaseConfig, times(1)).closeResultSet(rs);
    verify(dataBaseConfig, times(1)).closePreparedStatement(ps);
    verify(dataBaseConfig, times(1)).closeConnection(connection);
  }

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