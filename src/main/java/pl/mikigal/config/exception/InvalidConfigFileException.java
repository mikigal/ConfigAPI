package pl.mikigal.config.exception;

/**
 * Should be thrown when something is wrong with config file
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class InvalidConfigFileException extends RuntimeException {

	public InvalidConfigFileException(String message) {
		super(message + " It's probably issue with your config file");
	}
}
