package com.winthier.sweet_home;

import com.winthier.playercache.PlayerCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class ListHomesCommand implements CommandExecutor {
    private final SweetHomePlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage("Player expected!");
            return true;
        }
        UUID targetUuid;
        if (args.length == 0) {
            targetUuid = player.getUniqueId();
        } else if (args.length == 1) {
            if (!player.hasPermission("sweethome.useanyhome")) return false;
            String targetName = args[0];
            targetUuid = PlayerCache.uuidForName(targetName);
            if (targetUuid == null) {
                Msg.warn(player, "Player not found: %s", targetName);
            }
        } else {
            return false;
        }
        List<Object> msg = new ArrayList<>();
        List<Home> ownedHomes = plugin.getHomesOwnedBy(targetUuid);
        Collections.sort(ownedHomes, Home.NAME_COMPARATOR);
        int maxHomes = plugin.getUser(player.getUniqueId()).getMaximumHomes(plugin, player);
        if (maxHomes != 1) {
            msg.add(Msg.button(ChatColor.WHITE, "You have &9" + ownedHomes.size() + "&r/&9" + maxHomes + "&r homes:", null, null, null));
        } else {
            msg.add(Msg.button(ChatColor.WHITE, "You have &9" + ownedHomes.size() + "&r/&9" + maxHomes + "&r home:", null, null, null));
        }
        for (Home home: ownedHomes) {
            msg.add(" ");
            if (home.getName().isEmpty()) {
                msg.add(Msg.button(ChatColor.BLUE, "&r[&9Default&r]", "/home", "&9Default home\n&r/home", "/home"));
            } else {
                msg.add(Msg.button(ChatColor.AQUA, "&r[&b" + home.getName() + "&r]", "/home " + home.getName(), "&b" + home.getName() + "\n&r/home " + home.getName(), "/home " + home.getName()));
            }
        }
        Msg.raw(player, msg);
        return true;
    }
}
