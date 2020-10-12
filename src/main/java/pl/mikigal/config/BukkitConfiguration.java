package pl.mikigal.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import pl.mikigal.config.annotation.Comment;
import pl.mikigal.config.exception.InvalidConfigException;
import pl.mikigal.config.exception.MissingSerializerException;
import pl.mikigal.config.serializer.Serializer;
import pl.mikigal.config.serializer.Serializers;
import pl.mikigal.config.style.CommentStyle;
import pl.mikigal.config.style.NameStyle;
import pl.mikigal.config.util.ConversionUtils;
import pl.mikigal.config.util.TypeUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

public class BukkitConfiguration extends YamlConfiguration {

	private static final Field yamlOptionsField;
	private static final Field yamlRepresenterField;
	private static final Field yamlField;

	static {
		try {
			yamlOptionsField = YamlConfiguration.class.getDeclaredField("yamlOptions");
			yamlOptionsField.setAccessible(true);

			yamlRepresenterField = YamlConfiguration.class.getDeclaredField("yamlRepresenter");
			yamlRepresenterField.setAccessible(true);

			yamlField = YamlConfiguration.class.getDeclaredField("yaml");
			yamlField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new InvalidConfigException("Could not find Fields of YamlConfiguration", e);
		}
	}

	private final File file;
	private final NameStyle nameStyle;
	private final CommentStyle commentStyle;
	private final boolean automaticColorStrings;
	private final Map<String, Object> cache;
	private final Map<String, String> comments;

	private final DumperOptions yamlOptions;
	private final YamlRepresenter yamlRepresenter;
	private final Yaml yaml;

	public BukkitConfiguration(File file, NameStyle nameStyle, CommentStyle commentStyle, boolean automaticColorStrings) {
		this.file = file;
		this.nameStyle = nameStyle;
		this.commentStyle = commentStyle;
		this.automaticColorStrings = automaticColorStrings;
		this.cache = new HashMap<>();
		this.comments = new HashMap<>();

		try {
			this.yamlOptions = (DumperOptions) yamlOptionsField.get(this);
			this.yamlRepresenter = (YamlRepresenter) yamlRepresenterField.get(this);
			this.yaml = (Yaml) yamlField.get(this);
		} catch (IllegalAccessException e) {
			throw new InvalidConfigException("Could not get values of Fields in YamlConfiguration", e);
		}

		this.copyDefaultConfig();
		this.load();
	}

	public void set(String path, Object value, Comment comment) {
		if (comment != null) {
			this.comments.put(path, comment.value());
		}

		this.set(path, value);
	}

	@Override
	public void set(String path, Object value) {
		if (value != null && value.getClass().isArray()) {
			value = Arrays.asList(((Object[]) value));
		}

		// Workaround for setting and loading empty List and Map.
		if ((value instanceof List && ((List<?>) value).size() == 0) || (value instanceof Map && ((Map<?, ?>) value).size() == 0)) {
			super.set(path, new ArrayList<>());
			return;
		}

		if (value == null || TypeUtils.isSimpleType(value)) {
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

	@Override
	public String saveToString() {
		this.yamlOptions.setIndent(this.options().indent());
		this.yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		this.yamlOptions.setAllowUnicode(true);
		this.yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

		String header = this.buildHeader();
		String dump = yaml.dump(this.getValues(false));
		if (dump.equals("{}\n")) {
			dump = "";
		}

		List<String> lines = new ArrayList<>();
		for (String line : (header + dump).split("\n")) {
			if (!line.contains(":") || line.startsWith(" ")) {
				lines.add(line);
				continue;
			}

			String key = line.split(":")[0];
			String comment = this.comments.get(key);
			if (comment != null) {
				if (this.commentStyle == CommentStyle.ABOVE_CONTENT) {
					lines.add("# " + comment);
				} else {
					lines.add(line + " # " + comment);
				}

				continue;
			}

			lines.add(line);
		}

		return String.join("\n", lines);
	}

	public void load() {
		try {
			this.cache.clear();
			super.load(this.file);
		} catch (IOException | InvalidConfigurationException e) {
			throw new InvalidConfigException("Could not load config file (name: " + this.file.getName() + ")", e);
		}
	}

	public void save() {
		try {
			this.save(this.file);
		} catch (IOException e) {
			throw new InvalidConfigException("Could not save config file (name: " + this.file.getName() + ")", e);
		}
	}

	private void copyDefaultConfig() {
		try {
			if (!ConfigAPI.getPlugin().getDataFolder().exists()) {
				ConfigAPI.getPlugin().getDataFolder().mkdir();
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
