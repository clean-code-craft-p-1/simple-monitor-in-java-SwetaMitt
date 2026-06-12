package vitals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link VitalsChecker}: thresholding, first-failure detection,
 * alert dispatch, and validation of invalid vital collections.
 * Vital construction and range rules are covered in {@link VitalTest}.
 */
public class VitalsCheckerTest {

  @Before
  public void disableBlinkDelay() {
    // Set to 0 so the production Alerter path runs instantly in tests.
    Alerter.blinkDelayMs = 0;
  }

  @After
  public void restoreBlinkDelay() {
    Alerter.blinkDelayMs = 1000;
  }

  // --- firstFailingVital: default three-reading API ---

  @Test
  public void allVitalsWithinRangePasses() {
    assertFalse(VitalsChecker.firstFailingVital(98.1f, 70, 98).isPresent());
  }

  @Test
  public void temperatureTooHighFails() {
    assertEquals(
        "Temperature is critical!",
        VitalsChecker.firstFailingVital(102.1f, 70, 98).get().outOfRangeMessage());
  }

  @Test
  public void temperatureTooLowFails() {
    assertEquals(
        "Temperature is critical!",
        VitalsChecker.firstFailingVital(94.9f, 70, 98).get().outOfRangeMessage());
  }

  @Test
  public void pulseRateTooLowFails() {
    assertEquals(
        "Pulse Rate is out of range!",
        VitalsChecker.firstFailingVital(98.1f, 59, 98).get().outOfRangeMessage());
  }

  @Test
  public void pulseRateTooHighFails() {
    assertEquals(
        "Pulse Rate is out of range!",
        VitalsChecker.firstFailingVital(98.1f, 101, 98).get().outOfRangeMessage());
  }

  @Test
  public void oxygenSaturationTooLowFails() {
    assertEquals(
        "Oxygen Saturation out of range!",
        VitalsChecker.firstFailingVital(98.1f, 70, 89).get().outOfRangeMessage());
  }

  @Test
  public void lowerBoundaryValuesAreOk() {
    assertFalse(VitalsChecker.firstFailingVital(95f, 60, 90).isPresent());
  }

  @Test
  public void upperBoundaryValuesAreOk() {
    assertFalse(VitalsChecker.firstFailingVital(102f, 100, 100).isPresent());
  }

  @Test
  public void firstFailingVitalIsReportedWhenMultipleAreOffRange() {
    assertEquals(
        "Temperature is critical!",
        VitalsChecker.firstFailingVital(103f, 40, 80).get().outOfRangeMessage());
  }

  // --- firstFailingVital: caller-supplied vital list (future extension) ---

  @Test
  public void additionalVendorVitalCanBeCheckedWithoutChangingChecker() {
    assertEquals(
        "Blood Pressure is out of range!",
        VitalsChecker.firstFailingVital(Arrays.asList(
            new Vital("Temperature is critical!", 98.1f, 95, 102),
            new Vital("Blood Pressure is out of range!", 141, 90, 120)))
            .get()
            .outOfRangeMessage());
  }

  @Test
  public void changedLimitsCanBeSuppliedByTheCaller() {
    assertFalse(VitalsChecker.firstFailingVital(Arrays.asList(
        new Vital("Pulse Rate is out of range!", 105, 80, 120)))
        .isPresent());
  }

  // --- vitalsOk: production overloads (Alerter path) ---

  @Test
  public void vitalsOkWithThreeFloatsReturnsTrueWhenAllInRange() throws InterruptedException {
    assertTrue(VitalsChecker.vitalsOk(98.1f, 70, 98));
  }

  @Test
  public void vitalsOkWithThreeFloatsReturnsFalseWhenTemperatureIsHigh() throws InterruptedException {
    assertFalse(VitalsChecker.vitalsOk(103f, 70, 98));
  }

  @Test
  public void vitalsOkWithListUsesProductionAlerterWhenAllInRange() throws InterruptedException {
    assertTrue(VitalsChecker.vitalsOk(Arrays.asList(
        new Vital("Temperature is critical!", 98.1f, 95, 102))));
  }

  @Test
  public void vitalsOkWithListUsesProductionAlerterWhenOutOfRange() throws InterruptedException {
    assertFalse(VitalsChecker.vitalsOk(Arrays.asList(
        new Vital("Temperature is critical!", 103, 95, 102))));
  }

  // --- vitalsOk: injectable alert seam ---

  @Test
  public void vitalsOkReturnsTrueWhenAllSuppliedVitalsAreValid() throws InterruptedException {
    assertTrue(VitalsChecker.vitalsOk(Arrays.asList(
        new Vital("Temperature is critical!", 98.1f, 95, 102)),
        message -> { }));
  }

  @Test
  public void vitalsOkReturnsTrueForAllDefaultVitalsInRange() throws InterruptedException {
    assertTrue(VitalsChecker.vitalsOk(Arrays.asList(
        new Vital("Temperature is critical!", 98.1f, 95, 102),
        new Vital("Pulse Rate is out of range!", 70, 60, 100),
        new Vital("Oxygen Saturation out of range!", 98, 90, Float.MAX_VALUE)),
        message -> { }));
  }

  @Test
  public void vitalsOkAlertsWhenAnySuppliedVitalFails() throws InterruptedException {
    List<String> alerts = new ArrayList<>();

    assertFalse(VitalsChecker.vitalsOk(Arrays.asList(
        new Vital("Temperature is critical!", 103, 95, 102)),
        alerts::add));
    assertEquals(Arrays.asList("Temperature is critical!"), alerts);
  }

  // --- Validation: invalid collections ---

  @Test
  public void nullVitalListIsRejected() {
    assertThrows(NullPointerException.class,
        () -> VitalsChecker.firstFailingVital((List<Vital>) null));
  }

  @Test
  public void emptyVitalListIsRejected() {
    List<Vital> empty = Arrays.asList();
    assertThrows(IllegalArgumentException.class,
        () -> VitalsChecker.firstFailingVital(empty));
  }

  @Test
  public void nullEntryInVitalListIsRejected() {
    List<Vital> listWithNull = Arrays.asList(new Vital("ok", 70, 60, 100), null);
    assertThrows(NullPointerException.class, () -> VitalsChecker.firstFailingVital(listWithNull));
  }

  @Test
  public void nullAlertBoundaryIsRejected() {
    List<Vital> vitals = Arrays.asList(new Vital("ok", 70, 60, 100));
    assertThrows(NullPointerException.class, () -> VitalsChecker.vitalsOk(vitals, null));
  }
}

