package me.whitetiger.partygames.games.helper;

import me.whitetiger.partygames.PartyGames;
import me.whitetiger.partygames.ScoreHelper;
import me.whitetiger.partygames.Session;
import me.whitetiger.partygames.utils.ListUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
// import org.bukkit.scoreboard.DisplaySlot;
// import org.bukkit.scoreboard.Objective;
// import org.bukkit.scoreboard.Scoreboard;
// import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AGames implements Listener {

    private static final List<AGames> currentGames = new ArrayList<>();

    public PartyGames plugin;
    public List<Player> players;
    public UUID gameID = UUID.randomUUID();
    private GameType gameType;


    private BukkitRunnable startTask = null;
    private BukkitRunnable scoreLeaderBoardTask = null;

    public AGames(GameType gameType) {
        plugin = PartyGames.getInstance();
        players = plugin.getPlayers();
        this.gameType = gameType;
        currentGames.add(this);
        players.forEach((player) -> {
            Session playerSession = Session.getSession(player);
            playerSession.isInGame = true;
        });
        if (startTask != null && !startTask.isCancelled()) {
            startTask.cancel();
        }
        if (scoreLeaderBoardTask != null && !scoreLeaderBoardTask.isCancelled()) {
            scoreLeaderBoardTask.cancel();
        }
    }

    abstract public void start();

    public void stop() {
        stopListening();
        removeCurrentGame(this);

        players.forEach(player -> {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            Session playerSession = Session.getSession(player);
            playerSession.isInGame = false;
            player.getInventory().clear();
        });
    }


    public void startListening() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void stopListening() {
        HandlerList.unregisterAll(this);
    }

    public void startUpdateLeaderBoardTask(ScoreHelper scoreHelper, HashMap<Player, Integer> playerPointsMap) {
        this.startUpdateLeaderBoardTask(scoreHelper, playerPointsMap, 10);
    }

    public void startUpdateLeaderBoardTask(ScoreHelper scoreHelper, HashMap<Player, Integer> playerPointsMap, Integer tickUpdateCount) {
        this.scoreLeaderBoardTask = new BukkitRunnable() {

            @Override
            public void run() {
                updateLeaderBoardScoreBoard(scoreHelper, playerPointsMap);
            }
            
        };
        this.scoreLeaderBoardTask.runTaskTimer(plugin, 0, 5);
    }

    @Override
    public String toString() {
        return "Game: " + gameID + " Type: " + this.getClass();
    }

    public UUID getGameID() {
        return gameID;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public GameType getGameType() {
        return gameType;
    }

    public static List<AGames> getCurrentGames() {
        return currentGames;
    }

    public void removeCurrentGame(AGames game) {
        currentGames.remove(game);
    }

    public void startTimer(int time, final Consumer<Integer> timingConsumer,  final Runnable endTask) {
        ScoreHelper waitingScoreHelper = createWaitingLeaderBoard(time);
        players.forEach((player) -> {
            waitingScoreHelper.addToPlayer(player);
        });
        startTask = new BukkitRunnable() {
            int loops = time;

            @Override
            public void run() {
                updateWaitingLeaderBoard(waitingScoreHelper, loops);
                switch (loops) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 10:
                        timingConsumer.accept(loops);
                        break;
                    case 0:
                        cancel();
                        endTask.run();
                        break;
                }
                loops--;
            }
        };

        startTask.runTaskTimer(plugin, 20, 20);
    }

    public static String defaultEndMessage(Player winner, Player second, Player third) {
        StringBuilder baseMessageBuilder = new StringBuilder().append(ChatColor.GOLD).append("------Winners------\n");
        if (winner != null) {
            baseMessageBuilder.append(ChatColor.GOLD).append("1st: ").append(ChatColor.GRAY).append(winner.getName()).append("\n");
        } else {
            baseMessageBuilder.append(ChatColor.GOLD).append("1st: ").append(ChatColor.RED).append("N/A").append("\n");
        }

        if (second != null) {
            baseMessageBuilder.append(ChatColor.WHITE).append("2nd: ").append(ChatColor.GRAY).append(second.getName()).append("\n");
        } else {
            baseMessageBuilder.append(ChatColor.WHITE).append("2nd: ").append(ChatColor.RED).append("N/A").append("\n");
        }

        if (third != null) {
            baseMessageBuilder.append(ChatColor.GRAY).append("3rd: ").append(third.getName()).append(ChatColor.GOLD).append("\n");
        } else {
            baseMessageBuilder.append(ChatColor.GRAY).append("3rd: ").append(ChatColor.RED).append("N/A").append("\n");
        }
        baseMessageBuilder.append(ChatColor.RESET);

        return baseMessageBuilder.toString();
    }

    public ScoreHelper createDefaultLeaderBoardScoreboard() {

        ScoreHelper scoreHelper = ScoreHelper.createScore();

        // Scoreboard playerBoard = Bukkit.getScoreboardManager().getNewScoreboard();
        
        scoreHelper.setTitle(ChatColor.GOLD + this.getGameType().getName());


        // Objective playerGameObjective = playerBoard.registerNewObjective(this.getGameType().getName(), "Playing", ChatColor.GOLD + this.getGameType().getName());

        // playerGameObjective.getScore("Leaderboard").setScore(4);

        scoreHelper.setSlot(4, "LeaderBoard");

        // Team firstPlace;
        // Team secondPlace;
        // Team thirdPlace;

        // firstPlace = playerBoard.registerNewTeam("First");
        // firstPlace.setAllowFriendlyFire(true);
        // firstPlace.setCanSeeFriendlyInvisibles(false);
        // firstPlace.addEntry(ChatColor.BLACK + "" + ChatColor.AQUA);
        // firstPlace.setPrefix(ChatColor.GOLD + "1: " + ChatColor.GRAY + "N/A");
        // playerGameObjective.getScore(ChatColor.BLACK + "" + ChatColor.AQUA).setScore(3);
        // scoreHelper.setSlot(3, ChatColor.GOLD + "1: " + ChatColor.GRAY + "N/A");

        // secondPlace = playerBoard.registerNewTeam("Second");
        // secondPlace.setAllowFriendlyFire(true);
        // secondPlace.setCanSeeFriendlyInvisibles(false);
        // secondPlace.addEntry(ChatColor.BLACK + "" + ChatColor.GOLD);
        // secondPlace.setPrefix(ChatColor.GOLD + "2: " + ChatColor.GRAY + "N/A");
        // playerGameObjective.getScore(ChatColor.BLACK + "" + ChatColor.GOLD).setScore(2);
        
        scoreHelper.setSlot(2, ChatColor.GOLD + "2: " + ChatColor.GRAY + "N/A");

        // thirdPlace = playerBoard.registerNewTeam("Third");
        // thirdPlace.setAllowFriendlyFire(true);
        // thirdPlace.setCanSeeFriendlyInvisibles(false);
        // thirdPlace.addEntry(ChatColor.BLACK + "" + ChatColor.WHITE);
        // thirdPlace.setPrefix(ChatColor.GOLD + "3: " + ChatColor.GRAY + "N/A");
        // playerGameObjective.getScore(ChatColor.BLACK + "" + ChatColor.WHITE).setScore(1);
        // scoreHelper.setSlot(1, ChatColor.GOLD + "3: " + ChatColor.GRAY + "N/A");


        // playerGameObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        return scoreHelper;
    }

    public void updateLeaderBoardScoreBoard(ScoreHelper scoreBoard, HashMap<Player, Integer> playerPoints, Player winner, Player second, Player third) {
        try {
            scoreBoard.setSlot(3, ChatColor.GOLD + "1: " + ChatColor.GRAY + Objects.requireNonNull(winner).getName() + "  " + ChatColor.GOLD + Objects.requireNonNull(playerPoints.get(winner)));
        } catch (NullPointerException e) {
            scoreBoard.setSlot(3, ChatColor.GOLD + "1: " + ChatColor.GRAY + "N/A" + "  " + ChatColor.GOLD + "0");
        }

        try {
            scoreBoard.setSlot(2, ChatColor.GOLD + "2: " + ChatColor.GRAY + Objects.requireNonNull(second).getName() + "  " + ChatColor.GOLD + Objects.requireNonNull(playerPoints.get(second)));
        }catch (NullPointerException e) {
            scoreBoard.setSlot(2, ChatColor.GOLD + "2: " + ChatColor.GRAY + "N/A" +  "  " +  ChatColor.GOLD + "0");
        }

        try {
            scoreBoard.setSlot(1, ChatColor.GOLD + "3: " + ChatColor.GRAY + Objects.requireNonNull(third).getName()  + "  " + ChatColor.GOLD + Objects.requireNonNull(playerPoints.get(third)));
        }catch (NullPointerException e) {
            scoreBoard.setSlot(1, ChatColor.GOLD + "3: " + ChatColor.GRAY + "N/A"  + "  " +  ChatColor.GOLD + "0");
        }
    }

    public void updateLeaderBoardScoreBoard(ScoreHelper scoreBoard, HashMap<Player, Integer> pointsMap) {
        updateLeaderBoardScoreBoard(scoreBoard, pointsMap, calculateStanding(pointsMap));
    }

    public void updateLeaderBoardScoreBoard(ScoreHelper scoreBoard, HashMap<Player, Integer> playerPoints, List<Player> topPlayerList) {
        Player winner = null;
        Player second = null;
        Player third = null;
        for (int i = 0; i < topPlayerList.size(); i++) {
            switch (i) {
                case 0:
                    winner = topPlayerList.get(i);break;
                case 1:
                    second = topPlayerList.get(i);break;
                case 2:
                    third = topPlayerList.get(i);break;
            }
        }
        updateLeaderBoardScoreBoard(scoreBoard, playerPoints, winner, second, third);
        
    }


    public ScoreHelper createWaitingLeaderBoard(int startTime) {
        ScoreHelper scoreHelper = ScoreHelper.createScore();

        scoreHelper.setTitle(ChatColor.GOLD + this.getGameType().getName());
        scoreHelper.setSlot(0, ChatColor.RED + ">> Waiting: " + ChatColor.BLACK + startTime);
        return scoreHelper;
    }

    public void updateWaitingLeaderBoard(ScoreHelper scoreHelper, int time) {
        scoreHelper.setSlot(0, ChatColor.RED + ">> Waiting: " + ChatColor.BLACK + time);

    }

    public class StandingsComparator implements Comparator<Entry<Player, Integer>> {

        @Override
        public int compare(Entry<Player, Integer> firstEntry, Entry<Player, Integer> secondEntry) {
            return firstEntry.getValue().compareTo(secondEntry.getValue());
        }
        
    }

    public List<Player> calculateStanding(HashMap<Player, Integer> playerPoints) {

        StandingsComparator standingsComparator = new StandingsComparator();

        Set<Entry<Player, Integer>> playerPointsSet = playerPoints.entrySet();
        List<Entry<Player, Integer>> playerPointsList = new ArrayList<>(playerPointsSet);
        Collections.sort(playerPointsList, standingsComparator);
        List<Player> playerList = playerPointsList.stream().map((playerEntry) -> playerEntry.getKey()).collect(Collectors.toList());

        Player winner = ListUtils.getFromListOrNull(playerList, 0);
        Player second = ListUtils.getFromListOrNull(playerList, 1);
        Player third = ListUtils.getFromListOrNull(playerList, 2);
        
        return new ArrayList<>(Arrays.asList(winner, second, third));
    }

    public void setPlayerPoints(Player winner, Player second, Player third) {
        Session wSession = Session.getSession(winner);
        if (wSession != null) {
            wSession.points += 3;
        }
        Session sSession = Session.getSession(second);
        if (sSession != null) {
            sSession.points += 2;
        }
        Session tSession = Session.getSession(third);
        if (tSession != null) {
            tSession.points += 1;
        }
    }

    public void stopTasks(List<BukkitRunnable> bukkitRunnables) {
        bukkitRunnables.forEach(bukkitRunnable -> {
            System.out.println(bukkitRunnable);
            stopTask(bukkitRunnable);
        });
    }

    public void stopTask(BukkitRunnable bukkitRunnable) {
        if (bukkitRunnable != null && !bukkitRunnable.isCancelled()) {
            bukkitRunnable.cancel();
        }
    }
}
