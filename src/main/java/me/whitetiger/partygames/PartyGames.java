package me.whitetiger.partygames;

import me.whitetiger.partygames.commands.CommandHandler;
import me.whitetiger.partygames.games.helper.AGames;
import me.whitetiger.partygames.games.helper.GameType;
import me.whitetiger.partygames.utils.StopPlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PartyGames extends JavaPlugin implements Listener {

    private static PartyGames Instance;
    private List<Player> players = new ArrayList<>();

    private StopPlayerListener playerStunner;

    public final String adminPerms = "partygames.admin";


    @Override
    public void onEnable() {
        Instance = this;

        Objects.requireNonNull(getCommand("party")).setExecutor(new CommandHandler());

        playerStunner = new StopPlayerListener();

        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(this ,this);
        pm.registerEvents(playerStunner, this);
        pm.registerEvents(new ServerEvents(this), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static PartyGames getInstance() {
        return Instance;
    }

    public List<Player> getPlayers() {
        return players;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("ServerName", "dummy", "TestServer");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score onlineName = obj.getScore(ChatColor.GRAY + ">> Online");
        onlineName.setScore(15);
        player.setScoreboard(board);
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
    }

    public void addCurrentOnlinePlayers() {
        players = new ArrayList<>(getServer().getOnlinePlayers());
        System.out.println(players);
    }

    public AGames getGameByName(String name) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> gameClass;


        gameClass = Class.forName("me.whitetiger.partygames.games." + name);

        return (AGames) gameClass.newInstance();

    }

    /**
     * Restart a game with the players in this game
     * @param game Game to restart
     */
    public void restartGame(AGames game) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        game.stop();

        game = getGameByName(game.getGameType().getName());

        game.start();
    }

    public void stopGame(AGames game) {
        game.stop();
    }

    public StopPlayerListener getPlayerStunner() {
        return playerStunner;
    }
}
