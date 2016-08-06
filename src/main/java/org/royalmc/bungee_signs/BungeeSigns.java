package org.royalmc.bungee_signs;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.royalmc.bungee_signs.commands.CommandBS;
import org.royalmc.bungee_signs.listeners.PlayerListener;
import org.royalmc.bungee_signs.sign.SignManager;
import org.royalmc.bungee_signs.sign.StatusSign;
import org.royalmc.bungee_signs.storage.YMLManager;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

public class BungeeSigns extends JavaPlugin implements PluginMessageListener {

	private YMLManager ymlManager;
	private SignManager signManager;
	
	public static HashMap<StatusSign, Boolean> signInProgress = new HashMap<>(); // need to find a better method

	@Override
	public void onEnable() {
		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

		ymlManager = new YMLManager(this);
		signManager = new SignManager(this);

		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		getCommand("bungeesigns").setExecutor(new CommandBS(this));
	}

	@Override
	public void onDisable() {
		signInProgress.clear();
	}

	public YMLManager getYmlManager() {
		return ymlManager;
	}

	public SignManager getSignManager() {
		return signManager;
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord"))
			return;

		ByteArrayDataInput in = ByteStreams.newDataInput(message);

		@SuppressWarnings("unused")
		String subChannel = in.readUTF();
		short len = in.readShort();
		byte[] msgbytes = new byte[len];
		in.readFully(msgbytes);

		DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
		try {
			String gameState = msgin.readUTF();
			String serverName = msgin.readUTF();
			msgin.readShort();

			if (gameState.equalsIgnoreCase("isjoinable")) {

				// update sign
				ConfigurationSection signsSection = this.ymlManager.getDatabaseFile().getConfigurationSection("Signs");
				String signName = "";

				for (String keys : signsSection.getKeys(false)) {
					if (keys.equalsIgnoreCase(serverName)) {
						signName = serverName;
						StatusSign s = signManager.getSign(signName);
						Sign b = s.getSign();

						b.setLine(1, SignManager.formatText("&8In Queue"));

						if (signInProgress.containsKey(s))
							signInProgress.replace(s, false);
						else
							signInProgress.put(s, false);

						signManager.update(s);
					}
				}
			} else if (gameState.equalsIgnoreCase("notjoinable")) {
				// update sign
				ConfigurationSection signsSection = this.ymlManager.getDatabaseFile().getConfigurationSection("Signs");
				String signName = "";

				for (String keys : signsSection.getKeys(false)) {
					if (keys.equalsIgnoreCase(serverName)) {

						signName = serverName;
						StatusSign s = signManager.getSign(signName);
						Sign b = s.getSign();

						b.setLine(1, SignManager.formatText("&8In Progress"));

						if (signInProgress.containsKey(s))
							signInProgress.replace(s, true);
						else
							signInProgress.put(s, true);

						signManager.update(s);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
