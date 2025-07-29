package me.willowdev.blockparty.command;

import me.willowdev.blockparty.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockPartyTabCompleter implements TabCompleter {
    private final List<String> subcommands = Arrays.asList(
        "start", "stop", "join", "leave", 
        "setlobby", "setarena", "setspectator", "setsign", "help"
    );

    public BlockPartyTabCompleter(GameManager gameManager) {
        // Constructor kept for compatibility, but gameManager is not used
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Primeiro argumento - mostrar todos os subcomandos
            String partial = args[0].toLowerCase();
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(partial)) {
                    completions.add(subcommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setarena")) {
            // Segundo argumento para setarena - sugerir nomes de arena
            completions.add("default");
            completions.add("arena1");
            completions.add("arena2");
            completions.add("main");
        }
        
        return completions;
    }
} 