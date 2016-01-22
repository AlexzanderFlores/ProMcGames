package promcgames.customevents.player.timed;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerFiveMinuteConnectedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    
    public PlayerFiveMinuteConnectedEvent(Player player) {
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
}