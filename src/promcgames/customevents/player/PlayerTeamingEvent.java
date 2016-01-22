package promcgames.customevents.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerTeamingEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player playerOne = null;
    private Player playerTwo = null;
    
    public PlayerTeamingEvent(Player playerOne, Player playerTwo) {
    	this.playerOne = playerOne;
    	this.playerTwo = playerTwo;
    }
    
    public Player getPlayerOne() {
    	return this.playerOne;
    }
    
    public Player getPlayerTwo() {
    	return this.playerTwo;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
