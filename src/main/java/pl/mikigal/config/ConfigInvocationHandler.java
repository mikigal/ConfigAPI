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

		this.validateConfig();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		String name = method.getName();
		if (name.equals("getBukkitConfiguration")) {
			return this.configuration;
		} else if (name.equals("toString")) {
			return this.clazz.toString();
		} else if (name.equals("hashCode")) {
			return this.hashCode();
		} else if (name.equals("equals")) {
			return proxy == args[0];
		} else if (name.startsWith("get")) {
			return this.processGetter(method);
		} else if (name.startsWith("set")) {
			this.processSetter(method, args);
			return null;
		}

		return null;
	}

	private Object processGetter(Method method) {
		if (!method.getName().startsWith("get")) {
			return null;
		}

		String path = this.getConfigPath(method);
		if (method.getReturnType().equals(String.class) && this.automaticColorStrings) {
			String cache = (String) this.configuration.get(path);
			if (cache == null) {
				throw new InvalidConfigFileException("Variable in config (path: " + path + ") is required, but is not set");
			}

			if (!this.configuration.getCache().containsKey(path)) {
				cache = ConversionUtils.fixColors(cache);
				this.configuration.addToCache(path, cache);
			}

			return cache;
		}

		if (TypeUtils.isSimpleType(method)) {
			Object value = this.configuration.get(path);
			if (value == null) {
				if (!method.isAnnotationPresent(ConfigOptional.class)) {
					throw new InvalidConfigFileException("Variable in config (path: " + path + ") is required, but is not set");
				}

				return null;
			}


			if (!method.getReturnType().isInstance(value) && !value.getClass().equals(TypeUtils.getWrapper(method.getReturnType()))) {
				throw new InvalidConfigException("Method " + method.getName() + " does not return type same as variable in config (path: " + path + "; " + value.getClass() + ")");
			}

			return value;
		}

		Serializer<?> serializer = Serializers.of(method.getReturnType());
		if (serializer == null) {
			throw new MissingSerializerException(method.getReturnType());
		}

		Object cache = this.configuration.get(path);
		if (cache == null) {
			throw new InvalidConfigFileException("Variable in config (path: " + path + ") is required, but is not set");
		}

		if (!serializer.getSerializerType().equals(cache.getClass())) {
			cache = serializer.deserialize(path, this.configuration);
			this.configuration.addToCache(path, cache);
		}

		return cache;
	}

	private void processSetter(Method method, Object[] args) {
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

			if (method.getParameterCount() != 1) {
				throw new InvalidConfigException("Setter method " + name + " has not 1 parameter");
			}

			if (method.isAnnotationPresent(ConfigOptional.class)) {
				throw new InvalidConfigException("Setter method " + name + " has ConfigOptional annotation");
			}

			String getter = name.replace("set", "get");
			if (!this.configPaths.containsKey(getter)) {
				throw new InvalidConfigException("Setter method " + name + " has not getter");
			}

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

	private boolean updateConfigFile() {
		boolean modified = false;
		Object proxy = ReflectionUtils.createHelperProxy(this.clazz);
		for (Method method : this.clazz.getDeclaredMethods()) {
			String name = method.getName();
			if (!name.startsWith("get") && !name.startsWith("set")) {
				throw new InvalidConfigException("Found non getter/setter method (name: " + name + ") in " + clazz.getCanonicalName());
			}

			if (!method.isDefault() || this.configuration.contains(this.getConfigPath(method))) {
				continue;
			}

			Object defaultValue = ReflectionUtils.getDefaultValue(proxy, method);
			if (defaultValue == null) {
				if (!method.isAnnotationPresent(ConfigOptional.class)) {
					throw new InvalidConfigException("Method " + method.getName() + " is not optional, but it's default value is null");
				}

				continue;
			}

			modified = true;
			this.configuration.set(this.getConfigPath(method), defaultValue, method.getAnnotation(Comment.class));
		}

		if (modified) {
			this.configuration.save();
		}

		return modified;
	}

	private void validateConfig() {
		for (Method method : this.clazz.getDeclaredMethods()) {
			this.processGetter(method);
		}
	}

	private String getConfigPath(Method method) {
		return this.configPaths.get(method.getName());
	}
}
