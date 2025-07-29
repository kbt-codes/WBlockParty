package me.willowdev.blockparty.command;

import me.willowdev.blockparty.GameManager;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockPartySetSignCommand implements CommandExecutor {
    private final GameManager gameManager;

    public BlockPartySetSignCommand(GameManager gameManager) {
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
        
        Block target = player.getTargetBlockExact(10);
        if (target == null) {
            player.sendMessage("§cOlhe para uma placa!");
            return true;
        }
        
        if (!(target.getState() instanceof Sign)) {
            player.sendMessage("§cO bloco alvo não é uma placa!");
            return true;
        }
        
        // Configurar a placa
        gameManager.setJoinSignLocation(target.getLocation());
        
        // Atualizar a placa visualmente
        Sign sign = (Sign) target.getState();
        sign.setLine(0, "§6[BlockParty]");
        sign.setLine(1, "§eClique para");
        sign.setLine(2, "§eentrar!");
        sign.setLine(3, "§7Dance e Sobreviva!");
        sign.update();
        
        player.sendMessage("§a✓ Placa de entrada configurada!");
        player.sendMessage("§7Agora os jogadores podem clicar na placa para entrar!");
        
        return true;
    }
}
