package promcgames.gameapi.tournament;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
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
	
	private static Events instance = null;
	private int counter = 0;
	
	public Events() {
		if(instance == null) {
			instance = this;
			counter = Tournament.getInstance().getBattleTimeLimit();
			EventUtil.register(this);
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		TournamentStatus tournamentStatus = Tournament.getInstance().getTournamentStatus();
		if(tournamentStatus == TournamentStatus.ONGOING_ROUND) {
			Tournament.getInstance().bossBarDisplay("&cRound ends in &b" + CountDownUtil.getCounterAsString(counter), 200); // TODO: figure out boss bar health stuff
			if(counter == 0) {
				Tournament.getInstance().callRoundEndEvent();
			}
		} else if(tournamentStatus == TournamentStatus.INBETWEEN_ROUND) {
			Tournament.getInstance().bossBarDisplay("&cRound begins in &b" + CountDownUtil.getCounterAsString(counter), 200); // TODO: figure out boss bar health stuff
			if(counter == 0) {
				Tournament.getInstance().callRoundStartEvent();
			}
		} else if(tournamentStatus == TournamentStatus.ENDING) {
			Tournament.getInstance().bossBarDisplay("&Tournament ends in &b" + CountDownUtil.getCounterAsString(counter), 200); // TODO: figure out boss bar health stuff
			if(counter == 0) {
				Tournament.getInstance().callEndEvent();
			}
		}
		--counter;
	}
	
	@EventHandler
	public void onTournamentWin(TournamentWinEvent event) {
		Player winner = event.getWinner();
		if(winner != null) {
			EmeraldsHandler.addEmeralds(winner, Tournament.getInstance().getWinEmeralds(), Tournament.getInstance().getTournamentWinReason(), true);
			MessageHandler.alert(AccountHandler.getPrefix(winner) + " &ahas won the tournament");
			Tournament.getInstance().callEndingEvent();
		} else {
			Tournament.getInstance().callEndEvent();
		}
	}
	
	@EventHandler
	public void onTournamentEnd(TournamentEndEvent event) {
		Tournament.getInstance().disable();
		disable();
	}
	
	@EventHandler
	public void onTournamentEnding(TournamentEndingEvent event) {
		Tournament.getInstance().setTournamentStatus(TournamentStatus.ENDING);
		counter = 11;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onTournamentRoundStart(TournamentRoundStartEvent event) {
		Tournament.getInstance().setTournamentStatus(TournamentStatus.ONGOING_ROUND);
		Tournament.getInstance().setPlayersMatchups();
		counter = Tournament.getInstance().getBattleTimeLimit() + 1;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onTournamentRoundEnd(TournamentRoundEndEvent event) {
		Tournament.getInstance().setTournamentStatus(TournamentStatus.INBETWEEN_ROUND);
		Tournament.getInstance().getNotPlaying().clear();
		Tournament.getInstance().eliminateAllMatchups();
		if(Tournament.getInstance().getPlayers().size() == 0) {
			Tournament.getInstance().callEndEvent();
		} else if(Tournament.getInstance().getPlayers().size() == 1) {
			Player winner = ProPlugin.getPlayer(Tournament.getInstance().getPlayers().get(0));
			if(winner != null) {
				Tournament.getInstance().callWinEvent(winner);
			} else {
				Tournament.getInstance().callEndEvent();
			}
		} else {
			counter = Tournament.getInstance().getBetweenRoundsTime() + 1;
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameDeath(GameDeathEvent event) {
		Player player = event.getPlayer();
		if(Tournament.getInstance().isPlayerInTournament(player)) {
			Tournament.getInstance().eliminatePlayer(player, event.getKiller());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if(Tournament.getInstance().didPlayerJustDie(player)) {
			event.setRespawnLocation(Tournament.getInstance().getRespawnLocation());
			if(Tournament.getInstance().getSpectateOnDeath()) {
				SpectatorHandler.add(player);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(Tournament.getInstance().isPlayerInTournament(player)) {
			TournamentStatus status = Tournament.getInstance().getTournamentStatus();
			if(status == TournamentStatus.ONGOING_ROUND) {
				Matchup matchup = Tournament.getInstance().getMatchup(player);
				if(matchup != null) {
					Tournament.getInstance().eliminatePlayer(player, matchup.getOtherPlayer(player));
				} else if(Tournament.getInstance().getNotPlaying().contains(player.getName())) {
					Tournament.getInstance().getNotPlaying().remove(player.getName());
					Tournament.getInstance().getPlayers().remove(player.getName());
					Tournament.getInstance().alert(AccountHandler.getPrefix(player) + " &ahas left the tournament");
				}
			} else if(status == TournamentStatus.INBETWEEN_ROUND) {
				Tournament.getInstance().getPlayers().remove(player.getName());
				Tournament.getInstance().alert(AccountHandler.getPrefix(player) + " &ahas left the tournament");
			}
		}
	}
	
	public void setCounter(int counter) {
		this.counter = counter;
	}
	
	public int getCounter() {
		return counter;
	}
	
	public void disable() {
		HandlerList.unregisterAll(this);
		instance = null;
	}
	
	public static Events getInstance() {
		return instance;
	}
	
}