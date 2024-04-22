package pl.mikigal.config.serializer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import pl.mikigal.config.exception.InvalidConfigException;
import pl.mikigal.config.exception.MissingSerializerException;
import pl.mikigal.config.serializer.bukkit.*;
import pl.mikigal.config.serializer.java.EnumSerializer;
import pl.mikigal.config.serializer.java.UUIDSerializer;
import pl.mikigal.config.serializer.universal.UniversalArraySerializer;
import pl.mikigal.config.serializer.universal.UniversalCollectionSerializer;
import pl.mikigal.config.serializer.universal.UniversalMapSerializer;
import pl.mikigal.config.serializer.universal.UniversalObjectSerializer;

import java.io.Serializable;
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
	public static final Map<Class<?>, Serializer<?>> SERIALIZERS = new LinkedHashMap<>();
	private static final Serializer UNIVERSAL_ARRAY_SERIALIZER = new UniversalArraySerializer();
	private static final Serializer UNIVERSAL_OBJECT_SERIALIZER = new UniversalObjectSerializer();

	static {
		register(UUID.class, new UUIDSerializer());
		register(Enum.class, new EnumSerializer());

		register(ItemStack.class, new ItemStackSerializer());
		register(Location.class, new LocationSerializer());
		register(ShapedRecipe.class, new ShapedRecipeSerializer());
		register(PotionEffect.class, new PotionEffectSerializer());
		register(Material.class, new MaterialSerializer());
		register(Biome.class, new BiomeSerializer());

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

		if (clazz.isArray()) {
			return UNIVERSAL_ARRAY_SERIALIZER;
		}

		if (Serializable.class.isAssignableFrom(clazz)) {
			return UNIVERSAL_OBJECT_SERIALIZER;
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

		if (type.getClass().isArray()) {
			return UNIVERSAL_ARRAY_SERIALIZER;
		}

		if (type instanceof Serializable) {
			return UNIVERSAL_OBJECT_SERIALIZER;
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
