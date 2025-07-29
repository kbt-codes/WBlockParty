package me.willowdev.blockparty.utils;

import org.bukkit.configuration.file.FileConfiguration;

public class MessageUtils {
    
    private static String prefix = "§6[BlockParty] §r";
    private static String successColor = "§a";
    private static String errorColor = "§c";
    private static String infoColor = "§e";
    private static String warningColor = "§6";
    
    public static void loadFromConfig(FileConfiguration config) {
        // Tentar carregar do novo sistema de mensagens
        if (config.contains("prefix")) {
            prefix = config.getString("prefix", "§6[BlockParty] §r");
        } else {
            // Fallback para o sistema antigo
            prefix = config.getString("message-prefix", "§6[BlockParty] §r");
        }
        
        // Carregar cores do novo sistema
        if (config.contains("colors")) {
            successColor = config.getString("colors.success", "§a");
            errorColor = config.getString("colors.error", "§c");
            infoColor = config.getString("colors.info", "§e");
            warningColor = config.getString("colors.warning", "§6");
        } else {
            // Fallback para o sistema antigo
            successColor = config.getString("success-color", "§a");
            errorColor = config.getString("error-color", "§c");
            infoColor = config.getString("info-color", "§e");
            warningColor = config.getString("warning-color", "§6");
        }
    }
    
    public static void loadFromConfigManager(ConfigManager configManager) {
        prefix = configManager.getMessagePrefix();
        successColor = configManager.getMessageColor("success");
        errorColor = configManager.getMessageColor("error");
        infoColor = configManager.getMessageColor("info");
        warningColor = configManager.getMessageColor("warning");
    }
    
    public static String getPrefix() {
        return prefix != null ? prefix : "§6[BlockParty] §r";
    }
    
    public static String success(String message) {
        return getPrefix() + successColor + message;
    }
    
    public static String error(String message) {
        return getPrefix() + errorColor + message;
    }
    
    public static String info(String message) {
        return getPrefix() + infoColor + message;
    }
    
    public static String warning(String message) {
        return getPrefix() + warningColor + message;
    }
    
    public static String format(String message) {
        if (message == null) return "";
        return message.replace("&", "§");
    }
    
    public static String formatMessage(String message, Object... replacements) {
        if (message == null) return "";
        String formatted = message;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                String placeholder = "{" + replacements[i] + "}";
                String replacement = String.valueOf(replacements[i + 1]);
                formatted = formatted.replace(placeholder, replacement);
            }
        }
        return format(formatted);
    }
    
    public static String getGameMessage(FileConfiguration config, String key, String defaultValue) {
        String message = config.getString("messages.game." + key, defaultValue);
        return format(message);
    }
    
    public static String getTitleMessage(FileConfiguration config, String key, String defaultValue) {
        String message = config.getString("messages.title." + key, defaultValue);
        return format(message);
    }
} 