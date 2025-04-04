package dev.tn3w.paperlogin.commands;

import dev.tn3w.paperlogin.services.AuthenticationService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VerifyCommand implements CommandExecutor {
    private final AuthenticationService authService;

    public VerifyCommand(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players")
                    .color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /verify <code>")
                    .color(NamedTextColor.RED));
            return false;
        }
        
        String verificationCode = args[0];
        String key = "paperlogin:web:" + verificationCode;
        
        // Check if the code exists in Redis
        if (!authService.verifyCodeExists(verificationCode)) {
            player.sendMessage(Component.text("Invalid verification code")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        // Store user information into the Redis field
        boolean success = authService.storeUserInfoForCode(player, verificationCode);
        
        if (success) {
            // Show verification message only on success
            player.sendMessage(Component.text("✓ Verification successful")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD));
        } else {
            player.sendMessage(Component.text("Verification failed")
                    .color(NamedTextColor.RED));
        }
        
        return true;
    }
} 