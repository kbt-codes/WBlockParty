package me.willowdev.blockparty.command;

import me.willowdev.blockparty.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockPartySetArenaSignCommand implements CommandExecutor {
    private final GameManager gameManager;

    public BlockPartySetArenaSignCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("blockparty.admin")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage("§eUso: /blockparty setarenasign <nome-da-arena>");
            return true;
        }
        
        String arenaName = args[0];
        
        // Verificar se a arena existe
        if (gameManager.getArena(arenaName) == null) {
            player.sendMessage("§cArena '" + arenaName + "' não encontrada!");
            player.sendMessage("§eArenas disponíveis: " + String.join(", ", gameManager.getArenaNames()));
            return true;
        }
        
        // Configurar placa na localização do jogador
        gameManager.setArenaSign(arenaName, player.getLocation());
        
        player.sendMessage("§a✓ Placa da arena '" + arenaName + "' configurada!");
        player.sendMessage("§7Posição: " + player.getLocation().getBlockX() + ", " + 
                         player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ());
        
        return true;
    }
} 