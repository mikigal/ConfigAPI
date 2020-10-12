package pl.mikigal.config.util;

import pl.mikigal.config.exception.InvalidConfigException;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeUtils {

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

	public static boolean isSimpleType(Method method) {
		Class<?> type = method.getReturnType();
		if (isPrimitiveOrWrapper(type) || type.equals(String.class)) {
			return true;
		}

		if (!type.equals(List.class)) { // Map is manually implemented in Proxy and UniversalMapSerializer
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

	public static boolean isSimpleType(Object object) {
		if (isPrimitiveOrWrapper(object.getClass()) || object.getClass().equals(String.class)) {
			return true;
		}

		if (!(object instanceof List)) {
			return false;
		}

		List<?> list = (List<?>) object;
		if (list.size() == 0) {
			throw new InvalidConfigException("Can't get generic type of empty List");
		}

		Class<?> generic = list.get(0).getClass();
		return isPrimitiveOrWrapper(generic) || generic.equals(String.class);
	}

	public static Class<?> getListGeneric(List<?> list) {
		if (list.size() == 0) {
			throw new InvalidConfigException("Can't get generic type of empty List");
		}

		return list.get(0).getClass();
	}

	public static Class<?>[] getMapGeneric(Map<?, ?> map) {
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			return new Class<?>[]{entry.getKey().getClass(), entry.getValue().getClass()};
		}

		throw new InvalidConfigException("Can't get generic type of empty Map");
	}

	public static boolean isSimpleType(Class<?> type) { // It does not handle List
		return isPrimitiveOrWrapper(type) || type.equals(String.class);
	}

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

	public static Class<?> getWrapper(Class<?> primitive) {
		return WRAPPERS.get(primitive);
	}
}
