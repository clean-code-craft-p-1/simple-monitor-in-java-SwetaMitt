package vitals;

import java.util.Objects;

/**
 * Domain object for one vital reading and its acceptable range.
 * Solves the README goals for modularity, testability, and future limits by
 * keeping threshold rules as data instead of branching inside the checker.
 */
public final class Vital {
  private final String outOfRangeMessage;
  private final float value;
  private final float lowerLimit;
  private final float upperLimit;

  /**
   * Creates a validated vital reading.
   * Security/robustness: rejects null messages, non-finite readings, and invalid
   * ranges so external sensor or vendor data cannot silently corrupt decisions.
   */
  public Vital(String outOfRangeMessage, float value, float lowerLimit, float upperLimit) {
    this.outOfRangeMessage = Objects.requireNonNull(outOfRangeMessage, "outOfRangeMessage");
    validateFinite(value, "value");
    validateFinite(lowerLimit, "lowerLimit");
    validateFinite(upperLimit, "upperLimit");
    if (lowerLimit > upperLimit) {
      throw new IllegalArgumentException("lowerLimit must be less than or equal to upperLimit");
    }
    this.value = value;
    this.lowerLimit = lowerLimit;
    this.upperLimit = upperLimit;
  }

  /**
   * Pure threshold check for one vital.
   * Solves the README request to shorten semantic distance between inputs,
   * acceptable limits, and the pass/fail result.
   */
  public boolean isOk() {
    return value >= lowerLimit && value <= upperLimit;
  }

  /**
   * Returns the consumer-facing message used when this vital fails.
   * Keeps alert wording close to the vital definition instead of duplicating it.
   */
  public String outOfRangeMessage() {
    return outOfRangeMessage;
  }

  /**
   * Rejects NaN and infinite readings from external devices or calculations.
   */
  private static void validateFinite(float reading, String name) {
    if (Float.isNaN(reading) || Float.isInfinite(reading)) {
      throw new IllegalArgumentException(name + " must be finite");
    }
  }
}
