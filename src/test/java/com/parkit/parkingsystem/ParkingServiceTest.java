package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.service.UserSurveyService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import nl.altindag.log.LogCaptor;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

  private static ParkingService parkingService;
  private static LogCaptor logCaptor;

  @Mock
  private static InputReaderUtil inputReaderUtil;
  @Mock
  private static ParkingSpotDAO parkingSpotDAO;
  @Mock
  private static TicketDAO ticketDAO;
  @Mock
  private static FareCalculatorService fareCalculatorService;
  @Mock
  private static UserSurveyService userSurveyService;

  @BeforeEach
  private void setUpPerTest() {
    parkingService = new ParkingService(inputReaderUtil, fareCalculatorService, userSurveyService,
            parkingSpotDAO, ticketDAO);
    logCaptor = LogCaptor.forName("ParkingService");
    logCaptor.setLogLevelToInfo();
  }

  @Nested
  class IncomingVehicleTest {

    @BeforeEach
    private void setUpPerTest() {
      try {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to set up test mock objects");
      }
    }

    @Test
    void processIncomingCar() {
      // GIVEN
      when(inputReaderUtil.readSelection()).thenReturn(1);
      when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
      final ArgumentCaptor<ParkingSpot> ParkingSpotCaptor = ArgumentCaptor
              .forClass(ParkingSpot.class);
      final ArgumentCaptor<Ticket> TicketCaptor = ArgumentCaptor.forClass(Ticket.class);

      // WHEN
      parkingService.processIncomingVehicle();

      // THEN
      verify(userSurveyService, times(1)).isRecurringUser("ABCDEF");
      verify(parkingSpotDAO, times(1)).updateParking(ParkingSpotCaptor.capture());
      verify(ticketDAO, times(1)).saveTicket(TicketCaptor.capture());
      final ParkingSpot parkingSpotArg = ParkingSpotCaptor.getValue();
      final Ticket ticketArg = TicketCaptor.getValue();
      assertThat(parkingSpotArg).extracting(spot -> spot.getId(), spot -> spot.getParkingType(),
              spot -> spot.isAvailable()).containsExactly(1, ParkingType.CAR, false);
      assertThat(ticketArg).extracting(ticket -> ticket.getParkingSpot(),
              ticket -> ticket.getVehicleRegNumber(), ticket -> ticket.getPrice())
              .containsExactly(parkingSpotArg, "ABCDEF", 0.0);
      assertThat(ticketArg.getInTime()).isNotNull();
      assertThat(ticketArg.getOutTime()).isNull();
    }

    @Test
    void processIncomingBike() {
      // GIVEN
      when(inputReaderUtil.readSelection()).thenReturn(2);
      when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
      final ArgumentCaptor<ParkingSpot> ParkingSpotCaptor = ArgumentCaptor
              .forClass(ParkingSpot.class);
      final ArgumentCaptor<Ticket> TicketCaptor = ArgumentCaptor.forClass(Ticket.class);

      // WHEN
      parkingService.processIncomingVehicle();

      // THEN
      verify(userSurveyService, times(1)).isRecurringUser("ABCDEF");
      verify(parkingSpotDAO, times(1)).updateParking(ParkingSpotCaptor.capture());
      verify(ticketDAO, times(1)).saveTicket(TicketCaptor.capture());
      final ParkingSpot parkingSpotArg = ParkingSpotCaptor.getValue();
      final Ticket ticketArg = TicketCaptor.getValue();
      assertThat(parkingSpotArg).extracting(spot -> spot.getId(), spot -> spot.getParkingType(),
              spot -> spot.isAvailable()).containsExactly(1, ParkingType.BIKE, false);
      assertThat(ticketArg).extracting(ticket -> ticket.getParkingSpot(),
              ticket -> ticket.getVehicleRegNumber(), ticket -> ticket.getPrice())
              .containsExactly(parkingSpotArg, "ABCDEF", 0.0);
      assertThat(ticketArg.getInTime()).isNotNull();
      assertThat(ticketArg.getOutTime()).isNull();
    }

    @Test
    void processIncomingCarRecurringUserDisplayMessage() {
      // GIVEN
      when(inputReaderUtil.readSelection()).thenReturn(1);
      when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
      when(userSurveyService.isRecurringUser(anyString())).thenReturn(true);
      
      // WHEN
      parkingService.processIncomingVehicle();

      // THEN
      verify(userSurveyService, times(1)).isRecurringUser("ABCDEF");
      assertThat(logCaptor.getInfoLogs()).containsExactly("Recurring user incomming");
    }

    @Test
    void processIncomingBikeRecurringUserDisplayMessage() {
      // GIVEN
      when(inputReaderUtil.readSelection()).thenReturn(2);
      when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
      when(userSurveyService.isRecurringUser(anyString())).thenReturn(true);

      // WHEN
      parkingService.processIncomingVehicle();

      // THEN
      assertThat(logCaptor.getInfoLogs()).containsExactly("Recurring user incomming");
    }
  }

  @Test
  void processIncomingVehicleWithInvalidType() {
    // GIVEN
    when(inputReaderUtil.readSelection()).thenReturn(3);

    // WHEN
    parkingService.processIncomingVehicle();

    // THEN
    assertThat(logCaptor.getErrorLogs())
            .containsExactly("Error parsing user input for type of vehicle");
  }

  @Test
  void processIncomingVehicleWhenParkingIsFull() {
    // GIVEN
    when(inputReaderUtil.readSelection()).thenReturn(1);
    when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

    // WHEN
    parkingService.processIncomingVehicle();

    // THEN
    assertThat(logCaptor.getErrorLogs())
            .containsExactly("Error fetching next available parking slot");
  }

  @Test
  void processIncomingVehicleWithInvalidRegNumber() {
    // GIVEN
    try {
      when(inputReaderUtil.readSelection()).thenReturn(1);
      when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
      when(inputReaderUtil.readVehicleRegistrationNumber())
              .thenThrow(IllegalArgumentException.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    parkingService.processIncomingVehicle();

    // THEN
    assertThat(logCaptor.getErrorLogs()).containsExactly("Unable to process incoming vehicle");
  }

  @Nested
  class ExitingVehicleTest {

    @BeforeEach
    private void setUpPerTest() {
      try {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(LocalDateTime.now().minusHours(1));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to set up test mock objects");
      }
    }

    @Test
    void processExitingVehicleTest() {
      // GIVEN
      when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
      when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
      final ArgumentCaptor<ParkingSpot> ParkingSpotCaptor = ArgumentCaptor
              .forClass(ParkingSpot.class);
      final ArgumentCaptor<Ticket> TicketCaptor = ArgumentCaptor.forClass(Ticket.class);

      // WHEN
      parkingService.processExitingVehicle();

      // THEN
      verify(ticketDAO, times(1)).updateTicket(TicketCaptor.capture());
      verify(parkingSpotDAO, times(1)).updateParking(ParkingSpotCaptor.capture());
      final ParkingSpot parkingSpotArg = ParkingSpotCaptor.getValue();
      final Ticket ticketArg = TicketCaptor.getValue();
      assertThat(parkingSpotArg).extracting(spot -> spot.getId(), spot -> spot.getParkingType(),
              spot -> spot.isAvailable()).containsExactly(1, ParkingType.CAR, true);
      assertThat(ticketArg)
              .extracting(ticket -> ticket.getParkingSpot(), ticket -> ticket.getVehicleRegNumber())
              .containsExactly(parkingSpotArg, "ABCDEF");
      assertThat(ticketArg.getOutTime()).isNotNull();
    }
    
    @Test
    void processExitingVehicleWithInvalidUpdateParkingSpotTest() {
      // GIVEN
      when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

      // WHEN
      parkingService.processExitingVehicle();

      // THEN
      assertThat(logCaptor.getErrorLogs()).containsExactly("Unable to update ticket information");
    }
  }
  
  @Test
  void processExitingVehicleWithInvalidRegNumberTest() {
    // GIVEN
    try {
      when(inputReaderUtil.readVehicleRegistrationNumber())
              .thenThrow(IllegalArgumentException.class);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    parkingService.processExitingVehicle();

    // THEN
    assertThat(logCaptor.getErrorLogs()).containsExactly("Unable to process exiting vehicle");
  }

}
