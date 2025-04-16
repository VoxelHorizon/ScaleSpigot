package com.voxelhorizons.scalespigot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.littleadventures.shaded.jedis.Jedis;

public class ScaleSpigot extends JavaPlugin {
    private Jedis redis;

    @Override
    public void onEnable() {
        String redisHost = System.getenv("REDIS_HOST");
        if (redisHost == null || redisHost.isEmpty()) {
            redisHost = "localhost"; // fallback if not set
            getLogger().warning("REDIS_HOST environment variable not set. Defaulting to localhost.");
        }

        String redisPortEnv = System.getenv("REDIS_PORT");
        int redisPort;
        if (redisPortEnv == null || redisPortEnv.isEmpty()) {
            redisPort = 6379;
            getLogger().warning("REDIS_PORT environment variable not set. Defaulting to 6379.");
        } else {
            try {
                redisPort = Integer.parseInt(redisPortEnv);
            } catch (NumberFormatException e) {
                getLogger().warning("REDIS_PORT is not a valid number. Defaulting to 6379.");
                redisPort = 6379;
            }
        }

        redis = new Jedis(redisHost, redisPort);

        String serverName = System.getenv("SERVER_NAME");
        if (serverName != null) {
            getLogger().info("Server Name: " + serverName);
        } else {
            getLogger().warning("SERVER_NAME environment variable is not set!");
            serverName = getServer().getName();
        }

        String host = Bukkit.getIp();
        int port = Bukkit.getPort();

        String message = String.format("{\"server\":\"%s\",\"host\":\"%s\",\"port\":%d}", serverName, host, port);
        redis.publish("server:register", message);

        getLogger().info("Published server registration to Redis.");
    }

    @Override
    public void onDisable() {
        if (redis != null) {
            String serverName = System.getenv("SERVER_NAME");
            if (serverName == null) {
                serverName = getServer().getName();
            }

            String message = String.format("{\"server\":\"%s\"}", serverName);
            redis.publish("server:deregister", message);
            getLogger().info("Published server deregistration to Redis.");

            redis.close();
        }
    }
}
