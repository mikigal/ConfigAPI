package pl.mikigal.config.serializer;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import pl.mikigal.config.exception.InvalidConfigException;
import pl.mikigal.config.exception.MissingSerializerException;
import pl.mikigal.config.serializer.universal.UniversalCollectionSerializer;
import pl.mikigal.config.serializer.universal.UniversalMapSerializer;

import java.util.*;

/**
 * Utilities for serializers management
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class Serializers {

	/**
	 * Map of registered serializers
	 */
	public static final Map<Class<?>, Serializer<?>> SERIALIZERS = new HashMap<>();

	static {
		register(ItemStack.class, new ItemStackSerializer());
		register(Location.class, new LocationSerializer());
		register(ShapedRecipe.class, new ShapedRecipeSerializer());
		register(PotionEffect.class, new PotionEffectSerializer());
		register(UUID.class, new UUIDSerializer());

		register(Collection.class, new UniversalCollectionSerializer());
		register(Map.class, new UniversalMapSerializer());
	}

	/**
	 * Allows to get Serializer for selected class
	 * @param clazz class for which you want to get serializer
	 * @return serializer for {@param clazz}, null if it does not exist
	 */
	public static <T> Serializer<T> of(Class<T> clazz) {
		if (SERIALIZERS.containsKey(clazz)) {
			return (Serializer<T>) SERIALIZERS.get(clazz);
		}

		for (Map.Entry<Class<?>, Serializer<?>> entry : SERIALIZERS.entrySet()) {
			if (entry.getKey().isAssignableFrom(clazz)) {
				return (Serializer<T>) entry.getValue();
			}
		}

		return null;
	}
	/**
	 * Allows to get Serializer for selected class
	 * @param type class for which you want to get serializer
	 * @return serializer for {@param type}, null if it does not exist
	 */
	public static <T> Serializer<T> of(T type) {
		if (SERIALIZERS.containsKey(type.getClass())) {
			return (Serializer<T>) SERIALIZERS.get(type.getClass());
		}

		for (Map.Entry<Class<?>, Serializer<?>> entry : SERIALIZERS.entrySet()) {
			if (entry.getKey().isAssignableFrom(type.getClass())) {
				return (Serializer<T>) entry.getValue();
			}
		}

		return null;
	}

	/**
	 * Allows to get Serializer for selected class
	 * @param classPath class for which you want to get serializer
	 * @return serializer for {@param classPath}, null if it does not exist
	 */
	public static Serializer<?> of(String classPath) {
		try {
			Class<?> clazz = Class.forName(classPath);
			return of(clazz);
		} catch (ClassNotFoundException e) {
			throw new MissingSerializerException(classPath);
		}
	}

	/**
	 * Allows to register serializer
	 * @param clazz class which serializer can process
	 * @param serializer instance of serializer
	 */
	public static void register(Class<?> clazz, Serializer<?> serializer) {
		if (!clazz.equals(serializer.getSerializerType())) {
			throw new InvalidConfigException("Can't register serializer " + serializer.getClass().getName());
		}

		SERIALIZERS.put(clazz, serializer);
	}

	/**
	 * Unregisters serializer
	 * @param clazz type which you want to unregister
	 */
	public static void unregister(Class<?> clazz) {
		SERIALIZERS.remove(clazz);
	}
}
