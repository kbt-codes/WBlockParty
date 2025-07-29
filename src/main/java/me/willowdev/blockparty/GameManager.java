// Package name is intentional - developer's namespace
package me.willowdev.blockparty;

import me.willowdev.blockparty.utils.ConfigManager;
import me.willowdev.blockparty.utils.MessageUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.bukkit.event.Listener;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.block.Block;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager implements Listener {
    private final JavaPlugin plugin;
    private ConfigManager configManager;
    
    // Estados do jogo
    public enum GameState { LOBBY, INGAME, ENDING, ERROR }
    private volatile GameState state = GameState.LOBBY;
    
    // Jogadores (usando ConcurrentHashMap para thread-safety)
    private final Set<Player> players = ConcurrentHashMap.newKeySet();
    private final Map<Player, Location> playerReturnLocations = new ConcurrentHashMap<>();
    private final Map<Player, Long> playerJoinTimes = new ConcurrentHashMap<>();
    
    // Configurações
    private int minPlayers;
    private int maxPlayers;
    private int lobbyTimer;
    private int rodadaTempoBase;
    private int rodadaTempoMin;
    private int gameEndDelay;
    private int maxGameTime;
    
    // Localizações
    private Location lobbyLocation;
    private Location spectatorLocation;
    private Location joinSignLocation;
    private ArenaData currentArena;
    
    // Sistema de múltiplas arenas
    private final Map<String, ArenaData> arenas = new ConcurrentHashMap<>();
    private final Map<String, Location> arenaSigns = new ConcurrentHashMap<>();
    private String currentArenaName = "default";
    
    // Tasks
    private BukkitTask lobbyTask;
    private BukkitTask roundTask;
    private BukkitTask countdownTask;
    private BukkitTask gameTimeTask;
    private BukkitTask fallCheckTask;
    private BukkitTask musicTask;
    
    // Jogo atual
    private Material blocoAtual;
    private int tempoRestante;
    private int tempoRodadaAtual;
    private int rodadaAtual;
    private long gameStartTime = 0;
    private boolean gamePaused = false;
    
    // Estatísticas
    private int totalGames = 0;
    private int totalPlayers = 0;
    private final Map<Player, Integer> playerWins = new ConcurrentHashMap<>();
    
    // Arena
    public static class ArenaData {
        public final String name;
        public final Location p1, p2, floorLocation;
        public final int floorY;
        public final int size;
        public final int maxPlayers;
        public final int minPlayers;
        public final boolean enabled;
        public final String displayName;
        
        public ArenaData(String name, Location p1, Location p2) {
            this(name, p1, p2, 20, 2, true, name);
        }
        
        public ArenaData(String name, Location p1, Location p2, int maxPlayers, int minPlayers, boolean enabled, String displayName) {
            this.name = name;
            this.p1 = p1;
            this.p2 = p2;
            this.floorY = (int) Math.min(p1.getY(), p2.getY());
            this.size = 37; // Tamanho padrão da arena
            this.maxPlayers = maxPlayers;
            this.minPlayers = minPlayers;
            this.enabled = enabled;
            this.displayName = displayName;
            
            // Centro da arena
            double centerX = (p1.getX() + p2.getX()) / 2.0 + 0.5;
            double centerY = this.floorY + 1;
            double centerZ = (p1.getZ() + p2.getZ()) / 2.0 + 0.5;
            this.floorLocation = new Location(p1.getWorld(), centerX, centerY, centerZ);
        }
        
        public boolean isValid() {
            return p1 != null && p2 != null && p1.getWorld() != null && 
                   p1.getWorld().equals(p2.getWorld()) && name != null && !name.isEmpty();
        }
        
        public int getArea() {
            int width = Math.abs(p2.getBlockX() - p1.getBlockX()) + 1;
            int length = Math.abs(p2.getBlockZ() - p1.getBlockZ()) + 1;
            return width * length;
        }
    }
    
    public enum FloorPattern {
        RANDOM,
        MULTICOLOR_CHECKER,
        HORIZONTAL_STRIPES,
        VERTICAL_STRIPES,
        CIRCLES,
        SPIRAL,
        ROWS
    }
    private FloorPattern floorPattern = FloorPattern.RANDOM;
    
    public void setFloorPattern(FloorPattern pattern) {
        this.floorPattern = pattern;
    }
    
    public FloorPattern getFloorPattern() {
        return floorPattern;
    }
    
    // ===========================================
    // SISTEMA DE MÚLTIPLAS ARENAS
    // ===========================================
    
    public void addArena(String name, Location p1, Location p2) {
        addArena(name, p1, p2, 20, 2, true, name);
    }
    
    public void addArena(String name, Location p1, Location p2, int maxPlayers, int minPlayers, boolean enabled, String displayName) {
        ArenaData arena = new ArenaData(name, p1, p2, maxPlayers, minPlayers, enabled, displayName);
        arenas.put(name, arena);
        saveArenaToConfig(name, arena);
    }
    
    public void removeArena(String name) {
        arenas.remove(name);
        arenaSigns.remove(name);
        removeArenaFromConfig(name);
    }
    
    public ArenaData getArena(String name) {
        return arenas.get(name);
    }
    
    public Set<String> getArenaNames() {
        return arenas.keySet();
    }
    
    public Set<String> getEnabledArenaNames() {
        return arenas.values().stream()
                .filter(arena -> arena.enabled)
                .map(arena -> arena.name)
                .collect(java.util.stream.Collectors.toSet());
    }
    
    public void setCurrentArena(String name) {
        if (arenas.containsKey(name)) {
            currentArenaName = name;
            currentArena = arenas.get(name);
            saveCurrentArenaToConfig();
        }
    }
    
    public String getCurrentArenaName() {
        return currentArenaName;
    }
    
    public void setArenaSign(String arenaName, Location signLocation) {
        arenaSigns.put(arenaName, signLocation);
        saveArenaSignToConfig(arenaName, signLocation);
    }
    
    public Location getArenaSign(String arenaName) {
        return arenaSigns.get(arenaName);
    }
    
    public void updateAllArenaSigns() {
        for (Map.Entry<String, Location> entry : arenaSigns.entrySet()) {
            String arenaName = entry.getKey();
            Location signLocation = entry.getValue();
            updateArenaSign(arenaName, signLocation);
        }
    }
    
    private void updateArenaSign(String arenaName, Location signLocation) {
        if (signLocation == null) return;
        
        Block block = signLocation.getBlock();
        if (!(block.getState() instanceof Sign)) return;
        
        Sign sign = (Sign) block.getState();
        ArenaData arena = arenas.get(arenaName);
        
        if (arena == null) {
            sign.setLine(0, "§c[Arena]");
            sign.setLine(1, "§cNão encontrada");
            sign.setLine(2, "");
            sign.setLine(3, "");
        } else if (!arena.enabled) {
            sign.setLine(0, "§c[Arena]");
            sign.setLine(1, "§cDesabilitada");
            sign.setLine(2, arena.displayName);
            sign.setLine(3, "");
        } else {
            int playersInArena = getPlayersInArena(arenaName);
            sign.setLine(0, "§6[Arena]");
            sign.setLine(1, arena.displayName);
            sign.setLine(2, "§eClique para");
            sign.setLine(3, "§eEntrar! §f" + playersInArena + "§7/" + arena.maxPlayers);
        }
        
        sign.update();
    }
    
    private int getPlayersInArena(String arenaName) {
        // Por enquanto, retorna 0. Em uma implementação completa,
        // você teria um mapa de jogadores por arena
        return 0;
    }
    
    // ===========================================
    // MÉTODOS DE CONFIGURAÇÃO DE ARENAS
    // ===========================================
    
    private void saveArenaToConfig(String name, ArenaData arena) {
        String path = "arenas." + name;
        plugin.getConfig().set(path + ".name", arena.name);
        plugin.getConfig().set(path + ".display-name", arena.displayName);
        plugin.getConfig().set(path + ".max-players", arena.maxPlayers);
        plugin.getConfig().set(path + ".min-players", arena.minPlayers);
        plugin.getConfig().set(path + ".enabled", arena.enabled);
        
        // Salvar coordenadas
        plugin.getConfig().set(path + ".p1.world", arena.p1.getWorld().getName());
        plugin.getConfig().set(path + ".p1.x", arena.p1.getX());
        plugin.getConfig().set(path + ".p1.y", arena.p1.getY());
        plugin.getConfig().set(path + ".p1.z", arena.p1.getZ());
        
        plugin.getConfig().set(path + ".p2.world", arena.p2.getWorld().getName());
        plugin.getConfig().set(path + ".p2.x", arena.p2.getX());
        plugin.getConfig().set(path + ".p2.y", arena.p2.getY());
        plugin.getConfig().set(path + ".p2.z", arena.p2.getZ());
        
        plugin.saveConfig();
    }
    
    private void removeArenaFromConfig(String name) {
        plugin.getConfig().set("arenas." + name, null);
        plugin.saveConfig();
    }
    
    private void saveCurrentArenaToConfig() {
        plugin.getConfig().set("current-arena", currentArenaName);
        plugin.saveConfig();
    }
    
    private void saveArenaSignToConfig(String arenaName, Location signLocation) {
        String path = "arena-signs." + arenaName;
        plugin.getConfig().set(path + ".world", signLocation.getWorld().getName());
        plugin.getConfig().set(path + ".x", signLocation.getX());
        plugin.getConfig().set(path + ".y", signLocation.getY());
        plugin.getConfig().set(path + ".z", signLocation.getZ());
        plugin.saveConfig();
    }
    
    private void loadArenasFromConfig() {
        if (!plugin.getConfig().contains("arenas")) return;
        
        for (String arenaName : plugin.getConfig().getConfigurationSection("arenas").getKeys(false)) {
            String path = "arenas." + arenaName;
            
            try {
                String worldName = plugin.getConfig().getString(path + ".p1.world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                
                // Carregar coordenadas
                double x1 = plugin.getConfig().getDouble(path + ".p1.x");
                double y1 = plugin.getConfig().getDouble(path + ".p1.y");
                double z1 = plugin.getConfig().getDouble(path + ".p1.z");
                
                double x2 = plugin.getConfig().getDouble(path + ".p2.x");
                double y2 = plugin.getConfig().getDouble(path + ".p2.y");
                double z2 = plugin.getConfig().getDouble(path + ".p2.z");
                
                Location p1 = new Location(world, x1, y1, z1);
                Location p2 = new Location(world, x2, y2, z2);
                
                // Carregar outras propriedades
                int maxPlayers = plugin.getConfig().getInt(path + ".max-players", 20);
                int minPlayers = plugin.getConfig().getInt(path + ".min-players", 2);
                boolean enabled = plugin.getConfig().getBoolean(path + ".enabled", true);
                String displayName = plugin.getConfig().getString(path + ".display-name", arenaName);
                
                ArenaData arena = new ArenaData(arenaName, p1, p2, maxPlayers, minPlayers, enabled, displayName);
                arenas.put(arenaName, arena);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao carregar arena '" + arenaName + "': " + e.getMessage());
            }
        }
        
        // Carregar arena atual
        currentArenaName = plugin.getConfig().getString("current-arena", "default");
        if (arenas.containsKey(currentArenaName)) {
            currentArena = arenas.get(currentArenaName);
        }
        
        // Carregar placas das arenas
        loadArenaSignsFromConfig();
    }
    
    private void loadArenaSignsFromConfig() {
        if (!plugin.getConfig().contains("arena-signs")) return;
        
        for (String arenaName : plugin.getConfig().getConfigurationSection("arena-signs").getKeys(false)) {
            String path = "arena-signs." + arenaName;
            
            try {
                String worldName = plugin.getConfig().getString(path + ".world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                
                double x = plugin.getConfig().getDouble(path + ".x");
                double y = plugin.getConfig().getDouble(path + ".y");
                double z = plugin.getConfig().getDouble(path + ".z");
                
                Location signLocation = new Location(world, x, y, z);
                arenaSigns.put(arenaName, signLocation);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao carregar placa da arena '" + arenaName + "': " + e.getMessage());
            }
        }
    }
    
    private Sound musicaAtual = null;
    private static final Sound[] DISCOS = {
        Sound.MUSIC_DISC_CAT,
        Sound.MUSIC_DISC_BLOCKS,
        Sound.MUSIC_DISC_CHIRP,
        Sound.MUSIC_DISC_FAR,
        Sound.MUSIC_DISC_MALL,
        Sound.MUSIC_DISC_MELLOHI,
        Sound.MUSIC_DISC_STAL,
        Sound.MUSIC_DISC_STRAD,
        Sound.MUSIC_DISC_WARD,
        Sound.MUSIC_DISC_WAIT,
        Sound.MUSIC_DISC_PIGSTEP,
        Sound.MUSIC_DISC_OTHERSIDE,
        Sound.MUSIC_DISC_RELIC,
        Sound.MUSIC_DISC_CREATOR,
        Sound.MUSIC_DISC_CREATOR_MUSIC_BOX
    };
    
    public GameManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configManager = new ConfigManager(plugin);
        
        // Inicializar MessageUtils com o novo sistema
        MessageUtils.loadFromConfigManager(configManager);
        
        loadConfig();
        loadSavedLocations();
        startFallCheckTask();
    }
    
    private void loadConfig() {
        this.minPlayers = configManager.getMinPlayers();
        this.maxPlayers = configManager.getMaxPlayers();
        this.lobbyTimer = configManager.getLobbyTimer();
        this.rodadaTempoBase = configManager.getRodadaTempoBase();
        this.rodadaTempoMin = configManager.getRodadaTempoMin();
        this.floorPattern = configManager.getFloorPattern();
        this.gameEndDelay = configManager.getGameEndDelay();
        this.maxGameTime = configManager.getMaxGameTime();
    }
    
    private void loadSavedLocations() {
        try {
            // Carregar lobby
            if (plugin.getConfig().contains("locations.lobby")) {
                String worldName = plugin.getConfig().getString("locations.lobby.world");
                if (worldName != null) {
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        double x = plugin.getConfig().getDouble("locations.lobby.x");
                        double y = plugin.getConfig().getDouble("locations.lobby.y");
                        double z = plugin.getConfig().getDouble("locations.lobby.z");
                        lobbyLocation = new Location(world, x, y, z);
                    }
                }
            }
            
            // Carregar múltiplas arenas
            loadArenasFromConfig();
            
            // Carregar arena antiga (compatibilidade)
            if (plugin.getConfig().contains("locations.arena") && arenas.isEmpty()) {
                String worldName = plugin.getConfig().getString("locations.arena.p1.world");
                if (worldName != null) {
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        double x1 = plugin.getConfig().getDouble("locations.arena.p1.x");
                        double y1 = plugin.getConfig().getDouble("locations.arena.p1.y");
                        double z1 = plugin.getConfig().getDouble("locations.arena.p1.z");
                        double x2 = plugin.getConfig().getDouble("locations.arena.p2.x");
                        double y2 = plugin.getConfig().getDouble("locations.arena.p2.y");
                        double z2 = plugin.getConfig().getDouble("locations.arena.p2.z");
                        String arenaName = plugin.getConfig().getString("locations.arena.name", "default");
                        
                        Location p1 = new Location(world, x1, y1, z1);
                        Location p2 = new Location(world, x2, y2, z2);
                        addArena(arenaName, p1, p2);
                        setCurrentArena(arenaName);
                    }
                }
            }
            
            // Carregar espectador
            if (plugin.getConfig().contains("locations.spectator")) {
                String worldName = plugin.getConfig().getString("locations.spectator.world");
                if (worldName != null) {
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        double x = plugin.getConfig().getDouble("locations.spectator.x");
                        double y = plugin.getConfig().getDouble("locations.spectator.y");
                        double z = plugin.getConfig().getDouble("locations.spectator.z");
                        spectatorLocation = new Location(world, x, y, z);
                    }
                }
            }
            
            // Carregar placa de entrada
            if (plugin.getConfig().contains("locations.join-sign")) {
                String worldName = plugin.getConfig().getString("locations.join-sign.world");
                if (worldName != null) {
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        double x = plugin.getConfig().getDouble("locations.join-sign.x");
                        double y = plugin.getConfig().getDouble("locations.join-sign.y");
                        double z = plugin.getConfig().getDouble("locations.join-sign.z");
                        joinSignLocation = new Location(world, x, y, z);
                    }
                }
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao carregar localizações: " + e.getMessage());
        }
    }
    
    // ===========================================
    // COMANDOS DE JOGO MELHORADOS
    // ===========================================
    
    public boolean addPlayer(Player player) {
        if (player == null) return false;
        
        try {
            // Validações de estado
            if (state != GameState.LOBBY) {
                player.sendMessage(MessageUtils.error("A partida já está em andamento!"));
                return false;
            }
            
            // Validações de jogador
            if (!player.isOnline()) {
                return false;
            }
            
            if (players.size() >= maxPlayers) {
                player.sendMessage(MessageUtils.error("A sala está cheia!"));
                return false;
            }
            
            if (players.contains(player)) {
                player.sendMessage(MessageUtils.info("Você já está na partida."));
                return false;
            }
            
            // Adicionar jogador
            if (players.add(player)) {
                savePlayerLocation(player);
                playerJoinTimes.put(player, System.currentTimeMillis());
                
                // Teleportar para lobby se configurado
                if (lobbyLocation != null && lobbyLocation.getWorld() != null) {
                    try {
                        player.teleport(lobbyLocation);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Erro ao teleportar jogador " + player.getName() + ": " + e.getMessage());
                    }
                }
                
                // Tocar som de entrada
                if (configManager.isSoundsEnabled()) {
                    try {
                        Sound joinSound = Sound.valueOf(configManager.getPlayerJoinSound());
                        player.playSound(player.getLocation(), joinSound, 
                            configManager.getSoundVolume(), configManager.getSoundPitch());
                    } catch (Exception e) {
                        plugin.getLogger().warning("Erro ao tocar som de entrada: " + e.getMessage());
                    }
                }
                
                // Mensagem de entrada
                String joinMessage = configManager.getGameMessage("join")
                    .replace("{player}", player.getName())
                    .replace("{current}", String.valueOf(players.size()))
                    .replace("{max}", String.valueOf(maxPlayers));
                broadcast(joinMessage);
                
                updateLobbyScoreboard();
                updateJoinSign();
                
                // Iniciar countdown se necessário
                if (players.size() >= minPlayers && (lobbyTask == null || lobbyTask.isCancelled())) {
                    startLobbyCountdown();
                }
                
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao adicionar jogador " + player.getName() + ": " + e.getMessage());
            player.sendMessage(MessageUtils.error("Erro interno ao entrar no jogo."));
        }
        
        return false;
    }
    
    public boolean removePlayer(Player player) {
        if (player == null) return false;
        
        try {
            if (players.remove(player)) {
                // Parar música para o jogador que saiu
                if (state == GameState.INGAME && musicaAtual != null) {
                    player.stopSound(musicaAtual);
                }
                
                // Tocar som de saída
                if (configManager.isSoundsEnabled()) {
                    try {
                        Sound leaveSound = Sound.valueOf(configManager.getPlayerLeaveSound());
                        player.playSound(player.getLocation(), leaveSound, 
                            configManager.getSoundVolume(), configManager.getSoundPitch());
                    } catch (Exception e) {
                        plugin.getLogger().warning("Erro ao tocar som de saída: " + e.getMessage());
                    }
                }
                
                player.sendMessage(MessageUtils.error("Você saiu do BlockParty."));
                returnPlayer(player);
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                
                // Limpar dados do jogador
                playerReturnLocations.remove(player);
                playerJoinTimes.remove(player);
                
                updateLobbyScoreboard();
                updateJoinSign();
                
                // Verificar se precisa parar o lobby
                if (state == GameState.LOBBY && players.size() < minPlayers && lobbyTask != null) {
                    lobbyTask.cancel();
                    broadcast(MessageUtils.info("Jogadores insuficientes. Aguardando mais jogadores..."));
                }
                
                // Verificar se precisa finalizar o jogo
                if (state == GameState.INGAME && players.isEmpty()) {
                    endGame(null);
                }
                
                return true;
            } else {
                player.sendMessage(MessageUtils.info("Você não está em uma partida."));
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao remover jogador " + player.getName() + ": " + e.getMessage());
            player.sendMessage(MessageUtils.error("Erro interno ao sair do jogo."));
        }
        
        return false;
    }
    
    public boolean startGame(boolean forcado) {
        try {
            // Validações de estado
            if (state != GameState.LOBBY) {
                broadcast(MessageUtils.error("Não é possível iniciar o jogo neste momento."));
                return false;
            }
            
            // Validações de jogadores
            if (!forcado && players.size() < minPlayers) {
                String message = configManager.getGameMessage("not-enough-players")
                    .replace("{min}", String.valueOf(minPlayers));
                broadcast(MessageUtils.error(message));
                return false;
            }
            
            // Validações de arena
            if (currentArena == null || !currentArena.isValid()) {
                broadcast(MessageUtils.error("A arena não está configurada!"));
                return false;
            }
            
            // Iniciar jogo
            state = GameState.INGAME;
            gameStartTime = System.currentTimeMillis();
            gamePaused = false;
            
            if (lobbyTask != null) lobbyTask.cancel();
            
            broadcast(configManager.getGameMessage("start"));
            
            // Criar blocos na arena
            criarBlocosArena();
            
            // Teleportar jogadores
            for (Player player : new ArrayList<>(players)) {
                try {
                    savePlayerLocation(player);
                    player.teleport(currentArena.floorLocation);
                } catch (Exception e) {
                    plugin.getLogger().warning("Erro ao teleportar jogador " + player.getName() + ": " + e.getMessage());
                    removePlayer(player);
                }
            }
            
            // Tocar música
            if (configManager.isMusicEnabled()) {
                try {
                    Sound[] availableDiscs = configManager.getAvailableDiscsAsSounds();
                    if (availableDiscs.length > 0) {
                        musicaAtual = availableDiscs[new Random().nextInt(availableDiscs.length)];
                        tocarMusicaParaTodos();
                        startMusicTask(); // Iniciar tarefa de música periódica
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Erro ao tocar música: " + e.getMessage());
                }
            }
            
            // Configurar primeira rodada
            tempoRodadaAtual = rodadaTempoBase;
            rodadaAtual = 1;
            
            // Iniciar primeira rodada
            startNewRound();
            
            // Iniciar timer de tempo máximo se configurado
            if (maxGameTime > 0) {
                startGameTimeTask();
            }
            
            totalGames++;
            totalPlayers += players.size();
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao iniciar jogo: " + e.getMessage());
            state = GameState.ERROR;
            broadcast(MessageUtils.error("Erro interno ao iniciar o jogo."));
            return false;
        }
    }
    
    private void startGameTimeTask() {
        if (gameTimeTask != null) gameTimeTask.cancel();
        
        gameTimeTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state == GameState.INGAME) {
                broadcast(MessageUtils.warning("Tempo máximo de jogo atingido! Finalizando partida..."));
                endGame(null);
            }
        }, maxGameTime * 20L);
    }
    
    private void startFallCheckTask() {
        if (fallCheckTask != null) fallCheckTask.cancel();
        
        fallCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (state == GameState.INGAME && !gamePaused) {
                eliminarJogadoresCaidos();
            }
        }, configManager.getFallCheckInterval(), configManager.getFallCheckInterval());
    }
    
    public void pauseGame() {
        if (state == GameState.INGAME) {
            gamePaused = true;
            broadcast(MessageUtils.info("Jogo pausado temporariamente."));
        }
    }
    
    public void resumeGame() {
        if (state == GameState.INGAME && gamePaused) {
            gamePaused = false;
            broadcast(MessageUtils.info("Jogo retomado!"));
        }
    }
    
    public boolean isGamePaused() {
        return gamePaused;
    }
    
    public long getGameTime() {
        if (state == GameState.INGAME && gameStartTime > 0) {
            return (System.currentTimeMillis() - gameStartTime) / 1000;
        }
        return 0;
    }
    
    public int getTotalGames() {
        return totalGames;
    }
    
    public int getTotalPlayers() {
        return totalPlayers;
    }
    
    public int getPlayerWins(Player player) {
        return playerWins.getOrDefault(player, 0);
    }
    
    public void addPlayerWin(Player player) {
        if (player != null) {
            playerWins.put(player, getPlayerWins(player) + 1);
        }
    }
    
    private void removerBlocosDiferentes(Material tipo) {
        if (currentArena == null) return;
        World world = currentArena.p1.getWorld();
        // Usar o mesmo centro e tamanho da arena 37x37
        int centerX = (currentArena.p1.getBlockX() + currentArena.p2.getBlockX()) / 2;
        int centerZ = (currentArena.p1.getBlockZ() + currentArena.p2.getBlockZ()) / 2;
        int y = currentArena.floorY;
        int minX = centerX - 18;
        int maxX = centerX + 18;
        int minZ = centerZ - 18;
        int maxZ = centerZ + 18;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (world.getBlockAt(x, y, z).getType() != tipo) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }
    
    private void eliminarJogadoresCaidos() {
        Iterator<Player> iterator = players.iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            Location loc = player.getLocation();

            // Elimina apenas se o jogador estiver abaixo do chão da arena
            if (loc.getY() < currentArena.floorY - 1) {
                // Parar música para o jogador eliminado
                if (musicaAtual != null) {
                    player.stopSound(musicaAtual);
                }
                
                // Tocar som de eliminação
                if (configManager.isSoundsEnabled()) {
                    try {
                        Sound eliminatedSound = Sound.valueOf(configManager.getPlayerEliminatedSound());
                        player.playSound(player.getLocation(), eliminatedSound, 
                            configManager.getSoundVolume(), configManager.getSoundPitch());
                    } catch (Exception e) {
                        plugin.getLogger().warning("Erro ao tocar som de eliminação: " + e.getMessage());
                    }
                }
                
                player.sendMessage(MessageUtils.error(configManager.getGameMessage("eliminated")));
                iterator.remove();
                returnPlayer(player);
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
    }
    
    // ===========================================
    // CONFIGURAÇÃO
    // ===========================================
    
    public void setLobby(Location loc) {
        this.lobbyLocation = loc;
        // Salvar em seção separada para não interferir com config principal
        plugin.getConfig().set("locations.lobby.world", loc.getWorld().getName());
        plugin.getConfig().set("locations.lobby.x", loc.getX());
        plugin.getConfig().set("locations.lobby.y", loc.getY());
        plugin.getConfig().set("locations.lobby.z", loc.getZ());
        plugin.saveConfig();
    }
    
    public Location getLobby() {
        return lobbyLocation;
    }
    
    public void setArenaFloor(String arenaName, Location p1, Location p2) {
        currentArena = new ArenaData(arenaName, p1, p2);
        // Salvar em seção separada para não interferir com config principal
        plugin.getConfig().set("locations.arena.name", arenaName);
        plugin.getConfig().set("locations.arena.p1.world", p1.getWorld().getName());
        plugin.getConfig().set("locations.arena.p1.x", p1.getX());
        plugin.getConfig().set("locations.arena.p1.y", p1.getY());
        plugin.getConfig().set("locations.arena.p1.z", p1.getZ());
        plugin.getConfig().set("locations.arena.p2.world", p2.getWorld().getName());
        plugin.getConfig().set("locations.arena.p2.x", p2.getX());
        plugin.getConfig().set("locations.arena.p2.y", p2.getY());
        plugin.getConfig().set("locations.arena.p2.z", p2.getZ());
        plugin.saveConfig();
    }
    
    public ArenaData getCurrentArena() {
        return currentArena;
    }
    
    public void setSpectatorLocation(Location loc) {
        this.spectatorLocation = loc;
        // Salvar em seção separada para não interferir com config principal
        plugin.getConfig().set("locations.spectator.world", loc.getWorld().getName());
        plugin.getConfig().set("locations.spectator.x", loc.getX());
        plugin.getConfig().set("locations.spectator.y", loc.getY());
        plugin.getConfig().set("locations.spectator.z", loc.getZ());
        plugin.saveConfig();
    }
    
    public Location getSpectatorLocation() {
        return spectatorLocation;
    }
    
    public void setJoinSignLocation(Location loc) {
        this.joinSignLocation = loc;
        // Salvar em seção separada para não interferir com config principal
        plugin.getConfig().set("locations.join-sign.world", loc.getWorld().getName());
        plugin.getConfig().set("locations.join-sign.x", loc.getX());
        plugin.getConfig().set("locations.join-sign.y", loc.getY());
        plugin.getConfig().set("locations.join-sign.z", loc.getZ());
        plugin.saveConfig();
    }
    
    public Location getJoinSignLocation() {
        return joinSignLocation;
    }
    
    // ===========================================
    // LÓGICA DO JOGO
    // ===========================================
    
    private void startLobbyCountdown() {
        final int[] timer = {lobbyTimer};
        lobbyTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (state != GameState.LOBBY || players.size() < minPlayers) {
                    lobbyTask.cancel();
                    return;
                }
                
                if (timer[0] <= 10 || timer[0] % 5 == 0) {
                    broadcast("§eA partida começa em §a" + timer[0] + "§e segundos!");
                }
                
                updateLobbyScoreboard();
                
                if (timer[0] <= 0) {
                    startGame(false);
                    return;
                }
                
                timer[0]--;
            }
        }, 0L, 20L);
    }
    
    private void startNewRound() {
        if (state != GameState.INGAME || players.isEmpty()) {
            endGame(null);
            return;
        }

        if (roundTask != null) roundTask.cancel();
        if (countdownTask != null) countdownTask.cancel();

        // Sorteia um novo padrão de piso
        FloorPattern[] patterns = FloorPattern.values();
        floorPattern = patterns[new Random().nextInt(patterns.length)];

        // Recria a arena com o novo padrão
        criarBlocosArena();
        
        // Sortear bloco
        Material[] blocos = {Material.WHITE_TERRACOTTA, Material.RED_TERRACOTTA, Material.BLUE_TERRACOTTA, 
                           Material.GREEN_TERRACOTTA, Material.YELLOW_TERRACOTTA, Material.PURPLE_TERRACOTTA,
                           Material.ORANGE_TERRACOTTA, Material.PINK_TERRACOTTA, Material.LIME_TERRACOTTA};
        blocoAtual = blocos[new Random().nextInt(blocos.length)];
        tempoRestante = tempoRodadaAtual;
        
        // Mostrar bloco no centro da tela
        mostrarBlocoNoCentro(blocoAtual);
        updateScoreboardPartida();
        
        // Countdown da rodada
        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (state != GameState.INGAME) return;
                
                tempoRestante--;
                updateScoreboardPartida();
                
                if (tempoRestante <= 3 && tempoRestante > 0) {
                    broadcast("§cOs blocos errados serão removidos em " + tempoRestante + "...");
                }
                
                if (tempoRestante <= 0) {
                    removerBlocosDiferentes(blocoAtual);
                    broadcast("§cOs blocos errados foram removidos!");
                    eliminarJogadoresCaidos();
                    updateScoreboardPartida();
                    
                    if (players.size() == 1) {
                        Player winner = players.iterator().next();
                        endGame(winner);
                        return;
                    }
                    
                    if (players.isEmpty()) {
                        endGame(null);
                        return;
                    }
                    
                    // Próxima rodada
                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                        @Override
                        public void run() {
                            if (tempoRodadaAtual > rodadaTempoMin) tempoRodadaAtual--;
                            rodadaAtual++;
                            startNewRound();
                        }
                    }, 20L);
                }
            }
        }, 20L, 20L);
    }
    
    private void criarBlocosArena() {
        if (currentArena == null) return;
        World world = currentArena.p1.getWorld();
        int centerX = (currentArena.p1.getBlockX() + currentArena.p2.getBlockX()) / 2;
        int centerZ = (currentArena.p1.getBlockZ() + currentArena.p2.getBlockZ()) / 2;
        int y = currentArena.floorY;
        int minX = centerX - 18; // 37x37
        int maxX = centerX + 18;
        int minZ = centerZ - 18;
        int maxZ = centerZ + 18;
        Material[] blocos = {Material.WHITE_TERRACOTTA, Material.RED_TERRACOTTA, Material.BLUE_TERRACOTTA, 
                Material.GREEN_TERRACOTTA, Material.YELLOW_TERRACOTTA, Material.PURPLE_TERRACOTTA,
                Material.ORANGE_TERRACOTTA, Material.PINK_TERRACOTTA, Material.LIME_TERRACOTTA};
        Random random = new Random();
        int cores = blocos.length;
        switch (floorPattern) {
            case RANDOM:
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Material bloco = blocos[random.nextInt(cores)];
                        world.getBlockAt(x, y, z).setType(bloco);
                    }
                }
                break;
            case MULTICOLOR_CHECKER:
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        int corIndex = (((x - minX) / 2) + ((z - minZ) / 2)) % cores;
                        Material bloco = blocos[corIndex];
                        world.getBlockAt(x, y, z).setType(bloco);
                    }
                }
                break;
            case HORIZONTAL_STRIPES:
                for (int z = minZ; z <= maxZ; z++) {
                    Material bloco = blocos[((z - minZ) / 2) % cores];
                    for (int x = minX; x <= maxX; x++) {
                        world.getBlockAt(x, y, z).setType(bloco);
                    }
                }
                break;
            case VERTICAL_STRIPES:
                for (int x = minX; x <= maxX; x++) {
                    Material bloco = blocos[((x - minX) / 2) % cores];
                    for (int z = minZ; z <= maxZ; z++) {
                        world.getBlockAt(x, y, z).setType(bloco);
                    }
                }
                break;
            case CIRCLES:
                int cx = (minX + maxX) / 2;
                int cz = (minZ + maxZ) / 2;
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        double dist = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(z - cz, 2));
                        int corIndex = (((int) dist) / 2) % cores;
                        Material bloco = blocos[corIndex];
                        world.getBlockAt(x, y, z).setType(bloco);
                    }
                }
                break;
            case SPIRAL:
                cx = (minX + maxX) / 2;
                cz = (minZ + maxZ) / 2;
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        int dx = x - cx;
                        int dz = z - cz;
                        double angle = Math.atan2(dz, dx);
                        double distance = Math.sqrt(dx * dx + dz * dz);
                        int spiralIndex = (((int) (distance / 2 + angle * 6)) % cores);
                        if (spiralIndex < 0) spiralIndex += cores;
                        Material bloco = blocos[spiralIndex];
                        world.getBlockAt(x, y, z).setType(bloco);
                    }
                }
                break;
            case ROWS:
                for (int x = minX; x <= maxX; x++) {
                    Material bloco = blocos[((x - minX) / 2) % cores];
                    for (int z = minZ; z <= maxZ; z++) {
                        world.getBlockAt(x, y, z).setType(bloco);
                    }
                }
                break;
        }
    }
    
    public void endGame(Player winner) {
        if (state != GameState.INGAME) return;
        
        state = GameState.ENDING;
        if (roundTask != null) roundTask.cancel();
        if (countdownTask != null) countdownTask.cancel();
        if (gameTimeTask != null) gameTimeTask.cancel();
        if (fallCheckTask != null) fallCheckTask.cancel();
        
        // Criar uma cópia da lista de jogadores antes de limpar
        Set<Player> jogadoresAtuais = new HashSet<>(players);
        if (winner != null && !jogadoresAtuais.contains(winner)) {
            jogadoresAtuais.add(winner); // Garante que o vencedor receba o anúncio
        }
        
        // Parar música para todos os jogadores que estavam no jogo
        if (musicaAtual != null) {
            for (Player player : jogadoresAtuais) {
                player.stopSound(musicaAtual);
            }
            musicaAtual = null;
        }
        
        // Anunciar ganhador para todos os jogadores ANTES de retorná-los
        if (winner != null) {
                    // Anúncio especial para o ganhador
        winner.showTitle(net.kyori.adventure.title.Title.title(
            LegacyComponentSerializer.legacyAmpersand().deserialize("§6🏆 VITÓRIA! 🏆"),
            LegacyComponentSerializer.legacyAmpersand().deserialize("§aVocê venceu o BlockParty!")
        ));
            
            // Anúncio para todos os jogadores que estavam no jogo
            for (Player player : jogadoresAtuais) {
                player.sendMessage(MessageUtils.format("§6"));
                player.sendMessage(MessageUtils.format("§6§l🏆 " + winner.getName() + " VENCEU O BLOCKPARTY! 🏆"));
                player.sendMessage(MessageUtils.format("§6"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
            addPlayerWin(winner);
        } else {
            // Anúncio para todos os jogadores que estavam no jogo
            for (Player player : jogadoresAtuais) {
                player.sendMessage(MessageUtils.format("§ePartida finalizada sem vencedor!"));
            }
        }
        
        // Aguardar um pouco para todos verem o anúncio
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                // Retornar jogadores
                for (Player player : jogadoresAtuais) {
                    returnPlayer(player);
                    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }
                
                players.clear();
                state = GameState.LOBBY;
                
                // Atualizar placa de join
                updateJoinSign();
                
                // Mostrar um floor pattern para não ficar vazio
                if (currentArena != null) {
                    // Usar um padrão fixo para mostrar que a arena está pronta
                    FloorPattern originalPattern = floorPattern;
                    floorPattern = FloorPattern.RANDOM;
                    criarBlocosArena();
                    floorPattern = originalPattern;
                }
                
                // Anunciar que o jogo acabou para todos os jogadores online
                Bukkit.broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize("§aBlockParty finalizado! Use /blockparty join para jogar novamente."));
            }
        }, gameEndDelay * 20L); // Aguardar 3 segundos (60 ticks)
    }
    
    // ===========================================
    // SCOREBOARD
    // ===========================================
    
    private void updateLobbyScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("lobby", "dummy", configManager.getLobbyScoreboardTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // Obter linhas do scoreboard da configuração
        String[] lines = configManager.getLobbyScoreboardLines();
        
        // Aplicar placeholders e definir scores
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // Substituir placeholders
            line = line.replace("{players}", String.valueOf(players.size()));
            line = line.replace("{max}", String.valueOf(maxPlayers));
            line = line.replace("{min}", String.valueOf(minPlayers));
            line = line.replace("{status}", "Aguardando...");
            
            // Definir score (de baixo para cima, começando em 1)
            objective.getScore(line).setScore(lines.length - i);
        }
        
        for (Player player : players) {
            player.setScoreboard(board);
        }
    }
    
    private void updateScoreboardPartida() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("partida", "dummy", configManager.getGameScoreboardTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Obter linhas do scoreboard da configuração
        String[] lines = configManager.getGameScoreboardLines();
        
        // Calcular tempo total em formato legível
        long totalSeconds = 0;
        if (gameStartTime > 0) {
            totalSeconds = (System.currentTimeMillis() - gameStartTime) / 1000;
        }
        String totalTimeFormatted = String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
        
        // Aplicar placeholders e definir scores
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            // Substituir placeholders
            line = line.replace("{round}", String.valueOf(rodadaAtual));
            line = line.replace("{block}", blocoAtual != null ? getBlocoDisplayName(blocoAtual) : "§7...");
            line = line.replace("{time}", String.valueOf(tempoRestante));
            line = line.replace("{players}", String.valueOf(players.size()));
            line = line.replace("{total-time}", totalTimeFormatted);
            
            // Definir score (de baixo para cima, começando em 1)
            objective.getScore(line).setScore(lines.length - i);
        }

        for (Player player : players) {
            player.setScoreboard(board);
        }
    }
    
    // ===========================================
    // UTILITÁRIOS
    // ===========================================
    
    public void broadcast(String msg) {
        for (Player player : players) {
            player.sendMessage(MessageUtils.format(msg));
        }
    }
    
    public void savePlayerLocation(Player player) {
        playerReturnLocations.put(player, player.getLocation());
    }
    
    public void returnPlayer(Player player) {
        Location returnLoc = playerReturnLocations.remove(player);
        if (returnLoc != null) {
            player.teleport(returnLoc);
        }
    }
    
    public boolean isPlayerInGame(Player player) {
        return players.contains(player);
    }
    
    public boolean isGameInProgress() {
        return state == GameState.INGAME;
    }
    
    public void cancelAllTasks() {
        if (lobbyTask != null) lobbyTask.cancel();
        if (roundTask != null) roundTask.cancel();
        if (countdownTask != null) countdownTask.cancel();
        if (gameTimeTask != null) gameTimeTask.cancel();
        if (fallCheckTask != null) fallCheckTask.cancel();
        if (musicTask != null) musicTask.cancel();
    }
    
    public void reloadConfig() {
        this.configManager = new ConfigManager(plugin);
        loadConfig();
        loadSavedLocations();
    }
    
    public String getStatus() {
        return state == GameState.INGAME
                ? "§aPartida em andamento. Jogadores: " + players.size()
                : "§eAguardando jogadores... (" + players.size() + "/" + maxPlayers + ")";
    }
    
    private void tocarMusicaParaTodos() {
        if (musicaAtual == null) return;
        
        // Obter volume e pitch das configurações
        float volume = configManager.getMusicVolume();
        float pitch = configManager.getMusicPitch();
        
        // Aumentar volume para arenas grandes
        if (currentArena != null && currentArena.size >= configManager.getLargeArenaMinSize()) {
            float multiplier = configManager.getLargeArenaVolumeMultiplier();
            volume = Math.min(volume * multiplier, 1.0f); // Aplicar multiplicador, mas não ultrapassar 1.0
        }
        
        for (Player player : players) {
            player.playSound(player.getLocation(), musicaAtual, volume, pitch);
        }
    }
    
    private void startMusicTask() {
        if (musicTask != null) musicTask.cancel();
        
        // Tocar música a cada 30 segundos para manter o volume consistente
        musicTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (state == GameState.INGAME && !gamePaused && musicaAtual != null) {
                tocarMusicaParaTodos();
            }
        }, 600L, 600L); // 600 ticks = 30 segundos
    }
    
    // ===========================================
    // MÉTODOS DE EXIBIÇÃO
    // ===========================================
    
    private void mostrarBlocoNoCentro(Material bloco) {
        String blocoNome = getBlocoDisplayName(bloco);
        String titulo = "§6§l" + blocoNome;
        String subtitulo = "§eFique neste bloco para sobreviver!";
        
        for (Player player : players) {
            player.showTitle(net.kyori.adventure.title.Title.title(
                LegacyComponentSerializer.legacyAmpersand().deserialize(titulo),
                LegacyComponentSerializer.legacyAmpersand().deserialize(subtitulo)
            ));
            // Tocar som de notificação
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        }
    }
    
    private String getBlocoDisplayName(Material bloco) {
        switch (bloco) {
            case WHITE_TERRACOTTA: return "§fBloco Branco";
            case RED_TERRACOTTA: return "§cBloco Vermelho";
            case BLUE_TERRACOTTA: return "§9Bloco Azul";
            case GREEN_TERRACOTTA: return "§aBloco Verde";
            case YELLOW_TERRACOTTA: return "§eBloco Amarelo";
            case PURPLE_TERRACOTTA: return "§5Bloco Roxo";
            case ORANGE_TERRACOTTA: return "§6Bloco Laranja";
            case PINK_TERRACOTTA: return "§dBloco Rosa";
            case LIME_TERRACOTTA: return "§aBloco Lima";
            default: return bloco.name();
        }
    }

    public void updateJoinSign() {
        // Atualizar placa principal (compatibilidade)
        if (joinSignLocation != null) {
            Block block = joinSignLocation.getBlock();
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                sign.setLine(0, "§6[BlockParty]");
                sign.setLine(1, "§eClique para");
                sign.setLine(2, "§eEntrar!");
                sign.setLine(3, "§fJogadores: §a" + players.size() + "§7/§c" + maxPlayers);
                sign.update();
            }
        }
        
        // Atualizar todas as placas das arenas
        updateAllArenaSigns();
    }
}