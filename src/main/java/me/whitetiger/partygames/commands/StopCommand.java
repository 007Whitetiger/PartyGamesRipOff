package me.whitetiger.partygames.commands;

import me.whitetiger.partygames.games.helper.AGames;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

public class StopCommand extends PartyCommand {
    public StopCommand() {
        super("stop", true);
    }

    @Override
    void onCommand(Player player, String[] args) {
        AGames game = null;
        for (AGames _game : AGames.getCurrentGames()) {
            if (_game.getPlayers().contains(player)) {
                game= _game;
            }
        }

        if (game == null) {
            player.sendMessage(ChatColor.RED + "Couldn't find a game!");
            return;
        }

        game.stop();

        player.sendMessage(ChatColor.GREEN + "Stopped a game with game type: " + ChatColor.GOLD + game.getGameType());
    }

    @Override
    List<String> getArguments(Player player, Command command, String alias, String[] args) {
        return null;
    }
}
