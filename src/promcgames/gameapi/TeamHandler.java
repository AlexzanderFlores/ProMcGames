package promcgames.gameapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.Team;

import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.game.GameLossEvent;
import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.player.MessageHandler;
import promcgames.server.ProMcGames;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class TeamHandler implements Listener {
	private static int numOfTeams = 0;
	private static List<Team> teams = null;
	private static Map<String, List<String>> died = null;
	private static boolean allowGameWinning = true;
	
	/*
	 * Max teams: 8
	 */
	public TeamHandler(int numOfTeams, String [] teamNames) {
		died = new HashMap<String, List<String>>();
		if(TeamHandler.teams != null) {
			for(Team team : TeamHandler.teams) {
				team.unregister();
			}
			TeamHandler.teams.clear();
		} else {
			TeamHandler.teams = new ArrayList<Team>();
		}
		if(numOfTeams > 8) {
			numOfTeams = 8;
		}
		TeamHandler.numOfTeams = numOfTeams;
		for(int i = 1; i <= numOfTeams; i++) {
			String teamName = "";
			try {
				teamName = teamNames[i - 1];
			} catch(Exception e) {
				teamName = "team" + i;
			}
			teams.add(ProMcGames.getScoreboard().registerNewTeam(teamName));
			List<String> list = new ArrayList<String>();
			died.put(teamName, list);
		}
		autoSetColors();
		EventUtil.register(this);
	}
	
	private static void autoSetColors() {
		for(int i = 0; i < numOfTeams; i++) {
			teams.get(i).setPrefix(getColor(i) + "");
		}
	}
	
	public static void add(OfflinePlayer offlinePlayer, int teamID) {
		getTeam(teamID).addPlayer(offlinePlayer);
	}
	
	public static void add(OfflinePlayer offlinePlayer, String teamName) {
		try {
			getTeam(teamName).addPlayer(offlinePlayer);
		} catch(Exception e) {
			
		}
	}
	
	/*
	 * Removes all players from all teams.
	 */
	public static void removeAll() {
		for(Team team : teams) {
			for(OfflinePlayer offlinePlayer : team.getPlayers()) {
				team.removePlayer(offlinePlayer);
			}
		}
	}
	
	public static void remove(Player player) {
		for(Team team : teams) {
			if(team.hasPlayer(player)) {
				team.removePlayer(player);
			}
		}
	}
	
	public static void setTeamDamage(boolean teamDamage) {
		for(Team team : teams) {
			team.setAllowFriendlyFire(teamDamage);
		}
	}
	
	/*
	 * Starts at zero. Goes to seven.
	 */
	public static ChatColor getColor(int teamID) {
		ChatColor chatColor = null;
		switch(teamID) {
		case 0: chatColor = ChatColor.RED;break;
		case 1: chatColor = ChatColor.BLUE;break;
		case 2: chatColor = ChatColor.GREEN;break;
		case 3: chatColor = ChatColor.YELLOW;break;
		case 4: chatColor = ChatColor.DARK_RED;break;
		case 5: chatColor = ChatColor.DARK_BLUE;break;
		case 6: chatColor = ChatColor.DARK_GREEN;break;
		case 7: chatColor = ChatColor.GOLD;break;
		}
		return chatColor;
	}
	
	public static ChatColor getDefaultTeamColor(Team team) {
		int index = TeamHandler.teams.indexOf(team);
		if(index == -1) {
			return null;
		}
		return getColor(index);
	}
	
	public static int getNumberOfTeams() {
		return numOfTeams;
	}
	
	public static int getTeamsLeft() {
		int teamsLeft = 0;
		for(Team team : teams) {
			if(team.getPlayers().size() != 0) {
				teamsLeft++;
			}
		}
		return teamsLeft;
	}
	
	public static boolean onSameTeam(Player p1, Player p2) {
		Team team1 = getTeam(p1);
		if(team1 == null) {
			return false;
		}
		Team team2 = getTeam(p2);
		if(team2 == null) {
			return false;
		}
		return team1 == team2;
	}
	
	public static boolean isOnTeam(Player player) {
		for(Team team : teams) {
			if(team.hasPlayer(player)) {
				return true;
			}
		}
		for(String teamName : died.keySet()) {
			if(died.get(teamName).contains(player.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public static List<Team> getTeams() {
		return teams;
	}
	
	public static Team getTeam(int teamID) {
		return teams.get(teamID);
	}
	
	public static Team getTeam(String teamName) {
		for(Team team : teams) {
			if(team.getName().equalsIgnoreCase(teamName)) {
				return team;
			}
		}
		return null;
	}
	
	public static Team getTeam(Player player) {
		for(Team team : teams) {
			if(team.hasPlayer(player)) {
				return team;
			}
		}
		return null;
	}
	
	/*
	 * This method is only useful if there is only one team left.
	 */
	public static Team getWinningTeam() {
		for(Team team : teams) {
			if(team.getPlayers().size() != 0) {
				return team;
			}
		}
		return null;
	}
	
	public static Map<String, List<String>> getDied() {
		return died;
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		Player player = event.getPlayer();
		Team team = getTeam(player);
		if(team != null && ProMcGames.getMiniGame() != null && ProMcGames.getMiniGame().getTeamBased() && ProMcGames.getMiniGame().getPlayersHaveOneLife()) {
			team.removePlayer(player);
			died.get(team.getName()).add(player.getName());
			if(team.getSize() <= 0) {
				Bukkit.getPluginManager().callEvent(new GameLossEvent(team));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if(ProMcGames.getMiniGame() != null && ProMcGames.getMiniGame().getPlayersHaveOneLife()) {
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					if(ProMcGames.getMiniGame().getTeamBased()) {
						if(TeamHandler.getTeamsLeft() == 1 && allowGameWinning) {
							allowGameWinning = false;
							new DelayedTask(new Runnable() {
								@Override
								public void run() {
									allowGameWinning = true;
								}
							}, 20 * 2);
							Bukkit.getPluginManager().callEvent(new GameWinEvent(TeamHandler.getWinningTeam()));
						} else if(TeamHandler.getTeamsLeft() <= 0) {
							ProMcGames.getMiniGame().setGameState(GameStates.ENDING);
						}
					}
				}
			});
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLeave(final PlayerLeaveEvent event) {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				Player player = event.getPlayer();
				Team team = getTeam(player);
				if(ProMcGames.getMiniGame() != null && ProMcGames.getMiniGame().getPlayersHaveOneLife() && ProMcGames.getMiniGame().getTeamBased() && 
						!SpectatorHandler.contains(player) && team != null) {
					GameStates gameState = ProMcGames.getMiniGame().getGameState();
					if(gameState == GameStates.STARTING || gameState == GameStates.STARTED) {
						team.removePlayer(player);
						if(gameState == GameStates.STARTED) {
							died.get(team.getName()).add(player.getName());
						}
						if(team.getSize() <= 0) {
							if(gameState == GameStates.STARTED) {
								Bukkit.getPluginManager().callEvent(new GameLossEvent(team));
								if(TeamHandler.getTeamsLeft() == 1) {
									Bukkit.getPluginManager().callEvent(new GameWinEvent(TeamHandler.getWinningTeam()));
								}
							} else if(TeamHandler.getTeamsLeft() == 1) {
								MessageHandler.alert("Not enough players on each team to start game");
								ProMcGames.getMiniGame().setGameState(GameStates.ENDING);
							}
						}
					}
				}
			}
		});
	}
}
