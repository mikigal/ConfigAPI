package pl.mikigal.config.serializer;

import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.exception.InvalidConfigException;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * Serializer allows you to make serializer for every Java type.
 * When you make serializer for e. g. ItemStack you can easily make getter/setter for this type in your config,
 * then API will automatically process it and save/read this object to your config
 * @param <T> class which you want to serialize
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public abstract class Serializer<T> {

	/**
	 * Type which serializer can process
	 */
	private final Class<T> serializerType;

	public Serializer() {
		ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
		if (!(type.getActualTypeArguments()[0] instanceof Class)) {
			throw new InvalidConfigException("Serializer can't have wildcard in generic");
		}

		this.serializerType = (Class<T>) type.getActualTypeArguments()[0];
	}

	/**
	 * Writes object to config, this method should be used by end user
	 * @param path path in config
	 * @param object object which you want to write
	 * @param configuration instance of BukkitConfiguration
	 * @see BukkitConfiguration
	 */
	public final void serialize(String path, Object object, BukkitConfiguration configuration) {
		configuration.set(path, null);
		if (object == null) {
			return;
		}

		this.saveObject(path, (T) object, configuration);
		configuration.addToCache(path, object);
	}

	/**
	 * Internal method for writing object to config.
	 * It's implementation in your serializer must write data from {@param object} to config using <code>set(Object, String)</code> method {@param configuration}
	 * @param path path in config
	 * @param object object which you want to write
	 * @param configuration instance of BukkitConfiguration
	 * @see BukkitConfiguration
	 */
	protected abstract void saveObject(String path, T object, BukkitConfiguration configuration);

	/**
	 * Reads data from config and returns instance of object created by readen data.
	 * It's implementation in your serializer must read data using <code>get*</code> methods from {@param configuration}
	 * @param path path of object in config
	 * @param configuration instance of BukkitConfiguration
	 * @return instance of readen object
	 * @see BukkitConfiguration
	 */
	public abstract T deserialize(String path, BukkitConfiguration configuration);

	/**
	 * Returns type which serializer can process
	 * @return type which serializer can process
	 */
	public Class<T> getSerializerType() {
		return serializerType;
	}
}
