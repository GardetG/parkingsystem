package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.service.UserSurveyService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import java.time.LocalDateTime;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


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
        // Mock user input registration number ABCDEF
        // Mock DAO save and update successful
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to set up test mock objects");
      }
    }

    @DisplayName("Process an incoming car with registration ABCDEF")
    @Test
    void processIncomingCar() {
      // GIVEN
      when(inputReaderUtil.readSelection()).thenReturn(1);
      when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
      // Mock user input choice for a car
      // Mock next available parking spot for a bike is 1
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

    @DisplayName("Process an incoming bike with registration ABCDEF")
    @Test
    void processIncomingBike() {
      // GIVEN
      when(inputReaderUtil.readSelection()).thenReturn(2);
      when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(4);
      // Mock user input choice for a car
      // Mock next available parking spot for a car is 4
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
              spot -> spot.isAvailable()).containsExactly(4, ParkingType.BIKE, false);
      assertThat(ticketArg).extracting(ticket -> ticket.getParkingSpot(),
              ticket -> ticket.getVehicleRegNumber(), ticket -> ticket.getPrice())
              .containsExactly(parkingSpotArg, "ABCDEF", 0.0);
      assertThat(ticketArg.getInTime()).isNotNull();
      assertThat(ticketArg.getOutTime()).isNull();
    }

    @DisplayName("Process an incoming recurring user's car with registration ABCDEF")
    @Test
    void processIncomingCarRecurringUserDisplayMessage() {
      // GIVEN
      when(inputReaderUtil.readSelection()).thenReturn(1);
      when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
      when(userSurveyService.isRecurringUser(anyString())).thenReturn(true);
      // Mock user input choice for a car and isRecurringUser return true
      // Mock next available parking spot for a car is 1

      // WHEN
      parkingService.processIncomingVehicle();

      // THEN
      verify(userSurveyService, times(1)).isRecurringUser("ABCDEF");
      assertThat(logCaptor.getInfoLogs()).containsExactly("Recurring user incomming");
    }

    @DisplayName("Process an incoming recurring user's bike with registration ABCDEF")
    @Test
    void processIncomingBikeRecurringUserDisplayMessage() {
      // GIVEN
      when(inputReaderUtil.readSelection()).thenReturn(2);
      when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(4);
      when(userSurveyService.isRecurringUser(anyString())).thenReturn(true);
      // Mock user input choice for a bike and isRecurringUser return true
      // Mock next available parking spot for a bike is 4

      // WHEN
      parkingService.processIncomingVehicle();

      // THEN
      verify(userSurveyService, times(1)).isRecurringUser("ABCDEF");
      assertThat(logCaptor.getInfoLogs()).containsExactly("Recurring user incomming");
    }
  }

  @DisplayName("Process an incoming vehicle with invalid input type should log an error")
  @Test
  void processIncomingVehicleWithInvalidType() {
    // GIVEN
    when(inputReaderUtil.readSelection()).thenReturn(3);
    // Mock an invalid input for vehicle type

    // WHEN
    parkingService.processIncomingVehicle();

    // THEN
    assertThat(logCaptor.getErrorLogs())
            .containsExactly("Error parsing user input for type of vehicle");
    verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
    verify(ticketDAO, times(0)).saveTicket(any(Ticket.class));
  }

  @DisplayName("Process an incoming vehicle when the parking is full should log an error")
  @Test
  void processIncomingVehicleWhenParkingIsFull() {
    // GIVEN
    when(inputReaderUtil.readSelection()).thenReturn(1);
    when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
    // Mock next available spot return as 0

    // WHEN
    parkingService.processIncomingVehicle();

    // THEN
    assertThat(logCaptor.getErrorLogs())
            .containsExactly("Error fetching next available parking slot");
    verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
    verify(ticketDAO, times(0)).saveTicket(any(Ticket.class));
  }

  @DisplayName("Process an incoming vehicle with invalid registartion input should log an error")
  @Test
  void processIncomingVehicleWithInvalidRegNumber() {
    // GIVEN
    try {
      when(inputReaderUtil.readSelection()).thenReturn(1);
      when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
      when(inputReaderUtil.readVehicleRegistrationNumber())
              .thenThrow(IllegalArgumentException.class);
      // Mock exception when input registration number
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to set up test mock objects");
    }

    // WHEN
    parkingService.processIncomingVehicle();

    // THEN
    assertThat(logCaptor.getErrorLogs()).containsExactly("Unable to process incoming vehicle");
    verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
    verify(ticketDAO, times(0)).saveTicket(any(Ticket.class));
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
        // Mock user input registration number ABCDEF
        // Mock DAO return a ticket for a car parked for one hour
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to set up test mock objects");
      }
    }

    @DisplayName("Process an exiting car with registation ABCDEF")
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

    @DisplayName("Process an exiting car when update ticket failed should log an error")
    @Test
    void processExitingVehicleWithInvalidUpdateParkingSpotTest() {
      // GIVEN
      when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

      // WHEN
      parkingService.processExitingVehicle();

      // THEN
      assertThat(logCaptor.getErrorLogs()).containsExactly("Unable to update ticket information");
      verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
      verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
    }
  }

  @DisplayName("Process an exiting car with invalid registration input should log an error")
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
    verify(ticketDAO, times(0)).updateTicket(any(Ticket.class));
    verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
  }

}
