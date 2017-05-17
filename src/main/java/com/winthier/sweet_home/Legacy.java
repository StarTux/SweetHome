package com.winthier.sweet_home;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class Legacy {
    private Legacy() { }

    static void migrate(SweetHomePlugin plugin, String database, String user, String password, String serverName) throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        String url = "jdbc:mysql://127.0.0.1:3306/" + database;
        Connection connection = DriverManager.getConnection(url, user, password);
        // Server
        ResultSet result = connection.createStatement().executeQuery("SELECT * FROM `servers`");
        Map<String, Integer> servers = new HashMap<>();
        while (result.next()) {
            servers.put(result.getString("name"), result.getInt("id"));
        }
        if (!servers.containsKey(serverName)) {
            System.out.println("Server not found: " + serverName);
            return;
        }
        int serverId = servers.get(serverName);
        // Players
        Map<Integer, UUID> players = new HashMap<>();
        result = connection.createStatement().executeQuery("SELECT * FROM `players` WHERE `server_id` = " + serverId);
        while (result.next()) {
            players.put(result.getInt("id"), UUID.fromString(result.getString("uuid")));
        }
        System.out.println("" + players.size() + " players found.");
        // Worlds
        Map<Integer, String> worlds = new HashMap<>();
        result = connection.createStatement().executeQuery("SELECT * FROM `worlds`");
        while (result.next()) {
            String worldName = result.getString("name");
            if (plugin.getServer().getWorld(worldName) != null) {
                String oldWorldName = worldName;
                worldName = plugin.getServer().getWorld(worldName).getName();
                if (!oldWorldName.equals(worldName)) {
                    System.out.println(oldWorldName + " => " + worldName);
                }
            }
            worlds.put(result.getInt("id"), worldName);
        }
        System.out.println("" + worlds.size() + " worlds found.");
        // Homes
        Map<Integer, Home> homes = new HashMap<>();
        result = connection.createStatement().executeQuery("SELECT * FROM `homes`");
        while (result.next()) {
            UUID owner = players.get(result.getInt("owner_id"));
            if (owner == null) continue;
            Home home = new Home();
            home.setOwner(owner);
            String homeName = result.getString("name");
            if (result.wasNull()) homeName = "";
            home.setName(homeName);
            home.setWorld(worlds.get(result.getInt("world_id")));
            home.setX(result.getDouble("x"));
            home.setY(result.getDouble("y"));
            home.setZ(result.getDouble("z"));
            home.setPitch(result.getDouble("pitch"));
            home.setYaw(result.getDouble("yaw"));
            // TODO public invite
            // TODO invites
            home.setCreationTime(result.getDate("date_created").getTime());
            homes.put(result.getInt("id"), home);
        }
        System.out.println("" + homes.size() + " homes found.");
        // Invites
        result = connection.createStatement().executeQuery("SELECT * FROM `invites`");
        int totalInvites = 0;
        int finishedInvites = 0;
        while (result.next()) {
            Home home = homes.get(result.getInt("home_id"));
            if (home == null) continue;
            totalInvites += 1;
            int inviteeId = result.getInt("invitee_id");
            if (result.wasNull()) {
                home.setPublicInvite(true);
            } else {
                UUID invitee = players.get(inviteeId);
                if (invitee == null) continue;
                home.getInvites().add(invitee);
            }
            finishedInvites += 1;
        }
        System.out.println("" + finishedInvites + "/" + totalInvites + " invites found.");
        for (Home home: homes.values()) plugin.getHomes().add(home);
        plugin.saveHomes();
        System.out.println("Migration complete. Total home count: " + plugin.getHomes().size());
    }
}
