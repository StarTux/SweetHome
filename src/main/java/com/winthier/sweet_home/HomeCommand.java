package com.winthier.sweet_home;

import com.winthier.playercache.PlayerCache;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class HomeCommand implements TabExecutor {
    private final SweetHomePlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage("Player expected!");
            return true;
        }
        Home home;
        if (args.length == 0) {
            home = plugin.getHomeByOwnerAndName(player.getUniqueId(), "");
            if (home == null) {
                Msg.warn(player, "Default home not found!");
                return true;
            }
        } else if (args.length == 1) {
            if (args[0].contains(":")) {
                String ownerName, homeName;
                String[] tokens = args[0].split(":", 2);
                ownerName = tokens[0];
                homeName = tokens[1];
                UUID uuid = PlayerCache.uuidForName(ownerName);
                if (uuid == null) {
                    Msg.warn(player, "Home not found: %s!", homeName);
                    return true;
                }
                home = plugin.getHomeByOwnerAndName(uuid, homeName);
                if (home == null || (!home.canUse(player.getUniqueId()) && !player.hasPermission("sweethome.useanyhome"))) {
                    Msg.warn(player, "Home not found: %s!", homeName);
                    return true;
                }
            } else {
                String homeName = args[0];
                home = plugin.getHomeByOwnerAndName(player.getUniqueId(), homeName);
                if (home == null) {
                    Msg.warn(player, "Home not found: %s!", homeName);
                    return true;
                }
            }
        } else {
            return false;
        }
        Location homeLocation = home.getLocation();
        if (homeLocation == null) {
            Msg.warn(player, "The home could not be located :(");
            return true;
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 0.35f, 1.2f);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0.0, player.getHeight() * 0.5, 0.0), 32, 0, 0, 0, 0.5);
        player.teleport(homeLocation);
        player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.1f, 1.2f);
        if (home.isOwner(player.getUniqueId())) {
            player.sendTitle("", Msg.format("&9Welcome home :)"));
            if (home.getName().isEmpty()) {
                Msg.info(player, "&9Teleporting home");
            } else {
                Msg.info(player, "&9Teleporting to home %s", home.getName());
            }
        } else {
            player.sendTitle("", Msg.format("&bWelcome friend"));
            if (home.getName().isEmpty()) {
                Msg.info(player, "&bVisiting home of %s", home.getOwnerName());
            } else {
                Msg.info(player, "&bVisting home %s of %s", home.getName(), home.getOwnerName());
            }
            if (!home.getVisitors().contains(player.getUniqueId())) {
                home.getVisitors().add(player.getUniqueId());
                plugin.saveHomes();
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) return null;
        UUID uuid = player.getUniqueId();
        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            String pattern = args[0].toLowerCase();
            for (Home home: plugin.getHomes()) {
                if (home.canUse(uuid) || player.hasPermission("sweethome.useanyhome")) {
                    String fullName;
                    if (home.isOwner(uuid)) {
                        fullName = home.getName();
                    } else {
                        fullName = home.getOwnerName() + ":" + home.getName();
                    }
                    if (pattern.isEmpty() || fullName.toLowerCase().startsWith(pattern)) result.add(fullName);
                }
            }
            return result;
        }
        return null;
    }
}
