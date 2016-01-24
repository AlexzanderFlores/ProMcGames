package promcgames.gameapi.games.clanbattles.setup;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import promcgames.customevents.ServerRestartEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.games.clanbattles.ClanBattle;
import promcgames.gameapi.games.clanbattles.setup.ClanBattleSetup.SetupPhase;
import promcgames.player.MessageHandler;
import promcgames.player.bossbar.BossBar;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.servers.clans.Clan;
import promcgames.server.servers.clans.ClanHandler;
import promcgames.server.servers.clans.ClanHandler.ClanRank;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class Events implements Listener {
	private static Events instance = null;
	private static int counter = 0;
	
	public Events() {
		instance = this;
		counter = 5 * 60;
		EventUtil.register(this);
	}
	
	public static void unregister() {
		HandlerList.unregisterAll(instance);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(ClanBattleSetup.getSetupPhase() == SetupPhase.SETTINGS || ClanBattleSetup.getSetupPhase() == SetupPhase.ROSTER) {
			BossBar.display("&b&lSeconds Left To Finish Setup: &a&l" + counter);
			if(counter == 0) {
				MessageHandler.alert("Server restarting...");
				ProPlugin.restartServer(5 * 20);
				return;
			} else if(counter != (5 * 60) && counter % 60 == 0) {
				MessageHandler.alert((counter / 60) + " minute(s) to finish setup, otherwise the server will restart");
			} else if(counter <= 50 && (counter % 10 == 0 || counter <= 5)) {
				MessageHandler.alert(counter + " second(s) to finish setup, otherwise the server will restart");
			}
			counter--;
		}
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		ClanBattleSetup.setSetupPhase(SetupPhase.DONE);
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		if(ClanBattleSetup.getSetupPhase() == SetupPhase.WAITING && !canJoin(player)) {
			event.disallow(Result.KICK_OTHER, ChatColor.RED + "You are not allowed to join this server at this time!");
		} else if((ClanBattleSetup.getSetupPhase() == SetupPhase.ROSTER || ClanBattleSetup.getSetupPhase() == SetupPhase.SETTINGS) && !ClanBattleSetup.isSetupPlayer(player)) {
			event.disallow(Result.KICK_OTHER, ChatColor.RED + "This clan battle is currently being setup! Try again later!");
		} else if(ClanBattleSetup.getSetupPhase() == SetupPhase.WAITING && !ClanHandler.isRank(player, ClanRank.FOUNDER) && !ClanHandler.isRank(player, ClanRank.GENERAL)) {
			event.disallow(Result.KICK_OTHER, ChatColor.RED + "Only a clan founder or general can join at this time!");
		} else {
			event.allow();
		}
	}
	
	private static boolean canJoin(Player player) {
		String uuid = player.getUniqueId().toString();
		return DB.NETWORK_CLANS_SETUP.isKeySet(new String [] {"server_name", "player1"}, new String [] {ProMcGames.getServerName(), uuid})
				|| DB.NETWORK_CLANS_SETUP.isKeySet(new String [] {"server_name", "player2"}, new String [] {ProMcGames.getServerName(), uuid});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if((ClanBattleSetup.getSetupPhase() == SetupPhase.ROSTER || ClanBattleSetup.getSetupPhase() == SetupPhase.SETTINGS) && !ClanBattleSetup.isSetupPlayer(event.getPlayer())) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou have been sent back to the clans hub because you are not a setup player!");
			ProPlugin.sendPlayerToServer(event.getPlayer(), "sghub");
		} else {
			Clan clan = ClanHandler.getClan(event.getPlayer());
			if(clan == null) {
				MessageHandler.sendMessage(event.getPlayer(), "&cAn unexpected error occured");
				ProPlugin.sendPlayerToServer(event.getPlayer(), "sghub");
				return;
			} else {
				if(!clan.getUsers().contains(event.getPlayer().getName())) {
					clan.getUsers().add(event.getPlayer().getName());
				}
			}
			if(ClanBattle.getClanOneLeader().equals("")) {
				ClanBattle.setClanOneLeader(event.getPlayer().getName());
			} else if(ClanBattle.getClanTwoLeader().equals("")) {
				ClanBattle.setClanTwoLeader(event.getPlayer().getName());
			}
			if(!ClanBattle.getClanOneLeader().equals("") && !ClanBattle.getClanTwoLeader().equals("")) {
				if(ClanBattleSetup.getSetupPhase() == SetupPhase.WAITING) {
					ClanBattleSetup.startSetup();
				}
			} else {
				//MessageHandler.sendMessage(event.getPlayer(), "Wait for one more clan founder to join...");
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(final PlayerLeaveEvent event) {
		if(ClanBattleSetup.getSetupPhase() == SetupPhase.WAITING && ClanBattleSetup.isSetupPlayer(event.getPlayer())) {
			ProPlugin.restartServer(3);
		} else if((ClanBattleSetup.getSetupPhase() == SetupPhase.ROSTER || ClanBattleSetup.getSetupPhase() == SetupPhase.SETTINGS) && ClanBattleSetup.isSetupPlayer(event.getPlayer())) {
			MessageHandler.alert("&b" + event.getPlayer().getName() + " &chas left! This user has &b30 &cseconds to rejoin otherwise, the server will restart!");
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					Player player = ProPlugin.getPlayer(event.getPlayer().getName());
					if(player == null) {
						ProPlugin.restartServer(5);
					}
				}
			}, 20 * 30);
		}
	}
}
