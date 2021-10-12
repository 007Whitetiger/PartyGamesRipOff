package me.whitetiger.partygames.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import me.whitetiger.partygames.Session;
import net.md_5.bungee.api.ChatColor;

public class PointsCommand extends PartyCommand {

    public PointsCommand() {
        super("points", false);
    }

    @Override
    void onCommand(Player player, String[] args) {
        Session playerSession = Session.getSession(player);
        player.sendMessage(ChatColor.DARK_AQUA + "You have " + ChatColor.GOLD + playerSession.points + ChatColor.DARK_AQUA + "!");
    }

    @Override
    List<String> getArguments(Player player, Command command, String alias, String[] args) {
        return null;
    }
    
}
