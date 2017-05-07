package com.winthier.sweet_home;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class DeleteHomeCommand implements TabExecutor {
    private final SweetHomePlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage("Player expected!");
            return true;
        }
        if (args.length != 1) return false;
        String homeName = args[0];
        if (homeName.isEmpty()) return false;
        Home home = plugin.getHomeByOwnerAndName(player.getUniqueId(), homeName);
        if (home == null) {
            Msg.warn(player, "Home not found: %s", homeName);
            return true;
        }
        plugin.getHomes().remove(home);
        plugin.saveHomes();
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 0.35f, 1.2f);
        Msg.info(player, "Home deleted: %s", home.getName());
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
            for (Home home: plugin.getHomesOwnedBy(uuid)) {
                if (!home.getName().isEmpty() && home.getName().toLowerCase().startsWith(pattern)) {
                    result.add(home.getName());
                }
            }
            return result;
        }
        return null;
    }
}
