package me.willowdev.blockparty.command;

import me.willowdev.blockparty.GameManager;
import me.willowdev.blockparty.WBlockParty;
import me.willowdev.blockparty.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BlockPartyCommand implements CommandExecutor {
    private final WBlockParty plugin;
    private final GameManager gameManager;

    public BlockPartyCommand(WBlockParty plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        try {
            switch (args[0].toLowerCase()) {
                case "start":
                    return new BlockPartyStartCommand(gameManager).onCommand(sender, command, label, args);
                case "stop":
                    return new BlockPartyStopCommand(gameManager).onCommand(sender, command, label, args);
                case "join":
                    return new BlockPartyJoinCommand(gameManager).onCommand(sender, command, label, args);
                case "leave":
                    return new BlockPartyLeaveCommand(gameManager).onCommand(sender, command, label, args);
                case "setlobby":
                    return new BlockPartySetLobbyCommand(gameManager).onCommand(sender, command, label, args);
                case "setarena":
                    return new BlockPartySetArenaCommand(gameManager).onCommand(sender, command, label, args);
                case "setspectator":
                    return new BlockPartySetSpectatorCommand(gameManager).onCommand(sender, command, label, args);
                case "setsign":
                    return new BlockPartySetSignCommand(gameManager).onCommand(sender, command, label, args);
                case "help":
                    showHelp(sender);
                    return true;
                case "status":
                    showStatus(sender);
                    return true;
                case "reload":
                    return handleReload(sender);
                default:
                    sender.sendMessage(MessageUtils.error("Comando desconhecido. Use /blockparty help para ver os comandos disponíveis."));
                    return true;
            }
        } catch (Exception e) {
            sender.sendMessage(MessageUtils.error("Ocorreu um erro ao executar o comando: " + e.getMessage()));
            plugin.getLogger().warning("Erro no comando BlockParty: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== BlockParty Help ===");
        sender.sendMessage("");
        sender.sendMessage("§eComandos de Jogo:");
        sender.sendMessage("§7  /blockparty join §8- Entrar no jogo");
        sender.sendMessage("§7  /blockparty leave §8- Sair do jogo");
        sender.sendMessage("");
        sender.sendMessage("§eComandos Administrativos:");
        sender.sendMessage("§7  /blockparty start §8- Iniciar partida");
        sender.sendMessage("§7  /blockparty stop §8- Parar partida");
        sender.sendMessage("§7  /blockparty status §8- Ver status do jogo");
        sender.sendMessage("");
        sender.sendMessage("§eComandos de Configuração:");
        sender.sendMessage("§7  /blockparty setlobby §8- Definir lobby");
        sender.sendMessage("§7  /blockparty setarena <nome> §8- Definir arena");
        sender.sendMessage("§7  /blockparty setspectator §8- Definir local de espectadores");
        sender.sendMessage("§7  /blockparty setsign §8- Definir placa de entrada");
        sender.sendMessage("");
        sender.sendMessage("§eOutros:");
        sender.sendMessage("§7  /blockparty help §8- Mostrar esta ajuda");
        sender.sendMessage("§7  /blockparty reload §8- Recarregar configuração");
    }
    
    private void showStatus(CommandSender sender) {
        if (!sender.hasPermission("blockparty.admin")) {
            sender.sendMessage(MessageUtils.error("Você não tem permissão para usar este comando!"));
            return;
        }
        
        String status = gameManager.getStatus();
        sender.sendMessage(MessageUtils.info("Status do BlockParty:"));
        sender.sendMessage("§7" + status);
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("blockparty.admin")) {
            sender.sendMessage(MessageUtils.error("Você não tem permissão para usar este comando!"));
            return true;
        }
        
        try {
            plugin.reloadConfig();
            gameManager.reloadConfig();
            sender.sendMessage(MessageUtils.success("Configuração recarregada com sucesso!"));
        } catch (Exception e) {
            sender.sendMessage(MessageUtils.error("Erro ao recarregar configuração: " + e.getMessage()));
            plugin.getLogger().severe("Erro ao recarregar configuração: " + e.getMessage());
        }
        return true;
    }
}