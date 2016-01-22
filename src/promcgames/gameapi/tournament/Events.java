package promcgames.gameapi.tournament;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import promcgames.ProPlugin;
import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.customevents.tournament.TournamentEndEvent;
import promcgames.customevents.tournament.TournamentEndingEvent;
import promcgames.customevents.tournament.TournamentRoundEndEvent;
import promcgames.customevents.tournament.TournamentRoundStartEvent;
import promcgames.customevents.tournament.TournamentWinEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.tournament.Tournament.Matchup;
import promcgames.gameapi.tournament.Tournament.TournamentStatus;
import promcgames.player.EmeraldsHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.util.CountDownUtil;
import promcgames.server.util.EventUtil;

public class Events implements Listener {
	
	private int counter = 0;
	
	public Events() {
		counter = Tournament.getTournament().getBattleTimeLimit();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		TournamentStatus tournamentStatus = Tournament.getTournament().getTournamentStatus();
		if(tournamentStatus == TournamentStatus.ONGOING_ROUND) {
			Tournament.getTournament().bossBarDisplay("&cRound ends in &b" + CountDownUtil.getCounterAsString(counter), 200); // TODO: figure out boss bar health stuff
			if(counter == 0) {
				Tournament.getTournament().callRoundEndEvent();
			}
		} else if(tournamentStatus == TournamentStatus.INBETWEEN_ROUND) {
			Tournament.getTournament().bossBarDisplay("&cRound begins in &b" + CountDownUtil.getCounterAsString(counter), 200); // TODO: figure out boss bar health stuff
			if(counter == 0) {
				Tournament.getTournament().callRoundStartEvent();
			}
		} else if(tournamentStatus == TournamentStatus.ENDING) {
			Tournament.getTournament().bossBarDisplay("&Tournament ends in &b" + CountDownUtil.getCounterAsString(counter), 200); // TODO: figure out boss bar health stuff
			if(counter == 0) {
				Tournament.getTournament().callEndEvent();
			}
		}
		--counter;
	}
	
	@EventHandler
	public void onTournamentWin(TournamentWinEvent event) {
		Player winner = event.getWinner();
		if(winner != null) {
			EmeraldsHandler.addEmeralds(winner, Tournament.getTournament().getWinEmeralds(), Tournament.getTournament().getTournamentWinReason(), true);
			MessageHandler.alert(AccountHandler.getPrefix(winner) + " &ahas won the tournament");
			Tournament.getTournament().callEndingEvent();
		} else {
			Tournament.getTournament().callEndEvent();
		}
	}
	
	@EventHandler
	public void onTournamentEnd(TournamentEndEvent event) {
		Tournament.getTournament().disable();
	}
	
	@EventHandler
	public void onTournamentEnding(TournamentEndingEvent event) {
		Tournament.getTournament().setTournamentStatus(TournamentStatus.ENDING);
		counter = 11;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onTournamentRoundStart(TournamentRoundStartEvent event) {
		Tournament.getTournament().setTournamentStatus(TournamentStatus.ONGOING_ROUND);
		Tournament.getTournament().setNextMatchups();
		counter = Tournament.getTournament().getBattleTimeLimit() + 1;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onTournamentRoundEnd(TournamentRoundEndEvent event) {
		Tournament.getTournament().setTournamentStatus(TournamentStatus.INBETWEEN_ROUND);
		Tournament.getTournament().eliminateAllMatchups();
		if(Tournament.getTournament().getPlayers().size() == 0) {
			Tournament.getTournament().callEndEvent();
		} else if(Tournament.getTournament().getPlayers().size() == 1) {
			Player winner = ProPlugin.getPlayer(Tournament.getTournament().getPlayers().get(0));
			if(winner != null) {
				Tournament.getTournament().callWinEvent(winner);
			} else {
				Tournament.getTournament().callEndEvent();
			}
		} else {
			counter = Tournament.getTournament().getBetweenRoundsTime() + 1;
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameDeath(GameDeathEvent event) {
		Player player = event.getPlayer();
		if(Tournament.getTournament().isPlayerInTournament(player)) {
			Tournament.getTournament().eliminatePlayer(player, event.getKiller());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if(Tournament.getTournament().isPlayerInTournament(player)) {
			event.setRespawnLocation(Tournament.getTournament().getRespawnLocation());
			if(Tournament.getTournament().getSpectateOnDeath()) {
				SpectatorHandler.add(player);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(Tournament.getTournament().isPlayerInTournament(player)) {
			TournamentStatus status = Tournament.getTournament().getTournamentStatus();
			if(status == TournamentStatus.ONGOING_ROUND) {
				Matchup matchup = Tournament.getTournament().getMatchup(player);
				if(matchup != null) {
					Tournament.getTournament().eliminatePlayer(player, matchup.getOtherPlayer(player));
				} else if(Tournament.getTournament().getNotPlaying().contains(player.getName())) {
					Tournament.getTournament().getNotPlaying().remove(player.getName());
					Tournament.getTournament().getPlayers().remove(player.getName());
					Tournament.getTournament().alert(AccountHandler.getPrefix(player) + " &ahas left the tournament");
				}
			} else if(status == TournamentStatus.INBETWEEN_ROUND) {
				Tournament.getTournament().getPlayers().remove(player.getName());
				Tournament.getTournament().alert(AccountHandler.getPrefix(player) + " &ahas left the tournament");
			}
		}
	}
	
	public void setCounter(int counter) {
		this.counter = counter;
	}
	
	public int getCounter() {
		return counter;
	}
	
}