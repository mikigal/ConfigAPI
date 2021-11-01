package pl.mikigal.config.serializer.universal;

import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.serializer.Serializer;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Helper built-in serializer for custom objects which implemenet Serializable interface.
 * It uses reflections to serialize all fields from given Object, which are not transient and static.
 * Class must have default constructor (no-args).
 * @see Serializer
 * @see Serializable
 * @since 1.1.7
 * @author Mikołaj Gałązka
 */
public class UniversalObjectSerializer extends Serializer<Serializable> {

	@Override
	protected void saveObject(String path, Serializable object, BukkitConfiguration configuration) {
		this.validateDefaultConstructor(object);

		try {
			for (Field field : object.getClass().getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
					continue;
				}

				field.setAccessible(true);
				Object value = field.get(object);

				try {
					configuration.set(path + "." + configuration.getNameStyle().format(field.getName()), value);
				} catch (Exception e) {
					throw new RuntimeException("An error occurred while serializing field '" + field.getName() + "' from class '" + object.getClass().getName() + "'", e);
				}
			}

			configuration.set(path + "." + configuration.getNameStyle().format("type"), object.getClass().getName());
		} catch (IllegalAccessException e) {
			throw new RuntimeException("An error occurred while serializing class '" + object.getClass().getName() + "'", e);
		}
	}

	@Override
	public Serializable deserialize(String path, BukkitConfiguration configuration) {
		String classPath = configuration.getString(path + "." + configuration.getNameStyle().format("type"));
		Class<?> clazz;

		try {
			clazz = Class.forName(classPath);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("An error occurred while deserializing class '" + classPath + "'", e);
		}

		if (!Serializable.class.isAssignableFrom(clazz)) {
			throw new RuntimeException("Class " + classPath + " does not implements Serializable");
		}

		Serializable instance;
		try {
			 instance = (Serializable) clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Could not create instance of class (" + classPath + ") with default constructor", e);
		}

		try {
			for (Field field : clazz.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
					continue;
				}

				field.setAccessible(true);
				field.set(instance, configuration.get(path + "." + configuration.getNameStyle().format(field.getName())));
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Could not deserialize " + classPath, e);
		}

		return instance;
	}

	private void validateDefaultConstructor(Object object) {
		try {
			object.getClass().getConstructor();
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Class " + object.getClass().getName() + " does not have a default constructor");
		}
	}
}
