package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.dao.TicketDAO;

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
  
}
