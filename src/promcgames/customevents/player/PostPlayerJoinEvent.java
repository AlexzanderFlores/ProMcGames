package promcgames.customevents.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import promcgames.customevents.timed.OneTickTaskEvent;
import promcgames.player.Disguise;
import promcgames.server.util.EventUtil;

public class PostPlayerJoinEvent extends Event implements Listener {
	private static List<String> players = null;
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    
    public PostPlayerJoinEvent() {
    	players = new ArrayList<String>();
    	EventUtil.register(this);
    }
 
    public PostPlayerJoinEvent(Player player) {
    	this.player = player;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @EventHandler
    public void onOneTickTask(OneTickTaskEvent event) {
    	for(Player player : Bukkit.getOnlinePlayers()) {
    		if(!players.contains(Disguise.getName(player)) && player.getTicksLived() >= 20) {
    			players.add(Disguise.getName(player));
    			Bukkit.getPluginManager().callEvent(new PostPlayerJoinEvent(player));
    		}
    	}
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
    	players.remove(event.getPlayer().getName());
    }
}
