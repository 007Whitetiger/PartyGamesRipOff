package me.whitetiger.partygames.games;

import me.whitetiger.partygames.ScoreHelper;
import me.whitetiger.partygames.games.helper.AGames;
import me.whitetiger.partygames.games.helper.GameType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.function.Consumer;

public class CannonPainters extends AGames {
    public CannonPainters() {
        super(GameType.CannonPainters);
    }
    
    private BukkitRunnable endTask;
    private final BukkitRunnable tickTask = new BukkitRunnable() {

        @Override
        public void run() {
            List<Player> scores = calculateScore();

            Player winner = scores.get(0);
            Player second = scores.get(1);
            Player third = scores.get(2);

            updateLeaderBoardScoreBoard(scoreBoard, playerPoints, winner, second, third);

        }
    };

    private final List<Material> _possibleColors = Arrays.asList(Material.BLACK_WOOL, Material.BLACK_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL, Material.CYAN_WOOL, Material.GRAY_WOOL, Material.GREEN_WOOL, Material.LIGHT_BLUE_WOOL);
    private final HashMap<Player, Material> playerColors = new HashMap<>();
    private final HashMap<Player, Integer> playerPoints = new HashMap<>();

    private final World world = Bukkit.getWorld("fishSlap");
    private final Location leftDownBoard = new Location(world, -249, 123, 241);
    private final Location rightUpBoard = new Location(world, -249, 142, 211);

    private ScoreHelper scoreBoard;


    @Override
    public void start() {
        startListening();
        register();
    }

    @Override
    public void stop() {
        super.stop();
        for (int y=leftDownBoard.getBlockY(); y<=rightUpBoard.getBlockY(); y++) {
            for (int z=leftDownBoard.getBlockZ(); z>=rightUpBoard.getBlockZ(); z--) {
                Block boardBlock = world.getBlockAt(leftDownBoard.getBlockX(), y, z);
                boardBlock.setType(Material.WHITE_WOOL);
            }
        }

        players.forEach(player -> {
            player.getInventory().clear();
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        });

        plugin.getPlayerStunner().removePlayers(players);

        stopTasks(endTask, tickTask);
    }

    static void stopTasks(BukkitRunnable endTask, BukkitRunnable tickTask) {
        if (endTask != null && !endTask.isCancelled()) {
            endTask.cancel();
        }
        if (tickTask != null && !tickTask.isCancelled()) {
            tickTask.cancel();
        }
    }

    private void register() {
        
        plugin.getPlayerStunner().addPlayers(players);

        ScoreHelper timeWaitingScoreboard = createWaitingLeaderBoard(10);


        for (int i = 0; i < players.size(); i++) {
            Player tempPlayer = players.get(i);
            playerColors.put(tempPlayer, _possibleColors.get(i));
            playerPoints.put(tempPlayer, 0);
            timeWaitingScoreboard.addToPlayer(tempPlayer);
        }

        Consumer<Integer> timeConsumer = (currentTime) -> {
            updateWaitingLeaderBoard(timeWaitingScoreboard, currentTime);
            players.forEach((player) -> {
                player.sendMessage("Starting in " + currentTime);
            });
        };
        Runnable endCountDownTask = () -> {
            endTask = new BukkitRunnable() {
                @Override
                public void run() {
                    end();
                }
            };
            endTask.runTaskLater(plugin, 10 * 20);
            System.out.println("endTaskStarted");
            plugin.getPlayerStunner().removePlayers(players);
            startGame();
            tickTask.runTaskTimerAsynchronously(plugin, 5, 5);
        };

        startTimer(10, timeConsumer, endCountDownTask);

        

        
    }
    
    private void startGame() {

        ScoreHelper playerBoard = createDefaultLeaderBoardScoreboard();

        scoreBoard = playerBoard;


        for (Player player : players) {
            player.getInventory().clear();
            scoreBoard.addToPlayer(player);
            for (int i=0; i <= 8; i++) {
                player.getInventory().addItem(new ItemStack(Material.EGG, 64));
            }
        }
    }
    
    private void end() {
        System.out.println("Ending CannonPainters Game");

        tickTask.cancel();
        List<Player> scores = calculateScore();

        System.out.println(playerPoints);

        Player winner = scores.get(0);
        Player second = scores.get(1);
        Player third = scores.get(2);


        String message = defaultEndMessage(winner, second, third);
        players.forEach(player -> {
            player.sendMessage(message);
        });

        setPlayerPoints(winner, second, third);
        
        stop();
    }

    private List<Player> calculateScore() {

        playerPoints.forEach((player, integer) -> {
            playerPoints.put(player, 0);
        });

        for (int y=leftDownBoard.getBlockY(); y<=rightUpBoard.getBlockY(); y++) {
            for (int z=leftDownBoard.getBlockZ(); z>=rightUpBoard.getBlockZ(); z--) {
                Block boardBlock = world.getBlockAt(leftDownBoard.getBlockX(), y, z);
                if (boardBlock.getType() == Material.WHITE_WOOL) continue;
                playerColors.forEach((p, mat) -> {
                    if (boardBlock.getType() == mat) {
                        playerPoints.put(p, playerPoints.get(p) + 1);
                    }
                });
            }
        }

        return calculateStanding(playerPoints);
    }

    @EventHandler
    public void onEggThrow(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile.getType() != EntityType.EGG)

        if (!(projectile.getShooter() instanceof Player)) return;

        Player player = (Player) projectile.getShooter();

        if (!players.contains(player)) return;

        projectile.setMetadata("CannonPainters", new FixedMetadataValue(plugin, "YES"));

        player.getInventory().getItemInMainHand().setAmount(64);

    }

    @EventHandler
    public void stopHatching(PlayerEggThrowEvent event) {

        Egg projectile = event.getEgg();

        if (!(projectile.getShooter() instanceof Player)) return;

        Player player = (Player) projectile.getShooter();

        if (!players.contains(player)) return;

        event.setHatching(false);
    }

    @EventHandler
    public void onEggLand(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile.getType() != EntityType.EGG) return;

        if (!(projectile.getShooter() instanceof Player)) return;

        Player player = (Player) projectile.getShooter();

        if (!players.contains(player)) return;

        if (projectile.getLocation().getBlockX() != -248)

        System.out.println(projectile.toString());

        if (!projectile.hasMetadata("CannonPainters")) return;

        Block eggBlock = event.getHitBlock();
        assert eggBlock != null;
        if (!eggBlock.getType().toString().toLowerCase().contains("wool")) return;

        List<BlockFace> verticalFaces = new ArrayList<>(Arrays.asList(BlockFace.DOWN, BlockFace.UP, BlockFace.SELF));
        List<BlockFace> horizontalFaces = new ArrayList<>(Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.SELF));

        for (BlockFace vertical : verticalFaces) {

            Block tempBlock = eggBlock.getRelative(vertical);
            for (BlockFace horizontal : horizontalFaces) {
                Block block = tempBlock.getRelative(horizontal);
                if (!eggBlock.getType().toString().toLowerCase().contains("wool"))continue;
                block.setType(playerColors.get(player));
            }
        }


    }
}
