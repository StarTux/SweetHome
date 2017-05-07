package com.winthier.sweet_home;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@Getter
final class Session {
    private final SweetHomePlugin plugin;
    private final List<Page> pages = new ArrayList<>();

    interface Page {
        void show(Player player);
    }

    Session(SweetHomePlugin plugin, Player player) {
        this.plugin = plugin;
    }

    void showPage(Player player, int page) {
        Msg.send(player, "Page &9%d&r/&9%d", page, plugin.getSession(player).getPages().size());
        plugin.getSession(player).getPages().get(page - 1).show(player);
        List<Object> msg = new ArrayList<>();
        if (page > 1) {
            msg.add(" ");
            int nextPage = page - 1;
            msg.add(Msg.button(ChatColor.GREEN, "&r[&aPrev&r]", null, "&aPrevious page\n&r/homes page " + nextPage, "/homes page " + nextPage));
        }
        if (page < plugin.getSession(player).getPages().size()) {
            msg.add(" ");
            int nextPage = page + 1;
            msg.add(Msg.button(ChatColor.GREEN, "&r[&aNext&r]", null, "&aNext page\n&r/homes page " + nextPage, "/homes page " + nextPage));
        }
        if (!msg.isEmpty()) Msg.raw(player, msg);
    }
}
