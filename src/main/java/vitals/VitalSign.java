package vitals;

/**
 * Abstraction for a single vital-sign check.
 * Allows different vital signs to carry their own logic (e.g., age-dependent
 * pulse-rate limits) without coupling unrelated parameters to each other.
 * {@link Vital} is the default range-based implementation; new logic requires
 * only a new implementation of this interface, leaving existing ones unchanged.
 */
public interface VitalSign {
  boolean isOk();
  String outOfRangeMessage();
}
