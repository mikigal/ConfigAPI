package pl.mikigal.config.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for conversion between types
 * @since 1.0
 * @author Mikołaj Gałązka
 */
public class ConversionUtils {

	/**
	 * Rounds double to 2 decimal places
	 * @param value non-rounded value
	 * @return {@param value} rounded to 2 decimal places
	 */
	public static double round(double value) {
		long factor = (long) Math.pow(10, 2);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	/**
	 * Converts String to int
	 * @param input number as String
	 * @return number as int, if input is invalid returns Integer.MIN_VALUE
	 */
	public static int asInt(String input) {
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return Integer.MIN_VALUE;
		}
	}

	/**
	 * Translates text colored by '&' to ChatColor based
	 * @param raw text colored by '&'
	 * @return text translated to ChatColor
	 * @see ChatColor
	 */
	public static String fixColors(String raw) {
		return ChatColor.translateAlternateColorCodes('&', raw);
	}

	/**
	 * Translates list of text colored by '&' to ChatColor based
	 * @param raw list of text with colored by '&'
	 * @return list of text translated to ChatColor
	 * @see ChatColor
	 */
	public static List<String> fixColors(List<String> raw) {
		List<String> colored = new ArrayList<>();
		for (String line : raw) {
			colored.add(fixColors(line));
		}

		return colored;
	}
}
