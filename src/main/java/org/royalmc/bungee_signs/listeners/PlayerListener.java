package org.royalmc.bungee_signs.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.royalmc.bungee_signs.BungeeSigns;
import org.royalmc.bungee_signs.sign.SignManager;
import org.royalmc.bungee_signs.sign.StatusSign;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class PlayerListener implements Listener {

	private BungeeSigns plugin;

	public PlayerListener(BungeeSigns plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Block block = e.getClickedBlock();

		if (block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
			return;
		}
		
		StatusSign sign = plugin.getSignManager().getSign(block.getLocation());
		
		if (sign == null) {
			return;
		}
		
		if(BungeeSigns.signInProgress.containsKey(sign))
		{
			if(BungeeSigns.signInProgress.get(sign) == true)
			{
				e.getPlayer().sendMessage(SignManager.formatText("&cThis game is currently in progress!"));
			}
			else
			{
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Connect");
				out.writeUTF(sign.getServerName());
				e.getPlayer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();

		if (block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
			return;
		}

		StatusSign sign = plugin.getSignManager().getSign(block.getLocation());
		
		if (sign == null) {
			return;
		}
		
		if (!event.getPlayer().hasPermission("bungeesigns.remove")) {
			event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to do this");
			event.setCancelled(true);
			return;
		}
		
		if (!event.getPlayer().isSneaking()) {
			event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "You must be sneaking to remove a server status sign!");
			event.setCancelled(true);
			return;
		}
		
		if (plugin.getSignManager().remove(sign)) {
			event.getPlayer().sendMessage(ChatColor.GREEN + "Successfully removed the status sign " + sign.getName() + "!");
			return;
		}
		
		event.getPlayer().sendMessage(ChatColor.RED + "An error occurred when removed the status sign " + sign.getName() + "!");
		event.setCancelled(true);
	}
}
