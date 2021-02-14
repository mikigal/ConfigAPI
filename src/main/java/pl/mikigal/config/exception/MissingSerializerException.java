package pl.mikigal.config.exception;

/**
 * Should be thrown when trying to access serializer which does not exists
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class MissingSerializerException extends InvalidConfigException {

	public MissingSerializerException(Class<?> clazz) {
		super("Serializer for " + clazz + " does not exists. Did you forget to register it?");
	}

	public MissingSerializerException(String clazz) {
		super("Class " + clazz + " does not exists, can't get Serializer of it");
	}

	public MissingSerializerException(Object object) {
		this(object.getClass());
	}
}
