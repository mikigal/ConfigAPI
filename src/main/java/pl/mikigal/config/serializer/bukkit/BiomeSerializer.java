package pl.mikigal.config.serializer.bukkit;

import org.bukkit.block.Biome;
import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.serializer.Serializer;

/**
 * Built-in serializer for Biome
 * @see Biome
 * @see Serializer
 * @since 1.1.9
 * @author Mikołaj Gałązka
 */
public class BiomeSerializer extends Serializer<Biome> {

	@Override
	protected void saveObject(String path, Biome object, BukkitConfiguration configuration) {
		configuration.set(path, object.toString());
	}

	@Override
	public Biome deserialize(String path, BukkitConfiguration configuration) {
		return Biome.valueOf(configuration.getString(path));
	}
}
