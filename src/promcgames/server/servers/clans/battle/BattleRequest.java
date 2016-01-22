package promcgames.server.servers.clans.battle;

import org.bukkit.entity.Player;

import promcgames.server.servers.clans.Clan;

public class BattleRequest {
	private Clan requestee = null;
	private Clan requester = null;
	private Player playerRequestee = null;
	private Player playerRequester = null;
	
	public BattleRequest(Clan requestee, Clan requester, Player playerRequestee, Player playerRequester) {
		this.requestee = requestee;
		this.requester = requester;
		this.playerRequestee = playerRequestee;
		this.playerRequester = playerRequester;
	}
	
	public Clan getRequestee() {
		return requestee;
	}
	
	public Clan getRequester() {
		return requester;
	}
	
	public Player getPlayerRequestee() {
		return playerRequestee;
	}
	
	public Player getPlayerRequester() {
		return playerRequester;
	}
}