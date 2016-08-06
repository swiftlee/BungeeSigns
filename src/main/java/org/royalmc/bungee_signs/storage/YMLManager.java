package org.royalmc.bungee_signs.storage;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;
import org.royalmc.bungee_signs.BungeeSigns;
/**
 * 
 * @author GregZ_
 *
 */
public class YMLManager {

	private final BungeeSigns plugin;
	
	private YamlConfiguration databaseFile;

	public YMLManager(BungeeSigns plugin) {
		this.plugin = plugin;;
		loadAllConfigurationFiles();
	}

	public void loadAllConfigurationFiles() {
		long time = System.currentTimeMillis();
		initConfig();
		initDatabase();
		time = (System.currentTimeMillis() - time) / 1000;
		plugin.getLogger().log(Level.INFO, "Successfully loaded all configuration files (" + time + (time == 1 ? " second" : " seconds") + ")!");
	}

	// File loading
	public void initConfig() {
		plugin.saveDefaultConfig();
	}

	public void initDatabase() {
		if (!new File(plugin.getDataFolder(), "database.yml").exists()) {
			plugin.saveResource("database.yml", false);
		}
		databaseFile = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "database.yml"));
	}

	public void reloadDatabase() {
		databaseFile = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "database.yml"));
		plugin.getSignManager().loadAll();
	}
	
	public void reloadConfig() {
		plugin.reloadConfig();
		plugin.getSignManager().loadSignText();
	}
	
	// File save
	public boolean saveDatabaseFile() {
		try {
			databaseFile.save(new File(plugin.getDataFolder(), "database.yml"));
			plugin.getLogger().log(Level.INFO, "Successfully saved the database.yml!");
			return true;
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "An error occurred when saving the database.yml ERROR: " + e.getMessage(), e);
		}
		return false;
	}
	
	// File getters
	public YamlConfiguration getDatabaseFile() {
		return databaseFile;
	}
}
