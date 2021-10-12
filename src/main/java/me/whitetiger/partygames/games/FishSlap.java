package me.whitetiger.partygames.games;

import me.whitetiger.partygames.games.helper.AGames;
import me.whitetiger.partygames.games.helper.GameType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FishSlap extends AGames {

    private final HashMap<Player, Integer> points = new HashMap<>();

    private final World world = plugin.getServer().getWorld("fishSlap");

    private List<Location> fishSpawnLocations = new ArrayList<>();

    private BukkitRunnable pointsTask;
    private BukkitRunnable timerTask;

    public FishSlap() {
        super(GameType.FishSlap);
    }


    public void start() {
        startListening();
        giveItems();
        startTask();
    }

    public void giveItems() {
        ItemStack slapper = new ItemStack(Material.TROPICAL_FISH);
        ItemMeta slapperMeta = slapper.getItemMeta();
        slapperMeta.addEnchant(Enchantment.KNOCKBACK, 3, true);
        slapperMeta.setDisplayName(ChatColor.GOLD + "The Fish Of Knockback");
        slapper.setItemMeta(slapperMeta);

        for (Player player : players) {
            player.getInventory().clear();
            player.getInventory().addItem(slapper);
            points.put(player, 0);
        }
    }

    public void startTask() {
        pointsTask = new BukkitRunnable() {

            @Override
            public void run() {
                for (Player player : players) {
                    if (getBlock(player).getType() == Material.RED_WOOL) {
                        System.out.println(points);
                        points.put(player, points.get(player) + 1);
                    }
                }
            }

            public Block getBlock(Player player) {
                Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

                while(block.getType() == Material.AIR) {
                    block = block.getRelative(BlockFace.DOWN);
                }

                return block;
            }
        };

        pointsTask.runTaskTimerAsynchronously(plugin, 20, 20);

        timerTask = new BukkitRunnable() {

            @Override
            public void run() {
                pointsTask.cancel();
                List<Player> players = new ArrayList<>();
                int winningPoints = 0;
                for (Player player : points.keySet()) {
                    int currentPoints = points.get(player);
                    if (currentPoints == winningPoints) {
                        players.add(player);
                    } else if (points.get(player) >= winningPoints) {
                        players = new ArrayList<>(Arrays.asList(player));
                    }
                }
                endGame(players);
            }
        };

        timerTask.runTaskLater(plugin, 20 * 10);
    }

    public void endGame(List<Player> winners) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ChatColor.GOLD).append("------Winners: -------\n1st. ");
        for (Player winner : winners) {
            winner.sendMessage(ChatColor.GREEN + "YOU WON!");
            stringBuilder.append(ChatColor.GOLD).append(winner.getName()).append(ChatColor.RED).append(" and ");
        }
        stringBuilder.delete(stringBuilder.length()- 5, stringBuilder.length());

        String message = stringBuilder.toString();
        System.out.println(message);

        for (Player player : players) {
            player.sendMessage(message);
        }

    }
}
