package promcgames.customevents.game;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DeathmatchStartEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private World arena = null;
    
    public DeathmatchStartEvent(World arena) {
    	this.arena = arena;
    }
    
    public World getArena() {
    	return this.arena;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
