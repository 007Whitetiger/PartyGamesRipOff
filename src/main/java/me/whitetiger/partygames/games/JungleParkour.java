package me.whitetiger.partygames.games;

import me.whitetiger.partygames.PartyGames;
import me.whitetiger.partygames.games.helper.GameType;
import me.whitetiger.partygames.games.helper.Parkour;
import org.bukkit.Location;

import java.util.Arrays;

public class JungleParkour extends Parkour {

    public static Location createLocation(int x, int y, int z) {
        return new Location(PartyGames.getInstance().getServer().getWorld("fishSlap"), x, y, z, 90, 0);
    }

    public JungleParkour() {
        super(GameType.JungleParkour, createLocation(-24, 123, 232), createLocation(-69, 123, 232), Arrays.asList(createLocation(-27, 123, 233), createLocation(-27, 123, 232), createLocation(-27, 123, 231)));
    }
}
