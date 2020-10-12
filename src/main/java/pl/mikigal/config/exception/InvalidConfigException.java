package pl.mikigal.config.exception;

public class InvalidConfigException extends RuntimeException {

	public InvalidConfigException(String message) {
		super(message + " It's probably issue with plugin, contact developer for support");
	}

	public InvalidConfigException(Throwable throwable) {
		super(throwable);
	}

	public InvalidConfigException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
