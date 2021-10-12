package me.whitetiger.partygames.commands;

import me.whitetiger.partygames.PartyGames;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final PartyGames plugin = PartyGames.getInstance();

    private List<String> commands = new ArrayList<>();

    public CommandHandler() {
        new StartGameCommand();
        new ReStartCommand();
        new StopCommand();
        new ReloadCommand();
        new CreateArmorstandCommand();
        new DeleteArmorstandCommand();
        new PartyGameRuleCommand();
        new PointsCommand();

        PartyCommand.getPartyCommands().forEach(partyCommand -> {
            commands.addAll(partyCommand.getAliases());
            commands.add(partyCommand.getCommand());
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You can only do this command in game!");
            return true;
        }

        Player player = (Player) sender;

        for (PartyCommand partyCommand : PartyCommand.getPartyCommands()) {
            if (partyCommand.isCommand(args[0])) {
                if (!partyCommand.isAdmin()) {
                    partyCommand.onCommand(player, args);
                } else if (partyCommand.isAdmin() && player.hasPermission(plugin.adminPerms)) {
                    partyCommand.onCommand(player, args);
                }
                return true;
            }
        }

        player.sendMessage(ChatColor.RED + "Please enter a valid sub-command!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (!(sender instanceof Player)) return null;

        Player player = (Player) sender;

        if (args.length < 2) {
            return commands;
        }
        else {
            for (PartyCommand partyCommand : PartyCommand.getPartyCommands()) {
                if (partyCommand.isCommand(args[0])) {
                    if (!partyCommand.isAdmin()) {
                        return partyCommand.getArguments(player, command, alias, args);
                    } else if (partyCommand.isAdmin() && player.hasPermission(plugin.adminPerms)) {
                        return partyCommand.getArguments(player, command, alias, args);
                    }
                }
            }
        }

        return null;
    }
}
