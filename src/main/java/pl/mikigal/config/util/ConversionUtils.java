package pl.mikigal.config.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class ConversionUtils {

	public static double round(double value) {
		long factor = (long) Math.pow(10, 2);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public static String fixColors(String raw) {
		return ChatColor.translateAlternateColorCodes('&', raw);
	}

	public static List<String> fixColors(List<String> raw) {
		List<String> colored = new ArrayList<>();
		for (String line : raw) {
			colored.add(fixColors(line));
		}

		return colored;
	}
}
