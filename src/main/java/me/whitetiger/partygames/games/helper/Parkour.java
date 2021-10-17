package me.whitetiger.partygames.games.helper;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import me.whitetiger.partygames.ScoreHelper;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Parkour extends AGames {

    public Parkour(GameType gameType, Location spawnLocation, Location checkpointLocation, List<Location> fenceLocations) {
        super(gameType);
        this.spawnLocation = spawnLocation;
        this.checkpointLocation = checkpointLocation;
        this.fenceLocations = fenceLocations;
        this.startX = this.spawnLocation.getBlockX();
    }

    public enum Checkpoint {
        SPAWN,
        CHECKPOINT
    }

    private class PlayerParkour {

        private final float time;
        private final Player player;

        public PlayerParkour(Player player, float time) {
            this.time = time;
            this.player = player;
            playerParkourHashMap.put(player, this);
        }

        public float getTime() {
            return time;
        }

        public String getName() {
            return player.getName();
        }
    }

    private final Location spawnLocation;
    private final Location checkpointLocation;

    private BukkitRunnable updateScoreBoardRunnable;
    private BukkitRunnable endTask;

    private long startTimeMs;
    private int startX;

    private final List<Location> fenceLocations;

    private final HashMap<Player, Checkpoint> playerCheckpoints = new HashMap<>();

    private final HashMap<Player, PlayerParkour> playerParkourHashMap = new HashMap<>();

    private PlayerParkour winner = null;
    private PlayerParkour second = null;
    private PlayerParkour third = null;
    private final List<PlayerParkour> finished = new ArrayList<>();

    @Override
    public void start() {
        startListening();
        registerPlayers();
    }

    @Override
    public void stop() {
        super.stop();
        reset();

        stopTasks(Arrays.asList(updateScoreBoardRunnable, endTask));
    }

    public void registerPlayers() {
        System.out.println(players);
        PotionEffect potionEffect = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1);
        for (Player player : players) {
            player.teleport(spawnLocation);
            player.addPotionEffect(potionEffect);
            player.setGameMode(GameMode.ADVENTURE);
            for (Player player1 : players) {
                if (player1 != player) {
                    player.hidePlayer(plugin, player1);
                }
            }
        }

        Consumer<Integer> countDownTask = (currentTime) -> {
            players.forEach((player) -> {
                player.sendMessage("Starting in " + currentTime);
            });
        };

        Runnable endCountDownTask = () -> {
            for (Location location : fenceLocations) {
                location.getBlock().setType(Material.AIR);
            }
            
            startTimeMs = System.currentTimeMillis();
            endTask = new BukkitRunnable() {
                @Override
                public void run() {
                    end();
                }
            };
            endTask.runTaskLater(plugin, 50 * 20);
            ScoreHelper scoreHelper = createDefaultLeaderBoardScoreboard();
            players.forEach((player) -> {
                scoreHelper.addToPlayer(player);
            });
            this.updateScoreBoardRunnable = new BukkitRunnable(){
                

                @Override
                public void run() {
                    HashMap<Player, Integer> playerProgressMap = new HashMap<>();
                    players.forEach((player -> {
                        playerProgressMap.put(player, Math.abs(player.getLocation().getBlockX()) - Math.abs(startX));
                    }));
                    updateLeaderBoardScoreBoard(scoreHelper, playerProgressMap);
                    
                }
                
            };
            this.updateScoreBoardRunnable.runTaskTimer(plugin, 0, 2);
        };

        this.startTimer(10, countDownTask, endCountDownTask);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (players.contains(player)) {

            if (Objects.requireNonNull(event.getTo()).getBlockY() <= 119) {
                respawn(player);
            }

            switch (Objects.requireNonNull(event.getTo()).getBlock().getType()) {
                case LAVA:
                    respawn(player);
                    break;
                case OAK_PRESSURE_PLATE: //Jungle Parkour
                case STONE_PRESSURE_PLATE: //Lava Parkour
                    // Checkpoint
                    if (playerCheckpoints.containsKey(player)) return;
                    playerCheckpoints.put(player, Checkpoint.CHECKPOINT);
                    player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "CHECKPOINT!");
                    break;
                case LIGHT_WEIGHTED_PRESSURE_PLATE:
                    // END
                    if (playerParkourHashMap.containsKey(player)) return;
                    System.out.println(System.currentTimeMillis() - startTimeMs);
                    float time = System.currentTimeMillis() - startTimeMs;
                    System.out.println(time);

                    PlayerParkour playerParkour = new PlayerParkour(player, time);

                    if (winner == null) {
                        winner=playerParkour;
                    }
                    else if (second == null) {
                        second =playerParkour;
                    }
                    else if (third == null) {
                        third=playerParkour;
                    }
                    finished.add(playerParkour);

                    player.setGameMode(GameMode.SPECTATOR);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    for (Player receiver : players) {
                        receiver.sendMessage(ChatColor.GRAY + player.getName() + ChatColor.YELLOW + " finished the parkour!");
                    }
                    break;
                default: {}

            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)) return;

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        event.setCancelled(true);

    }

    public void respawn(Player player) {
        System.out.println("L" + player.getName());
        switch (playerCheckpoints.getOrDefault(player, Checkpoint.SPAWN)) {
            case SPAWN:
                player.teleport(spawnLocation);
                break;
            case CHECKPOINT:
                player.teleport(checkpointLocation);
                break;
        }
    }

    private void reset() {
        for (Player player : players) {

            player.setGameMode(GameMode.SPECTATOR);
            player.removePotionEffect(PotionEffectType.SPEED);

            for (Player player1 : players) {
                player.showPlayer(plugin, player1);
            }
        }

        for (Location location : fenceLocations) {
            switch (getGameType()) {
                case LavaParkour:
                    location.getBlock().setType(Material.STONE_BRICK_WALL);
                    break;
                case JungleParkour:
                    location.getBlock().setType(Material.MOSSY_COBBLESTONE_WALL);
                    break;
                default: {
                    plugin.getLogger().severe("HOW THE FUCK DID THIS HAPPEN. Gametype: " + getGameType().getName() + "In Parkour.java");
                    new Throwable().printStackTrace();
                }
            }
        }
    }

    public void end() {
        StringBuilder baseMessageBuilder = new StringBuilder().append(ChatColor.GOLD).append("------Winners------\n");

        Player wPlayer = null;
        Player sPlayer = null;
        Player tPlayer = null;

        if (winner != null) {
            wPlayer = winner.player;
            baseMessageBuilder.append(ChatColor.GOLD).append("1st: ").append(ChatColor.GRAY).append(winner.getName()).append(ChatColor.GOLD).append(" : ").append(winner.getTime()).append("s").append("\n");
        } else {
            baseMessageBuilder.append(ChatColor.GOLD).append("1st: ").append(ChatColor.RED).append("N/A").append("\n");
        }

        if (second != null) {
            sPlayer = second.player;
            baseMessageBuilder.append(ChatColor.WHITE).append("2nd: ").append(ChatColor.GRAY).append(second.getName()).append(ChatColor.GOLD).append(" : ").append(second.getTime()).append("s").append("\n");
        } else {
            baseMessageBuilder.append(ChatColor.WHITE).append("2nd: ").append(ChatColor.RED).append("N/A").append("\n");
        }

        if (third != null) {
            tPlayer = third.player;
            baseMessageBuilder.append(ChatColor.GRAY).append("3rd: ").append(third.getName()).append(ChatColor.GOLD).append(" : ").append(second.getTime()).append("s").append("\n");
        } else {
            baseMessageBuilder.append(ChatColor.GRAY).append("3rd: ").append(ChatColor.RED).append("N/A").append("\n");
        }
        baseMessageBuilder.append(ChatColor.RESET);

        String baseMessage = baseMessageBuilder.toString();

        for (Player player : players) {
            PlayerParkour playerParkour = playerParkourHashMap.get(player);
            player.sendMessage(baseMessage);
            if (finished.contains(playerParkour)) {
                player.sendMessage(ChatColor.GREEN + "You Finished!");
            } else {
                player.sendMessage(ChatColor.RED + "You didn't finish!");
            }
        }

        setPlayerPoints(wPlayer, sPlayer, tPlayer);

        stop();
    }
}
