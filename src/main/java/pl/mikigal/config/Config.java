package pl.mikigal.config;

/**
 * Every config should extend this interface, it allows to access to BukkitConfiguration
 * @see BukkitConfiguration
 * @since 1.0
 * @author Mikołąj Gałązka
 */
public interface Config {

	/**
	 * Returns instance of BukkitConfiguration which allow to access raw Bukkit's config methods
	 * @see BukkitConfiguration
	 * @return instance of BukkitConfiguration for current config
	 */
	BukkitConfiguration getBukkitConfiguration();
}
