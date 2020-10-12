package pl.mikigal.config;

import org.bukkit.plugin.java.JavaPlugin;
import pl.mikigal.config.annotation.ConfigName;
import pl.mikigal.config.exception.InvalidConfigException;
import pl.mikigal.config.serializer.Serializer;
import pl.mikigal.config.serializer.Serializers;
import pl.mikigal.config.style.CommentStyle;
import pl.mikigal.config.style.NameStyle;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class ConfigAPI {

	private static JavaPlugin plugin;
	private static final Map<String, Config> configurations = new HashMap<>();
	private static final Map<String, BukkitConfiguration> rawConfigurations = new HashMap<>();

	public static <T extends Config> T init(Class<T> clazz, NameStyle nameStyle, CommentStyle commentStyle, boolean automaticColorStrings, JavaPlugin plugin) {
		ConfigAPI.plugin = plugin;
		ConfigName configName = clazz.getAnnotation(ConfigName.class);
		if (configName == null) {
			throw new InvalidConfigException("Config must have annotation ConfigName with file's name");
		}

		String name = configName.value() + (configName.value().endsWith(".yml") ? "" : ".yml");
		File file = new File(plugin.getDataFolder(), name);

		BukkitConfiguration rawConfiguration = new BukkitConfiguration(file, nameStyle, commentStyle, automaticColorStrings);
		rawConfigurations.put(name, rawConfiguration);

		T configuration = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ConfigInvocationHandler(clazz, rawConfiguration, automaticColorStrings));
		configurations.put(name, configuration);

		return configuration;
	}

	public static BukkitConfiguration getRawConfiguration(String name) {
		return rawConfigurations.get(name.endsWith(".yml") ? name : name + ".yml");
	}

	public static BukkitConfiguration getRawConfiguration(Class<? extends Config> config) {
		ConfigName configName = config.getAnnotation(ConfigName.class);
		if (configName == null) {
			throw new InvalidConfigException("Config must have annotation ConfigName with file's name");
		}

		String name = configName.value() + (configName.value().endsWith(".yml") ? "" : ".yml");
		return rawConfigurations.get(name);
	}

	public static Config getConfiguration(String name) {
		return configurations.get(name.endsWith(".yml") ? name : name + ".yml");
	}

	public static <T extends Config> T getConfiguration(Class<T> config) {
		ConfigName configName = config.getAnnotation(ConfigName.class);
		if (configName == null) {
			throw new InvalidConfigException("Config must have annotation ConfigName with file's name");
		}

		String name = configName.value() + (configName.value().endsWith(".yml") ? "" : ".yml");
		return (T) configurations.get(name);
	}

	public static void registerSerializer(Class<?> clazz, Serializer<?> serializer) {
		Serializers.register(clazz, serializer);
	}

	public static JavaPlugin getPlugin() {
		return plugin;
	}
}
