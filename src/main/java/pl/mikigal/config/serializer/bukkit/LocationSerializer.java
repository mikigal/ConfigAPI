package pl.mikigal.config.serializer.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.exception.InvalidConfigFileException;
import pl.mikigal.config.serializer.Serializer;
import pl.mikigal.config.util.ConversionUtils;

/**
 * Built-in serializer for Location
 * @see Location
 * @see Serializer
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class LocationSerializer extends Serializer<Location> {

	@Override
	protected void saveObject(String path, Location object, BukkitConfiguration configuration) {
		configuration.set(path + ".world", object.getWorld().getName());
		configuration.set(path + ".x", ConversionUtils.round(object.getX()));
		configuration.set(path + ".y", ConversionUtils.round(object.getY()));
		configuration.set(path + ".z", ConversionUtils.round(object.getZ()));
		configuration.set(path + ".yaw", ConversionUtils.round(object.getYaw()));
		configuration.set(path + ".pitch", ConversionUtils.round(object.getPitch()));
	}

	@Override
	public Location deserialize(String path, BukkitConfiguration configuration) {
		ConfigurationSection section = configuration.getConfigurationSection(path);
		World world = Bukkit.getWorld(section.getString("world"));
		if (world == null) {
			throw new InvalidConfigFileException("Invalid Location (path: " + section.getName() + "), world " + section.getString("world") + " does not exist");
		}

		return new Location(
				world,
				section.getDouble("x"),
				section.getDouble("y"),
				section.getDouble("z"),
				section.contains("yaw") ? (float) section.getDouble("yaw") : 0,
				section.contains("pitch") ? (float) section.getDouble("pitch") : 0);
	}
}
