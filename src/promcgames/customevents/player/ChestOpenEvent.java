package promcgames.customevents.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChestOpenEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player = null;
    private Block block = null;
    
    public ChestOpenEvent(Player player, Block block) {
    	this.player = player;
    	this.block = block;
    }
    
    public Player getPlayer() {
    	return this.player;
    }
    
    public Block getChest() {
    	return this.block;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
