package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;
import java.time.Duration;
import java.time.LocalDateTime;
import org.apache.commons.math3.util.Precision;

/**
 * Service Class handling the calculation of the ticket fare.
 * 
 *
 */
public class FareCalculatorService {

  private UserSurveyService userSurveyService;

  public FareCalculatorService(UserSurveyService userSurveyService) {
    this.userSurveyService = userSurveyService;
  }

  /**
   * Calculate and set the price of the provided ticket according to the parking
   * type and the parking duration.
   * 

   * @param ticket for which we want to calculate the price.
   */
  public void calculateFare(Ticket ticket) throws IllegalArgumentException, NullPointerException {
    double duration = calculateDuration(ticket.getInTime(), ticket.getOutTime());

    double price = 0;
    if (duration >= 0.5) {
      switch (ticket.getParkingSpot().getParkingType()) {
        case CAR:
          price = duration * Fare.CAR_RATE_PER_HOUR;
          break;
        case BIKE:
          price = duration * Fare.BIKE_RATE_PER_HOUR;
          break;
        default:
          throw new IllegalArgumentException("Unkown Parking Type");
      }

      if (userSurveyService.isRecurringUser(ticket.getVehicleRegNumber())) {
        price -= price * 5.0 / 100;
      }
    }

    ticket.setPrice(Precision.round(price, 2));
  }

  /**
   * Calculate the duration between in time and out time and return the number of
   * hours passed. The returned duration could be a decimal number representing
   * the fraction passed hour.
   * 

   * @param inTime  for the calculate duration
   * @param outTime for the calculate duration
   * @return duration
   */
  public double calculateDuration(LocalDateTime inTime, LocalDateTime outTime) {
    if (inTime == null) {
      throw new IllegalArgumentException("In time provided is incorrect:null");
    }
    if ((outTime == null) || (outTime.isBefore(inTime))) {
      String invalidOutTime = (outTime == null) ? "null" : outTime.toString();
      throw new IllegalArgumentException("Out time provided is incorrect:" + invalidOutTime);
    }

    Duration duration = Duration.between(inTime, outTime);

    return duration.toMinutes() / 60.0;
  }
}