package com.parkit.parkingsystem.util;

import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility Class handling the console input from the user.
 * 
 *
 */
public class InputReaderUtil {

  private static Scanner scan = new Scanner(System.in, "UTF-8");
  private static final Logger logger = LogManager.getLogger("InputReaderUtil");

  /**
   * Read the menu option provide by the user and return an associated integer or
   * -1 in case of an invalid input.
   *

   * @return integer associated to user's input.
   */
  public int readSelection() {
    try {
      return Integer.parseInt(scan.nextLine());
    } catch (Exception e) {
      logger.error("Error while reading user input from Shell", e);
      System.out.println("Error reading input. Please enter valid number for proceeding further");
      return -1;
    }
  }

  /**
   * Read the vehicle registration number provide by the user and return a string
   * or throws an exception in case of an invalid input.
   * 

   * @return String associated to vehicle registration number.
   * @throws IllegalArgumentException if invalid input provide
   */
  public String readVehicleRegistrationNumber() throws IllegalArgumentException {
    try {
      String vehicleRegNumber = scan.nextLine();
      if (vehicleRegNumber == null || vehicleRegNumber.trim().length() == 0) {
        throw new IllegalArgumentException("Invalid input provided");
      }
      return vehicleRegNumber;
    } catch (Exception e) {
      logger.error("Error while reading user input from Shell", e);
      System.out.println(
          "Error reading input. Please enter a valid string for vehicle registration number");
      throw e;
    }
  }

}
