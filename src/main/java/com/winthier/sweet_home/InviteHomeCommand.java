package com.winthier.sweet_home;

import com.winthier.playercache.PlayerCache;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class InviteHomeCommand implements TabExecutor {
    private final SweetHomePlugin plugin;
    private final boolean uninvite;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage("Player expected!");
            return true;
        }
        if (args.length != 1 && args.length != 2) return false;
        String targetName = args[0];
        UUID targetUuid;
        if (targetName.equals("*")) {
            targetUuid = null;
        } else {
            targetUuid = PlayerCache.uuidForName(targetName);
            if (targetUuid == null) {
                Msg.warn(player, "Player not found: %s", targetName);
                return true;
            }
        }
        String homeName;
        if (args.length < 2) {
            homeName = "";
        } else {
            homeName = args[1];
        }
        Home home = plugin.getHomeByOwnerAndName(player.getUniqueId(), homeName);
        if (home == null) {
            Msg.warn(player, "Home not found!");
            return true;
        }
        if (targetUuid == null) {
            if (uninvite) {
                if (!home.isPublicInvite()) {
                    Msg.warn(player, "Home is not public!");
                } else {
                    home.setPublicInvite(false);
                    plugin.saveHomes();
                    Msg.info(player, "Public uninvited!");
                }
            } else {
                if (home.isPublicInvite()) {
                    Msg.warn(player, "Home is already public!");
                } else {
                    home.setPublicInvite(true);
                    plugin.saveHomes();
                    Msg.info(player, "Made home public!");
                }
            }
        } else {
            if (uninvite) {
                if (!home.getInvites().contains(targetUuid)) {
                    Msg.warn(player, "%s not invited!", targetName);
                } else {
                    home.getInvites().remove(targetUuid);
                    plugin.saveHomes();
                    Msg.info(player, "%s uninvited!", targetName);
                }
            } else {
                if (targetUuid.equals(player.getUniqueId())) {
                    Msg.warn(player, "You cannot invite yourself!");
                } else if (home.getInvites().contains(targetUuid)) {
                    Msg.warn(player, "%s already invited!", targetName);
                } else {
                    home.getInvites().add(targetUuid);
                    plugin.saveHomes();
                    Msg.info(player, "%s invited!", targetName);
                    Player target = plugin.getServer().getPlayer(targetUuid);
                    if (target != null) {
                        String canonical = home.getCanonicalName();
                        if (home.getName().isEmpty()) {
                            Msg.raw(target, Msg.button(player.getName() + " invited you to their home. Click here to visit!",
                                                       "/home " + canonical,
                                                       "&9/home " + canonical,
                                                       "/home " + canonical,
                                                       ChatColor.WHITE));
                        } else {
                            Msg.raw(target, Msg.button(player.getName() + " invited you to their home &9" + home.getName() + "&r. Click here to visit!",
                                                       "/home " + canonical,
                                                       "&9/home " + canonical,
                                                       "/home " + canonical,
                                                       ChatColor.WHITE));
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) return null;
        UUID uuid = player.getUniqueId();
        if (args.length == 2) {
            List<String> result = new ArrayList<>();
            String pattern = args[1].toLowerCase();
            for (Home home: plugin.getHomes()) {
                if (home.isOwner(uuid) && home.getName().toLowerCase().startsWith(pattern)) {
                    result.add(home.getName());
                }
            }
            return result;
        }
        return null;
    }
}
