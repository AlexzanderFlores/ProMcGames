package promcgames.customevents.tournament;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TournamentEndingEvent extends Event {
	
private static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }
	
}