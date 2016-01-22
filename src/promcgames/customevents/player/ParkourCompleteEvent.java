package promcgames.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParkourCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private int seconds = 0;
    private boolean cancelled = false;
    
    public ParkourCompleteEvent(Player player, int seconds) {
    	this.player = player;
    	this.seconds = seconds;
    }
    
    public Player getPlayer() {
    	return player;
    }
    
    public int getSeconds() {
    	return seconds;
    }
    
    public void setCancelled(boolean cancelled) {
    	this.cancelled = cancelled;
    }
    
    public boolean isCancelled() {
    	return cancelled;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
