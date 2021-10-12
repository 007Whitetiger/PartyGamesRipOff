package me.whitetiger.partygames.commands;

/**
import com.rylinaux.plugman.PlugMan;
import com.rylinaux.plugman.util.PluginUtil;
*/
import me.whitetiger.partygames.PartyGames;
import me.whitetiger.partygames.games.helper.AGames;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends PartyCommand{
    public ReloadCommand() {
        super("reload", true);
    }

    @Override
    void onCommand(Player player, String[] args) {
        Plugin target = PartyGames.getInstance();

        List<AGames> gamesList = new ArrayList<>(AGames.getCurrentGames());

        gamesList.forEach(AGames::stop);

        //PluginUtil.reload(target);

        player.sendMessage(ChatColor.GREEN + "Plugin has reloaded!");
    }

    @Override
    List<String> getArguments(Player player, Command command, String alias, String[] args) {
        return null;
    }
}
