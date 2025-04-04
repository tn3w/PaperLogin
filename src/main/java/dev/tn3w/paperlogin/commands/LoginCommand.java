package dev.tn3w.paperlogin.commands;

import dev.tn3w.paperlogin.services.AuthenticationService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LoginCommand implements CommandExecutor {
    private final AuthenticationService authService;

    public LoginCommand(AuthenticationService authService) {
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
        
        // Generate login code
        boolean isExistingCode = authService.hasExistingLoginCode(player);
        String code = authService.generateLoginCode(player);
        
        // Send code to player
        Component message = Component.text("Login Code: ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(code)
                        .color(NamedTextColor.AQUA)
                        .decorate(TextDecoration.BOLD));
        
        // If website URL is configured, display it
        String websiteUrl = authService.getWebsiteUrlWithCode(code);
        if (websiteUrl != null) {
            message = message.append(Component.newline())
                    .append(Component.text("Click to login: ")
                            .color(NamedTextColor.YELLOW)
                            .append(Component.text(websiteUrl)
                                    .color(NamedTextColor.AQUA)
                                    .decorate(TextDecoration.UNDERLINED)
                                    .clickEvent(ClickEvent.openUrl(websiteUrl))));
        }
        
        message = message.append(Component.newline())
                .append(Component.text("Expires in 5 minutes")
                        .color(NamedTextColor.GRAY)
                        .decorate(TextDecoration.ITALIC));
        
        player.sendMessage(message);
        
        return true;
    }
} 