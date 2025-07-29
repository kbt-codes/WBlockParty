package me.willowdev.blockparty;

import me.willowdev.blockparty.command.BlockPartyCommand;
import me.willowdev.blockparty.command.BlockPartyTabCompleter;
import me.willowdev.blockparty.listener.SignClickListener;
import org.bukkit.plugin.java.JavaPlugin;

public class WBlockParty extends JavaPlugin {

    private GameManager gameManager;

    @Override
    public void onEnable() {
        try {
            // Salvar config padrão apenas se não existir
            saveDefaultConfig();
            reloadConfig();
            
            this.gameManager = new GameManager(this);

            // Registrar comando principal
            registerCommand("blockparty", new BlockPartyCommand(this, gameManager));
            
            // Registrar TabCompleter
            getCommand("blockparty").setTabCompleter(new BlockPartyTabCompleter(gameManager));
            
            // Registrar listeners
            getServer().getPluginManager().registerEvents(new SignClickListener(gameManager), this);

            getLogger().info("§a✓ WBlockParty foi ativado com sucesso!");
            getLogger().info("§7Versão: " + getPluginMeta().getVersion());
            
        } catch (Exception e) {
            getLogger().severe("§c✗ Erro ao ativar o plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void registerCommand(String name, Object executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor((org.bukkit.command.CommandExecutor) executor);
        } else {
            getLogger().severe("§c✗ Comando '" + name + "' não encontrado no plugin.yml!");
        }
    }

    @Override
    public void onDisable() {
        try {
            // Cancelar todas as tasks ativas
            if (gameManager != null) {
                gameManager.cancelAllTasks();
                getLogger().info("§7Todas as tasks do BlockParty foram canceladas.");
            }
            getLogger().info("§c✗ WBlockParty foi desativado!");
        } catch (Exception e) {
            getLogger().severe("§c✗ Erro ao desativar o plugin: " + e.getMessage());
        }
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}