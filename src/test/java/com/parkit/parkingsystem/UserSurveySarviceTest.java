package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.UserSurveyService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class UserSurveySarviceTest {

  private static UserSurveyService userSurveyService;

  private List<Ticket> allTickets;

  @Mock
  private static TicketDAO ticketDAO;

  @BeforeEach
  private void setUp() throws Exception {
    userSurveyService = new UserSurveyService(ticketDAO);
    allTickets = new ArrayList<>();
  }

  @DisplayName("Checking recurring user when no tickets found should return false")
  @Test
  void isRecurringUserWhenNoTicketsFoundTest() {
    // GIVEN
    allTickets = new ArrayList<>();
    when(ticketDAO.getAllTickets(anyString())).thenReturn(allTickets);

    // WHEN
    boolean result = userSurveyService.isRecurringUser("ABCDEF");

    // THEN
    assertThat(result).isFalse();
    verify(ticketDAO, times(1)).getAllTickets("ABCDEF");
  }

  @DisplayName("Checking recurring user when DAO return null should return false")
  @Test
  void isRecurringUserWhenDAOReturnNullTest() {
    // GIVEN
    allTickets = null;
    when(ticketDAO.getAllTickets(anyString())).thenReturn(allTickets);

    // WHEN
    boolean result = userSurveyService.isRecurringUser("ABCDEF");

    // THEN
    assertThat(result).isFalse();
    verify(ticketDAO, times(1)).getAllTickets("ABCDEF");
  }

  @DisplayName("Checking recurring user when only currently parked car should return false")
  @Test
  void isRecurringUserWithNoPreviouslyParkedCarTest() {

    // GIVEN
    Ticket currentTicket = new Ticket();
    currentTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
    currentTicket.setVehicleRegNumber("ABCDEF");
    currentTicket.setInTime(LocalDateTime.now().minusMinutes(60));
    currentTicket.setOutTime(null);
    currentTicket.setPrice(0);
    // A ticket of a currently parked car without out time
    allTickets.add(currentTicket);
    when(ticketDAO.getAllTickets(anyString())).thenReturn(allTickets);

    // WHEN
    boolean result = userSurveyService.isRecurringUser("ABCDEF");

    // THEN
    assertThat(result).isFalse();
    verify(ticketDAO, times(1)).getAllTickets("ABCDEF");
  }

  @DisplayName("Checking recurring user with a previous parked and exited car should return true")
  @Test
  void isRecurringUserWithPreviouslyParkedAndExitedCarTest() {
    // GIVEN
    Ticket currentTicket = new Ticket();
    currentTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
    currentTicket.setVehicleRegNumber("ABCDEF");
    currentTicket.setInTime(LocalDateTime.now().minusMinutes(60));
    currentTicket.setOutTime(null);
    currentTicket.setPrice(0);
    // A ticket of a currently parked
    allTickets.add(currentTicket);

    Ticket previousTicket = new Ticket();
    previousTicket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
    previousTicket.setVehicleRegNumber("ABCDEF");
    previousTicket.setInTime(LocalDateTime.now().minusWeeks(1).minusMinutes(60));
    previousTicket.setOutTime(LocalDateTime.now().minusWeeks(1));
    previousTicket.setPrice(Fare.CAR_RATE_PER_HOUR);
    // A ticket of a previously parked and exited car with registration ABCDEF
    allTickets.add(previousTicket);
    when(ticketDAO.getAllTickets(anyString())).thenReturn(allTickets);

    // WHEN
    boolean result = userSurveyService.isRecurringUser("ABCDEF");

    // THEN
    assertThat(result).isTrue();
    verify(ticketDAO, times(1)).getAllTickets("ABCDEF");
  }
}
