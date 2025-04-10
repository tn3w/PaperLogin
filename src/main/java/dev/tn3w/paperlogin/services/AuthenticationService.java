package dev.tn3w.paperlogin.services;

import dev.tn3w.paperlogin.config.ConfigManager;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AuthenticationService {
  private final RedisService redisService;
  private final JavaPlugin plugin;
  private final ConfigManager configManager;
  private final SecureRandom secureRandom;
  private static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  private static final String LOGIN_KEY_PREFIX = "paperlogin:code:";
  private static final String WEB_KEY_PREFIX = "paperlogin:web:";

  public AuthenticationService(RedisService redisService, JavaPlugin plugin) {
    this.redisService = redisService;
    this.plugin = plugin;
    this.configManager = new ConfigManager(plugin);
    this.configManager.loadConfig();
    this.secureRandom = new SecureRandom();
  }

  /**
   * Generates a login code for a player and stores user info in Redis
   *
   * @param player the player to generate a code for
   * @return the generated login code
   */
  public String generateLoginCode(Player player) {
    String existingCode = findExistingLoginCodeForPlayer(player);
    if (existingCode != null) {
      String key = LOGIN_KEY_PREFIX + existingCode.toUpperCase();
      redisService.expire(key, configManager.getLoginCodeValiditySeconds());
      return existingCode.toUpperCase();
    }

    String code = generateRandomCode(configManager.getLoginCodeLength()).toUpperCase();
    String key = LOGIN_KEY_PREFIX + code;

    Map<String, String> userInfo = new HashMap<>();
    userInfo.put("uuid", player.getUniqueId().toString());
    userInfo.put("username", player.getName());
    userInfo.put("isOp", String.valueOf(player.isOp()));

    for (Map.Entry<String, String> entry : userInfo.entrySet()) {
      redisService.hset(key, entry.getKey(), entry.getValue());
    }

    redisService.expire(key, configManager.getLoginCodeValiditySeconds());

    return code;
  }

  /**
   * Find existing login code for a player
   *
   * @param player the player to find a code for
   * @return the existing code, or null if not found
   */
  private String findExistingLoginCodeForPlayer(Player player) {
    Set<String> keys = redisService.scanKeys(LOGIN_KEY_PREFIX + "*");
    String playerUuid = player.getUniqueId().toString();

    for (String key : keys) {
      String uuid = redisService.hget(key, "uuid");
      if (playerUuid.equals(uuid)) {
        return key.substring(LOGIN_KEY_PREFIX.length());
      }
    }

    return null;
  }

  /**
   * Check if a player has an existing login code
   *
   * @param player the player to check
   * @return true if the player has an existing code
   */
  public boolean hasExistingLoginCode(Player player) {
    return findExistingLoginCodeForPlayer(player) != null;
  }

  /**
   * Get the website URL with the code if configured
   *
   * @param code the login code
   * @return the website URL with the code, or null if not configured
   */
  public String getWebsiteUrlWithCode(String code) {
    if (!configManager.hasWebsiteUrl()) {
      return null;
    }

    return configManager.getWebsiteUrl().replace("{code}", code);
  }

  /**
   * Generates a random code of the specified length
   *
   * @param length the length of the code
   * @return the generated code
   */
  private String generateRandomCode(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int randomIndex = secureRandom.nextInt(CHARACTERS.length());
      sb.append(CHARACTERS.charAt(randomIndex));
    }
    return sb.toString();
  }

  /**
   * Verifies a code from the website
   *
   * @param player the player to verify
   * @param code the verification code from the website
   * @return true if verification was successful
   */
  public boolean verifyWebCode(Player player, String code) {
    String key = WEB_KEY_PREFIX + code;

    if (redisService.exists(key)) {
      String expectedPlayerId = redisService.hget(key, "uuid");

      if (expectedPlayerId != null && expectedPlayerId.equals(player.getUniqueId().toString())) {
        redisService.delete(key);
        return true;
      }
    }

    return false;
  }

  /**
   * Stores a web verification code for a player
   *
   * @param player the player to generate a code for
   * @return the verification code
   */
  public String generateWebVerificationCode(Player player) {
    String code = generateRandomCode(8);
    String key = WEB_KEY_PREFIX + code;

    Map<String, String> userInfo = new HashMap<>();
    userInfo.put("uuid", player.getUniqueId().toString());
    userInfo.put("username", player.getName());
    userInfo.put("isOp", String.valueOf(player.isOp()));

    for (Map.Entry<String, String> entry : userInfo.entrySet()) {
      redisService.hset(key, entry.getKey(), entry.getValue());
    }

    redisService.expire(key, configManager.getWebCodeValiditySeconds());

    return code;
  }

  /**
   * Checks if the given verification code exists in the Redis database
   *
   * @param code the verification code to check
   * @return true if the code exists
   */
  public boolean verifyCodeExists(String code) {
    String key = WEB_KEY_PREFIX + code;
    return redisService.exists(key);
  }

  /**
   * Stores user information in the Redis database for the given code If user information is already
   * stored, it just resets the TTL
   *
   * @param player the player to store information for
   * @param code the verification code
   * @return true if the operation was successful
   */
  public boolean storeUserInfoForCode(Player player, String code) {
    String key = WEB_KEY_PREFIX + code;

    try {
      if (!redisService.exists(key)) {
        return false;
      }

      String identifier = redisService.hget(key, "identifier");
      String existingUuid = redisService.hget(key, "uuid");

      boolean shouldStoreInfo =
          identifier != null
              || existingUuid == null
              || !existingUuid.equals(player.getUniqueId().toString());

      if (shouldStoreInfo) {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("uuid", player.getUniqueId().toString());
        userInfo.put("username", player.getName());
        userInfo.put("isOp", String.valueOf(player.isOp()));

        if (identifier != null) {
          userInfo.put("identifier", identifier);
        }

        for (Map.Entry<String, String> entry : userInfo.entrySet()) {
          redisService.hset(key, entry.getKey(), entry.getValue());
        }
      }

      redisService.expire(key, configManager.getWebCodeValiditySeconds());

      return true;
    } catch (Exception e) {
      plugin.getLogger().warning("Failed to store user info for code: " + e.getMessage());
      return false;
    }
  }
}
