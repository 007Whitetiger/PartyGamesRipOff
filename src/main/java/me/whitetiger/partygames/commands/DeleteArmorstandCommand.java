package me.whitetiger.partygames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeleteArmorstandCommand extends PartyCommand {

    public DeleteArmorstandCommand() {
        super("deletearmor", new ArrayList<>(Arrays.asList("deletearmorstand")), true);
    }

    @Override
    void onCommand(Player player, String[] args) {
        Location playerLocation = player.getLocation();

        List<Entity> nearbyEntities = player.getNearbyEntities(2, 2, 2);

        nearbyEntities.forEach(entity -> {
            if (playerLocation.distance(entity.getLocation()) <= 2) {
                entity.remove();
            }
        });

        player.sendMessage(ChatColor.GREEN + "Deleted ArmorStands!");
    }

    @Override
    List<String> getArguments(Player player, Command command, String alias, String[] args) {
        return null;
    }
}
