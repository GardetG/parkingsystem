package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

class FareCalculatorServiceTest {

  private static FareCalculatorService fareCalculatorService;
  private Ticket ticket;
  private LocalDateTime inTime;
  private LocalDateTime outTime;

  @BeforeAll
  private static void setUp() {
    fareCalculatorService = new FareCalculatorService();
  }

  @DisplayName("calculateFare method test")
  @Nested
  class CalculateFareTest {

    @BeforeEach
    private void setUpPerTest() {
      ticket = new Ticket();
      outTime = LocalDateTime.now();
      ticket.setOutTime(outTime);
    }

    @DisplayName("Calculate bike fare")
    @ParameterizedTest(name = "Bike fare for {0} minutes should equal to {1} * "
            + Fare.BIKE_RATE_PER_HOUR)
    @CsvSource({ "0,0", "45,0.75", "60,1","90,1.5" ,"1440,24" })
    void calculateFareBike(int numberOfMinutes, double expectedMultiplier) {
      // GIVEN
      inTime = outTime.minusMinutes(numberOfMinutes);
      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

      ticket.setInTime(inTime);
      ticket.setParkingSpot(parkingSpot);
      double expectedFare = expectedMultiplier * Fare.BIKE_RATE_PER_HOUR;

      // WHEN
      fareCalculatorService.calculateFare(ticket);

      // THEN
      assertThat(ticket.getPrice()).isEqualTo(expectedFare);
    }

    @DisplayName("Calculate car fare")
    @ParameterizedTest(name = "Car fare for {0} minutes should equal to {1} *"
            + Fare.CAR_RATE_PER_HOUR)
    @CsvSource({ "0,0", "45,0.75", "60,1","90,1.5", "1440,24" })
    void calculateFareCar(int numberOfMinutes, double expectedMultiplier) {
      // GIVEN
      inTime = outTime.minusMinutes(numberOfMinutes);
      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

      ticket.setInTime(inTime);
      ticket.setParkingSpot(parkingSpot);
      double expectedFare = expectedMultiplier * Fare.CAR_RATE_PER_HOUR;

      // WHEN
      fareCalculatorService.calculateFare(ticket);

      // THEN
      assertThat(ticket.getPrice()).isEqualTo(expectedFare);
    }

    @DisplayName("Calculate fare with future in time")
    @ParameterizedTest(name = "Calculate {0} fare with future in time should throws exception")
    @CsvSource({ "Bike", "Car" })
    void calculateFareWithFutureInTime(String type) {
      // GIVEN
      inTime = outTime.plusMinutes(60);
      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.valueOf(type.toUpperCase()), false);

      ticket.setInTime(inTime);
      ticket.setParkingSpot(parkingSpot);

      // WHEN
      Throwable thrown = catchThrowable(() -> fareCalculatorService.calculateFare(ticket));

      // THEN
      assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Out time provided is incorrect:");
    }

    @DisplayName("Calculate fare with null out time")
    @ParameterizedTest(name = "Calculate {0} fare with null out time should throws exception")
    @CsvSource({ "Bike", "Car" })
    void calculateFareWithNullOutTime(String type) {
      // GIVEN
      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.valueOf(type.toUpperCase()), false);

      ticket.setInTime(LocalDateTime.now());
      ticket.setOutTime(null);
      ticket.setParkingSpot(parkingSpot);

      // WHEN
      Throwable thrown = catchThrowable(() -> fareCalculatorService.calculateFare(ticket));

      // THEN
      assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Out time provided is incorrect:null");
    }

    @DisplayName("Calculate fare with null in time")
    @ParameterizedTest(name = "Calculate {0} fare with null in time should throws exception")
    @CsvSource({ "Bike", "Car" })
    void calculateFareWithNullInTime(String type) {
      // GIVEN
      ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.valueOf(type.toUpperCase()), false);

      ticket.setInTime(null);
      ticket.setOutTime(LocalDateTime.now());
      ticket.setParkingSpot(parkingSpot);

      // WHEN
      Throwable thrown = catchThrowable(() -> fareCalculatorService.calculateFare(ticket));

      // THEN
      assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("In time provided is incorrect:null");
    }

    @DisplayName("Calculate fare for null parking type should throws exception")
    @Test
    void calculateFareNullType() {
      // GIVEN
      inTime = outTime.minusMinutes(60);
      ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

      ticket.setInTime(inTime);
      ticket.setParkingSpot(parkingSpot);

      // WHEN
      Throwable thrown = catchThrowable(() -> fareCalculatorService.calculateFare(ticket));

      // THEN
      assertThat(thrown).isInstanceOf(NullPointerException.class);
    }
  }

  @DisplayName("calculateDuration method test")
  @Nested
  class CalculateDurationTest {

    @DisplayName("calculate duration")
    @ParameterizedTest(name = "Duration for {0} minutes should equal to {1}")
    @CsvSource({ "0,0", "15,0.25", "30,0.5", "45,0.75", "60,1", "90,1.5", "1440,24" })
    void calculateDuration(int numberOfMinutes, double expectedDuration) {
      // GIVEN
      outTime = LocalDateTime.now();
      inTime = outTime.minusMinutes(numberOfMinutes);

      // WHEN
      double actualDuration = fareCalculatorService.calculateDuration(inTime, outTime);

      // THEN
      assertThat(actualDuration).isEqualTo(expectedDuration);
    }

    @DisplayName("calculate duration futur in time should throws an exception")
    @Test
    void calculateDurationWithFutureInTime() {
      // GIVEN
      outTime = LocalDateTime.now();
      inTime = outTime.plusMinutes(60);

      // WHEN
      Throwable thrown = catchThrowable(
              () -> fareCalculatorService.calculateDuration(inTime, outTime));

      // THEN
      assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Out time provided is incorrect:");
    }

    @DisplayName("calculate duration when out time is null should throws an exception")
    @Test
    void calculateDurationWithOutTimeNull() {
      // GIVEN
      outTime = null;
      inTime = LocalDateTime.now();

      // WHEN
      Throwable thrown = catchThrowable(
              () -> fareCalculatorService.calculateDuration(inTime, outTime));

      // THEN
      assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Out time provided is incorrect:null");
    }

    @DisplayName("calculate duration when in time is null should throws an exception")
    @Test
    void calculateDurationWithInTimeNull() {
      // GIVEN
      outTime = LocalDateTime.now();
      inTime = null;

      // WHEN
      Throwable thrown = catchThrowable(
              () -> fareCalculatorService.calculateDuration(inTime, outTime));

      // THEN
      assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("In time provided is incorrect:null");
    }
  }
}
