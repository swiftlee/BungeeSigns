package org.royalmc.bungee_signs.sign;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.royalmc.bungee_signs.BungeeSigns;
import org.royalmc.bungee_signs.ping.ServerListPing;
import org.royalmc.bungee_signs.ping.ServerStatus;
import org.royalmc.bungee_signs.utils.LocationUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * This is the class for managing the loading, creation and removal of {@link StatusSign} as well as
 * there updating via a Ping to the requested server.
 *
 * @author GregZ_
 * @since 1.1
 */
public class SignManager {

    private final LocationUtils locationUtils;
    BungeeSigns plugin;
    private String[] signText;
    private String onlineText;
    private String offlineText;
    private ArrayList<StatusSign> signs;
    private BukkitRunnable updateTask;

    public SignManager(BungeeSigns plugin) {
        this.plugin = plugin;
        locationUtils = new LocationUtils(plugin);
        loadSignText();
        loadAll();

        updateTask = new BukkitRunnable() {
            public void run() {
                for (final StatusSign s : signs) {
                    update(s);
                }
            }
        };
        updateTask.runTaskTimer(plugin, 0, 20);
    }

    public static String formatText(String text) {
        text = ChatColor.translateAlternateColorCodes('&', text);
        return text;
    }

    public void loadSignText() {
        FileConfiguration cfg = plugin.getConfig();
        signText = new String[]{ChatColor.translateAlternateColorCodes('&', cfg.getString("presets.lines.1", "&8[&aJoin&8]")), ChatColor.translateAlternateColorCodes('&', cfg.getString("presets.lines.2", "%STATUS%")), ChatColor.translateAlternateColorCodes('&', cfg.getString("presets.lines.3", "%SERVER-NAME%")), ChatColor.translateAlternateColorCodes('&', cfg.getString("presets.lines.4", "&6%ONLINE%&8/&6%MAX-PLAYERS%"))};
        onlineText = ChatColor.translateAlternateColorCodes('&', cfg.getString("presets.status.online", "&aONLINE"));
        offlineText = ChatColor.translateAlternateColorCodes('&', cfg.getString("presets.status.offline", "&cOFFLINE"));
    }

    public void loadAll() {
        signs = new ArrayList<>();
        int loaded = 0;
        if (plugin.getYmlManager().getDatabaseFile().contains("Signs")) {
            for (String key : plugin.getYmlManager().getDatabaseFile().getConfigurationSection("Signs.").getKeys(false)) {
                if (load(key)) {
                    loaded++;
                }
            }
        }
        plugin.getLogger().log(Level.INFO, "Loaded " + loaded + (loaded == 1 ? " bungee sign!" : " bungee signs!"));
    }

    private boolean load(String key) {
        if (getSign(key) != null) {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + key + " is already loaded!");
            return false;
        }

        String path = "Signs." + key;
        FileConfiguration cfg = plugin.getYmlManager().getDatabaseFile();

