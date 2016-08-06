package org.royalmc.BungeeSigns;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BGS_BlockBreakEvent implements Listener 
{

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) 
	{          
		Block block = e.getBlock();

		if (block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) 
			return;

		for (BGS_StatusSign s : BGS_Main.signs) 
		{
			if (s.getLocation().getBlockX() == block.getX() && s.getLocation().getBlockY() == block.getY() && s.getLocation().getBlockZ() == block.getZ()) 
			{

				System.out.println("DETECTED SIGN!");

				try 
				{

					for(String str : BGS_Main.staticPlugin.getConfig().getKeys(false))
					{
						
						BGS_Main.staticPlugin.getConfig().set(str, null);
						BGS_Main.staticPlugin.reloadConfig();
						
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}
}
