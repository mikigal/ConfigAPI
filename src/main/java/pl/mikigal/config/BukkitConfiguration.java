package pl.mikigal.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import pl.mikigal.config.annotation.Comment;
import pl.mikigal.config.exception.InvalidConfigException;
import pl.mikigal.config.exception.MissingSerializerException;
import pl.mikigal.config.serializer.Serializer;
import pl.mikigal.config.serializer.Serializers;
import pl.mikigal.config.style.CommentStyle;
import pl.mikigal.config.style.NameStyle;
import pl.mikigal.config.util.ConversionUtils;
import pl.mikigal.config.util.ReflectionUtils;
import pl.mikigal.config.util.TypeUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Utilities for management of config files
 * @since 1.0
 * @author Mikołaj Gałązka
 * @see YamlConfiguration
 */
public class BukkitConfiguration extends YamlConfiguration {

	/**
	 * Properties of config
	 */
	private final File directory;
	private final File file;
	private final NameStyle nameStyle;
	private final CommentStyle commentStyle;
	private final boolean automaticColorStrings;
	private final String configComment;

	/**
	 * Caches
	 */
	private final Map<String, Object> cache;
	private final Map<String, String> comments;

	/**
	 *
	 * Reflection Path
	 */
	private final String dumperFieldName = ReflectionUtils.isVeryNewVersion() ? "yamlDumperOptions" : "yamlOptions";

	public BukkitConfiguration(File directory, File file, NameStyle nameStyle, CommentStyle commentStyle,
							   boolean automaticColorStrings, String configComment) {
		this.directory = directory;
		this.file = file;
		this.nameStyle = nameStyle;
		this.commentStyle = commentStyle;
		this.automaticColorStrings = automaticColorStrings;
		this.cache = new HashMap<>();
		this.comments = new HashMap<>();
		this.configComment = configComment;

		this.copyDefaultConfig();
		this.load();
	}

	/**
	 * Set value of field
	 * @param path path in config
	 * @param value value which you want to set
	 * @param comment field's comment, can be null
	 */
	public void set(String path, Object value, Comment comment) {
		if (comment != null) {
			this.comments.put(path, comment.value());
		}

		this.set(path, value);
	}

	@Override
	public void set(String path, Object value) {
		if (!(value instanceof Collection) && !(value instanceof Map) && (value == null || TypeUtils.isSimpleType(value))) {
			super.set(path, value);

			if (value == null) {
				if (this.cache.containsKey(path)) {
					this.cache.put(path, null);
				}

				return;
			}

			if (value.getClass().equals(String.class) && this.automaticColorStrings) {
				this.cache.put(path, ConversionUtils.fixColors(value.toString()));
			}

			return;
		}

		Serializer<?> serializer = Serializers.of(value);
		if (serializer == null) {
			throw new MissingSerializerException(value);
		}

		this.cache.put(path, value);
		serializer.serialize(path, value, this);
	}

	@Override
	public Object get(String path) {
		return this.cache.containsKey(path) ? this.cache.get(path) : super.get(path);
	}

	/**
	 * Workaround for writing comments to .yml file, Bukkit does to allow to do it
	 * @return Content of config parsed to YAML
	 */
	@Override
	public String saveToString() {
		this.overrideMaxLineWidth();

		String yaml = super.saveToString();

		List<String> lines = new ArrayList<>();
		if (this.configComment != null) {
			lines.add("# " + this.configComment);
		}

		for (String line : yaml.split("\n")) {
			// It's not line with new field
			if (!line.contains(":") || line.startsWith(" ") || line.startsWith("\t")) {
				lines.add(line);
				continue;
			}

			String configFieldName = line.split(":")[0];
			String comment = this.comments.get(configFieldName);
			if (comment == null) {
				lines.add(line);
				continue;
			}

			if (this.commentStyle == CommentStyle.INLINE) {
				lines.add(line + " # " + comment);
				continue;
			}

			// ABOVE_CONTENT
			lines.add("# " + comment);
			lines.add(line);
		}

		return String.join("\n", lines);
	}

	/**
	 * Loads data from config file
	 */
	public void load() {
		this.load(this.file);
	}

	@Override
	public void load(File file) {
		try {
			this.cache.clear();
			super.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			throw new InvalidConfigException("Could not load config file (name: " + this.file.getName() + ")", e);
		}
	}

	/**
	 * Saves data to config file
	 */
	public void save() {
		try {
			this.save(this.file);
		} catch (IOException e) {
			throw new InvalidConfigException("Could not save config file (name: " + this.file.getName() + ")", e);
		}
	}

	/**
	 * Copy default .yml file of config, if exists
	 */
	private void copyDefaultConfig() {
		try {
			if (!directory.exists()) {
				directory.mkdir();
			}

			if (this.file.exists()) {
				return;
			}

			InputStream input = ConfigAPI.getPlugin().getResource(file.getName());
			if (input == null) {
				this.file.createNewFile();
				return;
			}

			OutputStream output = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;

			while ((len = input.read(buf)) > 0) {
				output.write(buf, 0, len);
			}

			output.close();
			input.close();
		} catch (IOException e) {
			throw new InvalidConfigException("Could not save default file", e);
		}
	}

	/**
	 * Set SnakeYaml's max line width to max Integer value.
	 * Required for inline comments
	 */
	private void overrideMaxLineWidth() {
		try {
			Field yamlOptionsField = YamlConfiguration.class.getDeclaredField(dumperFieldName);
			yamlOptionsField.setAccessible(true);
			DumperOptions yamlOptions = (DumperOptions) yamlOptionsField.get(this);
			yamlOptions.setWidth(Integer.MAX_VALUE);
		} catch (Exception e) {
			throw new RuntimeException("Could not set max YAML line width", e);
		}
	}

	/**
	 * Add value to cache for optimization, to do not parse it every time user want to access it
	 * @param path path in config
	 * @param value value of field
	 */
	public void addToCache(String path, Object value) {
		this.cache.put(path, value);
	}

	public File getFile() {
		return file;
	}

	public NameStyle getNameStyle() {
		return nameStyle;
	}

	public CommentStyle getCommentStyle() {
		return commentStyle;
	}

	public Map<String, Object> getCache() {
		return cache;
	}
}
