package pl.mikigal.config.serializer.bukkit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.exception.InvalidConfigFileException;
import pl.mikigal.config.serializer.Serializer;
import pl.mikigal.config.serializer.Serializers;
import pl.mikigal.config.util.ConversionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Built-in serializer for ItemStack
 * @see ItemStack
 * @see Serializer
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class ItemStackSerializer extends Serializer<ItemStack> {

	@Override
	protected void saveObject(String path, ItemStack object, BukkitConfiguration configuration) {
		configuration.set(path + ".material", object.getType().toString());
		configuration.set(path + ".amount", object.getAmount());
		if (object.getDurability() != 0) {
			configuration.set(path + ".durability", object.getDurability());
		}

		for (Map.Entry<Enchantment, Integer> entry : object.getEnchantments().entrySet()) {
			configuration.set(path + ".enchantments." + entry.getKey().getName(), entry.getValue());
		}

		ItemMeta itemMeta = object.getItemMeta();
		if (itemMeta == null) {
			return;
		}

		if (itemMeta.getDisplayName() != null) {
			configuration.set(path + ".name", itemMeta.getDisplayName().replace("§", "&"));
		}

		if (itemMeta.getLore() != null && itemMeta.getLore().size() != 0) {
			List<String> raw = new ArrayList<>();
			for (String line : itemMeta.getLore()) {
				raw.add(line.replace("§", "&"));
			}

			configuration.set(path + ".lore", raw);
		}

		object.setItemMeta(itemMeta);
	}

	@Override
	public ItemStack deserialize(String path, BukkitConfiguration configuration) {
		ConfigurationSection section = configuration.getConfigurationSection(path);

		String rawMaterial = section.getString("material");
		if (rawMaterial == null) {
			throw new InvalidConfigFileException("Invalid material (" + rawMaterial + ") in ItemStack (path: " + section.getName() + ")");
		}

		Material material = Material.getMaterial(rawMaterial);
		if (material == null) {
			throw new InvalidConfigFileException("Invalid material (" + rawMaterial + ") in ItemStack (path: " + section.getName() + ")");
		}

		int amount = section.contains("amount") ? section.getInt("amount") : 1;
		short durability = section.contains("durability") ? (short) section.getInt("durability") : 0;
		String name = section.getString("name");

		boolean hasLore = section.getConfigurationSection("lore") != null &&
				section.getConfigurationSection("lore").getString("structure") != null;
		List<String> lore = hasLore ?
				Serializers.of(List.class).deserialize(path + ".lore", configuration) :
				new ArrayList<>();

		ItemStack itemStack = new ItemStack(material, amount, durability);
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (name != null) {
			itemMeta.setDisplayName(ConversionUtils.fixColors(name));
		}

		if (lore != null && lore.size() != 0) {
			itemMeta.setLore(ConversionUtils.fixColors(lore));
		}

		itemStack.setItemMeta(itemMeta);

		if (section.contains("enchantments")) {
			ConfigurationSection enchantments = section.getConfigurationSection("enchantments");
			for (String key : enchantments.getKeys(false)) {
				itemStack.addUnsafeEnchantment(Enchantment.getByName(key), enchantments.getInt(key));
			}
		}

		return itemStack;
	}
}
