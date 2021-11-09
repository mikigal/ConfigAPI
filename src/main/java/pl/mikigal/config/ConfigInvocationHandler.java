package pl.mikigal.config;

import pl.mikigal.config.annotation.Comment;
import pl.mikigal.config.annotation.ConfigOptional;
import pl.mikigal.config.annotation.ConfigPath;
import pl.mikigal.config.exception.InvalidConfigException;
import pl.mikigal.config.exception.InvalidConfigFileException;
import pl.mikigal.config.exception.MissingSerializerException;
import pl.mikigal.config.serializer.Serializer;
import pl.mikigal.config.serializer.Serializers;
import pl.mikigal.config.util.ConversionUtils;
import pl.mikigal.config.util.ReflectionUtils;
import pl.mikigal.config.util.TypeUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Proxy invocation handler for configs
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class ConfigInvocationHandler implements InvocationHandler {

	private final Class<? extends Config> clazz;
	private final Map<String, String> configPaths;
	private final BukkitConfiguration configuration;
	private final boolean automaticColorStrings;

	public ConfigInvocationHandler(Class<? extends Config> clazz, BukkitConfiguration configuration, boolean automaticColorStrings) {
		this.clazz = clazz;
		this.configPaths = new HashMap<>();
		this.configuration = configuration;
		this.automaticColorStrings = automaticColorStrings;

		this.prepareMethods();
		if (this.updateConfigFile()) {
			this.configuration.load();
			this.prepareMethods();
		}

		// Execute all getter for test and fill cache
		for (Method method : this.clazz.getDeclaredMethods()) {
			this.executeGetter(method);
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		String name = method.getName();
		if (name.equals("getBukkitConfiguration")) {
			return this.configuration;
		}
		else if (name.equals("toString")) {
			return this.clazz.toString();
		}
		else if (name.equals("hashCode")) {
			return this.hashCode();
		}
		else if (name.equals("equals")) {
			return proxy == args[0];
		}
		else if (name.startsWith("get")) {
			return this.executeGetter(method);
		}
		else if (name.startsWith("set")) {
			this.executeSetter(method, args);
			return null;
		}

		return null;
	}

	/**
	 * Execute getter method
	 * @param method instance of called method
	 */
	private Object executeGetter(Method method) {
		if (!method.getName().startsWith("get")) {
			return null;
		}

		String path = this.getConfigPath(method);
		Object value = this.configuration.get(path);

		if (value == null) {
			if (!method.isAnnotationPresent(ConfigOptional.class)) {
				throw new InvalidConfigFileException("Variable in config (path: " + path + ") is required, but is not set");
			}

			return null;
		}

		if (method.getReturnType().equals(String.class) && this.automaticColorStrings) {
			String asString = (String) value;
			if (!this.configuration.getCache().containsKey(path)) {
				asString = ConversionUtils.fixColors(asString);
				this.configuration.addToCache(path, asString);
			}

			return asString;
		}

		if (TypeUtils.isSimpleType(method)) {
			return value;
		}

		Serializer<?> serializer = Serializers.of(method.getReturnType());
		if (serializer == null) {
			throw new MissingSerializerException(method.getReturnType());
		}

		if (!serializer.getSerializerType().equals(value.getClass())) {
			value = serializer.deserialize(path, this.configuration);
			this.configuration.addToCache(path, value);
		}

		return value;
	}

	/**
	 * Execute setter method
	 * @param method instance of called method
	 * @param args arguments of called method
	 */
	private void executeSetter(Method method, Object[] args) {
		if (!method.getName().startsWith("set")) {
			return;
		}

		Object value = args[0];
		if (value == null && !method.isAnnotationPresent(ConfigOptional.class)) {
			throw new InvalidConfigException("You can't set value to config setter that isn't @ConfigOptional (method: " + method + ")");
		}

		configuration.set(this.getConfigPath(method), value, method.getAnnotation(Comment.class));
		this.configuration.save();
	}

	/**
	 * Validate methods, prepare paths of fields
	 */
	private void prepareMethods() {
		// Process getters
		for (Method method : this.clazz.getDeclaredMethods()) {
			String name = method.getName();
			if (!name.startsWith("get")) {
				continue;
			}

			if (!method.isDefault()) {
				throw new InvalidConfigException("Getter method " + name + " has not default value");
			}

			if (Map.class.isAssignableFrom(method.getReturnType())) {
				ParameterizedType returnTypes = (ParameterizedType) method.getGenericReturnType();
				if (!returnTypes.getActualTypeArguments()[0].equals(String.class)) {
					throw new InvalidConfigException("You can serialize Map only with String key");
				}
			}

			if (TypeUtils.isPrimitiveArray(method.getReturnType())) {
				throw new InvalidConfigException("Arrays with primitives are not supported");
			}

			ConfigPath configPath = method.getAnnotation(ConfigPath.class);
			this.configPaths.put(name, configPath == null ? configuration.getNameStyle().format(name) : configPath.value());
		}

		// Process setters
		for (Method method : this.clazz.getDeclaredMethods()) {
			String name = method.getName();
			if (!name.startsWith("set")) {
				continue;
			}

			if (method.isDefault()) {
				throw new InvalidConfigException("Setter method " + name + " has default value");
			}

			if (!method.getReturnType().equals(void.class)) {
				throw new InvalidConfigException("Setter method " + name + " is not void type");
			}

			if (TypeUtils.isPrimitiveArray(method.getReturnType())) {
				throw new InvalidConfigException("Arrays with primitives are not supported");
			}

			if (method.getParameterCount() != 1) {
				throw new InvalidConfigException("Setter method " + name + " has not 1 parameter");
			}

			if (method.isAnnotationPresent(ConfigOptional.class)) {
				throw new InvalidConfigException("Setter method " + name + " has ConfigOptional annotation");
			}

			String getter = name.replaceFirst("set", "get");

			try {
				if (!this.clazz.getDeclaredMethod(getter).getReturnType().equals(method.getParameters()[0].getType())) {
					throw new InvalidConfigException("Setter method " + name + " has another parameter type than getter");
				}
			} catch (NoSuchMethodException e) {
				throw new InvalidConfigException("Setter method " + name + " has not getter");
			}

			ConfigPath configPath = method.getAnnotation(ConfigPath.class);
			this.configPaths.put(name, configPath == null ? configuration.getNameStyle().format(name) : configPath.value());
		}
	}

	/**
	 * Check for new methods in config's class, update config's file if new methods exist
	 * @return true if config was update, else false
	 */
	private boolean updateConfigFile() {
		boolean modified = false;
		for (Method method : this.clazz.getDeclaredMethods()) {
			String name = method.getName();
			if (!name.startsWith("get") && !name.startsWith("set")) {
				throw new InvalidConfigException("Found non getter/setter method (name: " + name + ") in " + clazz.getCanonicalName());
			}

			if (!name.startsWith("set") && method.getParameters().length != 0) {
				throw new InvalidConfigException("Found method with parameters (name: " + name + ") in " + clazz.getCanonicalName());
			}

			if (!method.isDefault() || this.configuration.contains(this.getConfigPath(method))) {
				continue;
			}

			Object defaultValue = ReflectionUtils.getDefaultValue(method);
			if (defaultValue == null && !method.isAnnotationPresent(ConfigOptional.class)) {
				throw new InvalidConfigException("Method " + method.getName() + " is not optional, but it's default value is null");
			}

			if (defaultValue instanceof Collection && ((Collection<?>) defaultValue).size() == 0) {
				throw new InvalidConfigException("Could not use empty Collection as default value, method: " + name);
			}

			if (defaultValue instanceof Map && ((Map<?, ?>) defaultValue).size() == 0) {
				throw new InvalidConfigException("Could not use empty Map as default value, method: " + name);
			}

			modified = true;
			this.configuration.set(this.getConfigPath(method), defaultValue, method.getAnnotation(Comment.class));
		}

		if (modified) {
			this.configuration.save();
		}

		return modified;
	}

	/**
	 * Get path of method from cache
	 * @param method instance of method
	 * @return field's path from cache
	 */
	private String getConfigPath(Method method) {
		return this.configPaths.get(method.getName());
	}
}
