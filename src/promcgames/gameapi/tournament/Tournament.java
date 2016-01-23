package promcgames.gameapi.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import promcgames.ProPlugin;
import promcgames.customevents.tournament.TournamentEndEvent;
import promcgames.customevents.tournament.TournamentEndingEvent;
import promcgames.customevents.tournament.TournamentRoundEndEvent;
import promcgames.customevents.tournament.TournamentRoundStartEvent;
import promcgames.customevents.tournament.TournamentWinEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.EmeraldsHandler.EmeraldReason;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.bossbar.BossBar;
import promcgames.server.tasks.DelayedTask;

/*
 * An instance of this object should only be initialized when a full
 * roster of participants is ready to go.
 * View TournamentQueueHandler.java for an example.
 */
public class Tournament {
	
	private static Tournament instance = null;
	public static enum TournamentStatus {ONGOING_ROUND, INBETWEEN_ROUND, ENDING};
	private TournamentStatus tournamentStatus = null;
	private List<Matchup> currentMatchups = null;
	private List<String> players = null;
	private List<String> notPlaying = null; // a list of players who will not play in the round; they will be matched up with each other in the end
	private List<String> justDied = null; // a list of players who just died; this is to change the respawn location; players removed one second later
	private int winEmeralds = 0;
	private int battleTimeLimit = 0; // in seconds; time limit for each battle
	private int betweenRoundsTime = 0; // in seconds; time in between rounds
	private Location respawnLocation = null; // where a player respawns after they die; default is first world spawn location
	private Location waitingLocation = null; // where a player will teleport when waiting for the next match; default is first world spawn location
	private boolean spectateOnDeath = false; // if a player should enter spectate mode when they die
	private boolean alertAll = true; // when false, will only alert players apart of the tournament of important messages
	private boolean bossBarDisplayAll = false; // if the boss bar information should be displayed to everyone or just players in the tournament
	private EmeraldReason tournamentWinReason = null;
	
	public static class Matchup {
		
		private String playerOneName = null;
		private String playerTwoName = null;
		private boolean fighting = false;
		
		public Matchup(String playerOneName, String playerTwoName) {
			this.playerOneName = playerOneName;
			this.playerTwoName = playerTwoName;
		}
		
		public void setFighting(boolean fighting) {
			this.fighting = fighting;
		}
		
		public Player getPlayerOne() {
			return ProPlugin.getPlayer(playerOneName);
		}
		
		public Player getPlayerTwo() {
			return ProPlugin.getPlayer(playerTwoName);
		}
		
		public Player getOtherPlayer(Player player) {
			String playerName = player.getName();
			if(playerName.equalsIgnoreCase(playerOneName)) {
				return getPlayerTwo();
			} else if(playerName.equalsIgnoreCase(playerTwoName)) {
				return getPlayerOne();
			}
			return null;
		}
		
		public String getPlayerOneName() {
			return playerOneName;
		}
		
		public String getPlayerTwoName() {
			return playerTwoName;
		}
		
		public boolean getFighting() {
			return fighting;
		}
		
	}
	
	public Tournament(List<String> players) {
		if(instance == null) {
			instance = this;
			this.players = players;
			this.notPlaying = new ArrayList<String>();
			this.currentMatchups = new ArrayList<Matchup>();
			this.justDied = new ArrayList<String>();
			this.waitingLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
			this.respawnLocation = waitingLocation;
			this.betweenRoundsTime = 30;
			this.battleTimeLimit = 180;
			this.tournamentWinReason = EmeraldReason.VERSUS_TOURNAMENT_WIN;
			if(!SpectatorHandler.isEnabled()) {
				new SpectatorHandler();
			}
			new Events();
			callRoundStartEvent();
		}
	}
	
	public void setRespawnLocationLocation(Location respawnLocation) {
		this.respawnLocation = respawnLocation;
	}
	
	public void setWaitingLocation(Location waitingLocation) {
		this.waitingLocation = waitingLocation;
	}
	
	public void setSpectateOnDeath(boolean spectateOnDeath) {
		this.spectateOnDeath = spectateOnDeath;
	}
	
	public void setBossBarDisplayAll(boolean bossBarDisplayAll) {
		this.bossBarDisplayAll = bossBarDisplayAll;
	}
	
