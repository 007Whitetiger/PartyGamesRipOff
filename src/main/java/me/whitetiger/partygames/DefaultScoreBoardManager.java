package me.whitetiger.partygames;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;

import net.md_5.bungee.api.ChatColor;

public class DefaultScoreBoardManager extends BukkitRunnable {

    private final PartyGames plugin;
    private final HashMap<Player, ScoreHelper> scoreHelperMap = new HashMap<>();

    public DefaultScoreBoardManager(PartyGames plugin) {
        this.plugin = plugin;
    }
 
    @Override
    public void run() {
        List<Player> players = plugin.getPlayers();
        
        players.forEach((player) -> {
            Session playerSession = Session.getSession(player);
            if (playerSession.isInGame) {
                scoreHelperMap.remove(player);
            } else {
                ScoreHelper scoreHelper;
                if (!scoreHelperMap.containsKey(player)) {
                    scoreHelper = ScoreHelper.createScore();
                    scoreHelperMap.put(player, scoreHelper);
                    scoreHelper.addToPlayer(player);
                } else {
                    scoreHelper = scoreHelperMap.get(player);
                }
                scoreHelper.setTitle(ChatColor.GOLD + "PartyGames");
                scoreHelper.setSlot(0, ChatColor.YELLOW +  "Score: " + ChatColor.GOLD +  playerSession.points);
            }
        });
    }
    
}
