package com.winthier.sweet_home;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class AdminCommand implements CommandExecutor {
    private final SweetHomePlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = args.length == 0 ? null : args[0].toLowerCase();
        if ("reload".equals(cmd)) {
            plugin.flushCache();
            sender.sendMessage("Config files reloaded.");
        } else if ("migrate".equals(cmd)) {
            sender.sendMessage("Migration. See console...");
            try {
                Legacy.migrate(plugin, args[1], args[2], args[3]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return false;
        }
        return true;
    }
}
