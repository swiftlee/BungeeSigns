package org.royalmc.BungeeSigns;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BGS_Main extends JavaPlugin implements PluginMessageListener, Listener
{
	
	public JavaPlugin plugin = this;
	public static BGS_Main staticPlugin = null;
	
	public boolean serverState = false;
	
	public static YamlConfiguration colorConfig = new YamlConfiguration();
	
	public static ArrayList<BGS_StatusSign> signs;


		public void onEnable()
		{
			
			loadColorConfig();
			
			
			String line1 = "presets.color.line1";
			String line2 = "presets.color.line2";
			String line2offline = "presets.color.line2offline";
			String line3 = "presets.color.line3";
			String line4 = "presets.color.line4";

			colorConfig.set(line1, "&a");
			colorConfig.set(line2, "&a");
			colorConfig.set(line2offline, "&c");
			colorConfig.set(line3, "&1");
			colorConfig.set(line4, "&6");
			
			String textFormat1 = "presets.text.line1";
			String textFormat2 = "presets.text.line2";
			String textFormat2offline = "presets.text.line2offline";
			String textFormat4 = "presets.text.line4";
			
			colorConfig.set(textFormat1, "[Join]");
			colorConfig.set(textFormat2, "ONLINE");
			colorConfig.set(textFormat2offline, "OFFLINE");
			colorConfig.set(textFormat4, "/");

			colorConfig.options().header("To change line colors use this format: &c");
			colorConfig.options().copyDefaults(true);
			saveColorConfig();
			
			
			saveDefaultConfig();
			
			
			
			
			//PLUGIN ACCESS THROUGH ALL CLASSES IN PACKAGE FOR PLUGIN
			staticPlugin = (BGS_Main)Bukkit.getPluginManager().getPlugin("RoyalMC_BungeeSigns");
			
			serverState = true;
			BGS_Main.signs = new ArrayList<BGS_StatusSign>();
			
			
			for(String str : getConfig().getKeys(false))
			{
				ConfigurationSection s = getConfig().getConfigurationSection(str);
				ConfigurationSection l = s.getConfigurationSection("loc");
				World w = Bukkit.getServer().getWorld(l.getString("world"));
				double x = l.getDouble("x"), y = l.getDouble("y"), z = l.getDouble("z");
				
				
				
				Location loc = new Location(w, x, y, z);
				if(loc.getBlock() == null)
				{
					getConfig().set(str, null);
				}
				else
				{
					signs.add(new BGS_StatusSign(loc,
							s.getString("name"), 
							s.getInt("port"),
							s.getString("serverName")));
				}
			}
			
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
			{
				public void run()
				{
					for(BGS_StatusSign s : signs)
					{
						s.update();
					}
				}
			}, 0, 20);
			
			//LISTENER REGISTRY
			getServer().getPluginManager().registerEvents(this, plugin);
			getServer().getPluginManager().registerEvents(new BGS_PlayerInteractEvent(), plugin);
			getServer().getPluginManager().registerEvents(new BGS_BlockBreakEvent(), plugin);
			
			Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
			Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
			
		}

		
		public void saveColorConfig() 
		{

			try {

				colorConfig.save("plugins/" + plugin.getName() + "/colorConfig.yml");

			}

			catch (Exception e1) {

				e1.printStackTrace();

			}

		}

		public void loadColorConfig() 
		{

			if (colorConfig == null) 
			{

				colorConfig = new YamlConfiguration();
				
				

			}

			try 
			{

				colorConfig.load("plugins/" + plugin.getName() + "/colorConfig.yml");

			}

			catch (FileNotFoundException e1)
			{

				saveColorConfig();

			}

			catch (Exception e) 
			{

				e.printStackTrace();

			}

		}
		
		public void reloadColorConfig()
		{
			loadColorConfig();
			saveColorConfig();
		}
	
		public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
		{
			if(sender.isOp())
			{
				if(cmd.getName().equalsIgnoreCase("reloadSignsConfig"))
				{
					plugin.reloadConfig();
					sender.sendMessage(formatString("&aSuccessfully reloaded MAIN configuration for BungeeSigns."));
				}
				if(cmd.getName().equalsIgnoreCase("reloadColorConfig"))
				{
					reloadColorConfig();
					sender.sendMessage(formatString("&aSuccessfully reloaded COLOR/TEXT configuration for BungeeSigns."));
				}

				if(cmd.getName().equalsIgnoreCase("createSign"))
				{
					if(!(sender instanceof Player))
					{
						sender.sendMessage(ChatColor.RED + "Only players may use this command!");
						return true;
					}
					else
					{
						Player p = (Player)sender;

						if(args.length != 3)
						{
							p.sendMessage(ChatColor.RED + "/createSign <level_name> <port> <server_name>");
							return true;
						}
						else
						{

							String serverName = args[2];

							int port;
							String name = args[0];

							try
							{
								port = Integer.valueOf(args[1]);
							}
							catch(Exception e)
							{
								p.sendMessage(ChatColor.RED + "Port entered was not a number.");
								return true;
							}

							Block block = p.getTargetBlock((Set<Material>)null, 10);
							if(block == null)
							{
								p.sendMessage(ChatColor.RED + "You are not looking at a sign!");
								return true;
							}

							if(block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN)
							{
								p.sendMessage(ChatColor.RED + "You are not looking at a sign!");
								return true;
							}

							//Sign s = (Sign)block.getState();

							BGS_StatusSign bungeeSign = new BGS_StatusSign(block.getLocation(), name, port, serverName);
							signs.add(bungeeSign);
							save(bungeeSign);
						}

					}
				}
			}
			else
			{
				sender.sendMessage(formatString("&4You do not have permission to use that command."));
			}
			return true;
			
		}
	
		@SuppressWarnings("static-access")
		private void save(BGS_StatusSign sign)
		{
			int size = getConfig().getKeys(false).size() + 1;
			
			getConfig().set(size + ".loc.world", sign.getLocation().getWorld().getName());
			getConfig().set(size + ".loc.x", sign.getLocation().getX());
			getConfig().set(size + ".loc.y", sign.getLocation().getY());
			getConfig().set(size + ".loc.z", sign.getLocation().getZ());
			getConfig().set(size + ".name", sign.getName());
			getConfig().set(size + ".port", sign.getPort());
			getConfig().set(size + ".serverName", sign.getServerName());
			
			
			saveConfig();
		}
		
		public static String formatString(String textToFormat) 
		{

			return textToFormat = ChatColor.translateAlternateColorCodes('&', textToFormat);

		}


		@Override
		public void onPluginMessageReceived(String channel, Player player, byte[] message) 
		{
		
			if(!channel.equals("BungeeCord"))
			{
				return;
			}
			
			try
			{
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
				
				String cmd = in.readUTF();
				
				if(cmd.equals("PlayerCount"))
				{
					String server = in.readUTF();
					int playerCount = in.readInt();
					
					System.out.println("Server " + server + " has " + playerCount + " player(s).");
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		
		}
}
