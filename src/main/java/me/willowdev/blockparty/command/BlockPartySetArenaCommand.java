package me.willowdev.blockparty.command;

import me.willowdev.blockparty.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockPartySetArenaCommand implements CommandExecutor {
    private final GameManager gameManager;

    public BlockPartySetArenaCommand(GameManager gameManager) {
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
            player.sendMessage("§eUso: /blockparty setarena <nome> [tamanho] [max-jogadores] [min-jogadores]");
            return true;
        }
        
        String arenaName = args[0];
        int size = args.length > 1 ? Integer.parseInt(args[1]) : 20;
        int maxPlayers = args.length > 2 ? Integer.parseInt(args[2]) : 20;
        int minPlayers = args.length > 3 ? Integer.parseInt(args[3]) : 2;
        
        // Criar arena ao redor do jogador
        org.bukkit.Location playerLoc = player.getLocation();
        org.bukkit.Location p1 = playerLoc.clone().add(-size/2, -1, -size/2);
        org.bukkit.Location p2 = playerLoc.clone().add(size/2, -1, size/2);
        
        gameManager.addArena(arenaName, p1, p2, maxPlayers, minPlayers, true, arenaName);
        gameManager.setCurrentArena(arenaName);
        
        player.sendMessage("§a✓ Arena '" + arenaName + "' configurada!");
        player.sendMessage("§7Tamanho: " + size + "x" + size + " blocos");
        player.sendMessage("§7Jogadores: " + minPlayers + "-" + maxPlayers);
        
        return true;
    }
}