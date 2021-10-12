package me.whitetiger.partygames.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PartyGameRuleCommand extends PartyCommand{

    private static final HashMap<String, Object> optionsMap = new HashMap<>();

    public PartyGameRuleCommand() {
        super("gamerule", Arrays.asList("rule"),true);

        optionsMap.put("ChickenRunMultiply", "0.6");

        optionsMap.forEach((option, object) -> {
            System.out.println("Option: " + option + ", Value: " + object.getClass().cast(object).toString());
        });
    }

    @Override
    void onCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Please add more arguments");
        }

        if (optionsMap.containsKey(args[1])) {
            optionsMap.put(args[1], args[2]);
            player.sendMessage(ChatColor.GREEN + "Value set!");
            System.out.println(optionsMap);
        } else {
            player.sendMessage(ChatColor.RED + "Please enter a valid key!");
        }
    }

    @Override
    List<String> getArguments(Player player, Command command, String alias, String[] args) {
        if (args.length <= 2) {
            return new ArrayList<>(optionsMap.keySet());
        }
        return null;
    }

    public static HashMap<String, Object> getOptionsMap() {
        return optionsMap;
    }
}
