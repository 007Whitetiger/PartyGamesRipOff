package me.whitetiger.partygames.games;

import me.whitetiger.partygames.commands.PartyGameRuleCommand;
import me.whitetiger.partygames.games.helper.AGames;
import me.whitetiger.partygames.games.helper.GameType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;
import java.util.Random;


public class ChickenRun extends AGames {
    public ChickenRun() {
        super(GameType.ChickenRun);
    }

    private final Random random = new Random();
    private final double multiplyEggVelocity = Double.parseDouble((String) PartyGameRuleCommand.getOptionsMap().get("ChickenRunMultiply"));

    private final World world = Bukkit.getWorld("ChickenRun");
    private final Location eggSpawnLocation = new Location(world, 27, 4, 2);

    private Egg currentEgg;

    private BukkitRunnable tickTask = new BukkitRunnable() {
        @Override
        public void run() {
            if (currentEgg.getLocation().getBlockY() >= 9) {
                currentEgg.remove();
                currentEgg = world.spawn(eggSpawnLocation, Egg.class);
                eggVelocity(currentEgg, true);
            }
        }
    };

    @Override
    public void start() {
        startListening();
        Egg egg = world.spawn(eggSpawnLocation, Egg.class);
        eggVelocity(egg, true);
        tickTask.runTaskTimer(plugin, 2, 2);
    }

    @Override
    public void stop() {
        super.stop();
        List<Creeper> creepers = (List<Creeper>) world.getEntitiesByClass(Creeper.class);
        List<Entity> eggs = (List<Entity>) world.getEntitiesByClasses(Egg.class);

        creepers.forEach(Entity::remove);
        eggs.forEach(Entity::remove);

        if (!tickTask.isCancelled()) {
            tickTask.cancel();
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        EntityType entityType = event.getEntityType();

        if (entityType != EntityType.EGG) return;

        Entity entity = event.getHitEntity();
        Block blockHit = event.getHitBlock();

        if (entity != null) {
            entity.sendMessage("HEY");
        }

        eggVelocity(world.spawn(projectile.getLocation(), Egg.class), blockHit == null || (blockHit.getType() != Material.AIR && !(blockHit.getLocation().getBlockY() >=4)));
    }

    @EventHandler
    public void eggSpawnEvent(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.EGG) return;

        event.setCancelled(true);
    }

    private void eggVelocity(Egg egg, boolean y) {
        if (y) {
            egg.setVelocity(new Vector(randomMiseMinus(Math.random()), Math.random(), randomMiseMinus(Math.random())).multiply(multiplyEggVelocity));
        }else {
            egg.setVelocity(new Vector(randomMiseMinus(Math.random()), 0.1, randomMiseMinus(Math.random())).multiply(multiplyEggVelocity));
        }
        currentEgg = egg;
    }

    private double randomMiseMinus(double val) {
        boolean randBoolean = random.nextBoolean();

        if (randBoolean) {
            return val;
        } else {
            return - + val;
        }
    }
}
