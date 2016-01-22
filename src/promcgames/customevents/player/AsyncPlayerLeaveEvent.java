package promcgames.customevents.player;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncPlayerLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private UUID uuid = null;
    private UUID realUUID = null;
    private String name = null;
    private String realName = null;
    
    public AsyncPlayerLeaveEvent(UUID uuid, UUID realUUID, String name, String realName) {
    	this.uuid = uuid;
    	this.realUUID = realUUID;
    	this.name = name;
    	this.realName = realName;
    }
    
    public UUID getUUID() {
    	return this.uuid;
    }
    
    public UUID getRealUUID() {
    	return this.realUUID;
    }
    
    public String getName() {
    	return this.name;
    }
    
    public String getRealName() {
    	return this.realName;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
