package promcgames.gameapi.games.kitpvp.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class JumpDownEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private boolean cancelled = false;
    
    public JumpDownEvent(Player player) {
    	this.player = player;
    }
    
    public Player getPlayer() {
    	return this.player;
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
