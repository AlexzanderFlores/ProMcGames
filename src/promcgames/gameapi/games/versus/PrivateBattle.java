package promcgames.gameapi.games.versus;

import org.bukkit.entity.Player;

import promcgames.gameapi.games.versus.kits.VersusKit;

public class PrivateBattle {
	private Player challenger;
	private Player challenged;
	private VersusKit kit = null;
	
	public PrivateBattle(Player challenger, Player challeneged, VersusKit kit) {
		this.challenger = challenger;
		this.challenged = challeneged;
		this.kit = kit;
	}
	
	public Player getChallenger() {
		return challenger;
	}
	
	public void setChallenger(Player challenger) {
		this.challenger = challenger;
	}
	
	public Player getChallenged() {
		return challenged;
	}
	
	public void setChallenged(Player challenged) {
		this.challenged = challenged;
	}
	
	public VersusKit getKit() {
		return kit;
	}
	
	public void setKit(VersusKit kit) {
		this.kit = kit;
	}
	
	public void remove() {
		challenger = null;
		challenged = null;
		kit = null;
	}
}
