package pl.mikigal.config.util;

import pl.mikigal.config.exception.InvalidConfigException;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Utilities for Java types
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class TypeUtils {

	/**
	 * Map of primitive types and it's wrappers
	 */
	public static final Map<Class<?>, Class<?>> WRAPPERS = new HashMap<>();

	static {
		WRAPPERS.put(boolean.class, Boolean.class);
		WRAPPERS.put(int.class, Integer.class);
		WRAPPERS.put(char.class, Character.class);
		WRAPPERS.put(byte.class, Byte.class);
		WRAPPERS.put(short.class, Short.class);
		WRAPPERS.put(double.class, Double.class);
		WRAPPERS.put(long.class, Long.class);
		WRAPPERS.put(float.class, Float.class);
	}

	/**
	 * Allow to check is type simple to handle for API
	 * It can't handle generic types except Collection
	 * @param method method which return type you want to check
	 * @return true if type is primitive, primitive's wrapper, String or Collection's generic is one of its, else false
	 */
	public static boolean isSimpleType(Method method) {
		Class<?> type = method.getReturnType();
		if (isSimpleType(type)) {
			return true;
		}

 		if (!type.isAssignableFrom(Collection.class)) {
			return false;
		}

		if (!(method.getGenericReturnType() instanceof ParameterizedType)) {
			throw new InvalidConfigException("Could not get generic type of " + method.getName());
		}

		ParameterizedType returnTypes = (ParameterizedType) method.getGenericReturnType();
		Type generic = returnTypes.getActualTypeArguments()[0];
		if (!(generic instanceof Class)) {
			throw new InvalidConfigException("Could not get generic type of " + method.getName() + ". Config's method generic type can't be wildcard");
		}

		return isSimpleType((Class<?>) generic);
	}

	/**
	 * Allow to check is type simple to handle for API
	 * It can't handle generic types except Collection
	 * @param object instance of object which you want to check
	 * @return true if type is primitive, primitive's wrapper, String or Collection's generic is one of its, else false
	 */
	public static boolean isSimpleType(Object object) {
		return isSimpleType(object.getClass()) ||
				(object instanceof Collection && isSimpleType(getCollectionGeneric((Collection<?>) object)));
	}

	/**
	 * Allow to check is type simple to handle for API
	 * It can't handle types with generics
	 * @param type type which you want to check
	 * @return true if type is primitive, primitive's wrapper or String, else false
	 */
	public static boolean isSimpleType(Class<?> type) {
		return isPrimitiveOrWrapper(type) || type.equals(String.class);
	}

	/**
	 * Return generic types of given non-empty Collection instance
	 * @param collection instance of Collection
	 * @return generic type of given class
	 * @throws InvalidConfigException if Collection is empty
	 */
	public static Class<?> getCollectionGeneric(Collection<?> collection) {
		for (Object element : collection) {
			if (element == null) {
				continue;
			}

			return element.getClass();
		}

		throw new InvalidConfigException("Can't get generic type of empty Collection");
	}

	/**
	 * Return generic types of given non-empty Map instance
	 * @param map instance of Map
	 * @return array with generic types of given class
	 * @throws InvalidConfigException if map is empty
	 */
	public static Class<?>[] getMapGeneric(Map<?, ?> map) {
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null) {
				continue;
			}

			return new Class<?>[]{entry.getKey().getClass(), entry.getValue().getClass()};
		}

		throw new InvalidConfigException("Can't get generic type of empty Map");
	}

	/**
	 * Return generic types of given non-empty array instance
	 * @param array instance of array
	 * @return generic type of given class
	 * @throws InvalidConfigException if array is empty
	 */
	public static Class<?> getArrayGeneric(Object[] array) {
		for (Object element : array) {
			if (element == null) {
				continue;
			}

			return element.getClass();
		}

		throw new InvalidConfigException("Can't get generic type of empty Array");
	}

	/**
	 * Allows to check it given type primitive or primitive's wrapper
	 * @param clazz class which you want to check
	 * @return true it given type is primitive of it's wrapper, else false
	 */
	public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		return clazz.isPrimitive() ||
				clazz.equals(Boolean.class) ||
				clazz.equals(Integer.class) ||
				clazz.equals(Character.class) ||
				clazz.equals(Byte.class) ||
				clazz.equals(Short.class) ||
				clazz.equals(Double.class) ||
				clazz.equals(Long.class) ||
				clazz.equals(Float.class);
	}

	/**
	 * Allows to get wrapper of primitive type
	 * @param primitive class of primitive type
	 * @return wrapper for selected {@param primitive}, null if given type is non primitive
	 */
	public static Class<?> getWrapper(Class<?> primitive) {
		return WRAPPERS.get(primitive);
	}

	/**
	 * Check is given object array of primitives
	 * @param object to check
	 * @return true if given object is array of primitives, else false
	 */
	public static boolean isPrimitiveArray(Object object) {
		return isPrimitiveArray(object.getClass());
	}

	/**
	 * Check is given clazz is array of primitives
	 * @param clazz to check
	 * @return true if given clazz is array of primitives, else false
	 */
	public static boolean isPrimitiveArray(Class<?> clazz) {
		return clazz.isArray() ||
				clazz.equals(boolean[].class) ||
				clazz.equals(int[].class) ||
				clazz.equals(char[].class) ||
				clazz.equals(byte[].class) ||
				clazz.equals(short[].class) ||
				clazz.equals(double[].class) ||
				clazz.equals(long[].class) ||
				clazz.equals(float[].class);
	}
}
