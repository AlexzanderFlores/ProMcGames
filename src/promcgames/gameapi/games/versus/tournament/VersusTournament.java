package promcgames.gameapi.games.versus.tournament;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.tournament.TournamentRoundStartEvent;
import promcgames.gameapi.games.versus.MapProvider;
import promcgames.gameapi.tournament.Tournament;
import promcgames.gameapi.tournament.Tournament.Matchup;
import promcgames.server.util.EventUtil;

public class VersusTournament implements Listener {
	
	private static boolean enabled = false;
	
	public VersusTournament() {
		if(!enabled) {
			enabled = true;
			new Tournament(TournamentQueueHandler.getQueue());
			EventUtil.register(this);
		}
	}
	
	@EventHandler
	public void onTournamentRoundStart(TournamentRoundStartEvent event) {
		List<Matchup> toRemove = new ArrayList<Matchup>();
		for(Matchup matchup : Tournament.getTournament().getCurrentMatchups()) {
			Player player1 = matchup.getPlayerOne();
			Player player2 = matchup.getPlayerTwo();
			if(player1 == null && player2 != null) {
				Tournament.getTournament().getNotPlaying().add(player2.getName());
			} else if(player1 != null && player2 == null) {
				Tournament.getTournament().getNotPlaying().add(player1.getName());
			} else if(player1 == null && player2 == null) {
				Tournament.getTournament().getPlayers().remove(matchup.getPlayerOneName());
				Tournament.getTournament().getPlayers().remove(matchup.getPlayerTwoName());
			} else {
				new MapProvider(player1, player2, Bukkit.getWorlds().get(0), true, true);
			}
		}
		for(Matchup matchup : toRemove) {
			Tournament.getTournament().getCurrentMatchups().remove(matchup);
		}
		toRemove.clear();
		toRemove = null;
	}
	
	public static boolean getEnabled() {
		return enabled;
	}
	
}