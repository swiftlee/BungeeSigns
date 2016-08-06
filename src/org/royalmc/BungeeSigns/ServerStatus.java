package org.royalmc.BungeeSigns;

import java.util.List;

public class ServerStatus {
    private String description;
    private Players players;
    private Version version;
    private String favicon;
    private int time;

    public Players getPlayers() {
        return players;
    }

    public int getTime() {
        return time;
    }

    public Version getVersion() {
        return version;
    }

    public String getFavicon() {
        return favicon;
    }

    public String getDescription() {
        return description;
    }

    public class Players {
        private int max;
        private int online;
        private List<Player> sample;

        public int getOnline() {
            return online;
        }

        public int getMax() {
            return max;
        }

        public List<Player> getSample() {
            return sample;
        }
    }

    public class Player {
        private String name;
        private String id;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public class Version {
        private String name;
        private String protocol;

        public String getName() {
            return name;
        }

        public String getProtocol() {
            return protocol;
        }
    }
}