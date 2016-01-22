package promcgames.server.servers.hub.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerRidePlayerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player topPlayer = null;
    private Player bottomPlayer = null;
    private boolean cancelled = false;
 
    public PlayerRidePlayerEvent(Player topPlayer, Player bottomPlayer) {
        this.topPlayer = topPlayer;
        this.bottomPlayer = bottomPlayer;
    }
    
    public Player getTopPlayer() {
    	return topPlayer;
    }
    
    public Player getBottomPlayer() {
    	return bottomPlayer;
    }
    
    public boolean isCancelled() {
    	return this.cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
    	this.cancelled = cancelled;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}