package promcgames.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class PlayerServerJoinEvent extends Event implements Listener {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private Player player = null;
    private String server = null;
 
    public PlayerServerJoinEvent(Player player, String server) {
    	this.player = player;
    	this.server = server;
    }
    
    public boolean isCancelled() {
    	return this.cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
    	this.cancelled = cancelled;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public String getServer() {
    	return this.server;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
