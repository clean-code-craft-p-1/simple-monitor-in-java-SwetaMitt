package vitals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Application service for vital thresholding.
 * Solves the goals for lower complexity, modular design, future vital
 * support, and testability by coordinating pure {@link Vital} rules with an
 * alert boundary only when output is needed.
 */
public final class VitalsChecker {

  // Adult clinical thresholds — named constants so future changes have one place to edit.
  private static final float TEMP_LOW   =  95f;
  private static final float TEMP_HIGH  = 102f;
  private static final float PULSE_LOW  =  60f;
  private static final float PULSE_HIGH = 100f;
  private static final float SPO2_LOW   =  90f;

  private VitalsChecker() {
  }

  /**
   * Builds the default adult vital set described by the original exercise.
   * Keeps the original three-input API while storing thresholds as vital data.
   * Private: only used internally; callers that need custom limits construct
   * their own {@link Vital} list and pass it to the List overloads.
   */
  private static List<Vital> vitalsOf(float temperature, float pulseRate, float spo2) {
    return Arrays.asList(
        new Vital("Temperature is critical!",       temperature, TEMP_LOW,  TEMP_HIGH),
        new Vital("Pulse Rate is out of range!",    pulseRate,   PULSE_LOW, PULSE_HIGH),
        new Vital("Oxygen Saturation out of range!", spo2,       SPO2_LOW,  Float.MAX_VALUE));
  }

  /**
   * Finds the first failing default vital without performing I/O.
   * Reduces cyclomatic complexity by delegating the actual range check to each
   * {@link Vital} object.
   */
  static Optional<VitalSign> firstFailingVital(float temperature, float pulseRate, float spo2) {
    return firstFailingVital(vitalsOf(temperature, pulseRate, spo2));
  }

  /**
   * Finds the first failing vital from any caller-supplied vital list.
   * Supports future README scenarios such as new vital signs, vendor readings,
   * and age-specific ranges without changing this checker.
   */
  static Optional<VitalSign> firstFailingVital(List<? extends VitalSign> vitals) {
    return validated(vitals).stream()
        .filter(vital -> !vital.isOk())
        .findFirst();
  }

  /**
   * Checks the original three vital readings and uses the production alerter.
   * Preserves the existing consumer behavior while the logic remains testable
   * through the pure methods and injectable alert overload.
   */
  static boolean vitalsOk(float temperature, float pulseRate, float spo2)
      throws InterruptedException {
    return vitalsOk(vitalsOf(temperature, pulseRate, spo2));
  }

  /**
   * Checks any supplied vital list and uses the production console alert.
   * Adds extensibility for new readings while keeping I/O at the outer boundary.
   */
  static boolean vitalsOk(List<? extends VitalSign> vitals) throws InterruptedException {
    return vitalsOk(vitals, Alerter::alert);
  }

  /**
   * Checks any supplied vital list with a caller-provided alert mechanism.
   * This is the test seam that proves alert behavior without sleeping or writing
   * to the console, satisfying the README's simple-and-testable direction.
   */
  static boolean vitalsOk(List<? extends VitalSign> vitals, VitalAlert alert) throws InterruptedException {
    Objects.requireNonNull(alert, "alert");
    Optional<VitalSign> failing = firstFailingVital(vitals);
    if (failing.isPresent()) {
      alert.alert(failing.get().outOfRangeMessage());
      return false;
    }
    return true;
  }

  /**
   * Validates caller-supplied vital collections before thresholding.
   * Security/robustness: rejects null or empty collections and null entries from
   * external providers instead of allowing ambiguous monitoring results.
   */
  private static List<VitalSign> validated(List<? extends VitalSign> vitals) {
    Objects.requireNonNull(vitals, "vitals");
    if (vitals.isEmpty()) {
      throw new IllegalArgumentException("vitals must not be empty");
    }
    for (VitalSign vital : vitals) {
      Objects.requireNonNull(vital, "vitals must not contain null entries");
    }
    return new ArrayList<>(vitals);
  }
}
