package org.royalmc.bungee_signs.utils;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.royalmc.bungee_signs.BungeeSigns;

/**
 * This is a class for parsing and serialising location objects for persistant storage.
 * 
 * @author GregZ_
 * @since 1.1
 */
public class LocationUtils {

	/** The reference to the main class that is used for dependency injection */
	private final BungeeSigns plugin;

	/**
	 * This is the constructor for the {@link LocationUtils} class.
	 * 
	 * @param plugin
	 *            The plugin parameter is used for dependency injection so that the plugin's logger can be 
	 *            used from this class without the need for a static reference to the {@link BungeeSigns} class.
	 * @since 1.1
	 */
	public LocationUtils(BungeeSigns plugin) {
		this.plugin = plugin;
	}

	/**
	 * This is a simple method for parsing strings to locations.
	 * 
	 * @param s
	 *            The string value of a serialised location which is to be converted back into a location object.
	 * @return The parsed location if parsing was successful or null if it was not.
	 * @since 1.1
	 */
	public Location parseLocation(String s) {
		String[] split = s.split(",");
		Location loc = null;

		try {
			World world = Bukkit.getWorld(split[0]);
			if (split.length == 4) {
				int x = Integer.parseInt(split[1]);
				int y = Integer.parseInt(split[2]);
				int z = Integer.parseInt(split[3]);
				loc = new Location(world, x, y, z);
			}
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			plugin.getLogger().log(Level.SEVERE, "Cannot parse location from string: " + s + " - Mabye this is the reason: " + e.toString(), e);
		}
		return loc;
	}

	/**
	 * This is a simple method for serialising locations to strings.
	 * 
	 * @param l
	 *            The Location object that is to be serialised.
	 * @return A serialised string for the location provided or null if no location was provided.
	 * @since 1.1
	 */
	public String serializeLocation(Location l) {
		if (l != null) {
			return new StringBuilder()
					.append(l.getWorld().getName()).append(",")
					.append(l.getBlockX()).append(",")
					.append(l.getBlockY()).append(",")
					.append(l.getBlockZ())
					.toString();
		}
		return null;
	}
}
