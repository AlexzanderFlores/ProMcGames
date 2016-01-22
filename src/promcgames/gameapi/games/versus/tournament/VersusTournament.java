package promcgames.gameapi.games.versus.tournament;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import promcgames.ProPlugin;
import promcgames.customevents.tournament.TournamentEndEvent;
import promcgames.customevents.tournament.TournamentRoundStartEvent;
import promcgames.gameapi.games.versus.MapProvider;
import promcgames.gameapi.games.versus.kits.VersusKit;
import promcgames.gameapi.tournament.Tournament;
import promcgames.gameapi.tournament.Tournament.Matchup;
import promcgames.player.MessageHandler;
import promcgames.server.util.EventUtil;

public class VersusTournament implements Listener {
	
	private static VersusTournament instance = null;
	private VersusKit kit = null;
	
	public VersusTournament() {
		if(instance == null) {
			instance = this;
			EventUtil.register(this);
			new Tournament(TournamentQueueHandler.getInstance().getQueue());
			TournamentQueueHandler.getInstance().disable();
		}
	}
	
	@EventHandler
	public void onTournamentRoundStart(TournamentRoundStartEvent event) {
		if(kit == null) {
			KitVoter.getInstance().chooseWinningKit();
		}
		
		List<String> notPlaying = Tournament.getInstance().getNotPlaying();
		List<String> players = Tournament.getInstance().getPlayers();
		List<Matchup> currentMatchups = Tournament.getInstance().getCurrentMatchups();
		
		Iterator<Matchup> it = currentMatchups.iterator();
		
		while(it.hasNext()) {
			Matchup matchup = it.next();
			Player playerOne = matchup.getPlayerOne();
			Player playerTwo = matchup.getPlayerTwo();
			if(playerOne == null || playerTwo == null) {
				currentMatchups.remove(matchup);
			}
			if(playerOne == null && playerTwo != null) {
				players.remove(matchup.getPlayerOneName());
				notPlaying.add(playerTwo.getName());
			} else if(playerOne != null && playerTwo == null) {
				notPlaying.add(playerOne.getName());
				players.remove(matchup.getPlayerTwoName());
			} else if(playerOne == null && playerTwo == null) {
				players.remove(matchup.getPlayerOneName());
				players.remove(matchup.getPlayerTwoName());
			} else {
				matchup.setFighting(true);
				//new MapProvider(playerOne, playerTwo, Bukkit.getWorlds().get(0), true, true);
				Bukkit.getLogger().info(playerOne.getName() + " will be fighting " + playerTwo.getName());
			}
		}
		while(notPlaying.size() >= 2) {
			Tournament.getInstance().setNotPlayingMatchups();
			it = currentMatchups.iterator();
			while(it.hasNext()) {
				Matchup matchup = it.next();
				if(!matchup.getFighting()) {
					Player playerOne = matchup.getPlayerOne();
					Player playerTwo = matchup.getPlayerTwo();
					if(playerOne == null || playerTwo == null) {
						currentMatchups.remove(matchup);
					}
					if(playerOne == null && playerTwo != null) {
						players.remove(matchup.getPlayerOneName());
						notPlaying.add(playerTwo.getName());
					} else if(playerOne != null && playerTwo == null) {
						notPlaying.add(playerOne.getName());
						players.remove(matchup.getPlayerTwoName());
					} else if(playerOne == null && playerTwo == null) {
						players.remove(matchup.getPlayerOneName());
						players.remove(matchup.getPlayerTwoName());
					} else {
						matchup.setFighting(true);
						new MapProvider(playerOne, playerTwo, Bukkit.getWorlds().get(0), true, true);
					}
				}
			}
		}
		if(notPlaying.size() == 1) {
			String playerName = notPlaying.get(0);
			Player player = ProPlugin.getPlayer(playerName);
			if(player != null) {
				player.teleport(Tournament.getInstance().getWaitingLocation());
				MessageHandler.sendMessage(player, "You will wait this round out");
			} else {
				notPlaying.remove(playerName);
				players.remove(playerName);
			}
		}
	}
	
	@EventHandler
	public void onTournamentEnd(TournamentEndEvent event) {
		disable();
	}
	
	public void disable() {
		HandlerList.unregisterAll(this);
		instance = null;
	}
	
	public void setKit(VersusKit kit) {
		this.kit = kit;
	}
	
	public VersusKit getKit() {
		return kit;
	}
	
	public static boolean getEnabled() {
		return instance != null;
	}
	
	public static VersusTournament getInstance() {
		return instance;
	}
	
}