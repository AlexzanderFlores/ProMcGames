package promcgames.customevents.player;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import promcgames.player.PartyHandler.Party;

public class PartyDeleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Party party = null;
    
    public PartyDeleteEvent(Party party) {
    	this.party = party;
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
