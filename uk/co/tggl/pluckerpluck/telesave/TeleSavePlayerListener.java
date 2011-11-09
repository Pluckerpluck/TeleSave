package uk.co.tggl.pluckerpluck.telesave;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Pluckerpluck
 * Date: 08/11/11
 * Time: 18:57
 * To change this template use File | Settings | File Templates.
 */
public class TeleSavePlayerListener extends PlayerListener {
    ArrayList<String> worldList = new ArrayList<String>();
    final TeleSave plugin;

    public TeleSavePlayerListener(TeleSave instance) {

        plugin = instance;

        //Get affected worlds and store in an array of lowercase strings
        List tempList = TeleSave.config.getList("worlds");
        if (tempList != null){
            for (Object next : tempList) {
                if (next instanceof String) {
                    worldList.add(((String) next).toLowerCase());
                }
            }
        }
    }

    public void onPlayerTeleport(PlayerTeleportEvent event){
        // Skip if player is ignored
        if (checkIgnore(event.getPlayer())){
            return;
        }

        Location to = event.getTo();
        Location from = event.getFrom();

        // Set new location based on plugin controls
        event.setTo(checkWorldChange(to, from, event.getPlayer().getName()));
    }

    public void onPlayerPortal(PlayerPortalEvent event){
        // Skip if player is ignored
        if (checkIgnore(event.getPlayer())){
            return;
        }

        Location to = event.getTo();
        Location from = event.getFrom();

        // Set new location based on plugin controls
        event.setTo(checkWorldChange(to, from, event.getPlayer().getName()));
    }

    public void onPlayerRespawn(PlayerRespawnEvent event){
        String playerName = event.getPlayer().getName();

        // Skip if player is ignored
        if (checkIgnore(event.getPlayer())){
            return;
        }

        Location to = event.getRespawnLocation();
        Location from = event.getPlayer().getLocation();

        Location newLocation = checkWorldChange(to, from, playerName);

        // If location has changed set new respawn and remove stored location in dead world
        if (newLocation.equals(to)){
            event.setRespawnLocation(newLocation);
            YamlConfiguration savedLocations = TeleSave.loadYamlConfig("saves" + File.separator + playerName + ".yml");
            savedLocations.set(from.getWorld().getName(), null);
            TeleSave.saveConfig("saves" + File.separator + playerName + ".yml", savedLocations);
        }
    }

    private Location checkWorldChange(Location to, Location from, String player){
        World fromWorld = from.getWorld();
        World toWorld = to.getWorld();

        Location newLocation = to;

        if (!fromWorld.equals(toWorld)){
            newLocation = playerChangingWorld(to, from, player);
        }
        return newLocation;
    }

    public Location playerChangingWorld(Location to, Location from, String player){

        // New Location to return
        Location newLocation = to;

        // Check the from world to see if it's a saved world
        String worldFrom = from.getWorld().getName();
        if (worldList.contains(worldFrom.toLowerCase())){
            saveLocation(from, worldFrom, player);
        }
        // Check the destination world to see if it's a saved world
        String worldTo = to.getWorld().getName();
        if (worldList.contains(worldTo.toLowerCase())){
            Location tempLocation = getSavedLocation(worldTo, player);
            if (tempLocation != null){
                newLocation = tempLocation;
            }
        }
        return newLocation;
    }

    private Location getSavedLocation(String locationKey, String player){
        YamlConfiguration savedLocations = TeleSave.loadYamlConfig("saves" + File.separator + player + ".yml");
        String locationString = savedLocations.getString(locationKey);
        Location newLocation = null;
        if (locationString != null){
            String[] locationParts = locationString.split(",");

            // Get world from locationKey name
            World world = Bukkit.getWorld(locationKey);

            // Parse string into number forms
            double x = Double.parseDouble(locationParts[0]);
            double y = Double.parseDouble(locationParts[1]);
            double z = Double.parseDouble(locationParts[2]);
            float yaw = Float.parseFloat(locationParts[3]);
            float pitch = Float.parseFloat(locationParts[4]);

            //Create a new location with the new co-ordinates
            newLocation = new Location(world, x, y, z, yaw, pitch);
        }
        return newLocation;
    }

    private void saveLocation(Location location, String locationKey, String player){
        YamlConfiguration savedLocations = TeleSave.loadYamlConfig("saves" + File.separator + player + ".yml");

        // Get all the co-ordinated of the location, ready to save
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();

        // Convert location into string we can store
        String locationString = x + "," + y + "," + z + "," + yaw + "," + pitch;

        savedLocations.set(locationKey, locationString);
        TeleSave.saveConfig("saves" + File.separator + player + ".yml", savedLocations);
    }

    //Function to check whether a player should be ignored or not
    private boolean checkIgnore(Player player){
        boolean returnBool = false;
        if (player.hasPermission("TeleSave.mod.ignore")){
            returnBool = true;
        }
        return returnBool;
    }


}

