package promcgames.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import promcgames.gameapi.kits.KitBase;

public class SpecialKitDenyEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private KitBase kit = null;
    private boolean cancelled = false;
    
    public SpecialKitDenyEvent(Player player, KitBase kit) {
    	this.player = player;
    	this.kit = kit;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public KitBase getKit() {
    	return this.kit;
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
