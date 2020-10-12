package pl.mikigal.config.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Item {

	private final ItemMeta itemMeta;
	private final ItemStack itemStack;

	public Item(ItemStack itemStack) {
		this.itemStack = itemStack;
		this.itemMeta = itemStack.getItemMeta();
	}

	public static Item of(Item item) {
		return new Item(item.toItem());
	}

	public static Item of(ItemStack itemStack) {
		return new Item(itemStack);
	}

	public static Item of(Material material) {
		return new Item(new ItemStack(material));
	}

	public static Item of(Material material, int amount) {
		return new Item(new ItemStack(material, amount));
	}

	public static Item of(Material material, int amount, short data) {
		return new Item(new ItemStack(material, amount, data));
	}

	public Item amount(int amount) {
		this.itemStack.setAmount(amount);
		return this;
	}

	public Item durability(short durability) {
		this.itemStack.setDurability(durability);
		return this;
	}

	public Item name(String name) {
		this.itemMeta.setDisplayName(ConversionUtils.fixColors(name));
		return this;
	}

	public Item lore(String... lore) {
		this.itemMeta.setLore(ConversionUtils.fixColors(Arrays.asList(lore)));
		return this;
	}

	public Item lore(List<String> lore) {
		this.itemMeta.setLore(ConversionUtils.fixColors(lore));
		return this;
	}

	public Item enchantment(Enchantment enchantment, int level) {
		this.itemStack.addUnsafeEnchantment(enchantment, level);
		return this;
	}

	public Item enchantments(Map<Enchantment, Integer> enchantments) {
		this.itemStack.addUnsafeEnchantments(enchantments);
		return this;
	}

	public ItemStack toItem() {
		this.itemStack.setItemMeta(this.itemMeta);
		return this.itemStack;
	}
}