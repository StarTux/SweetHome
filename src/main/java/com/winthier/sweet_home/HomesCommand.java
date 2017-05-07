package com.winthier.sweet_home;

import com.winthier.playercache.PlayerCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class HomesCommand implements TabExecutor {
    private final SweetHomePlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage("Player expected!");
            return true;
        }
        String cmd = args.length == 0 ? null : args[0].toLowerCase();
        if (cmd == null) {
            List<Home> homes = plugin.getHomesOwnedBy(player.getUniqueId());
            Collections.sort(homes, Home.NAME_COMPARATOR);
            int maxHomes = plugin.getUser(player.getUniqueId()).getMaximumHomes(plugin, player);
            if (maxHomes != 1) {
                Msg.info(player, "You have %d/%d homes set.", homes.size(), maxHomes);
            } else {
                Msg.info(player, "You have %d/%d home set.", homes.size(), maxHomes);
            }
            for (Home home: homes) {
                List<Object> msg = new ArrayList<>();
                String homeName = home.getName().isEmpty() ? "Default" : home.getName();
                String homeSuffix = home.getName().isEmpty() ? "" : " " + home.getName();
                String homeCommand = home.getName().isEmpty() ? "/home" : "/home " + home.getName();
                msg.add(" ");
                msg.add(Msg.button(ChatColor.BLUE, "&r[&9" + homeName + "&r]", homeCommand, "&9" + homeName + "\n&r" + homeCommand, homeCommand + " "));
                Msg.raw(player, msg);
                if (home.isPublicInvite() || !home.getInvites().isEmpty()) {
                    int count = (home.isPublicInvite() ? 1 : 0) + home.getInvites().size();
                    msg.clear();
                    msg.add(Msg.format("&o %d invites:", count));
                    if (home.isPublicInvite()) {
                        String commandSuggestion = "/uninvitehome *" + homeSuffix;
                        msg.add(" ");
                        msg.add(Msg.button(ChatColor.AQUA, "*", "Public", commandSuggestion, commandSuggestion + " "));
                    }
                    for (UUID invite: home.getInvites()) {
                        String inviteName = PlayerCache.nameForUuid(invite);
                        if (inviteName != null) {
                            String commandSuggestion = "/uninvitehome " + inviteName + homeSuffix;
                            msg.add(" ");
                            msg.add(Msg.button(ChatColor.AQUA, inviteName, commandSuggestion, "&a" + inviteName + "\n" + commandSuggestion, commandSuggestion + " "));
                        }
                    }
                    Msg.raw(player, msg);
                }
            }
        } else if (cmd.equals("page") && args.length == 2) {
            int page;
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                page = -1;
            }
            if (page < 1 || page > plugin.getSession(player).getPages().size()) {
                Msg.warn(player, "Page not found: %s", args[1]);
                return true;
            }
            plugin.getSession(player).showPage(player, page);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
