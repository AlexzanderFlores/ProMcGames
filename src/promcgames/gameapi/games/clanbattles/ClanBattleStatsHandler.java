package promcgames.gameapi.games.clanbattles;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import promcgames.customevents.game.GameStatChangeEvent;
import promcgames.gameapi.StatsHandler;
import promcgames.server.DB;

public class ClanBattleStatsHandler extends StatsHandler {
	public ClanBattleStatsHandler() {
		super(DB.PLAYERS_STATS_CLANS);
		setEloDB(DB.PLAYERS_ELO_CLANS);
	}
	
	@EventHandler
	public void onGameStatChange(GameStatChangeEvent event) {
		Player player = event.getPlayer();
		GameStats gameStats = event.getGameStats();
		DB.PLAYERS_CLANS.updateInt("battle_wins", getWins(player) + (gameStats.getWins() - gameStats.getOriginalWins()), "uuid", event.getPlayer().getUniqueId().toString());
		DB.PLAYERS_CLANS.updateInt("battle_losses", getLosses(player) + (gameStats.getLosses() - gameStats.getOriginalLosses()), "uuid", event.getPlayer().getUniqueId().toString());
		DB.PLAYERS_CLANS.updateInt("battle_kills", getKills(player) + (gameStats.getKills() - gameStats.getOriginalKills()), "uuid", event.getPlayer().getUniqueId().toString());
		DB.PLAYERS_CLANS.updateInt("battle_deaths", getDeaths(player) + (gameStats.getDeaths() - gameStats.getOriginalDeaths()), "uuid", event.getPlayer().getUniqueId().toString());
	}
	
	public static int getWins(Player player) {
		return DB.PLAYERS_CLANS.getInt("uuid", player.getUniqueId().toString(), "battle_wins");
	}
	
	public static int getLosses(Player player) {
		return DB.PLAYERS_CLANS.getInt("uuid", player.getUniqueId().toString(), "battle_losses");
	}
	
	public static int getKills(Player player) {
		return DB.PLAYERS_CLANS.getInt("uuid", player.getUniqueId().toString(), "battle_kills");
	}
	
	public static int getDeaths(Player player) {
		return DB.PLAYERS_CLANS.getInt("uuid", player.getUniqueId().toString(), "battle_deaths");
	}
}