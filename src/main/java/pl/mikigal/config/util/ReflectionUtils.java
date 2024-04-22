package pl.mikigal.config.util;

import org.bukkit.Bukkit;
import pl.mikigal.config.exception.InvalidConfigException;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for reflections
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class ReflectionUtils {

	private static final Map<Class<?>, MethodHandles.Lookup> lookups = new HashMap<>();

	/**
	 * Allows to get default value of method from interface
	 * @param method method of which you want to get default value
	 * @return default value of method from interface
	 */
	public static Object getDefaultValue(Method method) {
		try {
			Class<?> clazz = method.getDeclaringClass();
			return getLookup(clazz)
					.in(clazz)
					.unreflectSpecial(method, clazz)
					.bindTo(createHelperProxy(method.getDeclaringClass()))
					.invoke();
		} catch (Throwable throwable) {
			throw new InvalidConfigException(throwable);
		}
	}

	/**
	 * Creates private lookup for given class
	 * For Java 8 it gets value of MethodHandles.Lookup.IMPL_LOOKUP, for newer versions invokes MethodHandles.privateLookupIn()
	 * Reference: https://github.com/OpenFeign/feign/commit/3494a76f160d6622129d59a6c79358dbccf6e6d6
	 * @param clazz for which you want to create lookup
	 * @return instance of lookup
	 */
	private static MethodHandles.Lookup createLookup(Class<?> clazz) {
		boolean oldJava = isOldJava();

		try {
			if (oldJava) {
				Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
				field.setAccessible(true);

				return (MethodHandles.Lookup) field.get(null);
			}

			Object privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class)
					.invoke(null, clazz, MethodHandles.lookup());

			return (MethodHandles.Lookup) privateLookupIn;
		} catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
			throw new InvalidConfigException("Could not get MethodHandles.Lookup for " + clazz.getName() + " (legacy way: " + oldJava + ")", e);
		}
	}

	/**
	 * Gets lookup for given class from cache or create new one if it doesn't exist
	 * @param clazz for which you want to get lookup
	 * @return instance of lookup for given class
	 */
	private static MethodHandles.Lookup getLookup(Class<?> clazz) {
		if (!lookups.containsKey(clazz)) {
			lookups.put(clazz, createLookup(clazz));
		}

		return lookups.get(clazz);
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
	 * Check Java version
	 * @return true for Java 8, false for Java 9 or newer
	 */
	private static boolean isOldJava() {
		String javaVersion = System.getProperty("java.version");
		return javaVersion.startsWith("1.8") || javaVersion.startsWith("8");
	}

	/**
	 * Check bukkit versions
	 * @return false for Minecraft 1.12 or older, true for 1.13 or newer
	 */
	public static boolean isNewVersion() {
		int version = Integer.parseInt(Bukkit.getBukkitVersion().split("\\.")[1].split("-")[0]);
		return version >= 12;
	}

	/**
	 * Check bukkit versions
	 * @return false for Minecraft 1.18 or older, true for 1.18 or newer
	 */
	public static boolean isVeryNewVersion() {
		int version = Integer.parseInt(Bukkit.getBukkitVersion().split("\\.")[1].split("-")[0]);
		return version >= 18;
	}
}
