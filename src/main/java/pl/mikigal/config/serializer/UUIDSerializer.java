package pl.mikigal.config.serializer;

import pl.mikigal.config.BukkitConfiguration;

import java.util.UUID;

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
