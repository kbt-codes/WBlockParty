package me.willowdev.blockparty.listener;

import me.willowdev.blockparty.GameManager;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignClickListener implements Listener {
    private final GameManager gameManager;

    public SignClickListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;
        
        Player player = event.getPlayer();
        Sign sign = (Sign) event.getClickedBlock().getState();
        
        // Verificar se é uma placa de arena
        String arenaName = getArenaNameFromSign(sign);
        if (arenaName != null) {
            event.setCancelled(true);
            
            // Verificar se a arena existe e está habilitada
            GameManager.ArenaData arena = gameManager.getArena(arenaName);
            if (arena == null) {
                player.sendMessage("§cArena não encontrada!");
                return;
            }
            
            if (!arena.enabled) {
                player.sendMessage("§cEsta arena está desabilitada!");
                return;
            }
            
            // Definir como arena atual e tentar entrar
            gameManager.setCurrentArena(arenaName);
            
            if (gameManager.addPlayer(player)) {
                player.sendMessage("§aVocê entrou na arena '" + arena.displayName + "'!");
            } else {
                player.sendMessage("§cNão foi possível entrar na arena!");
            }
            
            return;
        }
        
        // Verificar se é a placa principal (compatibilidade)
        if (isMainJoinSign(sign)) {
            event.setCancelled(true);
            
            if (gameManager.addPlayer(player)) {
                player.sendMessage("§aVocê entrou no BlockParty!");
            } else {
                player.sendMessage("§cNão foi possível entrar no jogo!");
            }
        }
    }
    
    private String getArenaNameFromSign(Sign sign) {
        String firstLine = sign.getLine(0);
        if (firstLine != null && firstLine.contains("[Arena]")) {
            // Procurar pela arena correspondente
            for (String arenaName : gameManager.getArenaNames()) {
                if (gameManager.getArenaSign(arenaName) != null) {
                    Sign arenaSign = (Sign) gameManager.getArenaSign(arenaName).getBlock().getState();
                    if (arenaSign.equals(sign)) {
                        return arenaName;
                    }
                }
            }
        }
        return null;
    }
    
    private boolean isMainJoinSign(Sign sign) {
        String firstLine = sign.getLine(0);
        return firstLine != null && firstLine.contains("[BlockParty]");
    }
} 