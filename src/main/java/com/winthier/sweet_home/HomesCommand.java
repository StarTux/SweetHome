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
                String homeSecret = home.getName().isEmpty() ? ":" : home.getName();
                msg.add(Msg.button(homeName,
                                   "/home" + homeSuffix,
                                   "&9" + homeName + "\n&r" + "/home" + homeSuffix,
                                   "/home" + homeSuffix + " ",
                                   ChatColor.BLUE, ChatColor.BOLD));
                msg.add(" ");
                msg.add(Msg.button("&r[&aRename&r]",
                                   "/homes rename " + homeSecret,
                                   "&aRename Home\n&r/homes rename " + homeSecret,
                                   "/homes rename " + homeSecret + " ",
                                   ChatColor.GREEN));
                msg.add(" ");
                msg.add(Msg.button("&r[&aDescribe&r]",
                                   "/homes describe " + homeSecret,
                                   "&aWrite Description\n&r/homes describe " + homeSecret,
                                   "/homes describe " + homeSecret + " ",
                                   ChatColor.GREEN));
                if (home.getName().isEmpty()) {
                    msg.add(" ");
                    msg.add(Msg.button("&r[&aInvite&r]",
                                       "/invitehome",
                                       "&aInvite Home\n&r/invitehome",
                                       "/invitehome ",
                                       ChatColor.GREEN));
                }
                if (!home.getName().isEmpty()) {
                    msg.add(" ");
                    msg.add(Msg.button("&r[&4Delete&r]",
                                       "/deletehome " + homeName,
                                       "&4Delete Home\n&r/deletehome " + homeName,
                                       "/deletehome " + homeName + " ",
                                       ChatColor.DARK_RED));
                }
                Msg.raw(player, "", msg);
                if (!home.getDescription().isEmpty()) {
                    Msg.raw(player, " ",
                            Msg.button(home.getDescription(),
                                       home.getDescription(),
                                       "&a" + homeName + "\n/home describe " + homeSecret + "\n&7" + Msg.fold(Msg.wrap(home.getDescription(), 24), "\n&7"),
                                       "/home describe " + homeSecret + " ",
                                       ChatColor.ITALIC, ChatColor.GRAY));
                }
                if (home.isPublicInvite() || !home.getInvites().isEmpty()) {
                    int count = (home.isPublicInvite() ? 1 : 0) + home.getInvites().size();
                    msg.clear();
                    msg.add(Msg.format("&o %d invites:", count));
                    if (home.isPublicInvite()) {
                        String commandSuggestion = "/uninvitehome *" + homeSuffix;
                        msg.add(" ");
                        msg.add(Msg.button("*",
                                           "Public",
                                           commandSuggestion,
                                           commandSuggestion + " ",
                                           ChatColor.AQUA));
                    }
                    for (UUID invite: home.getInvites()) {
                        String inviteName = PlayerCache.nameForUuid(invite);
                        if (inviteName != null) {
                            String commandSuggestion = "/uninvitehome " + inviteName + homeSuffix;
                            msg.add(" ");
                            msg.add(Msg.button(inviteName,
                                               commandSuggestion,
                                               "&a" + inviteName + "\n" + commandSuggestion,
                                               commandSuggestion + " ",
                                               ChatColor.AQUA));
                        }
                    }
                    Msg.raw(player, "", msg);
                }
            }
            if (maxHomes > homes.size()) {
                Msg.raw(player, "",
                        Msg.button("&r[&bSet Home&r]",
                                   "/sethome",
                                   "&aSet Home\n&r/sethome",
                                   "/sethome ",
                                   ChatColor.AQUA));
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
        } else if (cmd.equals("describe") && args.length >= 2) {
            String homeName = args[1];
            Home home;
            if (homeName.equals(":")) {
                home = plugin.getHomeByOwnerAndName(player.getUniqueId(), "");
            } else {
                home = plugin.getHomeByOwnerAndName(player.getUniqueId(), homeName);
            }
            if (home == null) {
                Msg.warn(player, "Home not found!");
                return true;
            }
            String newDescription;
            if (args.length == 2) {
                newDescription = "";
            } else {
                StringBuilder sb = new StringBuilder(args[2]);
                for (int i = 3; i < args.length; i += 1) sb.append(" ").append(args[i]);
                newDescription = sb.toString();
            }
            home.setDescription(newDescription);
            plugin.saveHomes();
            Msg.info(player, "Description updated");
        } else if (cmd.equals("rename") && args.length >= 2) {
            String homeName = args[1];
            Home home;
            if (homeName.equals(":")) {
                home = plugin.getHomeByOwnerAndName(player.getUniqueId(), "");
            } else {
                home = plugin.getHomeByOwnerAndName(player.getUniqueId(), homeName);
            }
            if (home == null) {
                Msg.warn(player, "Home not found!");
                return true;
            }
            String newName;
            if (args.length >= 3) {
                newName = args[2];
            } else {
                newName = "";
            }
            Home existingHome = plugin.getHomeByOwnerAndName(player.getUniqueId(), newName);
            if (existingHome != null) {
                Msg.warn(player, "Home already exists!");
                return true;
            }
            home.setName(newName);
            plugin.saveHomes();
            Msg.info(player, "Home renamed");
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
