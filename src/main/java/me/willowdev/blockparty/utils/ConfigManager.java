package me.willowdev.blockparty.utils;

import me.willowdev.blockparty.GameManager;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final FileConfiguration mainConfig;
    
    // Arquivos de configuração separados
    private FileConfiguration messagesConfig;
    private FileConfiguration soundsConfig;
    private FileConfiguration scoreboardConfig;
    private FileConfiguration arenaConfig;
    private FileConfiguration dataConfig;
    
    // Arquivos físicos
    private File messagesFile;
    private File soundsFile;
    private File scoreboardFile;
    private File arenaFile;
    private File dataFile;
    
    // Cache de configurações
    private final Map<String, Object> configCache = new HashMap<>();
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.mainConfig = plugin.getConfig();
        loadAllConfigs();
    }
    
    /**
     * Carrega todos os arquivos de configuração
     */
    public void loadAllConfigs() {
        try {
            // Carregar arquivos separados se habilitado
            if (mainConfig.getBoolean("files.separate-files", true)) {
                loadMessagesConfig();
                loadSoundsConfig();
                loadScoreboardConfig();
                loadArenaConfig();
                loadDataConfig();
            }
            
            // Limpar cache
            configCache.clear();
            
            plugin.getLogger().info("Todas as configurações foram carregadas com sucesso!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao carregar configurações: " + e.getMessage());
        }
    }
    
    /**
     * Carrega o arquivo de mensagens
     */
    private void loadMessagesConfig() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    /**
     * Carrega o arquivo de sons
     */
    private void loadSoundsConfig() {
        soundsFile = new File(plugin.getDataFolder(), "sounds.yml");
        if (!soundsFile.exists()) {
            plugin.saveResource("sounds.yml", false);
        }
        soundsConfig = YamlConfiguration.loadConfiguration(soundsFile);
    }
    
    /**
     * Carrega o arquivo de scoreboard
     */
    private void loadScoreboardConfig() {
        scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        if (!scoreboardFile.exists()) {
            plugin.saveResource("scoreboard.yml", false);
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }
    
    /**
     * Carrega o arquivo de arena
     */
    private void loadArenaConfig() {
        arenaFile = new File(plugin.getDataFolder(), "arena.yml");
        if (!arenaFile.exists()) {
            plugin.saveResource("arena.yml", false);
        }
        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
    }
    
    /**
     * Carrega o arquivo de dados
     */
    private void loadDataConfig() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            plugin.saveResource("data.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    /**
     * Salva todos os arquivos de configuração
     */
    public void saveAllConfigs() {
        try {
            if (messagesConfig != null) messagesConfig.save(messagesFile);
            if (soundsConfig != null) soundsConfig.save(soundsFile);
            if (scoreboardConfig != null) scoreboardConfig.save(scoreboardFile);
            if (arenaConfig != null) arenaConfig.save(arenaFile);
            if (dataConfig != null) dataConfig.save(dataFile);
            
            plugin.getLogger().info("Todas as configurações foram salvas com sucesso!");
            
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar configurações: " + e.getMessage());
        }
    }
    
    /**
     * Recarrega todas as configurações
     */
    public void reloadAllConfigs() {
        plugin.reloadConfig();
        loadAllConfigs();
    }
    
    // ===========================================
    // CONFIGURAÇÕES GERAIS (config.yml)
    // ===========================================
    
    public int getMinPlayers() {
        return mainConfig.getInt("game.min-players", 2);
    }
    
    public int getMaxPlayers() {
        return mainConfig.getInt("game.max-players", 20);
    }
    
    public int getLobbyTimer() {
        return mainConfig.getInt("game.lobby-timer", 30);
    }
    
    public int getRodadaTempoBase() {
        return mainConfig.getInt("game.round-base-time", 30);
    }
    
    public int getRodadaTempoMin() {
        return mainConfig.getInt("game.round-min-time", 10);
    }
    
    public int getMaxGameTime() {
        return mainConfig.getInt("game.max-game-time", 600);
    }
    
    public int getGameEndDelay() {
        return mainConfig.getInt("game.game-end-delay", 3);
    }
    
    public int getFallCheckInterval() {
        return mainConfig.getInt("game.fall-check-interval", 10);
    }
    
    public boolean isDebugEnabled() {
        return mainConfig.getBoolean("debug.enabled", false);
    }
    
    public int getDebugLevel() {
        return mainConfig.getInt("debug.level", 1);
    }
    
    public int getAutoSaveInterval() {
        return mainConfig.getInt("performance.auto-save-interval", 300);
    }
    
    public boolean isAutoCleanupEnabled() {
        return mainConfig.getBoolean("performance.auto-cleanup", true);
    }
    
    public int getCleanupDays() {
        return mainConfig.getInt("performance.cleanup-days", 30);
    }
    
    // ===========================================
    // CONFIGURAÇÕES DE MENSAGENS (messages.yml)
    // ===========================================
    
    public String getMessagePrefix() {
        return getMessageConfig().getString("prefix", "§6[BlockParty] §r");
    }
    
    public String getMessageColor(String type) {
        return getMessageConfig().getString("colors." + type, "§f");
    }
    
    public String getGameMessage(String key) {
        return getMessageConfig().getString("game." + key, "Mensagem não encontrada: " + key);
    }
    
    public String getTitleMessage(String key) {
        return getMessageConfig().getString("title." + key, "Título não encontrado: " + key);
    }
    
    public String getCommandMessage(String subcommand, String key) {
        return getMessageConfig().getString("commands." + subcommand + "." + key, "Comando não encontrado");
    }
    
    public String getErrorMessage(String key) {
        return getMessageConfig().getString("errors." + key, "Erro não encontrado: " + key);
    }
    
    public String getDebugMessage(String key) {
        return getMessageConfig().getString("debug." + key, "Debug não encontrado: " + key);
    }
    
    public String[] getSignLines(String type) {
        return getMessageConfig().getStringList("signs." + type).toArray(new String[0]);
    }
    
    private FileConfiguration getMessageConfig() {
        return messagesConfig != null ? messagesConfig : mainConfig;
    }
    
    // ===========================================
    // CONFIGURAÇÕES DE SONS (sounds.yml)
    // ===========================================
    
    public boolean isSoundsEnabled() {
        return getSoundsConfig().getBoolean("enabled", true);
    }
    
    public float getSoundVolume() {
        return (float) getSoundsConfig().getDouble("volume", 1.0);
    }
    
    public float getSoundPitch() {
        return (float) getSoundsConfig().getDouble("pitch", 1.0);
    }
    
    public String getPlayerJoinSound() {
        return getSoundsConfig().getString("players.join", "ENTITY_PLAYER_LEVELUP");
    }
    
    public String getPlayerLeaveSound() {
        return getSoundsConfig().getString("players.leave", "ENTITY_PLAYER_LEVELUP");
    }
    
    public String getPlayerEliminatedSound() {
        return getSoundsConfig().getString("players.eliminated", "ENTITY_PLAYER_DEATH");
    }
    
    public String getGameStartSound() {
        return getSoundsConfig().getString("game.start", "ENTITY_PLAYER_LEVELUP");
    }
    
    public String getGameEndSound() {
        return getSoundsConfig().getString("game.end", "ENTITY_PLAYER_LEVELUP");
    }
    
    public String getBlockShowSound() {
        return getSoundsConfig().getString("game.block-show", "BLOCK_NOTE_BLOCK_PLING");
    }
    
    public boolean isMusicEnabled() {
        return getSoundsConfig().getBoolean("music.enabled", true);
    }
    
    public float getMusicVolume() {
        return (float) getSoundsConfig().getDouble("music.volume", 0.5);
    }
    
    public float getMusicPitch() {
        return (float) getSoundsConfig().getDouble("music.pitch", 1.0);
    }
    
    public float getLargeArenaVolumeMultiplier() {
        return (float) getSoundsConfig().getDouble("music.large-arena.volume-multiplier", 2.0);
    }
    
    public int getLargeArenaMinSize() {
        return getSoundsConfig().getInt("music.large-arena.min-size", 50);
    }
    
    public String[] getAvailableDiscs() {
        return getSoundsConfig().getStringList("music.available-discs").toArray(new String[0]);
    }
    
    public Sound[] getAvailableDiscsAsSounds() {
        String[] discStrings = getAvailableDiscs();
        Sound[] sounds = new Sound[discStrings.length];
        for (int i = 0; i < discStrings.length; i++) {
            try {
                sounds[i] = Sound.valueOf(discStrings[i]);
            } catch (IllegalArgumentException e) {
                sounds[i] = Sound.MUSIC_DISC_CAT; // Fallback
            }
        }
        return sounds;
    }
    
    private FileConfiguration getSoundsConfig() {
        return soundsConfig != null ? soundsConfig : mainConfig;
    }
    
    // ===========================================
    // CONFIGURAÇÕES DE SCOREBOARD (scoreboard.yml)
    // ===========================================
    
    public boolean isScoreboardEnabled() {
        return getScoreboardConfig().getBoolean("enabled", true);
    }
    
    public int getScoreboardUpdateInterval() {
        return getScoreboardConfig().getInt("update-interval", 20);
    }
    
    public String getLobbyScoreboardTitle() {
        return getScoreboardConfig().getString("lobby.title", "§6§lBLOCKPARTY");
    }
    
    public String[] getLobbyScoreboardLines() {
        return getScoreboardConfig().getStringList("lobby.lines").toArray(new String[0]);
    }
    
    public String getGameScoreboardTitle() {
        return getScoreboardConfig().getString("game.title", "§6§lBLOCKPARTY");
    }
    
    public String[] getGameScoreboardLines() {
        return getScoreboardConfig().getStringList("game.lines").toArray(new String[0]);
    }
    
    private FileConfiguration getScoreboardConfig() {
        return scoreboardConfig != null ? scoreboardConfig : mainConfig;
    }
    
    // ===========================================
    // CONFIGURAÇÕES DE ARENA (arena.yml)
    // ===========================================
    
    public GameManager.FloorPattern getFloorPattern() {
        String pattern = getArenaConfig().getString("floor-pattern", "RANDOM");
        try {
            return GameManager.FloorPattern.valueOf(pattern);
        } catch (IllegalArgumentException e) {
            return GameManager.FloorPattern.RANDOM;
        }
    }
    
    public int getArenaSize() {
        return getArenaConfig().getInt("arena-size", 37);
    }
    
    public int getArenaHeight() {
        return getArenaConfig().getInt("arena-height", 1);
    }
    
    public String[] getAvailableBlocks() {
        return getArenaConfig().getStringList("available-blocks").toArray(new String[0]);
    }
    
    private FileConfiguration getArenaConfig() {
        return arenaConfig != null ? arenaConfig : mainConfig;
    }
    
    // ===========================================
    // CONFIGURAÇÕES DE DADOS (data.yml)
    // ===========================================
    
    public FileConfiguration getDataConfig() {
        return dataConfig;
    }
    
    public void saveDataConfig() {
        if (dataConfig != null && dataFile != null) {
            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Erro ao salvar dados: " + e.getMessage());
            }
        }
    }
    
    // ===========================================
    // MÉTODOS AUXILIARES
    // ===========================================
    
    /**
     * Obtém uma configuração com cache
     */
    public Object getCachedConfig(String key, Object defaultValue) {
        return configCache.computeIfAbsent(key, k -> defaultValue);
    }
    
    /**
     * Define uma configuração com cache
     */
    public void setCachedConfig(String key, Object value) {
        configCache.put(key, value);
    }
    
    /**
     * Limpa o cache de configurações
     */
    public void clearCache() {
        configCache.clear();
    }
    
    /**
     * Verifica se um arquivo de configuração existe
     */
    public boolean configFileExists(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        return file.exists();
    }
    
    /**
     * Obtém o caminho de um arquivo de configuração
     */
    public String getConfigFilePath(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        return file.getAbsolutePath();
    }
} 