package com.parkit.parkingsystem.service;

import java.time.LocalDateTime;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

/**
 * Service Class handling the calculation of the ticket fare.
 * 
 *
 */
public class FareCalculatorService {

  /**
   * Calculate and set the price of the provided ticket according to the parking
   * type and the parking duration.
   * 

   * @param ticket for which we want to calculate the price.
   */
  public void calculateFare(Ticket ticket) throws IllegalArgumentException  {
    if ((ticket.getOutTime() == null) || (ticket.getOutTime().isBefore(ticket.getInTime()))) {
      throw new IllegalArgumentException(
              "Out time provided is incorrect:" + ticket.getOutTime().toString());
    }

    int inHour = ticket.getInTime().getHour();
    int outHour = ticket.getOutTime().getHour();

    // TODO: Some tests are failing here. Need to check if this logic is correct
    int duration = outHour - inHour;

    switch (ticket.getParkingSpot().getParkingType()) {
      case CAR:
        ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
        break;
      case BIKE:
        ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
        break;
      default:
        throw new IllegalArgumentException("Unkown Parking Type");
    }
  }

  /**
   * Calculate the duration between in time and out time and return the number of
   * hours passed. If the duration is less than one hour, the return value is the
   * fraction of the passed hour.
   * 

   * @param inTime  for the calculate duration
   * @param outTime for the calculate duration
   * @return duration
   */
  public double calculateDuration(LocalDateTime inTime, LocalDateTime outTime) {
    // TODO Auto-generated method stub
    return 0;
  }
}