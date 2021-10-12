package me.whitetiger.partygames.commands;

import com.google.common.base.Functions;
import me.whitetiger.partygames.PartyGames;
import me.whitetiger.partygames.games.helper.AGames;
import me.whitetiger.partygames.games.helper.GameType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StartGameCommand extends PartyCommand{

    PartyGames plugin = PartyGames.getInstance();

    public StartGameCommand() {
        super("start", true);
    }

    @Override
    void onCommand(Player player, String[] args) {
        plugin.addCurrentOnlinePlayers();

        AGames game;
        player.sendMessage(ChatColor.GOLD + "Starting game of Type: " + args[1]);
        System.out.println(ChatColor.GOLD + "Starting game of Type: " + args[1]);


        try {
            game = plugin.getGameByName(args[1]);
        } catch (ClassNotFoundException | LinkageError | InstantiationException | IllegalAccessException | SecurityException e) {
            player.sendMessage(ChatColor.RED + "Game was not found!");
            e.printStackTrace();
            return;
        }

        game.start();
        player.sendMessage(ChatColor.GREEN + "StartedGame:\n" + ChatColor.GREEN + "GameID:     " + ChatColor.GOLD + game.getGameID() + ChatColor.GREEN + "\nGameType: " + ChatColor.GOLD + game.getGameType().getName());
        System.out.println(ChatColor.GREEN + "StartedGame:\n" + ChatColor.GREEN + "GameID:     " + ChatColor.GOLD + game.getGameID() + ChatColor.GREEN + "\nGameType: " + ChatColor.GOLD + game.getGameType().getName());
    }

    @Override
    List<String> getArguments(Player player, Command command, String alias, String[] args) {
        return Arrays.stream(GameType.values()).map(Functions.toStringFunction()).collect(Collectors.toList());
    }
}
