package dev.tn3w.paperlogin;

import java.security.SecureRandom;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisException;

public final class PaperLogin extends JavaPlugin {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final String LOGIN_PREFIX = "paperlogin:code:";
    private static final String PLAYER_PREFIX = "paperlogin:player:";
    private static final String WEB_PREFIX = "paperlogin:web:";

    private final SecureRandom random = new SecureRandom();
    private JedisPooled redis;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        var config = getConfig();
        var password = config.getString("redis.password", "");
        redis = new JedisPooled(
                config.getString("redis.host", "localhost"),
                config.getInt("redis.port", 6379),
                null,
                password.isEmpty() ? null : password);
    }

    @Override
    public void onDisable() {
        if (redis != null) redis.close();
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] arguments) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(error("This command can only be used by players"));
            return true;
        }

        boolean isLogin = command.getName().equals("login");
        if (!isLogin && arguments.length != 1) return false;

        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                var reply = isLogin ? login(player) : verify(player, arguments[0]);
                player.sendMessage(reply);
            } catch (JedisException exception) {
                getLogger().warning("Redis error: " + exception.getMessage());
                player.sendMessage(error("Login service unavailable, try again later"));
            }
        });
        return true;
    }

    private Component login(Player player) {
        int validity = getConfig().getInt("auth.login-code-validity", 300);
        String playerKey = PLAYER_PREFIX + player.getUniqueId();
        String code = redis.get(playerKey);

        if (code == null || !redis.exists(LOGIN_PREFIX + code)) {
            code = randomCode(getConfig().getInt("auth.login-code-length", 9));
            redis.hset(LOGIN_PREFIX + code, playerInfo(player));
        }
        redis.expire(LOGIN_PREFIX + code, validity);
        redis.setex(playerKey, validity, code);

        return Component.text("Login code: ", NamedTextColor.GREEN)
                .append(Component.text(code, NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(websiteLink(code))
                .append(Component.newline())
                .append(Component.text(
                        "Expires in " + formatDuration(validity),
                        NamedTextColor.GRAY,
                        TextDecoration.ITALIC));
    }

    private Component verify(Player player, String code) {
        String key = WEB_PREFIX + code;
        if (!redis.exists(key)) return error("Invalid verification code");

        String claimedBy = redis.hget(key, "uuid");
        if (claimedBy != null && !claimedBy.equals(player.getUniqueId().toString())) {
            return error("This code was already used by another player");
        }

        redis.hset(key, playerInfo(player));
        redis.expire(key, getConfig().getInt("auth.web-code-validity", 600));
        return Component.text(
                "✓ Verification successful", NamedTextColor.GREEN, TextDecoration.BOLD);
    }

    private Component websiteLink(String code) {
        String pattern = getConfig().getString("auth.website-url", "");
        if (pattern.isEmpty()) return Component.empty();

        String url = pattern.replace("{code}", code);
        var link = Component.text(url, NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(url));
        return Component.newline()
                .append(Component.text("Click to log in: ", NamedTextColor.YELLOW))
                .append(link);
    }

    private String randomCode(int length) {
        var code = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }

    private static Map<String, String> playerInfo(Player player) {
        return Map.of(
                "uuid", player.getUniqueId().toString(),
                "username", player.getName(),
                "isOp", String.valueOf(player.isOp()));
    }

    private static String formatDuration(int seconds) {
        if (seconds % 60 != 0) return seconds + " seconds";
        int minutes = seconds / 60;
        return minutes + (minutes == 1 ? " minute" : " minutes");
    }

    private static Component error(String message) {
        return Component.text(message, NamedTextColor.RED);
    }
}
