package org.royalmc.bungee_signs.sign;

import org.bukkit.Location;
import org.bukkit.block.Sign;

public class StatusSign {

	private Location location;
	private Sign sign;
	private String name;
	private String host;
	private int port;
	private String server;

	public StatusSign(Location location, String name, String host, int port, String server) {
		this.location = location;
		this.sign = (Sign) location.getBlock().getState();
		this.name = name;
		this.host = host;
		this.port = port;
		this.server = server;
	}

	public Location getLocation() {
		return location;
	}

	public Sign getSign() {
		return sign;
	}
	
	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getServerName() {
		return server;
	}
}
