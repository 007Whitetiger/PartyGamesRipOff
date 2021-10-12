package me.whitetiger.partygames.games;

import me.whitetiger.partygames.games.helper.AGames;
import me.whitetiger.partygames.games.helper.GameType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;

public class JigsawRush extends AGames {
    public JigsawRush() {
        super(GameType.JigsawRush);
    }

    private final World world = plugin.getServer().getWorld("fishSlap");

    private final List<Material> blocks = Arrays.asList(Material.DIRT, Material.COBBLESTONE, Material.STONE, Material.OAK_LOG, Material.OAK_PLANKS,
            Material.BRICKS, Material.GOLD_BLOCK, Material.NETHERRACK, Material.END_STONE);

    private final Location firstSpawnLocation = new Location(world, 149, 136, 173, 180, 0);
    private final Location secondSpawnLocation = new Location(world, 149, 136, 187, 0, 0);

    private final Location firstBoardLocation = new Location(world, 149, 138, 169);
    private final Location secondBoardLocation = new Location(world, 149, 138, 191);

    private final HashMap<Player, Location> playerToBoard = new HashMap<>();
    private final HashMap<Player, Integer> playerPoints = new HashMap<>();


    private final Location firstLeftUp = new Location(world, 155, 148, 176);
    private final Location firstRightDown = new Location(world, 155, 140, 184);

    private BukkitRunnable endTask;

    private Player winner = null;
    private Player second = null;
    private Player third = null;
    private final List<Player> finished = new ArrayList<>();

    @Override
    public void start() {
        startListening();
        register();
    }

    @Override
    public void stop() {
        super.stop();

        for (Player player : playerToBoard.keySet()) {
            List<BlockFace> blockFaces = new ArrayList<>(Arrays.asList(BlockFace.EAST, BlockFace.SELF, BlockFace.WEST));
            List<BlockFace> tryFaces = new ArrayList<>(Arrays.asList(BlockFace.UP, BlockFace.SELF, BlockFace.DOWN));

            Location boardLocation = playerToBoard.get(player);
            Block boardBlock = boardLocation.getBlock();

            for (BlockFace firstFace : blockFaces) {
                Block tempBlock = boardBlock.getRelative(firstFace);

                for (BlockFace secondFace : tryFaces) {
                    tempBlock.getRelative(secondFace).setType(Material.SNOW_BLOCK);
                }
            }

            player.getInventory().clear();
        }

        for (int y=firstLeftUp.getBlockY(); y >= firstRightDown.getBlockY(); y--) {
            for (int z = firstLeftUp.getBlockZ(); z <= firstRightDown.getBlockZ(); z++) {
                new Location(world, firstLeftUp.getBlockX() ,y, z).getBlock().setType(Material.WHITE_WOOL);
                new Location(world, 113 ,y, z).getBlock().setType(Material.WHITE_WOOL);
            }
        }
        
        if (endTask != null && !endTask.isCancelled()) {
            endTask.cancel();
        }
    }

    private final List<Material> tempMaterials = new ArrayList<>(blocks);

    private Material switchY(int y, int matInt) {
        switch (y) {
            case 148:
            case 147:
            case 146:
                return tempMaterials.get(matInt);
            case 145:
            case 144:
            case 143:
                return tempMaterials.get(matInt + 3);
            case 142:
            case 141:
            case 140:
                return tempMaterials.get(matInt + 6);
        }
        return Material.WHITE_WOOL;
    }

