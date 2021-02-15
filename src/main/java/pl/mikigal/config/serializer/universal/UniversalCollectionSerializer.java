package pl.mikigal.config.serializer.universal;

import org.bukkit.configuration.ConfigurationSection;
import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.exception.InvalidConfigFileException;
import pl.mikigal.config.exception.MissingSerializerException;
import pl.mikigal.config.serializer.Serializer;
import pl.mikigal.config.serializer.Serializers;
import pl.mikigal.config.util.TypeUtils;
import test.TestConfig;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Helper built-in serializer for processing Collections
 * @see Collection
 * @see Serializer
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class UniversalCollectionSerializer extends Serializer<Collection> {

	@Override
	protected void saveObject(String path, Collection object, BukkitConfiguration configuration) {
		if (object.size() == 0) {
			// Java's generics suck so I can't check generic type of empty Collection
			throw new IllegalStateException("Can't set empty Collection to config");
		}

		Class<?> generic = TypeUtils.getCollectionGeneric(object);
		Serializer<?> serializer = Serializers.of(generic);
		if (serializer == null && !TypeUtils.isSimpleType(generic)) {
			throw new MissingSerializerException(generic);
		}

		configuration.set(path + ".collection", object.getClass().getName());
		configuration.set(path + ".type", generic.getName());

		int index = 0;
		for (Object element : object) {
			if (serializer == null) {
				configuration.set(path + "." + index, element);
			}
			else {
				serializer.serialize(path + "." + index, element, configuration);
			}

			index++;
		}
	}

	@Override
	public Collection<?> deserialize(String path, BukkitConfiguration configuration) {
		ConfigurationSection section = configuration.getConfigurationSection(path);

		String collectionRaw = section.getString("collection");
		String serializerRaw = section.getString("type");

		Objects.requireNonNull(collectionRaw, "Collection type is not defined for " + path);
		Objects.requireNonNull(serializerRaw, "Serializer type is not defined for " + path);

		try {
			Serializer<?> serializer = Serializers.of(serializerRaw);
			Class<?> collectionClass = Class.forName(collectionRaw);
			Class<?> serializerClass = Class.forName(serializerRaw);

			if (serializer == null && !TypeUtils.isSimpleType(serializerClass)) {
				throw new MissingSerializerException(serializerClass);
			}

			Collection collection = (Collection) collectionClass.newInstance();
			for (String index : section.getKeys(false)) {
				if (index.equals("type") || index.equals("collection")) {
					continue;
				}

				if (serializer == null) {
					collection.add(configuration.get(path + "." + index));
					continue;
				}

				collection.add(serializer.deserialize(path + "." + index, configuration));
			}

			return collection;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new MissingSerializerException("Could not find class", e);
		}
	}
}
