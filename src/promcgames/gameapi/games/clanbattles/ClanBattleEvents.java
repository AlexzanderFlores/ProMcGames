package promcgames.gameapi.games.clanbattles;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.scoreboard.Team;

import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.game.GameKillEvent;
import promcgames.customevents.game.GameStartingEvent;
import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.player.PlayerViewStatsEvent;
import promcgames.gameapi.EloHandler;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.TeamHandler;
import promcgames.gameapi.games.clanbattles.setup.ClanBattleSetup;
import promcgames.gameapi.games.survivalgames.Events;
import promcgames.player.MessageHandler;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.servers.clans.Clan;
import promcgames.server.servers.clans.ClanHandler;
import promcgames.server.util.EventUtil;

public class ClanBattleEvents implements Listener {
	private boolean alertedWin = false;
	
	public ClanBattleEvents() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(!TeamHandler.isOnTeam(event.getPlayer())) {
			event.disallow(Result.KICK_OTHER, ChatColor.RED + "You can't join this clan battle at this time!");
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Team team = TeamHandler.getTeam(player);
		if(team != null) {
			String tabName = team.getPrefix() + player.getName();
			if(tabName.length() > 16) {
				tabName = tabName.substring(0, 16);
			}
			player.setPlayerListName(tabName);
		}
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		int ranked = ClanBattleSetup.ClanSetting.RANKED.getCurrentOption().equalsIgnoreCase("yes") ? 1 : 0;
		int clanOne = ClanBattle.getClanOneID();
		int clanTwo = ClanBattle.getClanTwoID();
		DB.NETWORK_ClANS_BATTLE_HISTORY.insert("'" + clanOne + "', '" + clanTwo + "', '" + (event.getTeam().getName().equalsIgnoreCase(ClanBattle.getClanOne()) ? 1 : 0) + "', '" + TeamHandler.getTeams().get(0).getSize() + "', '" + ranked + "'");
		DB.NETWORK_ClANS_BATTLE_HISTORY.insert("'" + clanTwo + "', '" + clanOne + "', '" + (event.getTeam().getName().equalsIgnoreCase(ClanBattle.getClanTwo()) ? 1 : 0) + "', '" + TeamHandler.getTeams().get(1).getSize() + "', '" + ranked + "'");
		if(ranked == 1) {
			int clan1Wins = ClanHandler.getClanBattleWins(ClanBattle.getClanOne());
			if(clan1Wins != -1) {
				if(event.getTeam().getName().equalsIgnoreCase(ClanBattle.getClanOne())) {
					DB.NETWORK_CLANS.updateInt("battle_wins", clan1Wins + 1, "clan_name", ClanBattle.getClanOne());
				} else {
					DB.NETWORK_CLANS.updateInt("battle_losses", ClanHandler.getClanBattleLosses(ClanBattle.getClanOne()) + 1, "clan_name", ClanBattle.getClanOne());
				}
			}
			int clan2Wins = ClanHandler.getClanBattleWins(ClanBattle.getClanTwo());
			if(clan2Wins != -1) {
				if(event.getTeam().getName().equalsIgnoreCase(ClanBattle.getClanTwo())) {
					DB.NETWORK_CLANS.updateInt("battle_wins", clan2Wins + 1, "clan_name", ClanBattle.getClanTwo());
				} else {
					DB.NETWORK_CLANS.updateInt("battle_losses", ClanHandler.getClanBattleLosses(ClanBattle.getClanTwo()) + 1, "clan_name", ClanBattle.getClanTwo());
				}
			}
			if(!alertedWin) {
				alertedWin = true;
				Clan winners = ClanHandler.getClan(event.getTeam().getName());
				Clan losers = winners.getClanID() == clanOne ? ClanHandler.getClan(clanTwo) : ClanHandler.getClan(clanOne);
				ProPlugin.dispatchCommandToGroup("sghub", "gameWon &a" + winners.getClanName() + " &ewon a &cClan Battle &eagainst &a" + losers.getClanName());
			}
		}
		DB.NETWORK_CLANS_BATTLES.delete(new String [] {"clan_one_id", "clan_two_id"}, new String [] {ClanBattle.getClanOneID() + "", ClanBattle.getClanTwoID() + ""});
		Clan clan = ClanHandler.getClan(event.getTeam().getName());
		MessageHandler.alertLine("&b");
		MessageHandler.alert("The clan " + clan.getColorTheme() + clan.getClanName() + " &ahas won the clan battle!");
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		Player killer = event.getPlayer();
		if(killer != null) {
			Player player = event.getKilled();
			if(player != null) {
				EloHandler.calculateWin(killer, player);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onGameDeath(GameDeathEvent event) {
		Team team = TeamHandler.getTeam(event.getPlayer());
		if(team.getName().equalsIgnoreCase(ClanBattle.getClanOne())) {
			DB.NETWORK_CLANS_BATTLES.updateInt("clan_one_score", (team.getSize() - 1), new String [] {"clan_one_id", "clan_two_id"}, new String [] {ClanBattle.getClanOneID() + "", ClanBattle.getClanTwoID() + ""});
		} else if(team.getName().equalsIgnoreCase(ClanBattle.getClanTwo())) {
			DB.NETWORK_CLANS_BATTLES.updateInt("clan_two_score", (team.getSize() - 1), new String [] {"clan_one_id", "clan_two_id"}, new String [] {ClanBattle.getClanOneID() + "", ClanBattle.getClanTwoID() + ""});
		}
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		if(ProPlugin.getPlayers().size() != ClanBattleSetup.getMaxPerTeam() * 2) {
			MessageHandler.alertLine("&b");
			MessageHandler.alert("&a&lInsufficient amount of players. Server restarting...");
			MessageHandler.alertLine("&b");
			ProMcGames.getMiniGame().setGameState(GameStates.ENDING);
		} else {
			List<Location> spawns = Events.getSpawnPointHandler().getSpawns();
			OfflinePlayer [] team1 = new OfflinePlayer[TeamHandler.getTeam(0).getSize()];
			TeamHandler.getTeam(0).getPlayers().toArray(team1);
			for(int i = 0; i < ClanBattleSetup.getMaxPerTeam(); i++) {
				Player player = team1[i].getPlayer();
				if(player != null) {
					player.teleport(spawns.get(i));
				}
			}
			OfflinePlayer [] team2 = new OfflinePlayer[TeamHandler.getTeam(1).getSize()];
			TeamHandler.getTeam(1).getPlayers().toArray(team2);
			for(int i = 11; i < ClanBattleSetup.getMaxPerTeam() + 11; i++) {
				Player player = team2[i - 11].getPlayer();
				if(player != null) {
					player.teleport(spawns.get(i));
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerViewStats(PlayerViewStatsEvent event) {
		Player player = event.getPlayer();
		Player target = ProPlugin.getPlayer(event.getTargetName());
		if(target == null) {
			MessageHandler.sendMessage(player, "&c" + event.getTargetName() + " is not online");
		} else {
			MessageHandler.sendMessage(player, "&eElo: &c" + EloHandler.getElo(target));
		}
	}
}