package pl.mikigal.config.serializer.bukkit;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.exception.InvalidConfigFileException;
import pl.mikigal.config.serializer.Serializer;

/**
 * Built-in serializer for PotionEffect
 * @see PotionEffect
 * @see Serializer
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class PotionEffectSerializer extends Serializer<PotionEffect> {

	@Override
	protected void saveObject(String path, PotionEffect object, BukkitConfiguration configuration) {
		configuration.set(path + ".type", object.getType().getName());
		configuration.set(path + ".duration", object.getDuration());
		configuration.set(path + ".amplifier", object.getAmplifier());
	}

	@Override
	public PotionEffect deserialize(String path, BukkitConfiguration configuration) {
		PotionEffectType type = PotionEffectType.getByName(configuration.getString(path + ".type"));
		int duration = configuration.getInt(path + ".duration");
		int amplifier = configuration.getInt(path + ".amplifier");

		if (type == null) {
			throw new InvalidConfigFileException("Invalid PotionEffect type (path: " + path + ")");
		}

		return new PotionEffect(type, duration, amplifier);
	}
}