	public void setTournamentStatus(TournamentStatus tournamentStatus) {
		this.tournamentStatus = tournamentStatus;
	}
	
	public void setWinEmeralds(int winEmeralds) {
		this.winEmeralds = winEmeralds;
	}
	
	public void setBattleTimeLimit(int battleTimeLimit) {
		this.battleTimeLimit = battleTimeLimit;
	}
	
	public void setBetweenRoundsTime(int betweenRoundsTime) {
		this.betweenRoundsTime = betweenRoundsTime;
	}
	
	public void setTournamentWinReason(EmeraldReason tournamentWinReason) {
		this.tournamentWinReason = tournamentWinReason;
	}
	
	public void setPlayersMatchups() {
		List<String> players = getPlayersCopy();
		notPlaying.clear();
		currentMatchups.clear();
		if(players.size() >= 2) {
			while(!players.isEmpty()) {
				Random random = new Random();
				String playerOneName = null, playerTwoName = null;
				playerOneName = players.get(random.nextInt(players.size()));
				players.remove(playerOneName);
				if(players.isEmpty()) {
					notPlaying.add(playerOneName);
				} else {
					playerTwoName = players.get(random.nextInt(players.size()));
					players.remove(playerTwoName);
					currentMatchups.add(new Matchup(playerOneName, playerTwoName));
				}
			}
		}
	}
	
	public void setNotPlayingMatchups() {
		List<String> notPlaying = getNotPlayingCopy();
		if(notPlaying.size() >= 2) {
			while(!notPlaying.isEmpty()) {
				Random random = new Random();
				String playerOneName = null, playerTwoName = null;
				playerOneName = notPlaying.get(random.nextInt(notPlaying.size()));
				notPlaying.remove(playerOneName);
				if(!notPlaying.isEmpty()) {
					playerTwoName = notPlaying.get(random.nextInt(notPlaying.size()));
					notPlaying.remove(playerTwoName);
					this.notPlaying.remove(playerOneName);
					this.notPlaying.remove(playerTwoName);
					currentMatchups.add(new Matchup(playerOneName, playerTwoName));
				}
			}
		}
	}
	
	public void disable() {
		for(String s : getPlayers()) {
			Player player = ProPlugin.getPlayer(s);
			if(player != null) {
				player.teleport(getRespawnLocation());
			}
		}
		currentMatchups.clear();
		currentMatchups = null;
		players.clear();
		players = null;
		respawnLocation = null;
		waitingLocation = null;
		instance = null;
	}
	
	public void callWinEvent(Player winner) {
		if(isPlayerInTournament(winner)) {
			Bukkit.getPluginManager().callEvent(new TournamentWinEvent(winner));
		}
	}
	
	public void callEndEvent() {
		Bukkit.getPluginManager().callEvent(new TournamentEndEvent());
	}
	
	public void callEndingEvent() {
		Bukkit.getPluginManager().callEvent(new TournamentEndingEvent());
	}
	
	public void callRoundStartEvent() {
		Bukkit.getPluginManager().callEvent(new TournamentRoundStartEvent());
	}
	
	public void callRoundEndEvent() {
		Bukkit.getPluginManager().callEvent(new TournamentRoundEndEvent());
	}
	
	public void eliminateAllMatchups() {
		for(Matchup matchup : currentMatchups) {
			eliminateMatchup(matchup);
		}
	}
	
	public void eliminateMatchup(Matchup matchup) {
		String playerOneDisplay = matchup.playerOneName, playerTwoDisplay = matchup.getPlayerTwoName();
		for(int a = 0; a < 2; a++) {
			String currentPlayer = matchup.getPlayerOneName();
			if(a == 1) {
				currentPlayer = matchup.getPlayerTwoName();
			}
			Player player = ProPlugin.getPlayer(currentPlayer);
			if(player != null) {
				MessageHandler.sendMessage(player, "&cYou were eliminated because you took too long");
				player.teleport(respawnLocation);
				if(a == 0) {
					playerOneDisplay = AccountHandler.getPrefix(player);
				} else if(a == 1) {
					playerTwoDisplay = AccountHandler.getPrefix(player);
				}
			}
			players.remove(currentPlayer);
		}
		alert(playerOneDisplay + " &aand " + playerTwoDisplay + " &ahave been eliminated from the tournament");
	}
	
