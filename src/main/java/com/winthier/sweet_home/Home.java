package com.winthier.sweet_home;

import com.winthier.playercache.PlayerCache;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

/**
 * Represent a home belonging to one user.
 */
@Data
public final class Home {
    private UUID owner;
    private String name;
    private String world;
    private double x, y, z, pitch, yaw;
    private boolean publicInvite;
    private final HashSet<UUID> invites = new HashSet<>();
    private final HashSet<UUID> visitors = new HashSet<>();
    private long creationTime;

    public static final Comparator<Home> NAME_COMPARATOR = new Comparator<Home>() {
            @Override public int compare(Home a, Home b) {
                return String.CASE_INSENSITIVE_ORDER.compare(a.name, b.name);
            }
        };
    public static final Comparator<Home> VISITORS_COMPARATOR = new Comparator<Home>() {
            @Override public int compare(Home a, Home b) {
                return Integer.compare(b.visitors.size(), a.visitors.size());
            }
        };

    Home() { }

    Home(OfflinePlayer owner, String name, Location location) {
        this.owner = owner.getUniqueId();
        this.name = name;
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.pitch = location.getPitch();
        this.yaw = location.getYaw();
        this.creationTime = System.currentTimeMillis();
    }

    // Serialization

    @SuppressWarnings("unchecked")
    Home(Map<?, ?> map) {
        owner = UUID.fromString((String)map.get("owner"));
        name = (String)map.get("name");
        world = (String)map.get("world");
        x = ((Number)map.get("x")).doubleValue();
        y = ((Number)map.get("y")).doubleValue();
        z = ((Number)map.get("z")).doubleValue();
        pitch = ((Number)map.get("pitch")).doubleValue();
        yaw = ((Number)map.get("yaw")).doubleValue();
        publicInvite = map.get("public") == Boolean.TRUE;
        for (String str: (List<String>)map.get("invites")) invites.add(UUID.fromString(str));
        for (String str: (List<String>)map.get("visitors")) visitors.add(UUID.fromString(str));
        creationTime = ((Number)map.get("creation_time")).longValue();
    }

    Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("owner", owner.toString());
        result.put("name", name);
        result.put("canonical_name", getCanonicalName());
        result.put("world", world);
        result.put("x", x);
        result.put("y", y);
        result.put("z", z);
        result.put("pitch", pitch);
        result.put("yaw", yaw);
        if (publicInvite) result.put("public", true);
        result.put("invites", invites.stream().map(uuid -> uuid.toString()).collect(Collectors.toList()));
        result.put("visitors", visitors.stream().map(uuid -> uuid.toString()).collect(Collectors.toList()));
        result.put("creation_time", creationTime);
        return result;
    }

    String getOwnerName() {
        return PlayerCache.nameForUuid(owner);
    }

    String getCanonicalName() {
        return getOwnerName() + ":" + getName();
    }

    Location getLocation() {
        World bukkitWorld = Bukkit.getServer().getWorld(world);
        if (bukkitWorld == null) return null;
        return new Location(bukkitWorld, x, y, z, (float)yaw, (float)pitch);
    }

    public boolean isOwner(UUID uuid) {
        return uuid.equals(owner);
    }

    public boolean canUse(UUID uuid) {
        return publicInvite || uuid.equals(owner) || invites.contains(uuid);
    }
}
