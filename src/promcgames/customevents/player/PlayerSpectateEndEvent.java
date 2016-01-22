package promcgames.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerSpectateEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private boolean cancelled = false;
 
    public PlayerSpectateEndEvent(Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
    	return player;
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