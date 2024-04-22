package pl.mikigal.config.serializer.bukkit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import pl.mikigal.config.BukkitConfiguration;
import pl.mikigal.config.ConfigAPI;
import pl.mikigal.config.exception.InvalidConfigException;
import pl.mikigal.config.serializer.Serializer;
import pl.mikigal.config.serializer.Serializers;
import pl.mikigal.config.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Built-in serializer for ShapedRecipe
 * @see ShapedRecipe
 * @see Serializer
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class ShapedRecipeSerializer extends Serializer<ShapedRecipe> {

	// Workaround to don't require both version to build
	private static Constructor<ShapedRecipe> newVersionsConstructor;
	private static Constructor<?> namespacedKeyConstructor;

	static {
		if (ReflectionUtils.isNewVersion()) {
			try {
				Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
				newVersionsConstructor = ShapedRecipe.class.getConstructor(namespacedKeyClass, ItemStack.class);
				namespacedKeyConstructor = namespacedKeyClass.getConstructor(Plugin.class, String.class);
			} catch (NoSuchMethodException | ClassNotFoundException e) {
				throw new InvalidConfigException("Could not find constructor for ShapedRecipe for new versions", e);
			}
		}
	}

	@Override
	protected void saveObject(String path, ShapedRecipe object, BukkitConfiguration configuration) {
		Serializers.of(ItemStack.class).serialize(path + ".result", object.getResult(), configuration);
		List<Character> shape = new ArrayList<>();
		for (String line : object.getShape()) {
			for (char ingredient : line.toCharArray()) {
				shape.add(ingredient);
			}
		}

		for (int i = 0; i < 9; i++) {
			char ingredient = shape.get(i);
			if (ingredient == ' ') {
				continue;
			}

			if (object.getIngredientMap().get(ingredient) == null) {
				throw new InvalidConfigException("Invalid ShapedRecipe, there's no defined ingredient for char '" + ingredient + "'");
			}

			configuration.set(path + "." + i, object.getIngredientMap().get(ingredient).getType().toString());
		}
	}

	@Override
	public ShapedRecipe deserialize(String path, BukkitConfiguration configuration) {
		ConfigurationSection section = configuration.getConfigurationSection(path);
		ItemStack result = Serializers.of(ItemStack.class).deserialize(path + ".result", configuration);
		ShapedRecipe recipe = ReflectionUtils.isNewVersion() ? this.createForNewVersion(result) : new ShapedRecipe(result);

		Map<Integer, Material> ingredients = new HashMap<>();
		for (int i = 0; i < 9; i++) {
			if (!section.contains(String.valueOf(i))) {
				continue;
			}

			ingredients.put(i, Material.valueOf(section.getString(String.valueOf(i))));
		}

		String shape = "";
		for (int i = 0; i < 9; i++) {
			shape += ingredients.containsKey(i) ? i : " ";
		}

		recipe = recipe.shape(shape.substring(0, 3), shape.substring(3, 6), shape.substring(6));

		for (Map.Entry<Integer, Material> ingredient : ingredients.entrySet()) {
			recipe = recipe.setIngredient(Character.forDigit(ingredient.getKey(), 10), ingredient.getValue());
		}

		return recipe;
	}

	private ShapedRecipe createForNewVersion(ItemStack result) {
		try {
			return newVersionsConstructor.newInstance(namespacedKeyConstructor.newInstance(ConfigAPI.getPlugin(), UUID.randomUUID().toString().substring(6)), result);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new InvalidConfigException("Could not create ShapedRecipe for new version");
		}
	}
}
