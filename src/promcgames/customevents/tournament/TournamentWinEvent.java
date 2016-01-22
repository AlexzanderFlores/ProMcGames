package promcgames.customevents.tournament;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TournamentWinEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private Player winner = null;
	
	public TournamentWinEvent(Player winner) {
		this.winner = winner;
	}
	
	public Player getWinner() {
		return winner;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }
	
}