    public void register() {
        for (int i=0; i < players.size(); i++) {
            Player player = players.get(i);

            if (i >= 6) {
                player.teleport(secondSpawnLocation.clone().subtract(6 * (i - 6), 0, 0));
                playerToBoard.put(player, secondBoardLocation.clone().subtract(6 * (i - 6), 0, 0));
            } else {
                player.teleport(firstSpawnLocation.clone().subtract(6 * i, 0, 0));
                playerToBoard.put(player, firstBoardLocation.clone().subtract(6 * i, 0, 0));
            }

            playerPoints.put(player, 0);

        }

        Collections.shuffle(tempMaterials);

        for (int y=firstLeftUp.getBlockY(); y >= firstRightDown.getBlockY(); y--) {
            for (int z= firstLeftUp.getBlockZ(); z <= firstRightDown.getBlockZ(); z++) {
                Material material = Material.WHITE_WOOL;
                Material material2 = Material.WHITE_WOOL;
                switch (z) {
                    case 176:
                    case 177:
                    case 178:
                        material = switchY(y, 0);
                        material2 = switchY(y, 2);
                        break;
                    case 179:
                    case 180:
                    case 181:
                        material = switchY(y, 1);
                        material2 = switchY(y, 1);
                        break;
                    case 182:
                    case 183:
                    case 184:
                        material = switchY(y, 2);
                        material2 = switchY(y, 0);
                        break;
                }
                new Location(world, firstLeftUp.getBlockX() ,y, z).getBlock().setType(material);
                new Location(world, 113 ,y, z).getBlock().setType(material2);

            }
        }

        Consumer<Integer> countDownCounsumer = (currentTime) -> {
            players.forEach((player) -> {
                player.sendMessage("Starting in " + currentTime);
            });
        };
        
        Runnable endCountDownTask = () -> {
            players.forEach((player) -> {
                for (Material material : blocks) {
                    player.getInventory().addItem(new ItemStack(material));
                }
            });
            endTask = new BukkitRunnable() {
                @Override
                public void run() {
                    end();
                }
            };
            endTask.runTaskLater(plugin, 15 * 20);
        };

        this.startTimer(10, countDownCounsumer, endCountDownTask);

    }

    private void end() {
        String message = AGames.defaultEndMessage(winner, second, third);

        for (Player player : players) {
            player.sendMessage(message);

            if (finished.contains(player)) {
                player.sendMessage(ChatColor.GREEN + "You finished!");
            } else {
                player.sendMessage(ChatColor.RED + "You didn't finish!");
            }
        }

        setPlayerPoints(winner, second, third);

        stop();
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        event.setCancelled(true);

        Block clickedBlock = event.getClickedBlock();
        Player player = event.getPlayer();
        Action action = event.getAction();
        Material playerMaterial = player.getInventory().getItemInMainHand().getType();

        if (!players.contains(player)) return;

        if (finished.contains(player)) return;

        if (!action.toString().toLowerCase().contains("right")) return;

        if (clickedBlock == null) return;

        if (!(clickedBlock.getLocation().getZ() == 169 || clickedBlock.getLocation().getZ() == 191)) return;

        clickedBlock.setType(playerMaterial);

        List<BlockFace> blockFaces = new ArrayList<>(Arrays.asList(BlockFace.EAST, BlockFace.SELF, BlockFace.WEST));
        List<BlockFace> tryFaces = new ArrayList<>(Arrays.asList(BlockFace.UP, BlockFace.SELF, BlockFace.DOWN));

        playerPoints.put(player, 0);

        Location boardLocation = playerToBoard.get(player);
        Block boardBlock = boardLocation.getBlock();

        boolean firstBoard = boardLocation.getZ() != 169;

        int block = 2;

        if (firstBoard) {
            block = 0;
        }

        for (BlockFace firstFace : blockFaces) {
            Block tempBlock = boardBlock.getRelative(firstFace);

            switch (block) {
                case 11:
                case 9:
                    block = 1;
                    break;
                case 10:
                    if (firstBoard) {
                        block = 2;
                    } else {
                        block = 0;
                    }
                    break;
            }


            for (BlockFace secondFace : tryFaces) {
                Material tempMaterial = tempBlock.getRelative(secondFace).getType();
                if (tempMaterial == tempMaterials.get(block)) {
                    playerPoints.put(player, playerPoints.get(player) + 1);
                    if (tempMaterial == playerMaterial) {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }
                }
                block += 3;
            }
        }

        if (finished.contains(player)) return;

        if (playerPoints.get(player) == 9) {
            finished.add(player);

            player.sendMessage(ChatColor.GREEN + "You're done!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

            if (winner == null) {
                winner = player;
            } else if (second == null) {
                second = player;
            } else if (third == null) {
                third = player;
            }

        }

    }
}
