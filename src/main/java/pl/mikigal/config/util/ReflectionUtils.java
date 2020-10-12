package pl.mikigal.config.util;

import org.bukkit.Bukkit;
import pl.mikigal.config.exception.InvalidConfigException;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReflectionUtils {

	private static final Constructor<MethodHandles.Lookup> lookupConstructor;

	static {
		try {
			lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
			lookupConstructor.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new InvalidConfigException("Could not get MethodHandles.Lookup constructor", e);
		}
	}

	public static Object getDefaultValue(Object proxy, Method method) {
		try {
			Class<?> clazz = method.getDeclaringClass();
			return lookupConstructor.newInstance(clazz, MethodHandles.Lookup.PRIVATE)
					.in(clazz)
					.unreflectSpecial(method, clazz)
					.bindTo(proxy)
					.invoke();
		} catch (Throwable throwable) {
			throw new InvalidConfigException(throwable);
		}
	}

	public static Object createHelperProxy(Class<?> clazz) {
		return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (Object object, Method method, Object[] args) -> null);
	}

	public static boolean isNewVersion() {
		int version = Integer.parseInt(Bukkit.getBukkitVersion().split("\\.")[1].split("-")[0]);
		return version >= 12;
	}
}
