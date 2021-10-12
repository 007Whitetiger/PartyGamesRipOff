package me.whitetiger.partygames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateArmorstandCommand extends PartyCommand {
    public CreateArmorstandCommand() {
        super("armorstand", true);
    }

    @Override
    void onCommand(Player player, String[] args) {

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "");
        }

        Location playerLocation = player.getLocation();

        List<String> listArgs = new ArrayList<>(Arrays.asList(args));

        listArgs.remove(0);

        String name = ChatColor.translateAlternateColorCodes('&', String.join(" ", listArgs));

        ArmorStand armorStand = playerLocation.getWorld().spawn(playerLocation, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setCustomName(name);
        System.out.println(name);
        armorStand.setCustomNameVisible(false);

        player.sendMessage("Armorstand has been placed with name: " + name);
    }

    @Override
    List<String> getArguments(Player player, Command command, String alias, String[] args) {
        return null;
    }
}
