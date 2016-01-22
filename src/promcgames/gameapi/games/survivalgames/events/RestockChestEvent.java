package promcgames.gameapi.games.survivalgames.events;

import org.bukkit.block.Chest;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RestockChestEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Chest chest = null;
    
    public RestockChestEvent(Chest chest) {
    	this.chest = chest;
    }
    
    public Chest getChest() {
    	return this.chest;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
