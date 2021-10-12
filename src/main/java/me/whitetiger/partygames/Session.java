package me.whitetiger.partygames;

import java.net.InetAddress;
import java.util.HashMap;

import com.google.common.net.InetAddresses;

import org.bukkit.entity.Player;

public class Session {

    public static HashMap<Player, Session> playerSessionMap = new HashMap<>();
    public static HashMap<String, Session> ipAddressSessionMap = new HashMap<>();


    public String ipAddress = null;
    public Player player = null;
    public int points = 0;
    public boolean isInGame = false;
    
    private void updateStaticMaps() {
        if (this.player != null) {
            playerSessionMap.put(player, this);
        }
        ipAddressSessionMap.put(ipAddress, this);
    }

    public Session(Player player) {
        this.player = player;
        this.ipAddress = player.getAddress().getAddress().toString();
        updateStaticMaps();
        
    }

    public Session(InetAddress ipAddress) {
        this.ipAddress = ipAddress.toString();
        updateStaticMaps();
        
    }

    public static Session getSession(Player player) {
        if (player == null) return null;
        Session session = playerSessionMap.get(player);
        if (session == null) {
            session = ipAddressSessionMap.get(player.getAddress().getAddress().toString());
            if (session == null) {
                return new Session(player);
            } else {
                session.player = player;
            }
        }
        return session;
    }

    public static Session getSession(InetAddress inetAddress) {
        if (inetAddress == null) return null;
        Session session = ipAddressSessionMap.get(inetAddress.toString());
        if (session == null) {
            return new Session(inetAddress);
        }
        return session;
    }
    
}
