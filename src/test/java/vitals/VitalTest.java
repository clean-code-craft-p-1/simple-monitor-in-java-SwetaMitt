package vitals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link Vital}: value construction, range checking,
 * and validation of invalid inputs.
 */
public class VitalTest {

  // --- isOk: range checks ---

  @Test
  public void vitalIsOkWhenValueIsWithinRange() {
    assertTrue(new Vital("ok", 70, 60, 100).isOk());
  }

  @Test
  public void vitalIsOkAtLowerBoundary() {
    assertTrue(new Vital("ok", 60, 60, 100).isOk());
  }

  @Test
  public void vitalIsOkAtUpperBoundary() {
    assertTrue(new Vital("ok", 100, 60, 100).isOk());
  }

  @Test
  public void vitalIsNotOkWhenBelowLowerBound() {
    assertFalse(new Vital("low", 59, 60, 100).isOk());
  }

  @Test
  public void vitalIsNotOkWhenAboveUpperBound() {
    assertFalse(new Vital("high", 101, 60, 100).isOk());
  }

  // --- Constructor: invalid inputs ---

  @Test
  public void nullMessageIsRejected() {
    assertThrows(NullPointerException.class, () -> new Vital(null, 70, 60, 100));
  }

  @Test
  public void nanValueIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Vital("invalid", Float.NaN, 60, 100));
  }

  @Test
  public void infiniteValueIsRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> new Vital("invalid", Float.POSITIVE_INFINITY, 60, 100));
  }

  @Test
  public void nanLowerLimitIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Vital("invalid", 70, Float.NaN, 100));
  }

  @Test
  public void infiniteUpperLimitIsRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> new Vital("invalid", 70, 60, Float.NEGATIVE_INFINITY));
  }

  @Test
  public void invertedRangeIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Vital("invalid", 70, 100, 60));
  }
}
