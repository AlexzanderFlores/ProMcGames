package promcgames.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class XPFillEvent_ extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
 
    public XPFillEvent_(Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
    	return player;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}