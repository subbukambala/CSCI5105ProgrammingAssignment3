/**
 * @authors Bala Subrahmanyam Kambala, Daniel William DaCosta
 * @license GPLv3 (http://www.gnu.org/copyleft/gpl.html)
 * @descriptrion Logging Facilities.
 */

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

public class Logger {

    private java.util.logging.Logger logger;

    Logger(String subsys, String filename) {
        // fh = new FileHandler("c:\\MyLogFile.log", true);

    }

    Logger(String subsys) {
        logger = java.util.logging.Logger.getLogger(subsys);
        logger.setLevel(Level.INFO);
        Handler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);

        // SimpleFormatter formatter = new SimpleFormatter();
        // StreamHandler sh = new StreamHandler(System.out, formatter);
        // sh.setLevel(Level.ALL);
        logger.addHandler(ch);
    }

    void log(Level lvl, String msg) {
        logger.log(lvl, msg);
    }
}
