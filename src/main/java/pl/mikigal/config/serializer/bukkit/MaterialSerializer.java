package pl.mikigal.config.serializer.bukkit;

import org.bukkit.Location;
import org.bukkit.Material;
import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.serializer.Serializer;

/**
 * Built-in serializer for Material
 * @see Material
 * @see Serializer
 * @since 1.1.9
 * @author Mikołaj Gałązka
 */
public class MaterialSerializer extends Serializer<Material> {

	@Override
	protected void saveObject(String path, Material object, BukkitConfiguration configuration) {
		configuration.set(path, object.toString());
	}

	@Override
	public Material deserialize(String path, BukkitConfiguration configuration) {
		return Material.valueOf(configuration.getString(path));
	}
}
