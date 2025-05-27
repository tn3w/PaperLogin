package dev.tn3w.paperlogin;

import dev.tn3w.paperlogin.commands.LoginCommand;
import dev.tn3w.paperlogin.commands.VerifyCommand;
import dev.tn3w.paperlogin.config.ConfigManager;
import dev.tn3w.paperlogin.services.AuthenticationService;
import dev.tn3w.paperlogin.services.RedisService;

import org.bukkit.plugin.java.JavaPlugin;

public class PaperLogin extends JavaPlugin {
    private RedisService redisService;
    private AuthenticationService authService;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.loadConfig();

        this.redisService =
                new RedisService(
                        configManager.getRedisHost(),
                        configManager.getRedisPort(),
                        configManager.getRedisPassword());

        this.authService = new AuthenticationService(redisService, this);

        getCommand("login").setExecutor(new LoginCommand(authService));
        getCommand("verify").setExecutor(new VerifyCommand(authService));

        getLogger().info("PaperLogin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (redisService != null) {
            redisService.shutdown();
        }

        getLogger().info("PaperLogin has been disabled!");
    }
}
