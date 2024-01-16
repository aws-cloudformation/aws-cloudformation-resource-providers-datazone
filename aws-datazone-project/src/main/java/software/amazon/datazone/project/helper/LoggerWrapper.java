package software.amazon.datazone.project.helper;

import software.amazon.cloudformation.proxy.Logger;

public class LoggerWrapper {
    private Logger logger;

    public LoggerWrapper(final Logger logger) {
        this.logger = logger;
    }

    public void info(final String message) {
        log("INFO", message);
    }

    private void log(final String logLevel, final String message) {
        logger.log(String.format("[%s] %s", logLevel, message));
    }

}
