package com.winthier.sweet_home;

import com.winthier.generic_events.GenericEventsPlugin;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class SetHomeCommand implements CommandExecutor {
    private final SweetHomePlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage("Player expected!");
            return true;
        }
        if (args.length > 1) return false;
        if (!plugin.getConfig().getStringList("Worlds").contains(player.getWorld().getName())) {
            Msg.warn(player, "You cannot set a home in this world!");
            return true;
        }
        final UUID uuid = player.getUniqueId();
        final User user = plugin.getUser(uuid);
        Location homeLocation = player.getLocation();
        Block homeBlock = homeLocation.getBlock();
        if (!GenericEventsPlugin.getInstance().playerCanBuild(player, homeBlock)) {
            Msg.warn(player, "You cannot set a home in this location!");
            return true;
        }
        String homeName = args.length == 0 ? "" : args[0];
        if (!homeName.matches("[a-zA-Z0-9-_]{0,32}")) {
            Msg.warn(player, "Invalid home name: %s", homeName);
            return true;
        }
        int requiredHomes = plugin.getHomesOwnedBy(uuid).size();
        Home existingHome = plugin.getHomeByOwnerAndName(player.getUniqueId(), homeName);
        if (existingHome == null) requiredHomes += 1;
        if (user.getMaximumHomes(plugin, player) < requiredHomes) {
            Msg.warn(player, "All your homes are set!");
            return true;
        }
        if (existingHome != null) plugin.getHomes().remove(existingHome);
        Home home = new Home(player, homeName, homeLocation);
        plugin.getHomes().add(home);
        plugin.saveHomes();
        plugin.getLogger().info(String.format("%s set home '%s' at %s %d %d %d", player.getName(), homeName, homeBlock.getWorld().getName(), homeBlock.getX(), homeBlock.getY(), homeBlock.getZ()));
        if (homeName.isEmpty()) {
            Msg.info(player, "Default home set");
            player.sendTitle("", Msg.format("&9Default home set"));
        } else {
            Msg.info(player, "Home set: %s", homeName);
            player.sendTitle(Msg.format("&9%s", homeName), Msg.format("&9Home set"));
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 0.35f, 1.2f);
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0.0, player.getHeight() * 0.5, 0.0), 32, 0, 0, 0, 0.5);
        return true;
    }
}
