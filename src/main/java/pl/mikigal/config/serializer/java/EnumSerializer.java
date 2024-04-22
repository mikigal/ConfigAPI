package pl.mikigal.config.serializer.java;

import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.exception.InvalidConfigException;
import pl.mikigal.config.serializer.Serializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Built-in serializer for Enums
 * @see Enum
 * @see Serializer
 * @since 1.2.6
 * @author Mikołaj Gałązka
 */
public class EnumSerializer extends Serializer<Enum> {

    @Override
    protected void saveObject(String path, Enum object, BukkitConfiguration configuration) {
        configuration.set(path + ".value", object.toString());
        configuration.set(path + ".type", object.getClass().getName());
    }

    @Override
    public Enum deserialize(String path, BukkitConfiguration configuration) {
        String value = configuration.getString(path + ".value");
        String classPath = configuration.getString(path + ".type");
        Class<?> clazz;
        Method valueOfMethod;

        try {
            clazz = Class.forName(classPath);
            valueOfMethod = clazz.getMethod("valueOf", String.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException("An error occurred while deserializing class '" + classPath + "'", e);
        }

        try {
            return (Enum<?>) valueOfMethod.invoke(null, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new InvalidConfigException("Value " + value + " is not valid for type " + classPath, e);
        }
    }
}
