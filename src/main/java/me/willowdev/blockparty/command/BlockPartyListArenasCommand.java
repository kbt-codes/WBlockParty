package me.willowdev.blockparty.command;

import me.willowdev.blockparty.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockPartyListArenasCommand implements CommandExecutor {
    private final GameManager gameManager;

    public BlockPartyListArenasCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("blockparty.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }
        
        sender.sendMessage("§6=== Arenas Configuradas ===");
        
        if (gameManager.getArenaNames().isEmpty()) {
            sender.sendMessage("§eNenhuma arena configurada.");
            return true;
        }
        
        for (String arenaName : gameManager.getArenaNames()) {
            GameManager.ArenaData arena = gameManager.getArena(arenaName);
            if (arena != null) {
                String status = arena.enabled ? "§aAtiva" : "§cDesabilitada";
                String current = arenaName.equals(gameManager.getCurrentArenaName()) ? " §6(Atual)" : "";
                
                sender.sendMessage("§f" + arena.displayName + ": " + status + current);
                sender.sendMessage("  §7Tamanho: " + arena.getArea() + " blocos");
                sender.sendMessage("  §7Jogadores: " + arena.minPlayers + "-" + arena.maxPlayers);
                
                if (gameManager.getArenaSign(arenaName) != null) {
                    sender.sendMessage("  §7Placa: Configurada");
                } else {
                    sender.sendMessage("  §7Placa: §cNão configurada");
                }
                sender.sendMessage("");
            }
        }
        
        return true;
    }
} 