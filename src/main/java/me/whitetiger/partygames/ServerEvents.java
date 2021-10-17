package me.whitetiger.partygames;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerEvents implements Listener {
    private final PartyGames plugin;
    public ServerEvents(PartyGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        Session session = Session.getSession(event.getAddress());
        if (session != null) {
            if (session.player != null) {
                event.setMotd(String.format("Welcome %s. You have %d points!", session.player.getName(), session.points));
            } else if (session.ipAddress != null) {
                event.setMotd(String.format("Welcome to the server!"));
            }
        } else {
            event.setMotd("Welcome to the server!");
        }
    }

    @EventHandler
    public void onServerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Session session = Session.getSession(player);
        player.sendMessage("Welcome " + player.getName() + ", you have " + session.points + " points!");
        if (plugin.debugEnabled) {
            plugin.addPlayer(player);
        }
    }
}