	public void teleportAllTo(Location location) {
		for(String playerName : players) {
			Player player = ProPlugin.getPlayer(playerName);
			if(player != null) {
				player.teleport(location);
			}
		}
	}
	
	public void eliminatePlayer(Player player, Player killer) {
		if(isPlayerInTournament(player)) {
			if(killer == null) {
				Matchup matchup = getMatchup(player);
				if(matchup != null) {
					killer = matchup.getOtherPlayer(player);
				}
			}
			if(killer != null && isPlayerInTournament(killer)) {
				killer.teleport(waitingLocation);
			}
			final String playerName = player.getName();
			players.remove(playerName);
			justDied.add(playerName);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					justDied.remove(playerName);
				}
			}, 20L);
			removeMatchup(player);
			alert(AccountHandler.getPrefix(player) + " &ahas been eliminated from the tournament" + (killer != null ? " by " + AccountHandler.getPrefix(killer) : ""));
			if(players.size() == 1) {
				callWinEvent(ProPlugin.getPlayer(players.get(0)));
			} else if(players.size() < 1) {
				callEndEvent();
			} else if(currentMatchups.isEmpty()) {
				callRoundEndEvent();
			}
		}
	}
	
	public void bossBarDisplay(String message, double health) {
		if(bossBarDisplayAll) {
			BossBar.display(message, health);
		} else {
			for(String playerName : players) {
				Player player = ProPlugin.getPlayer(playerName);
				if(player != null) {
					BossBar.display(player, message, health);
				}
			}
		}
	}
	
	public void removeMatchup(Player player) {
		if(isPlayerInTournament(player)) {
			Matchup matchup = getMatchup(player);
			if(matchup != null) {
				currentMatchups.remove(matchup);
			}
		}
	}
	
	public void alert(String message) {
		if(alertAll) {
			MessageHandler.alert(message);
		} else {
			for(String s : players) {
				Player player = ProPlugin.getPlayer(s);
				if(player != null) {
					MessageHandler.sendMessage(player, message);
				}
			}
		}
	}
	
	public boolean isPlayerInTournament(Player player) {
		return players.contains(player.getName());
	}
	
	public boolean didPlayerJustDie(Player player) {
		return justDied.contains(player.getName());
	}
	
	public boolean getSpectateOnDeath() {
		return spectateOnDeath;
	}
	
	public boolean getAlertAll() {
		return alertAll;
	}
	
	public boolean getBossBarDisplayAll() {
		return bossBarDisplayAll;
	}
	
	public List<String> getPlayers() {
		return players;
	}
	
	private List<String> getPlayersCopy() {
		List<String> list = new ArrayList<>();
		list.addAll(getPlayers());
		return list;
	}
	
	public List<String> getNotPlaying() {
		return notPlaying;
	}
	
	private List<String> getNotPlayingCopy() {
		List<String> list = new ArrayList<>();
		list.addAll(getNotPlaying());
		return list;
	}
	
	public List<String> getJustDied() {
		return justDied;
	}
	
	public List<Matchup> getCurrentMatchups() {
		return currentMatchups;
	}
	
	public int getWinEmeralds() {
		return winEmeralds;
	}
	
	public int getBattleTimeLimit() {
		return battleTimeLimit;
	}
	
	public int getBetweenRoundsTime() {
		return betweenRoundsTime;
	}
	
	public Location getRespawnLocation() {
		return respawnLocation;
	}
	
	public Location getWaitingLocation() {
		return waitingLocation;
	}
	
	public TournamentStatus getTournamentStatus() {
		return tournamentStatus;
	}
	
	public EmeraldReason getTournamentWinReason() {
		return tournamentWinReason;
	}
	
	public Matchup getMatchup(Player player) {
		String playerName = player.getName();
		for(Matchup matchup : currentMatchups) {
			if(matchup.getPlayerOneName().equalsIgnoreCase(playerName) || matchup.getPlayerTwoName().equalsIgnoreCase(playerName)) {
				return matchup;
			}
		}
		return null;
	}
	
	public static Tournament getInstance() {
		return instance;
	}
	
}