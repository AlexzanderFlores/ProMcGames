package promcgames.server.servers.clans;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.account.AccountHandler;
import promcgames.server.DB;
import promcgames.server.util.EventUtil;

public class ClansHubStatsHandler implements Listener {
	private static Map<String, ClanStats> clanStatsList = null;
	
	public static class ClanStats {
		private int wins = 0;
		private int losses = 0;
		private int kills = 0;
		private int deaths = 0;
		
		public ClanStats(int wins, int losses, int kills, int deaths) {
			this.wins = wins;
			this.losses = losses;
			this.kills = kills;
			this.deaths = deaths;
		}
		
		public int getWins() {
			return wins;
		}
		
		public int getLosses() {
			return losses;
		}
		
		public int getKills() {
			return kills;
		}
		
		public int getDeaths() {
			return deaths;
		}
	}
	
	public ClansHubStatsHandler() {
		clanStatsList = new HashMap<String, ClanStats>();
		EventUtil.register(this);
	}
	
	public static ClanStats getClanStats(UUID uuid) {
		String name = AccountHandler.getName(uuid);
		if(name != null) {
			if(clanStatsList.containsKey(name)) {
				return clanStatsList.get(name);
			}
			if(DB.PLAYERS_CLANS.isKeySet("uuid", uuid.toString())) {
				int wins = DB.PLAYERS_CLANS.getInt("uuid", uuid.toString(), "battle_wins");
				int deaths = DB.PLAYERS_CLANS.getInt("uuid", uuid.toString(), "battle_deaths");
				int kills = DB.PLAYERS_CLANS.getInt("uuid", uuid.toString(), "battle_kills");
				int losses = DB.PLAYERS_CLANS.getInt("uuid", uuid.toString(), "battle_losses");
				ClanStats clanStats = new ClanStats(wins, losses, kills, deaths);
				clanStatsList.put(name, clanStats);
				return clanStats;
			}
		}
		return null;
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		String name = event.getPlayer().getName();
		if(clanStatsList.containsKey(name)) {
			clanStatsList.remove(name);
		}
	}
}