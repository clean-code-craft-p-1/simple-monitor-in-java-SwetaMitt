package vitals;

/**
 * Boundary interface for alert output.
 * Solves the clean-architecture/testability concern by letting business rules
 * depend on an abstraction instead of direct console and sleep calls.
 */
@FunctionalInterface
interface VitalAlert {
  /**
   * Sends an alert message through the chosen output mechanism.
   */
  void alert(String message) throws InterruptedException;
}