        if (!cfg.contains(path)) {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + key + " does not exist!");
            return false;
        }

        path += ".";

        if (!cfg.contains(path + "location")) {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + key + " does not have a set location!");
            return false;
        }

        if (!cfg.contains(path + "ip")) {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + key + " does not have a set ip address!");
            return false;
        }

        if (!cfg.contains(path + "port")) {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + key + " does not have a set port!");
            return false;
        }

        if (!cfg.contains(path + "server")) {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + key + " does not have a set server name!");
            return false;
        }

        Location loc = locationUtils.parseLocation(cfg.getString(path + "location"));

        if (loc == null) {
            return false;
        }

        if (!(loc.getBlock().getState() instanceof Sign) || !(loc.getBlock().getType() == Material.SIGN) || !(loc.getBlock().getType() == Material.SIGN_POST) || !(loc.getBlock().getType() == Material.WALL_SIGN)) {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + key + " is registered to a block that is not a sign!");
            return false;
        }

        if (getSign(loc) != null) {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + key + " is registered to a sign already covered by the status sign " + getSign(loc).getName() + "!");
            return false;
        }

        String host = cfg.getString(path + "ip");
        int port = cfg.getInt(path + "port");
        String server = cfg.getString(path + "server");

        signs.add(new StatusSign(loc, key, host, port, server));
        return true;
    }

    public boolean make(String name, Location loc, String ip, int port, String server) {
        if (name == null || name.trim() == "") {
            plugin.getLogger().log(Level.WARNING, "A status sign name cannot be empty or whitespace");
            return false;
        }
        String path = "Signs." + name.trim();

        if (plugin.getYmlManager().getDatabaseFile().contains(path)) {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + name + " already exists!");
            return false;
        }

        if (!(loc.getBlock().getState() instanceof Sign)) {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + name + " is attempting to register to a block that is not a sign!");
            return false;
        }

        path += ".";

        String locString = locationUtils.serializeLocation(loc);

        if (locString == null) {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + name + " was provided a null location during creation!");
            return false;
        }

        if (ip == null || ip.trim() == "") {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + name + " was provided a null or empty ip during creation!");
            return false;
        }

        if (server == null || server.trim() == "") {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + name + " was provided a null or empty server name during creation!");
            return false;
        }

        plugin.getYmlManager().getDatabaseFile().set(path + "location", locString);
        plugin.getYmlManager().getDatabaseFile().set(path + "ip", ip);
        plugin.getYmlManager().getDatabaseFile().set(path + "port", port);
        plugin.getYmlManager().getDatabaseFile().set(path + "server", server);
        plugin.getYmlManager().saveDatabaseFile();

        signs.add(new StatusSign(loc, name, ip, port, server));
        return true;
    }

    public boolean remove(StatusSign sign) {
        if (sign == null) {
            plugin.getLogger().log(Level.WARNING, "The Status Sign provided for removal is null!");
            return false;
        }
        return remove(sign.getName());
    }

    public boolean remove(String name) {
        if (name == null || name.trim() == "") {
            plugin.getLogger().log(Level.WARNING, "The Status Sign name provided for removal is null or empty!");
            return false;
        }

        String path = "Signs." + name.trim();
        if (!plugin.getYmlManager().getDatabaseFile().contains(path)) {
            plugin.getLogger().log(Level.WARNING, "Status Sign " + name + " does not exist!");
            return false;
        }

        StatusSign sign = getSign(name);
        if (sign != null) {
            signs.remove(sign);
        }
        plugin.getYmlManager().getDatabaseFile().set(path, null);
        plugin.getYmlManager().saveDatabaseFile();

        plugin.getLogger().log(Level.WARNING, "Successfully removed the Status Sign " + name + "!");
        return true;
    }

    public StatusSign getSign(String name) {
        for (StatusSign sign : signs) {
            if (sign.getName().equalsIgnoreCase(name))
                return sign;
        }
        return null;
    }

    public StatusSign getSign(Location loc) {
        for (StatusSign sign : signs) {
            if (sign.getLocation().getBlockX() == loc.getX() && sign.getLocation().getBlockY() == loc.getY() && sign.getLocation().getBlockZ() == loc.getZ())
                return sign;
        }
        return null;
    }

    public void update(final StatusSign sign) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            int onlinePlayers = 0;
            int maxPlayers = 0;
            String status;
            boolean serverIsOffline = false;

            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                try {
                    ServerStatus response = new ServerListPing(new InetSocketAddress(sign.getHost(), sign.getPort())).fetchData();
                    onlinePlayers = response.getPlayers().getOnline();
                    maxPlayers = response.getPlayers().getMax();
                    status = onlineText;

                } catch (UnknownHostException e) {
                    status = "§cIP ERROR";
                    syncUpdate(sign, status, onlinePlayers, maxPlayers);
                    updateWool(sign.getSign(), DyeColor.RED.getData()); //deprecated
                    BungeeSigns.signInProgress.replace(sign, false);
                } catch (IOException e) {
                    status = offlineText;
                    serverIsOffline = true;
                    syncUpdate(sign, status, onlinePlayers, maxPlayers);
                    updateWool(sign.getSign(), DyeColor.RED.getData()); //deprecated
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (!(sign.getLocation().getBlock().getState() instanceof Sign)) {
                            signs.remove(sign);
                            return;
                        }

                        if (!serverIsOffline) {

                            BungeeSigns.signInProgress.putIfAbsent(sign, false);

                            if (onlinePlayers > 0) {
                                if (!BungeeSigns.signInProgress.get(sign)) {
                                    syncUpdate(sign, formatText("&8In Queue"), onlinePlayers, maxPlayers);
                                    updateWool(sign.getSign(), DyeColor.GREEN.getData()); //deprecated
                                    BungeeSigns.signInProgress.replace(sign, false);
                                } else {
                                    syncUpdate(sign, formatText("&8In Progress"), onlinePlayers, maxPlayers);
                                    updateWool(sign.getSign(), DyeColor.RED.getData()); //deprecated
                                    BungeeSigns.signInProgress.replace(sign, true);
                                }
                            } else {
                                syncUpdate(sign, formatText("&8In Queue"), onlinePlayers, maxPlayers);
                                updateWool(sign.getSign(), DyeColor.GREEN.getData()); //deprecated
                                BungeeSigns.signInProgress.replace(sign, false);
                            }
                        }
                    }
                });
            }
        });
    }

    private void updateWool(Sign s, byte value) {
        String w = s.getWorld().getName();
        Location loc = Bukkit.getWorld(w).getBlockAt(s.getLocation()).getLocation();

        Location left = loc.getBlock().getRelative(BlockFace.WEST).getLocation();
        Location right = loc.getBlock().getRelative(BlockFace.EAST).getLocation();
        Location up = loc.getBlock().getRelative(BlockFace.UP).getLocation();
        Location down = loc.getBlock().getRelative(BlockFace.DOWN).getLocation();
        Location front = loc.getBlock().getRelative(BlockFace.NORTH).getLocation();
        Location back = loc.getBlock().getRelative(BlockFace.SOUTH).getLocation();

        List<Location> locations = new ArrayList<>();
        locations.add(back);
        locations.add(left);
        locations.add(right);
        locations.add(up);
        locations.add(down);
        locations.add(front);

        for (Location l : locations) {
            if (l.getBlock().getType() == Material.WOOL && l.getBlock().getData() == value) {
                break;
            } else if (l.getBlock().getType() == Material.WOOL) {
                l.getBlock().setData(value);
                break;
            }
        }

    }

    private void syncUpdate(StatusSign sign, String status, int onlinePlayers, int maxPlayers) {
        Sign s = sign.getSign();
        s.setLine(0, signText[0].replace("%STATUS%", status).replace("%SERVER-NAME%", sign.getServerName()).replace("%ONLINE%", String.valueOf(onlinePlayers)).replace("%MAX-PLAYERS%", String.valueOf(maxPlayers)).replace("%NAME%", sign.getName()));
        s.setLine(1, signText[1].replace("%STATUS%", status).replace("%SERVER-NAME%", sign.getServerName()).replace("%ONLINE%", String.valueOf(onlinePlayers)).replace("%MAX-PLAYERS%", String.valueOf(maxPlayers)).replace("%NAME%", sign.getName()));
        s.setLine(2, signText[2].replace("%STATUS%", status).replace("%SERVER-NAME%", sign.getServerName()).replace("%ONLINE%", String.valueOf(onlinePlayers)).replace("%MAX-PLAYERS%", String.valueOf(maxPlayers)).replace("%NAME%", sign.getName()));
        s.setLine(3, signText[3].replace("%STATUS%", status).replace("%SERVER-NAME%", sign.getServerName()).replace("%ONLINE%", String.valueOf(onlinePlayers)).replace("%MAX-PLAYERS%", String.valueOf(maxPlayers)).replace("%NAME%", sign.getName()));
        s.update();
    }
}
