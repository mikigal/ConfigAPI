package pl.mikigal.config;

import org.bukkit.plugin.java.JavaPlugin;
import pl.mikigal.config.annotation.Comment;
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

/**
 * Main class of API, it allows to manage configs
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class ConfigAPI {

	/**
	 * Instance of plugin
	 */
	private static JavaPlugin plugin;

	/**
	 * Map for keeping instances of initialized configs
	 */
	private static final Map<String, Config> configurations = new HashMap<>();

	/**
	 * Map for keeping instances of BukkitConfiguration for configs
	 * @see BukkitConfiguration
	 */
	private static final Map<String, BukkitConfiguration> rawConfigurations = new HashMap<>();

	/**
	 * Initializes instance of Config
	 * @param clazz Class of your Config interface
	 * @param nameStyle Style of config's fields names
	 * @param commentStyle Style of config's comments
	 * @param automaticColorStrings Automatic translate '&' based colors
	 * @param directory The config's directory.
	 * @param plugin Instance of your plugin
	 * @see NameStyle
	 * @see CommentStyle
	 * @return Instance of {@param clazz} ready to use methods
	 */
	public static <T extends Config> T init(Class<T> clazz, NameStyle nameStyle, CommentStyle commentStyle,
											boolean automaticColorStrings, File directory, JavaPlugin plugin){

		ConfigAPI.plugin = plugin;
		ConfigName configName = clazz.getAnnotation(ConfigName.class);
		if (configName == null) {
			throw new InvalidConfigException("Config must have annotation ConfigName with file's name");
		}

		Comment configCommentAnnotation = clazz.getAnnotation(Comment.class);
		String configComment = configCommentAnnotation == null ? null : configCommentAnnotation.value();

		String name = configName.value() + (configName.value().endsWith(".yml") ? "" : ".yml");
		File file = new File(directory, name);

		BukkitConfiguration rawConfiguration = new BukkitConfiguration(directory, file, nameStyle, commentStyle,
				automaticColorStrings, configComment);
		rawConfigurations.put(name, rawConfiguration);

		T configuration = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
				new ConfigInvocationHandler(clazz, rawConfiguration, automaticColorStrings));
		configurations.put(name, configuration);

		return configuration;
	}

	/**
	 * Initializes instance of Config
	 * @param clazz Class of your Config interface
	 * @param nameStyle Style of config's fields names
	 * @param commentStyle Style of config's comments
	 * @param automaticColorStrings Automatic translate '&' based colors
	 * @param plugin Instance of your plugin
	 * @see NameStyle
	 * @see CommentStyle
	 * @return Instance of {@param clazz} ready to use methods
	 */
	public static <T extends Config> T init(Class<T> clazz, NameStyle nameStyle, CommentStyle commentStyle,
											boolean automaticColorStrings, JavaPlugin plugin) {
		return init(clazz, nameStyle, commentStyle, automaticColorStrings, plugin.getDataFolder(), plugin);
	}



	/**
	 * Initializes instance of Config with default values
	 * (CAMEL_CASE as NameStyle, ABOVE_CONTENT as CommentStyle, enabled automatic translation of '&' based colors)
	 * @param clazz Class of your Config interface
	 * @param plugin Instance of your plugin
	 * @return Instance of {@param clazz} ready to use methods
	 */
	public static <T extends Config> T init(Class<T> clazz, JavaPlugin plugin) {
		return init(clazz, NameStyle.CAMEL_CASE, CommentStyle.ABOVE_CONTENT,
				true, plugin.getDataFolder(), plugin);
	}

	/**
	 * Initializes instance of Config with default values
	 * (CAMEL_CASE as NameStyle, ABOVE_CONTENT as CommentStyle, enabled automatic translation of '&' based colors)
	 * @param clazz Class of your Config interface
	 * @param directory The config's directory.
	 * @param plugin Instance of your plugin
	 * @return Instance of {@param clazz} ready to use methods
	 */
	public static <T extends Config> T init(Class<T> clazz, File directory, JavaPlugin plugin) {
		return init(clazz, NameStyle.CAMEL_CASE, CommentStyle.ABOVE_CONTENT, true, directory, plugin);
	}

	/**
	 * Allows to get BukkitConfiguration object for config. It allows to access Bukkit's YamlConfiguration raw methods
	 * @param name Name of your config
	 * @see BukkitConfiguration
	 * @see org.bukkit.configuration.file.YamlConfiguration
	 * @return Instance of BukkitConfiguration for config for {@param name}
	 */
	public static BukkitConfiguration getRawConfiguration(String name) {
		return rawConfigurations.get(name.endsWith(".yml") ? name : name + ".yml");
	}

	/**
	 * Allows to get BukkitConfiguration object for config. It allows to access Bukkit's YamlConfiguration raw methods
	 * @param config Class of your config
	 * @see BukkitConfiguration
	 * @see org.bukkit.configuration.file.YamlConfiguration
	 * @return Instance of BukkitConfiguration for config with {@param config}
	 */
	public static BukkitConfiguration getRawConfiguration(Class<? extends Config> config) {
		ConfigName configName = config.getAnnotation(ConfigName.class);
		if (configName == null) {
			throw new InvalidConfigException("Config must have annotation ConfigName with file's name");
		}

		String name = configName.value() + (configName.value().endsWith(".yml") ? "" : ".yml");
		return rawConfigurations.get(name);
	}

	/**
	 * Allows to get instance of config by class
	 * @param config class of config
	 * @return instance of previously initialized config
	 */
	public static <T extends Config> T getConfiguration(Class<T> config) {
		ConfigName configName = config.getAnnotation(ConfigName.class);
		if (configName == null) {
			throw new InvalidConfigException("Config must have annotation ConfigName with file's name");
		}

		String name = configName.value() + (configName.value().endsWith(".yml") ? "" : ".yml");
		return (T) configurations.get(name);
	}

	/**
	 * Registers serializer, all serializers must be registered before using <code>ConfigAPI.init()</code> method
	 * @param clazz class which serializer can process
	 * @param serializer instance of serializer
	 */
	public static void registerSerializer(Class<?> clazz, Serializer<?> serializer) {
		Serializers.register(clazz, serializer);
	}

	/**
	 * Return instance of plugin given in <code>ConfigAPI.init()</code> method
	 * @return instance of plugin, nullable if called before <code>ConfigAPI.init()</code>
	 */
	public static JavaPlugin getPlugin() {
		return plugin;
	}
}
