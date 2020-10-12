package pl.mikigal.config.exception;

public class InvalidConfigFileException extends RuntimeException {

	public InvalidConfigFileException(String message) {
		super(message + " It's probably issue with your config file");
	}
}
