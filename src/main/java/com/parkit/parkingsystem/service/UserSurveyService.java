package com.parkit.parkingsystem.service;

import java.util.List;
import java.util.Objects;

import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

/**
 * Service Class handling the fetching of data on users.
 * 
 *
 */
public class UserSurveyService {

  private TicketDAO ticketDAO;

  public UserSurveyService(TicketDAO ticketDAO) {
    this.ticketDAO = ticketDAO;
  }

  /**
   * Check the list of all tickets with the registration number provided for at
   * least one ticket with out time not null. In this case the user have already
   * parked and exited his vehicle, return true. The method return false in the
   * others cases of if the list of tickets is null or empty.
   * 

   * @param vehicleRegNumber to check
   * @return if the user is a recurring user
   */
  public boolean isRecurringUser(String vehicleRegNumber) {
    List<Ticket> allTickets = ticketDAO.getAllTickets(vehicleRegNumber);
    if ((allTickets != null) && !allTickets.isEmpty()) {
      return allTickets.stream().anyMatch(ticket -> Objects.nonNull(ticket.getOutTime()));
    }
    return false;
  }

}
