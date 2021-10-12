package me.whitetiger.partygames.games;

import com.google.common.collect.Sets;
import me.whitetiger.partygames.games.helper.AGames;
import me.whitetiger.partygames.games.helper.GameType;
import me.whitetiger.partygames.utils.MathUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;

public class HoeHoeHoe extends AGames {

    private final World world = plugin.getServer().getWorld("fishSlap");

    private boolean started = false;

    private final HashMap<Player, Integer> pointsHashMap = new HashMap<>();
    private final HashMap<Player, Material> playerColors = new HashMap<>();
    
    private final List<Material> _possibleColors = Arrays.asList(Material.BLACK_WOOL, Material.BLACK_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL, Material.CYAN_WOOL, Material.GRAY_WOOL, Material.GREEN_WOOL, Material.LIGHT_BLUE_WOOL);

    private final HashMap<Block, Material> changedBlock = new HashMap<>();

    public HoeHoeHoe() {
        super(GameType.HoeHoeHoe);
    }

    private BukkitRunnable endTask;

    @Override
    public void start() {
        startListening();
        register();
    }

    @Override
    public void stop() {
        super.stop();
        reset();
        if (endTask != null && !endTask.isCancelled()) {
            endTask.cancel();
        }
    }

    private void register() {
        for (int i = 0; i < players.size(); i++) {
            pointsHashMap.put(players.get(i), 0);
            playerColors.put(players.get(i), _possibleColors.get(i));
            
        }

        Consumer<Integer> countDownConsumer = (currentTime) -> {
            players.forEach((player) -> {
                player.sendMessage("Starting in " + currentTime);
            });
            
        };

        Runnable endCountDownTask = () -> {
            players.forEach((player) -> {
                player.teleport(new Location(world, -71 + MathUtils.getRandomIntUnderNumber(20), 113, 142 + MathUtils.getRandomIntUnderNumber(20)));

            });
            
            started = true;
            endTask = new BukkitRunnable() {
                @Override
                public void run() {
                    end();
                }
            };
            endTask.runTaskLater(plugin, 60 * 20);
        };

        this.startTimer(10, countDownConsumer, endCountDownTask);
    }

    private Player getPlayer(List<Player> winners, int index) {
        try {
            return winners.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private void end() {
        List<Integer> points = new ArrayList<>();
        List<Player> winners = new ArrayList<>();
        for (Player player : pointsHashMap.keySet()) {
            if (winners.isEmpty()) {
                points.add(pointsHashMap.get(player));
                winners.add(player);
            }
            int point = pointsHashMap.get(player);
            for (int i=0; i < points.size(); i++) {
                if (point > points.get(i)) {
                    points.add(i, point);
                    winners.add(i, player);
                }
            }
        }


        String message = AGames.defaultEndMessage(getPlayer(winners, 0), getPlayer(winners, 1), getPlayer(winners, 2));
        players.forEach(player -> {
            player.sendMessage(message);
            player.setGameMode(GameMode.SPECTATOR);
        });

        setPlayerPoints(getPlayer(winners, 0), getPlayer(winners, 1), getPlayer(winners, 2));

        stop();
    }

    private void reset() {
        for (Block block : changedBlock.keySet()) {
            block.setType(changedBlock.get(block));
        }
    }


    private final Set<Material> allowedBlocks = Sets.newHashSet(Material.GRASS_BLOCK, Material.MYCELIUM);
    private final Set<BlockFace> blockFaces = Sets.newHashSet(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
    @EventHandler
    public void onClickBlock(PlayerInteractEvent event) {

        event.setCancelled(true);

        Player player = event.getPlayer();
        Material playerColor = playerColors.get(player);

        if (player.getInventory().getItemInMainHand().getType() != Material.DIAMOND_HOE) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (!allowedBlocks.contains(block.getType())) return;

        boolean relative = false;

        for (BlockFace blockFace : blockFaces) {
            if (block.getRelative(blockFace).getType() == playerColor) {
                relative = true;
            }
        }

        if (!relative && pointsHashMap.get(player) == 0) {
            relative = true;
        }

        if (!relative) return;

        changedBlock.put(block, block.getType());
        pointsHashMap.put(player, pointsHashMap.get(player) + 1);

        block.setType(playerColor);

    }

}
