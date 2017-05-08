package com.winthier.sweet_home;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class SweetHomePlugin extends JavaPlugin implements Listener {
    private List<Home> homes;
    private Map<UUID, User> users;
    private final Map<UUID, Session> sessions = new HashMap<>();
    @Setter private boolean dirty;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("homes").setExecutor(new HomesCommand(this));
        getCommand("sethome").setExecutor(new SetHomeCommand(this));
        getCommand("invitehome").setExecutor(new InviteHomeCommand(this, false));
        getCommand("uninvitehome").setExecutor(new InviteHomeCommand(this, true));
        getCommand("listhomes").setExecutor(new ListHomesCommand(this));
        getCommand("listinvites").setExecutor(new ListInvitesCommand(this));
        getCommand("deletehome").setExecutor(new DeleteHomeCommand(this));
        getCommand("homeadmin").setExecutor(new AdminCommand(this));
    }

    @Override
    public void onDisable() {
        flushCache();
        if (dirty) {
            dirty = false;
            saveHomes();
        }
    }

    void flushCache() {
        reloadConfig();
        homes = null;
        users = null;
        sessions.clear();
    }

    List<Home> getHomes() {
        if (homes == null) {
            homes = new ArrayList<>();
            for (Map<?, ?> map: YamlConfiguration.loadConfiguration(new File(getDataFolder(), "homes.yml")).getMapList("homes")) {
                homes.add(new Home(map));
            }
        }
        return homes;
    }

    void saveHomes() {
        if (homes == null) return;
        dirty = false;
        YamlConfiguration config = new YamlConfiguration();
        config.set("homes", homes.stream().map(home -> home.serialize()).collect(Collectors.toList()));
        try {
            config.save(new File(getDataFolder(), "homes.yml"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    Map<UUID, User> getUsers() {
        if (users == null) {
            users = new HashMap<>();
            for (Map<?, ?> map: YamlConfiguration.loadConfiguration(new File(getDataFolder(), "users.yml")).getMapList("users")) {
                User user = new User(map);
                users.put(user.getUuid(), user);
            }
        }
        return users;
    }

    void saveUsers() {
        if (users == null) return;
        YamlConfiguration config = new YamlConfiguration();
        config.set("users", users.values().stream().map(user -> user.serialize()).collect(Collectors.toList()));
        try {
            config.save(new File(getDataFolder(), "users.yml"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    List<Home> getHomesOwnedBy(UUID uuid) {
        return getHomes().stream().filter(home -> home.isOwner(uuid)).collect(Collectors.toList());
    }

    List<Home> getHomesUsableBy(UUID uuid) {
        return getHomes().stream().filter(home -> home.canUse(uuid)).collect(Collectors.toList());
    }

    Home getHomeByOwnerAndName(UUID uuid, String name) {
        for (Home home: getHomes()) {
            if (home.isOwner(uuid) && name.equals(home.getName())) return home;
        }
        return null;
    }

    User getUser(UUID uuid) {
        User user = getUsers().get(uuid);
        if (user == null) {
            user = new User(uuid);
            getUsers().put(uuid, user);
        }
        return user;
    }

    Session getSession(Player player) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null) {
            session = new Session(this, player);
            sessions.put(player.getUniqueId(), session);
        }
        return session;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        sessions.remove(event.getPlayer().getUniqueId());
    }
}
