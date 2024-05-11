package lightwolf.lwcheck;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class LwCheck extends JavaPlugin {
    public static FileConfiguration config;
    public static final String NOTIFY_PERMISSION = "LwCheck.notify";
    public static final Map<Player, Player> checkRequests = new HashMap();
    public static String banReason;
    public static boolean Quit;
    public static String banQuit;
    public static String bancommand;
    public static int banforQuit;
    public static int banDuration;
    public static String banIgnore;
    public static String message1;
    public static String message2;

    public void onEnable() {
        this.getLogger().info("||||||||||||||||||||||||||||||||||||||||||||||||||");
        this.getLogger().info("Плагин успешно включен!");
        this.getLogger().info("Плагин был сделан LightWolf");
        this.getLogger().info("||||||||||||||||||||||||||||||||||||||||||||||||||");
        Check check = new Check(this);
        this.getCommand("check").setExecutor(check);
        Bukkit.getPluginManager().registerEvents(new Events(), this);
        config = this.getConfig();
        config.options().copyDefaults(true);
        this.saveConfig();
        banQuit = config.getString("Ban.banQuit");
        banforQuit = config.getInt("Ban.banquitday");
        Quit = config.getBoolean("Ban.Quit");
        banReason = config.getString("Ban.banReason");
        banDuration = config.getInt("Ban.banDuration");
        bancommand = config.getString("Ban.BanCommand");
        banIgnore = config.getString("Ban.BanIgnore");
        message1 = config.getString("messages.message1");
        message2 = config.getString("messages.message2");
    }
}
