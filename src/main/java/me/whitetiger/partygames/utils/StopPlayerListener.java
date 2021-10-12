package me.whitetiger.partygames.utils;

import me.whitetiger.partygames.PartyGames;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;

public class StopPlayerListener implements Listener {

    private final List<List<Player>> stunnedPlayers = new ArrayList<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        for (List<Player> playerList : stunnedPlayers) {
            if (playerList.contains(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    public void addPlayers(List<Player> playerList) {
        stunnedPlayers.add(playerList);
    }

    public void removePlayers(List<Player> playerList) {
        stunnedPlayers.remove(playerList);
    }
}
