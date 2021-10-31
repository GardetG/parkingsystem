package com.parkit.parkingsystem.constants;

/**
 * Class storing fare constants for each vehicle type.
 * 
 *
 */
public class Fare {

  private Fare() {
    throw new IllegalStateException("Utility class");
  }

  public static final double BIKE_RATE_PER_HOUR = 1.0;
  public static final double CAR_RATE_PER_HOUR = 1.5;
}
