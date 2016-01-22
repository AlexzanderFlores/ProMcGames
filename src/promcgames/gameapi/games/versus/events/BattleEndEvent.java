package promcgames.gameapi.games.versus.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import promcgames.gameapi.games.versus.kits.VersusKit;

public class BattleEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player loser = null;
    private Player winner = null;
    private VersusKit kit = null;
    
    public BattleEndEvent(Player winner, Player loser, VersusKit kit) {
    	this.winner = winner;
    	this.loser = loser;
    	this.kit = kit;
    }
    
    public Player getWinner() {
    	return this.winner;
    }
    
    public Player getLoser() {
    	return this.loser;
    }
    
    public VersusKit getKit() {
    	return this.kit;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}