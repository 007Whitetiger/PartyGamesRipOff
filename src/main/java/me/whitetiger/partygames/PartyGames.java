package me.whitetiger.partygames;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.whitetiger.partygames.commands.CommandHandler;
import me.whitetiger.partygames.games.helper.AGames;
import me.whitetiger.partygames.utils.StopPlayerListener;

public class PartyGames extends JavaPlugin implements Listener {

    private static PartyGames Instance;
    private List<Player> players = new ArrayList<>();
    public boolean debugEnabled = true;

    private StopPlayerListener playerStunner;

    public final String adminPerms = "partygames.admin";


    @Override
    public void onEnable() {
        Instance = this;
        if (this.debugEnabled) {
            this.addCurrentOnlinePlayers();
        }

        Objects.requireNonNull(getCommand("party")).setExecutor(new CommandHandler());

        playerStunner = new StopPlayerListener();
        new DefaultScoreBoardManager(this).runTaskTimer(this, 10, 10);

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

    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }
    
    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void addCurrentOnlinePlayers() {
        players = new ArrayList<>(getServer().getOnlinePlayers());
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
