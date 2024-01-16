package software.amazon.datazone.environment.helper;

import software.amazon.cloudformation.proxy.Logger;

public class LoggerWrapper {
    private Logger logger;

    public LoggerWrapper(final Logger logger) {
        this.logger = logger;
    }

    public void info(final String message) {
        log("INFO", message);
    }

    public void info(final String template, final Object... args) {
        this.log("INFO", String.format(template, args));
    }

    private void log(final String logLevel, final String message) {
        this.logger.log(String.format("[%s] %s", logLevel, message));
    }

}
