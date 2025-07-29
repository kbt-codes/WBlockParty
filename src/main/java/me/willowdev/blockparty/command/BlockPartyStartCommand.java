package me.willowdev.blockparty.command;

import me.willowdev.blockparty.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BlockPartyStartCommand implements CommandExecutor {
    private final GameManager gameManager;

    public BlockPartyStartCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("blockparty.admin")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando!");
            return true;
        }
        
        gameManager.startGame(false);
        return true;
    }
}