package pl.mikigal.config.serializer;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import pl.mikigal.config.exception.InvalidConfigException;
import pl.mikigal.config.exception.MissingSerializerException;
import pl.mikigal.config.serializer.universal.UniversalListSerializer;
import pl.mikigal.config.serializer.universal.UniversalMapSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Serializers {

	public static final Map<Class<?>, Serializer<?>> SERIALIZERS = new HashMap<>();

	static {
		register(ItemStack.class, new ItemStackSerializer());
		register(Location.class, new LocationSerializer());
		register(ShapedRecipe.class, new ShapedRecipeSerializer());
		register(PotionEffect.class, new PotionEffectSerializer());
		register(UUID.class, new UUIDSerializer());

		register(List.class, new UniversalListSerializer());
		register(Map.class, new UniversalMapSerializer());
	}

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

	public static Serializer<?> of(String classPath) {
		try {
			Class<?> clazz = Class.forName(classPath);
			return of(clazz);
		} catch (ClassNotFoundException e) {
			throw new MissingSerializerException(classPath);
		}
	}

	public static void register(Class<?> clazz, Serializer<?> serializer) {
		if (!clazz.equals(serializer.getSerializerType())) {
			throw new InvalidConfigException("Can't register serializer " + serializer.getClass().getName() + "! You tried to register it for another Class than it's generic type");
		}

		SERIALIZERS.put(clazz, serializer);
	}
}
