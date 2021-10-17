package me.whitetiger.partygames.games;

import me.whitetiger.partygames.ScoreHelper;
import me.whitetiger.partygames.games.helper.AGames;
import me.whitetiger.partygames.games.helper.GameType;
import me.whitetiger.partygames.utils.Cuboid;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PigFishing extends AGames {
    public PigFishing() {
        super(GameType.PigFishing);
    }

    private BukkitRunnable endTask;
    private BukkitRunnable tickTask;
    private BukkitRunnable pistonRunnable = null;

    private World world = Bukkit.getWorld("fishSlap");

    private final Location pigSpawnLocation = new Location(world, -168, 143, 332);

    private final HashMap<Player, Integer> pointsMap = new HashMap<>();
    private final HashMap<Character, Player> characterPlayerHashMap = new HashMap<>();

    private final List<ArmorStand> spawnsUsed = new ArrayList<>();

    @Override
    public void start() {
        startListening();
        register();
    }

    @Override
    public void stop() {
        super.stop();

        world.getEntities().forEach(entity -> {
            if (entity.getType() == EntityType.PIG) {
                entity.remove();
            }
        });
        
        if (tickTask != null) {
            tickTask.cancel();
        }
        if (endTask != null) {
            endTask.cancel();
        }
    }

    private void register() {

        players.forEach(player -> {
            pointsMap.put(player, 0);

            List<ArmorStand> entities = new ArrayList<>(world.getEntitiesByClass(ArmorStand.class));
            for (ArmorStand armorStand : entities) {
                if (Objects.requireNonNull(armorStand.getCustomName()).contains("PigFishing") && !spawnsUsed.contains(armorStand) && armorStand.getLocation().getBlockY() != 120) {
                    System.out.println(armorStand);
                    player.teleport(armorStand.getLocation());
                    spawnsUsed.add(armorStand);
                    char number = armorStand.getCustomName().charAt(10);
                    characterPlayerHashMap.put(number, player);
                    break;
                }
            };
        });

        Consumer<Integer> countDownConsumer = (currentTime) -> {
            players.forEach((player) -> {
                player.sendMessage("Starting in " + currentTime);
            });
        };

        Runnable endCountDownRunnable = () -> {
            endTask = new BukkitRunnable() {
                @Override
                public void run() {
                    end();
                }
            };
            endTask.runTaskLater(plugin, 30 * 20);
            startTickTask();
            startGame();

        };

        this.startTimer(10, countDownConsumer, endCountDownRunnable);
    }

    private void end() {
        List<Player> scores = calculateStanding(pointsMap);
        

        Player winner = scores.get(0);
        Player second = scores.get(1);
        Player third = scores.get(2);

        String message = defaultEndMessage(winner, second, third);
        
        setPlayerPoints(winner, second, third);

        players.forEach(player -> player.sendMessage(message));

        stop();
    }

    private void startGame() {
        ScoreHelper playerBoard = createDefaultLeaderBoardScoreboard();
        this.startUpdateLeaderBoardTask(playerBoard, pointsMap);

        players.forEach(player -> {
            playerBoard.addToPlayer(player);
            player.getInventory().clear();
            player.getInventory().addItem(new ItemStack(Material.FISHING_ROD));
        });

    }

    private void activateSuperPig(Player player) {

        players.forEach(receiver -> receiver.sendMessage(ChatColor.GRAY + player.getName() + ChatColor.RED + " has caught a" + ChatColor.RED + " Super Pig"));

        Cuboid cuboid = new Cuboid(new Location(world, -182, 126, 314), new Location(world, -154, 126, 346));

        cuboid.getBlocks().forEach(block -> {
            if (block.getType().equals(Material.STICKY_PISTON)) {
                Collection<Entity> nearbyEntities = world.getNearbyEntities(block.getLocation(), 3, 10, 3);
                List<Entity> nEntitiesList = new ArrayList<>(nearbyEntities);
                Player armorStandPlayer = getPlayerFromEntities(nEntitiesList);

                if (player == armorStandPlayer) {
                    return;
                }
                block.getRelative(BlockFace.DOWN).setType(Material.REDSTONE_BLOCK);
            }
        });

        pistonRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                deactivateSuperPig();
            }
        };

        pistonRunnable.runTaskLater(plugin, 100);
    }

    private void deactivateSuperPig() {
        Cuboid cuboid = new Cuboid(new Location(world, -182, 126, 314), new Location(world, -154, 126, 346));

        cuboid.getBlocks().forEach(block -> {
            if (block.getType().equals(Material.STICKY_PISTON)) {
                block.getRelative(BlockFace.DOWN).setType(Material.SPRUCE_PLANKS);
            }
        });
    }

    private void checkPigs(List<Pig> pigs) {

        pigs.forEach(pig -> {
            if (pig.getLocation().getBlockY() == 120) {
                Location deathLocation = pig.getLocation();
                pig.remove();

                List<Entity> nearbyEntities = pig.getNearbyEntities(3, 1, 3);

                Player player = getPlayerFromEntities(nearbyEntities);
                if (player != null) {
                    pointsMap.put(player, pointsMap.get(player) + 1);
                } else {
                    return;
                }



                

                if (Objects.equals(pig.getCustomName(), ChatColor.RED + "Super Pig")) {
                    activateSuperPig(player);
                }

                Firework firework = world.spawn(deathLocation, Firework.class);

                FireworkMeta fireworkMeta = firework.getFireworkMeta();

                fireworkMeta.setPower(10);
                FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withFlicker().withTrail().withColor(Color.BLUE, Color.AQUA).withFade(Color.BLACK).build();
                fireworkMeta.addEffects(effect);

                firework.setFireworkMeta(fireworkMeta);
                firework.detonate();

                for (int i=0; i<5; i++) {
                    firework = world.spawn(deathLocation, Firework.class);
                    firework.setFireworkMeta(fireworkMeta);
                    firework.detonate();
                }
            }
        });
    }

    private void spawnPig(float rand, List<Pig> pigs) {
        int maxSize = players.size() * 5;

        if (maxSize > 20) {
            maxSize = 20;
        }
        if (pigs.size() >= players.size() * 5) {
            return;
        }

        if (rand > 0.4) {
            Pig pig = world.spawn(pigSpawnLocation, Pig.class);
            pig.setSaddle(true);
            if (rand > 0.95) {
                pig.setBaby();
                pig.setCustomName(ChatColor.RED + "Super Pig");
                pig.setCustomNameVisible(true);
            }
        }
    }

    private void startTickTask() {
        tickTask = new BukkitRunnable() {

            private final Random random = new Random();

            @Override
            public void run() {

                Collection<Pig> entities = world.getEntitiesByClass(Pig.class);
                List<Pig> pigs = new ArrayList<>(entities);

                float rand = random.nextFloat();

                checkPigs(pigs);
                spawnPig(rand, pigs);


            }
        };
        tickTask.runTaskTimer(plugin, 10, 10);
    }

    private Player getPlayerFromEntities(List<Entity> entities) {
        for (Entity entity : entities) {
            if (entity instanceof ArmorStand) {
                Player temPlayer = getPlayerFromArmorstand((ArmorStand) entity);
                if (temPlayer != null) {
                    return temPlayer;
                }
            }
        }
        return null;
    }

    private Player getPlayerFromArmorstand(ArmorStand armorStand) {
        AtomicReference<Character> number = new AtomicReference<>('x');
        if (armorStand.getCustomName().contains("PigFishing")) {
            number.set(armorStand.getCustomName().charAt(10));
        }

        Player player;

        if (number.get() != 'x') {
            player =  characterPlayerHashMap.get(number.get());
            return player;
        } else {
            return null;
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location toLocation = Objects.requireNonNull(event.getTo()).clone();

        toLocation.setY(118);

        Block checkBlock = toLocation.getBlock();

        if (checkBlock.getType() == Material.TRIPWIRE) {
            event.setCancelled(true);
        }
        
    }

    @EventHandler
    public void onEntityHitEntityEvent(PlayerFishEvent event) {
        Entity entity = event.getCaught();
        FishHook fishHook = event.getHook();

        if (entity != null) {

            new BukkitRunnable() {
                @Override
                public void run() {
                    entity.setVelocity(entity.getVelocity().multiply(1.2).add(new Vector(0, 0.2, 0)));
                }
            }.runTaskLater(plugin, 1);
        } else {
            fishHook.setVelocity(fishHook.getVelocity().multiply(1.5));
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (players.contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

}
