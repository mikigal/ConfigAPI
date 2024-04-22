package pl.mikigal.config.serializer.java;

import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.serializer.Serializer;

import java.util.UUID;

/**
 * Built-in serializer for UUID
 * @see UUID
 * @see Serializer
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class UUIDSerializer extends Serializer<UUID> {

	@Override
	protected void saveObject(String path, UUID object, BukkitConfiguration configuration) {
		configuration.set(path, object.toString());
	}

	@Override
	public UUID deserialize(String path, BukkitConfiguration configuration) {
		return UUID.fromString(configuration.getString(path));
	}
}
