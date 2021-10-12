package me.whitetiger.partygames.games;

import com.google.common.collect.Sets;
import me.whitetiger.partygames.games.helper.AGames;
import me.whitetiger.partygames.games.helper.GameType;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class Trampoline extends AGames {
    public Trampoline() {
        super(GameType.Trampoline);
    }

    private final World world = plugin.getServer().getWorld("fishSlap");
    private final Location testLocation = new Location(world, -58, 135, 38);

    private List<BukkitRunnable> runnables = new ArrayList<>();

    private BukkitRunnable jumpTask = null;
    private BukkitRunnable pointsTask = null;
    private BukkitRunnable endTask = null;

    private final List<FallingBlock> spawnedFallingBlocks = new ArrayList<>();

    @Override
    public void start() {
        startListening();
        register();

        runnables.addAll(Arrays.asList(jumpTask, pointsTask, endTask));
    }

    @Override
    public void stop() {
        super.stop();

        stopTasks(runnables);
    }

    private void register() {

        spawnFallingBlock(Material.ORANGE_WOOL, testLocation);

        Consumer<Integer> countDownConsumer = (currentTimer) -> {
            players.forEach((player) -> {
                player.sendMessage("Starting in " + currentTimer);
            });
        };

        Runnable endCountDownTask = () -> {
            endTask = new BukkitRunnable() {
                @Override
                public void run() {
                    end();
                }
            };
            endTask.runTaskLater(plugin, 15 * 20);

            jumpTask = new BukkitRunnable() {
                
                final Set<Material> floorMaterials = Sets.newHashSet(Material.BLACK_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS);
                
                @Override
                public void run() {
                    for (Player player : players) {
                        
                        if (!floorMaterials.contains(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType())) continue;
                        
                        Vector direction = player.getEyeLocation().getDirection();

                        if (direction.getY() <= 1) {
                            direction.setY(1);
                        }

                        direction.multiply(new Vector(1.2, 1.7, 1.2));

                        player.setVelocity(new Vector(0, 0, 0));
                        player.setVelocity(direction);

                    }
                }
            };
            jumpTask.runTaskTimerAsynchronously(plugin, 10, 10);

            pointsTask = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : players) {
                        for (Entity entity : spawnedFallingBlocks) {

                            if (entity.getLocation().getBlock().getLocation() != player.getLocation().getBlock().getLocation()) continue;

                            spawnedFallingBlocks.remove(entity);

                            entity.remove();
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 1);

                        }
                    }
                }
            };

            pointsTask.runTaskTimer(plugin, 1, 1);
        };

        this.startTimer(10, countDownConsumer, endCountDownTask);
    }

    private void end() {
        stop();
    }

    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        if (!players.contains(player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }


    }

    private void spawnFallingBlock(Material material, Location location) {
        FallingBlock fallingBlock = world.spawnFallingBlock(location, Bukkit.createBlockData(material));

        fallingBlock.setGravity(false);
        fallingBlock.setPersistent(false);
        fallingBlock.setInvulnerable(true);

        spawnedFallingBlocks.add(fallingBlock);
    }

}
