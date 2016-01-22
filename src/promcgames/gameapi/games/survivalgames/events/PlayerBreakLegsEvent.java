package promcgames.gameapi.games.survivalgames.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBreakLegsEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private Player player = null;
 
    public PlayerBreakLegsEvent(Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
    	return player;
    }
    
    public void setCancelled(boolean cancelled) {
    	this.cancelled = cancelled;
    }
    
    public boolean isCancelled() {
    	return this.cancelled;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}