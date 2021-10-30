package com.parkit.parkingsystem.service;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

/**
 * Service Class processing the incoming and exiting vehicle according to user
 * input, generates ticket and attributes parking spot.
 * 
 *
 */
public class ParkingService {

  private static final Logger logger = LogManager.getLogger("ParkingService");

  private InputReaderUtil inputReaderUtil;
  private FareCalculatorService fareCalculatorService;
  private UserSurveyService userSurveyService;
  private ParkingSpotDAO parkingSpotDAO;
  private TicketDAO ticketDAO;

  public ParkingService(InputReaderUtil inputReaderUtil,
          FareCalculatorService fareCalculatorService, UserSurveyService userSurveyService,
          ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
    this.inputReaderUtil = inputReaderUtil;
    this.fareCalculatorService = fareCalculatorService;
    this.userSurveyService = userSurveyService;
    this.parkingSpotDAO = parkingSpotDAO;
    this.ticketDAO = ticketDAO;
  }

  /**
   * Process an incoming vehicle by reading the user input, looking for and
   * attributing a free parking spot and finally save the ticket with in time and
   * update the parking spot availability.
   */
  public void processIncomingVehicle() {
    try {
      ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
      if (parkingSpot != null) {
        String vehicleRegNumber = getVehichleRegNumber();
        parkingSpot.setAvailable(false);
        parkingSpotDAO.updateParking(parkingSpot);
        // allot this parking space and mark it's availability as false

        LocalDateTime inTime = LocalDateTime.now();
        Ticket ticket = new Ticket();
        // ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setPrice(0);
        ticket.setInTime(inTime);
        ticket.setOutTime(null);
        ticketDAO.saveTicket(ticket);
        System.out.println("Generated Ticket and saved in DB");
        if (userSurveyService.isRecurringUser(vehicleRegNumber)) {
          System.out.println(
                  "Welcome back! As a recurring user of our parking lot, you'll benefit from a 5% discount.");
          logger.info("Recurring user incomming");
        }
        System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
        System.out.println(
                "Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
      }
    } catch (Exception e) {
      logger.error("Unable to process incoming vehicle", e);
    }
  }

  private String getVehichleRegNumber() throws IllegalArgumentException {
    System.out.println("Please type the vehicle registration number and press enter key");
    return inputReaderUtil.readVehicleRegistrationNumber();
  }

  /**
   * Look for the next parking spot available according to the type of vehicle
   * provide by the user and return a ParkingSpot. In case of error parsing the
   * user input or fetching an available spot then the method return null.
   * 

   * @return next available ParkingSpot
   */
  public ParkingSpot getNextParkingNumberIfAvailable() {
    int parkingNumber = 0;
    ParkingSpot parkingSpot = null;
    try {
      ParkingType parkingType = getVehichleType();
      parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
      if (parkingNumber > 0) {
        parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
      } else {
        throw new NullPointerException(
                "Error fetching parking number from DB. Parking slots might be full");
      }
    } catch (IllegalArgumentException ie) {
      logger.error("Error parsing user input for type of vehicle", ie);
    } catch (NullPointerException e) {
      logger.error("Error fetching next available parking slot", e);
    }
    return parkingSpot;
  }

  private ParkingType getVehichleType() throws IllegalArgumentException {
    System.out.println("Please select vehicle type from menu");
    System.out.println("1 CAR");
    System.out.println("2 BIKE");
    int input = inputReaderUtil.readSelection();
    switch (input) {
      case 1:
        return ParkingType.CAR;
      case 2:
        return ParkingType.BIKE;
      default: {
        System.out.println("Incorrect input provided");
        throw new IllegalArgumentException("Entered input is invalid");
      }
    }
  }

  /**
   * Process an exiting vehicle by reading the user input and fetching the saved
   * ticket to evaluate the price according to the in and out time. The method
   * save the updated ticket and update the parking spot availability.
   */
  public void processExitingVehicle() {
    try {
      String vehicleRegNumber = getVehichleRegNumber();
      Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
      LocalDateTime outTime = LocalDateTime.now();
      ticket.setOutTime(outTime);
      fareCalculatorService.calculateFare(ticket);
      if (ticketDAO.updateTicket(ticket)) {
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        parkingSpot.setAvailable(true);
        parkingSpotDAO.updateParking(parkingSpot);
        System.out.println("Please pay the parking fare:" + ticket.getPrice());
        System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber()
                + " is:" + outTime);
      } else {
        System.out.println("Unable to update ticket information. Error occurred");
        logger.error("Unable to update ticket information");
      }
    } catch (Exception e) {
      logger.error("Unable to process exiting vehicle", e);
    }
  }
}
