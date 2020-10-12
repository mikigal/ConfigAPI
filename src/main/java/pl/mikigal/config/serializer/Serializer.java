package pl.mikigal.config.serializer;

import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.exception.InvalidConfigException;

import java.lang.reflect.ParameterizedType;

public abstract class Serializer<T> {

	private final Class<T> serializerType;

	public Serializer() {
		ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
		if (!(type.getActualTypeArguments()[0] instanceof Class)) {
			throw new InvalidConfigException("Serializer can't have wildcard in generic");
		}

		this.serializerType = (Class<T>) type.getActualTypeArguments()[0];
	}

	public final void serialize(String path, Object object, BukkitConfiguration configuration) {
		configuration.set(path, null);
		if (object == null) {
			return;
		}

		this.saveObject(path, (T) object, configuration);
	}

	protected abstract void saveObject(String path, T object, BukkitConfiguration configuration);

	public abstract T deserialize(String path, BukkitConfiguration configuration);

	public Class<T> getSerializerType() {
		return serializerType;
	}
}
