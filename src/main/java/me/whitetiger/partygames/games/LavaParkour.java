package me.whitetiger.partygames.games;

import me.whitetiger.partygames.PartyGames;
import me.whitetiger.partygames.games.helper.GameType;
import me.whitetiger.partygames.games.helper.Parkour;
import org.bukkit.Location;

import java.util.Arrays;

public class LavaParkour extends Parkour {

    public static Location createLocation(int x, int y, int z) {
        return new Location(PartyGames.getInstance().getServer().getWorld("fishSlap"), x, y, z, 90, 0);
    }

    public LavaParkour() {
        super(GameType.LavaParkour, createLocation(-23, 123, 215), createLocation(-66, 123, 215), Arrays.asList(createLocation(-27, 123, 214), createLocation(-27, 123, 215), createLocation(-27, 123, 216)));
    }

}
