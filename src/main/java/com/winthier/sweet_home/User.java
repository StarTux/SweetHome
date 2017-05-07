package com.winthier.sweet_home;

import com.winthier.playercache.PlayerCache;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Store persistent data about one user.
 *
 * Maximum home count is determined per user by adding up:
 * - Flat value in config.DefaultHomes
 * - All numbers mapped by permission nodes in config.AdditionalHomes possessed by the player
 * - extraHomes
 */
@Data
public final class User {
    private final UUID uuid;
    private int extraHomes;

    User(UUID uuid) {
        this.uuid = uuid;
    }

    User(Map<?, ?> map) {
        this.uuid = UUID.fromString((String)map.get("UUID"));
        this.extraHomes = ((Number)map.get("ExtraHomes")).intValue();
    }

    Map<?, ?> serialize() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("UUID", uuid.toString());
        result.put("Name", getName());
        result.put("ExtraHomes", extraHomes);
        return result;
    }

    String getName() {
        return PlayerCache.nameForUuid(uuid);
    }

    public int getMaximumHomes(SweetHomePlugin plugin, Player player) {
        int result = plugin.getConfig().getInt("DefaultHomes") + extraHomes;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("AdditionalHomes");
        for (String perm: section.getKeys(true)) {
            if (section.isInt(perm) && player.hasPermission(perm)) result += section.getInt(perm);
        }
        return result;
    }
}
