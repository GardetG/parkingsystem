package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.service.UserSurveyService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
class ParkingDataBaseIT {

  private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
  private static ParkingSpotDAO parkingSpotDAO;
  private static TicketDAO ticketDAO;
  private static DataBasePrepareService dataBasePrepareService;
  private static FareCalculatorService fareCalculatorService;
  private static UserSurveyService userSurveyService;

  @Mock
  private static InputReaderUtil inputReaderUtil;

  @BeforeAll
  private static void setUp() throws Exception {
    parkingSpotDAO = new ParkingSpotDAO();
    parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
    ticketDAO = new TicketDAO();
    ticketDAO.dataBaseConfig = dataBaseTestConfig;
    userSurveyService = new UserSurveyService(ticketDAO);
    fareCalculatorService = new FareCalculatorService(userSurveyService);
    dataBasePrepareService = new DataBasePrepareService();
  }

  @BeforeEach
  private void setUpPerTest() throws Exception {
    when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    dataBasePrepareService.clearDataBaseEntries();
  }

  @AfterAll
  private static void tearDown() {

  }

  @DisplayName("Parking a car")
  @Test
  void testParkingACar() {
    // GIVEN
    when(inputReaderUtil.readSelection()).thenReturn(1);
    // Parking a car with registration ABCDEF
    ParkingService parkingService = new ParkingService(inputReaderUtil, fareCalculatorService,
            userSurveyService, parkingSpotDAO, ticketDAO);

    // WHEN
    parkingService.processIncomingVehicle();

    // THEN
    Ticket savedTicket = ticketDAO.getTicket("ABCDEF");
    assertThat(savedTicket)
            .extracting(ticket -> ticket.getParkingSpot(), ticket -> ticket.getVehicleRegNumber(),
                    ticket -> ticket.getPrice())
            .containsExactly(new ParkingSpot(1, ParkingType.CAR, false), "ABCDEF", 0.0);
    assertThat(savedTicket.getInTime()).isNotNull();
    assertThat(savedTicket.getOutTime()).isNull();
    assertThat(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).isNotEqualTo(1);
  }

  @DisplayName("Exiting a car parked for one hour")
  @Test
  void testParkingLotExit() {
    // GIVEN
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    Ticket currentTicket = new Ticket();
    currentTicket.setParkingSpot(parkingSpot);
    currentTicket.setVehicleRegNumber("ABCDEF");
    currentTicket.setInTime(LocalDateTime.now().minusMinutes(60));
    currentTicket.setOutTime(null);
    currentTicket.setPrice(0);
    ticketDAO.saveTicket(currentTicket);
    parkingSpotDAO.updateParking(parkingSpot);
    // A car parked for an hour at parking spot 1 with registration ABCDEF
    ParkingService parkingService = new ParkingService(inputReaderUtil, fareCalculatorService,
            userSurveyService, parkingSpotDAO, ticketDAO);
    double expectedFare = Fare.CAR_RATE_PER_HOUR;

    // WHEN
    parkingService.processExitingVehicle();

    // THEN
    Ticket savedTicket = ticketDAO.getTicket("ABCDEF");
    assertThat(savedTicket).extracting(ticket -> ticket.getParkingSpot(),
            ticket -> ticket.getVehicleRegNumber(), ticket -> ticket.getPrice())
            .containsExactly(new ParkingSpot(1, ParkingType.CAR, false), "ABCDEF", expectedFare);
    assertThat(savedTicket.getInTime()).isNotNull();
    assertThat(savedTicket.getOutTime()).isNotNull();
  }
  
  @DisplayName("Exiting a car parked for less than 30 minutes")
  @Test
  void testParkingLotExitLessThan30Minutes() {
    // GIVEN
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    Ticket currentTicket = new Ticket();
    currentTicket.setParkingSpot(parkingSpot);
    currentTicket.setVehicleRegNumber("ABCDEF");
    currentTicket.setInTime(LocalDateTime.now().minusMinutes(15));
    currentTicket.setOutTime(null);
    currentTicket.setPrice(0);
    ticketDAO.saveTicket(currentTicket);
    parkingSpotDAO.updateParking(parkingSpot);
    // A car parked for an hour at parking spot 1 with registration ABCDEF
    ParkingService parkingService = new ParkingService(inputReaderUtil, fareCalculatorService,
            userSurveyService, parkingSpotDAO, ticketDAO);
    double expectedFare = 0.0;

    // WHEN
    parkingService.processExitingVehicle();

    // THEN
    Ticket savedTicket = ticketDAO.getTicket("ABCDEF");
    assertThat(savedTicket).extracting(ticket -> ticket.getParkingSpot(),
            ticket -> ticket.getVehicleRegNumber(), ticket -> ticket.getPrice())
            .containsExactly(new ParkingSpot(1, ParkingType.CAR, false), "ABCDEF", expectedFare);
    assertThat(savedTicket.getInTime()).isNotNull();
    assertThat(savedTicket.getOutTime()).isNotNull();
  }

  @DisplayName("Exiting a recurring user's car parked for one hour ")
  @Test
  void testParkingLotExitRecurringUser() {
    // GIVEN
    Ticket previousTicket = new Ticket();
    previousTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
    previousTicket.setVehicleRegNumber("ABCDEF");
    previousTicket.setInTime(LocalDateTime.now().minusWeeks(1).minusMinutes(60));
    previousTicket.setOutTime(LocalDateTime.now().minusWeeks(1));
    previousTicket.setPrice(Fare.CAR_RATE_PER_HOUR);
    ticketDAO.saveTicket(previousTicket);
    // A ticket of a previously parked and exited car with registration ABCDEF

    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    Ticket currentTicket = new Ticket();
    currentTicket.setParkingSpot(parkingSpot);
    currentTicket.setVehicleRegNumber("ABCDEF");
    currentTicket.setInTime(LocalDateTime.now().minusMinutes(60));
    currentTicket.setOutTime(null);
    currentTicket.setPrice(0);
    ticketDAO.saveTicket(currentTicket);
    parkingSpotDAO.updateParking(parkingSpot);
    // A car parked for an hour at parking spot 1 with registration ABCDEF
    ParkingService parkingService = new ParkingService(inputReaderUtil, fareCalculatorService,
            userSurveyService, parkingSpotDAO, ticketDAO);
    double expectedFare = Fare.CAR_RATE_PER_HOUR - (Fare.CAR_RATE_PER_HOUR * 5.0 / 100);
    // Expected a 5% discount

    // WHEN
    parkingService.processExitingVehicle();

    // THEN
    Ticket savedTicket = ticketDAO.getTicket("ABCDEF");
    assertThat(savedTicket).extracting(ticket -> ticket.getParkingSpot(),
            ticket -> ticket.getVehicleRegNumber(), ticket -> ticket.getPrice())
            .containsExactly(new ParkingSpot(1, ParkingType.CAR, false), "ABCDEF", expectedFare);
    assertThat(savedTicket.getInTime()).isNotNull();
    assertThat(savedTicket.getOutTime()).isNotNull();
  }

}
