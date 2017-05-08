package com.winthier.sweet_home;

import com.winthier.playercache.PlayerCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class ListInvitesCommand implements TabExecutor {
    private final SweetHomePlugin plugin;

    private void addPage(Player player, List<String> entriesCopy) {
        List<String> entries = new ArrayList<>(entriesCopy);
        entriesCopy.clear();
        plugin.getSession(player).getPages().add(new Session.Page() {
                @Override public void show(Player player) {
                    Iterator<String> iter = entries.iterator();
                    while (iter.hasNext()) {
                        String name = iter.next();
                        String desc = iter.next();
                        String sdesc = desc;
                        if (sdesc.length() > 32) sdesc = sdesc.substring(0, 32);
                        Msg.raw(player, " ",
                                Msg.button("&r[&b" + name + "&r]",
                                           name,
                                           "&b" + name + "\n&r/home " + name + "\n&7" + Msg.fold(Msg.wrap(desc, 24), "\n&7"),
                                           "/home " + name,
                                           ChatColor.AQUA),
                                " ",
                                Msg.button(sdesc, sdesc, null, null, ChatColor.GRAY, ChatColor.ITALIC));
                    }
                }
            });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage("Player expected!");
            return true;
        }
        List<Home> homes;
        if (args.length == 0) {
            homes = plugin.getHomes().stream().filter(home -> (!home.isOwner(player.getUniqueId()) || home.isPublicInvite()) && home.canUse(player.getUniqueId())).collect(Collectors.toList());
        } else if (args.length == 1) {
            String targetName = args[0];
            UUID targetUuid = PlayerCache.uuidForName(targetName);
            if (targetUuid == null) {
                Msg.warn(player, "No invites found!");
                return true;
            }
            homes = plugin.getHomes().stream().filter(home -> !home.isOwner(player.getUniqueId()) && home.isOwner(targetUuid) && home.canUse(player.getUniqueId())).collect(Collectors.toList());
        } else {
            return false;
        }
        if (homes.isEmpty()) {
            Msg.warn(player, "No invites found!");
            return true;
        }
        Collections.sort(homes, Home.VISITORS_COMPARATOR);
        plugin.getSession(player).getPages().clear();
        List<String> entries = new ArrayList<>();
        for (Home home: homes) {
            entries.add(home.getCanonicalName());
            entries.add(home.getDescription());
            if (entries.size() >= 18) {
                addPage(player, entries);
            }
        }
        if (!entries.isEmpty()) addPage(player, entries);
        plugin.getSession(player).showPage(player, 1);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) return null;
        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            String pattern = args[0].toLowerCase();
            for (Home home: plugin.getHomes()) {
                if (home.canUse(player.getUniqueId()) && !home.isOwner(player.getUniqueId())) {
                    String ownerName = home.getOwnerName();
                    if (!result.contains(ownerName) && ownerName.toLowerCase().startsWith(pattern)) {
                        result.add(ownerName);
                    }
                }
            }
            return result;
        }
        return null;
    }
}
