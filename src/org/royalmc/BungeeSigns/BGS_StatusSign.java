package org.royalmc.BungeeSigns;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class BGS_StatusSign {

	private static Location location;
	private static Sign sign;
	private static String name;
	private static String serverName;
	private static int port;
	
	public BGS_StatusSign(Location location, String name, int port, String serverName)
	{
		BGS_StatusSign.location = location;
		
		if(location.getBlock().getState() instanceof Sign)
		BGS_StatusSign.sign = (Sign) location.getBlock().getState();
		
		BGS_StatusSign.name = name;
		BGS_StatusSign.serverName = serverName;
		BGS_StatusSign.port = port;
	}
	
	public static String getServerName()
	{
		return serverName;
	}
	
	public Location getLocation()
	{
		return location;
	}
	
	public static String getName()
	{
		return name;
	}
	
	public static int getPort()
	{
		return port;
	}
	
	public void update()
	{
		try 
		{
			ServerListPing ping = new ServerListPing(new InetSocketAddress("localhost", port));

			   ServerStatus response = ping.fetchData();

			   int onlinePlayers = response.getPlayers().getOnline();
			   int maxPlayers = response.getPlayers().getMax();
			   //String MOTD = response.getDescription();
			
			sign.setLine(0, BGS_Main.formatString(BGS_Main.colorConfig.getString("presets.color.line1") + BGS_Main.colorConfig.getString("presets.text.line1")));
			sign.setLine(1, BGS_Main.formatString(BGS_Main.colorConfig.getString("presets.color.line2") + BGS_Main.colorConfig.getString("presets.text.line2")));
			sign.setLine(2, BGS_Main.formatString(BGS_Main.colorConfig.getString("presets.color.line3") + name));
			sign.setLine(3, BGS_Main.formatString(BGS_Main.colorConfig.getString("presets.color.line4") + onlinePlayers + BGS_Main.colorConfig.getString("presets.text.line4") + maxPlayers));
			
		}
		catch (UnknownHostException ex1) 
		{
		       // OFFLINE
			//System.out.print("UNKNOWNHOSTEXCEPTION");
			sign.setLine(0, BGS_Main.formatString(BGS_Main.colorConfig.getString("presets.color.line1") + BGS_Main.colorConfig.getString("presets.text.line1")));
			sign.setLine(1, BGS_Main.formatString(BGS_Main.colorConfig.getString("presets.color.line2offline") + BGS_Main.colorConfig.getString("presets.text.line2offline")));
			sign.setLine(2, BGS_Main.formatString(BGS_Main.colorConfig.getString("presets.color.line3") + name));
			sign.setLine(3, BGS_Main.formatString(BGS_Main.colorConfig.getString("presets.color.line4") + 0 + BGS_Main.colorConfig.getString("presets.text.line4") + 0));
				
		} 
		catch (IOException ex2) 
		{
		       // OFFLINE
			//System.out.print("IOEXCEPTION");
			sign.setLine(0, BGS_Main.formatString(BGS_Main.colorConfig.getString("presets.color.line1") + BGS_Main.colorConfig.getString("presets.text.line1")));
			sign.setLine(1, BGS_Main.formatString(BGS_Main.colorConfig.getString("presets.color.line2offline") + BGS_Main.colorConfig.getString("presets.text.line2offline")));
			sign.setLine(2, BGS_Main.formatString(BGS_Main.colorConfig.getString("presets.color.line3") + name));
			sign.setLine(3, BGS_Main.formatString(BGS_Main.colorConfig.getString("presets.color.line4") + 0 + BGS_Main.colorConfig.getString("presets.text.line4") + 0));
		}
		
		sign.update();
	}
}
