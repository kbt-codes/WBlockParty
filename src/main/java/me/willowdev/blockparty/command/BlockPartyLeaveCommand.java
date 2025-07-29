package me.willowdev.blockparty.command;

import me.willowdev.blockparty.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockPartyLeaveCommand implements CommandExecutor {
    private final GameManager gameManager;

    public BlockPartyLeaveCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("blockparty.use")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }
        
        gameManager.removePlayer(player);
        return true;
    }
}