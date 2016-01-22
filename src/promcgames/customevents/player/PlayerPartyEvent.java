package promcgames.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import promcgames.player.PartyHandler.Party;

public class PlayerPartyEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private PartyEvent partyEvent = null;
    private Party party = null;
    private boolean cancelled = false;
    public enum PartyEvent {CREATE, ADD_PLAYER}
    
    public PlayerPartyEvent(Player player, PartyEvent partyEvent) {
    	this(player, partyEvent, null);
    }
    
    public PlayerPartyEvent(Player player, PartyEvent partyEvent, Party party) {
    	this.player = player;
    	this.partyEvent = partyEvent;
    	this.party = party;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public PartyEvent getPartyEvent() {
    	return this.partyEvent;
    }
    
    public Party getParty() {
    	return this.party;
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
