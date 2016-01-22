package promcgames.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import promcgames.player.PartyHandler.Party;

public class PartyChangeServerEvent extends Event implements Listener {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private Player player = null;
    private Party party = null;
 
    public PartyChangeServerEvent(Player player, Party party) {
    	this.player = player;
    	this.party = party;
    }
    
    public boolean isCancelled() {
    	return this.cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
    	this.cancelled = cancelled;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public Party getParty() {
    	return this.party;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
