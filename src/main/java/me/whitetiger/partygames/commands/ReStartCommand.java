package me.whitetiger.partygames.commands;

import me.whitetiger.partygames.PartyGames;
import me.whitetiger.partygames.games.helper.AGames;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.List;

public class ReStartCommand extends PartyCommand{

    private final PartyGames plugin = PartyGames.getInstance();


    public ReStartCommand() {
        super("restart", true);
    }

    @Override
    void onCommand(Player player, String[] args) {

        player.sendMessage(ChatColor.GOLD + "Restarting game of Type: " + args[1]);
        System.out.println(ChatColor.GOLD + "Restarting game of Type: " + args[1]);

        AGames game = null;

        for (AGames _game : AGames.getCurrentGames()) {
            if (_game.getPlayers().contains(player)) {
                game = _game;
            }
        }

        if (game == null) {
            player.sendMessage(ChatColor.RED + "OldGame was not found!");
            return;
        }

        try {
            plugin.restartGame(game);
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            player.sendMessage(ChatColor.RED + "Game could not be started!");
            e.printStackTrace();
            return;
        }

        player.sendMessage(ChatColor.GREEN + "RestartedGame:\n" + ChatColor.GREEN + "GameID:     " + ChatColor.GOLD + game.getGameID() + ChatColor.GREEN + "\nGameType: " + ChatColor.GOLD + game.getGameType().getName());
        System.out.println(ChatColor.GREEN + "RestartedGame:\n" + ChatColor.GREEN + "GameID:     " + ChatColor.GOLD + game.getGameID() + ChatColor.GREEN + "\nGameType: " + ChatColor.GOLD + game.getGameType().getName());

    }

    @Override
    List<String> getArguments(Player player, Command command, String alias, String[] args) {
        return null;
    }
}
