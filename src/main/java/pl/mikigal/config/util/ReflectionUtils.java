package pl.mikigal.config.util;

import org.bukkit.Bukkit;
import pl.mikigal.config.exception.InvalidConfigException;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Utilities for reflections
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class ReflectionUtils {

	private static final MethodHandles.Lookup lookup;

	static {
		try {
			Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
			field.setAccessible(true);

			lookup = (MethodHandles.Lookup) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new InvalidConfigException("Could not get MethodHandles.Lookup", e);
		}
	}

	/**
	 * Allows to get default value of method from interface
	 * @param method method of which you want to get default value
	 * @return default value of method from interface
	 */
	public static Object getDefaultValue(Method method) {
		try {
			Class<?> clazz = method.getDeclaringClass();
			return lookup
					.in(clazz)
					.unreflectSpecial(method, clazz)
					.bindTo(createHelperProxy(method.getDeclaringClass()))
					.invoke();
		} catch (Throwable throwable) {
			throw new InvalidConfigException(throwable);
		}
	}

	/**
	 * Creates instance of proxy
	 * @param clazz class which you want to get instance of
	 * @return instance of proxy
	 */
	private static Object createHelperProxy(Class<?> clazz) {
		return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
				(Object object, Method method, Object[] args) -> null);
	}

	/**
	 * Check bukkit versions
	 * @return false for Minecraft 1.12 or older, true for 1.13 or newer
	 */
	public static boolean isNewVersion() {
		int version = Integer.parseInt(Bukkit.getBukkitVersion().split("\\.")[1].split("-")[0]);
		return version >= 12;
	}
}
