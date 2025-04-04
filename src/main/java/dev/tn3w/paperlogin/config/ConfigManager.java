package dev.tn3w.paperlogin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private String redisHost;
    private int redisPort;
    private String redisPassword;
    private boolean redisAuthEnabled;
    private int loginCodeLength;
    private int loginCodeValiditySeconds;
    private int webCodeValiditySeconds;
    private String websiteUrl;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        
        // Load Redis configuration
        this.redisHost = config.getString("redis.host", "localhost");
        this.redisPort = config.getInt("redis.port", 6379);
        this.redisPassword = config.getString("redis.password", "");
        this.redisAuthEnabled = config.getBoolean("redis.auth-enabled", false);
        
        // Load authentication settings
        this.loginCodeLength = config.getInt("auth.login-code-length", 9);
        this.loginCodeValiditySeconds = config.getInt("auth.login-code-validity", 300);
        this.webCodeValiditySeconds = config.getInt("auth.web-code-validity", 600);
        this.websiteUrl = config.getString("auth.website-url", "");
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisPassword() {
        return redisAuthEnabled ? redisPassword : null;
    }
    
    public int getLoginCodeValiditySeconds() {
        return loginCodeValiditySeconds;
    }
    
    public int getWebCodeValiditySeconds() {
        return webCodeValiditySeconds;
    }
    
    public int getLoginCodeLength() {
        return loginCodeLength;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public boolean hasWebsiteUrl() {
        return websiteUrl != null && !websiteUrl.isEmpty();
    }
} 