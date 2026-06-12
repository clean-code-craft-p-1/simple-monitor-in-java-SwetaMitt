package vitals;

import java.util.logging.Logger;

/**
 * Console adapter for alert output.
 * Solves the README request to separate pure functions from I/O by isolating
 * printing and waiting away from vital range decisions.
 */
public final class Alerter {
  private static final Logger LOGGER = Logger.getLogger(Alerter.class.getName());

  /**
   * Delay between blink steps in milliseconds.
   * Package-private so tests can set it to 0 to avoid 12-second waits while
   * still exercising the production alert path end-to-end.
   */
  static long blinkDelayMs = 1000;

  private Alerter() {
  }

  /**
   * Logs the failing vital message and starts the visible alert signal.
   * Security/clean-code: uses a logger instead of raw console output.
   */
  static void alert(String message) throws InterruptedException {
    LOGGER.warning(message);
    blink();
  }

  /**
   * Performs the repeated visual alert that used to be duplicated for each vital.
   */
  private static void blink() throws InterruptedException {
    for (int i = 0; i < 6; i++) {
      LOGGER.info("* ");
      Thread.sleep(blinkDelayMs);
      LOGGER.info(" *");
      Thread.sleep(blinkDelayMs);
    }
  }
}
