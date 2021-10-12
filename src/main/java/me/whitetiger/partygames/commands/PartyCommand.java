package me.whitetiger.partygames.commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class PartyCommand {

    private static List<PartyCommand> partyCommands = new ArrayList<>();

    private final String command;
    private final List<String> aliases;
    private final boolean admin;

    public PartyCommand(String command, List<String> aliases, boolean admin) {
        this.command = command;
        this.aliases = aliases;
        this.admin = admin;
        partyCommands.add(this);
    }

    public PartyCommand(String command, boolean admin) {
        this(command, Collections.emptyList(), admin);
    }

    abstract void onCommand(Player player, String[] args);

    abstract List<String> getArguments(Player player, Command command, String alias, String[] args);

    public boolean isCommand(String command) {
        return this.command.equalsIgnoreCase(command) || aliases.contains(command);
    }

    public String getCommand() {
        return command;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isAdmin() {
        return admin;
    }

    public static List<PartyCommand> getPartyCommands() {
        return partyCommands;
    }

}
