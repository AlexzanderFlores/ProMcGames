package promcgames.server.servers.clans;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;

import promcgames.ProPlugin;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PostPlayerJoinEvent;
import promcgames.customevents.player.timed.PlayerTenSecondConnectedEvent;
import promcgames.gameapi.EloHandler;
import promcgames.player.scoreboard.SidebarScoreboardUtil;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class ScoreboardHandlerx implements Listener {
	private static Map<String, SidebarScoreboardUtil> sgSB = null;
	private static Map<String, SidebarScoreboardUtil> clansSB = null;
	
	public ScoreboardHandlerx() {
		sgSB = new HashMap<String, SidebarScoreboardUtil>();
		clansSB = new HashMap<String, SidebarScoreboardUtil>();
		EventUtil.register(this);
	}
	
	public static void updateClanName(Player player, String newClanName) {
		/*String user = player.getName();
		if(clansSB.containsKey(user)) {
			if(newClanName.length() > 16) {
				newClanName = newClanName.substring(0, 16);
			}
			clansSB.get(user).removeScore(-3);
			clansSB.get(user).setText(newClanName, -3);
			clansSB.get(user).update(player);
		}*/
	}
	
	@EventHandler
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		final String name = player.getName();
		// SG
		SidebarScoreboardUtil sgSidebar = new SidebarScoreboardUtil(Bukkit.getScoreboardManager().getNewScoreboard(), "&eSG Stats");
		sgSidebar.setText("Wins", 0);
		sgSidebar.setText("Losses", 0);
		sgSidebar.setText("Kills", 0);
		sgSidebar.setText("Deaths", 0);
		sgSidebar.update(player);
		player.setScoreboard(sgSidebar.getScoreboard());
		sgSB.put(name, sgSidebar);
		
		// Clans
		SidebarScoreboardUtil clanSidebar = new SidebarScoreboardUtil(Bukkit.getScoreboardManager().getNewScoreboard(), "&eClan Stats");
		clanSidebar.setText("Clan Wins", 0);
		clanSidebar.setText("Clan Losses", 0);
		clanSidebar.setText("Clan Battles", 0);
		clanSidebar.setText("Clan Kills", 0);
		clanSidebar.setText("Clan Deaths", 0);
		String clanName = "&fNone";
		Clan clan = ClanHandler.getClan(player);
		if(clan != null) {
			clanName = clan.getColorTheme() + clan.getClanName();
			if(clanName.length() > 16) {
				clanName = clanName.substring(0, 16);
			}
		}
		clanSidebar.setText(new String [] {
			" ",
			"&eClan:",
			clanName,
			"  "
		});
		clansSB.put(name, clanSidebar);
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					UUID uuid = player.getUniqueId();
					int wins = DB.PLAYERS_STATS_SURVIVAL_GAMES.getInt("uuid", uuid.toString(), "wins");
					int losses = DB.PLAYERS_STATS_SURVIVAL_GAMES.getInt("uuid", uuid.toString(), "losses");
					int kills = DB.PLAYERS_STATS_SURVIVAL_GAMES.getInt("uuid", uuid.toString(), "kills");
					int deaths = DB.PLAYERS_STATS_SURVIVAL_GAMES.getInt("uuid", uuid.toString(), "deaths");
					SidebarScoreboardUtil sgSidebar = sgSB.get(name);
					sgSidebar.setText("Wins", wins);
					sgSidebar.setText("Losses", losses);
					sgSidebar.setText("Games", wins + losses);
					sgSidebar.setText("Kills", kills);
					sgSidebar.setText("Deaths", deaths);
					sgSidebar.setText(new String [] {
						" ",
						"&eWant to reset",
						"&eyour stats?",
						"&b/buy"
					}, -1);
					sgSidebar.update();
					
					wins = DB.PLAYERS_STATS_CLANS.getInt("uuid", uuid.toString(), "wins");
					losses = DB.PLAYERS_STATS_CLANS.getInt("uuid", uuid.toString(), "wins");
					SidebarScoreboardUtil clanSidebar = clansSB.get(name);
					clanSidebar.setText("Clan Wins", wins);
					clanSidebar.setText("Clan Losses", losses);
					clanSidebar.setText("Clan Battles", wins + losses);
					clanSidebar.setText("Clan Kills", DB.PLAYERS_STATS_CLANS.getInt("uuid", uuid.toString(), "kills"));
					clanSidebar.setText("Clan Deaths", DB.PLAYERS_STATS_CLANS.getInt("uuid", uuid.toString(), "deaths"));
					clanSidebar.setText("&c" + EloHandler.getElo(player) + " &aElo", -5);
					clanSidebar.update();
				}
			}
		}, 25);
	}
	
	@EventHandler
	public void onPlayerTenSecondConnected(PlayerTenSecondConnectedEvent event) {
		Player player = event.getPlayer();
		try {
			if(player.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getDisplayName().contains("Clan")) {
				player.setScoreboard(sgSB.get(player.getName()).getScoreboard());
			} else {
				player.setScoreboard(clansSB.get(player.getName()).getScoreboard());
			}
		} catch(NullPointerException e) {
			
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		String name = event.getPlayer().getName();
		sgSB.get(name).remove();
		sgSB.remove(name);
		clansSB.get(name).remove();
		clansSB.remove(name);
	}
}