package uk.co.tggl.pluckerpluck.telesave;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

/**
 * User: Pluckerpluck
 * Date: 08/11/11
 * Time: 18:43
 */
public class TeleSave extends JavaPlugin{

    // Register the logger
    static final Logger log = Logger.getLogger("Minecraft");
    static String pluginName = "";
    static YamlConfiguration config;

    // Register listeners
    TeleSavePlayerListener playerListener;


    public void onDisable() {
    }

    public void onEnable() {
        // Set plugin info tags
        PluginDescriptionFile pdfFile = this.getDescription();
        pluginName = pdfFile.getName();

        log.info("[" + pluginName +"] enabling...");

        // Grab config file here
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (configFile.exists()){
            try{
                config = new YamlConfiguration();
                config.load(configFile);
            }catch(Exception e){
                e.printStackTrace();
            }
        }else{
            TeleSave.log.info("[" + pluginName +"] Config file not found, creating blank file...");
            createDefaultConfig();
        }


        // Initialize listeners
        playerListener = new TeleSavePlayerListener(this);

        // Load events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_PORTAL, playerListener, Event.Priority.Highest, this);
    }

    static void saveConfig(String fileName, FileConfiguration configuration){
        // Define plugin to allow class to be static
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);

        File saveFile = new File(plugin.getDataFolder(), fileName);
        try{
            configuration.save(saveFile);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    static YamlConfiguration loadYamlConfig(String name){
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        // Grab saved locations
        File savesFile = new File(plugin.getDataFolder(), name);
        YamlConfiguration config = new YamlConfiguration();
        if (savesFile.exists()){
            try{
                config.load(savesFile);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return config;
    }

    private void createDefaultConfig(){
        File savesFile = new File(this.getDataFolder(), "config.yml");
        YamlConfiguration config = new YamlConfiguration();
        try{
            config.save(savesFile);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
