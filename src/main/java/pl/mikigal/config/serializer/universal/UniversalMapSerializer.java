package pl.mikigal.config.serializer.universal;

import org.bukkit.configuration.ConfigurationSection;
import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.exception.InvalidConfigFileException;
import pl.mikigal.config.exception.MissingSerializerException;
import pl.mikigal.config.serializer.Serializer;
import pl.mikigal.config.serializer.Serializers;
import pl.mikigal.config.util.TypeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper built-in serializer for processing Map
 * @see Map
 * @see Serializer
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class UniversalMapSerializer extends Serializer<Map> {

	@Override
	protected void saveObject(String path, Map object, BukkitConfiguration configuration) {
		Class<?> generic = TypeUtils.getMapGeneric(object)[1];
		Serializer<?> serializer = Serializers.of(generic);
		if (serializer == null && !TypeUtils.isSimpleType(generic)) {
			throw new MissingSerializerException(generic);
		}

		configuration.set(path + ".type", generic.getName());
		for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
			if (serializer == null) {
				configuration.set(path + "." + entry.getKey(), entry.getValue());
				continue;
			}

			serializer.serialize(path + "." + entry.getKey(), entry.getValue(), configuration);
		}
	}

	@Override
	public Map<?, ?> deserialize(String path, BukkitConfiguration configuration) {
		ConfigurationSection section = configuration.getConfigurationSection(path);
		if (section == null || section.getKeys(false).size() == 0) {
			return new HashMap<>();
		}

		if (!section.contains("type")) {
			Map map = new HashMap<>();
			for (String key : section.getKeys(false)) {
				map.put(key, section.get(key));
			}

			return map;
		}

		String serializerClass = section.getString("type");
		if (serializerClass == null) {
			throw new InvalidConfigFileException("Serializer type is not defined for " + path);
		}

		Serializer<?> serializer = Serializers.of(serializerClass);

		try {
			if (serializer == null && !TypeUtils.isSimpleType(Class.forName(serializerClass))) {
				throw new MissingSerializerException(Class.forName(serializerClass));
			}
		} catch (ClassNotFoundException e) {
			throw new MissingSerializerException("Could not find class " + serializerClass);
		}

		Map map = new HashMap<>();
		for (String key : section.getKeys(false)) {
			if (key.equals("type")) {
				continue;
			}

			if (serializer == null) {
				map.put(key, configuration.get(path + "." + key));
				continue;
			}

			map.put(key, serializer.deserialize(path + "." + key, configuration));
		}

		return map;
	}
}
