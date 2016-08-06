package org.royalmc.bungee_signs.commands;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.royalmc.bungee_signs.BungeeSigns;

public class CommandBS implements CommandExecutor{

	private BungeeSigns plugin;

	public CommandBS(BungeeSigns plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			showHelp(sender);
			return true;
		}
		
		if(args[0].equalsIgnoreCase("create")) {
			if (!sender.hasPermission("bungeesigns.create")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
				return true;
			}
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Only players may use this command!");
				return true;
			}
			Player p = (Player) sender;

			if (args.length < 5) {
				p.sendMessage(ChatColor.RED + "/bungeesigns create <SIGN NAME> <HOST> <PORT> <SERVER NAME>");
				return true;
			}
			
			int port;
			try {
				port = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				p.sendMessage(ChatColor.RED + "Port entered was not a number.");
				return true;
			}
			
			Block block = p.getTargetBlock((Set<Material>) null, 10);
			if (block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
				p.sendMessage(ChatColor.RED + "You are not looking at a sign!");
				return true;
			}
			
			if (plugin.getSignManager().make(args[1], block.getLocation(), args[2], port, args[4])) {
				p.sendMessage(ChatColor.GREEN + "Successfully created status sign " + args[1] + " for the server " + args[4] + "!");
				return true;
			}
			
			p.sendMessage(ChatColor.RED + "Successfully created status sign " + args[1] + " for the server " + args[4] + "!");
			return true;
		} else if(args[0].equalsIgnoreCase("remove")) {
			if (!sender.hasPermission("bungeesigns.remove")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
				return true;
			}
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "/bungeesigns remove <SIGN NAME>");
				return true;
			}
			if (!plugin.getYmlManager().getDatabaseFile().contains("Signs." + args[1])) {
				sender.sendMessage(ChatColor.RED + "Unable to find the sign " + args[1] + " in the database file!");
				return true;
			}
			if (plugin.getSignManager().remove(args[1])) {
				sender.sendMessage(ChatColor.GREEN + "Successfully removed the Status Sign " + args[1] + "!");
				return true;
			}
			sender.sendMessage(ChatColor.RED + "An error occurred when removing the status sign " + args[1] + " please see the console for details!");
			return true;
		} else if(args[0].equalsIgnoreCase("reload")) {
			if (args.length == 1) {
				if (!sender.hasPermission("bungeesigns.reload.*")) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
					return true;
				}
				plugin.getYmlManager().reloadConfig();
				plugin.getYmlManager().reloadDatabase();
				sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the config.yml and database.yml!");
				return true;
			} else if (args[1].equalsIgnoreCase("config")) {
				if (!sender.hasPermission("bungeesigns.reload.config")) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
					return true;
				}
				plugin.getYmlManager().reloadConfig();
				sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the config.yml!");
				return true;
			} else if (args[1].equalsIgnoreCase("database")) {
				if (!sender.hasPermission("bungeesigns.reload.database")) {
					sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
					return true;
				}
				plugin.getYmlManager().reloadDatabase();
				sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the database.yml!");
				return true;
			}
			if (!sender.hasPermission("bungeesigns.reload.*") && !sender.hasPermission("bungeesigns.reload.config") && !sender.hasPermission("bungeesigns.reload.database")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
				return true;
			}
			sender.sendMessage(ChatColor.RED + "The name " + args[1] + " is not a valis config file!\n"
					+ "Valid config files are either 'config' or 'database'");
		} else {
			showHelp(sender);
			return true;
		}
		return true;
	}
	
	private void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&dBungeeSigns&8] &e Version: " + plugin.getDescription().getVersion() + " &7&m--&r &ePlugin developed by " +  String.join(" and ", plugin.getDescription().getAuthors())));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/&6bungeesigns create <SIGN NAME> <HOST> <PORT> <SERVER NAME> &7- &eMake the sign you are looking at into a status sign!"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8/&6bungeesigns remove <SIGN NAME> &7- &eRemove the status sign with the given name if it exists!"));
	}
}
