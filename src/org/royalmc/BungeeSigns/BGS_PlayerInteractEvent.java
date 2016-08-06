package org.royalmc.BungeeSigns;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BGS_PlayerInteractEvent implements Listener
{

	 @SuppressWarnings("static-access")
	@EventHandler
     public void onPlayerInteract(PlayerInteractEvent e) 
	 {
             if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            	 return;
            
             Block block = e.getClickedBlock();
            
             if (block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) 
            	 return;
            
             for (BGS_StatusSign s : BGS_Main.signs) 
             {
                     if (s.getLocation().equals(block.getLocation())) 
                     {
                             try 
                             {
                                     ByteArrayOutputStream b = new ByteArrayOutputStream();
                                     DataOutputStream out = new DataOutputStream(b);

                                     out.writeUTF("Connect");
                                     out.writeUTF(s.getServerName());
                                    
                                     e.getPlayer().sendPluginMessage(BGS_Main.staticPlugin, "BungeeCord", b.toByteArray());
                             } 
                             catch (Exception ex)
                             {
                                     ex.printStackTrace();
                             }
                     }
             }
     }
}
