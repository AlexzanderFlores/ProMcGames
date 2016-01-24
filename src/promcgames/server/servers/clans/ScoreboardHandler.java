package promcgames.server.servers.clans;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.player.scoreboard.SidebarScoreboardUtil;
import promcgames.server.ProMcGames;
import promcgames.server.servers.clans.battle.BattleHandler;
import promcgames.server.util.EventUtil;

public class ScoreboardHandler implements Listener {
	public ScoreboardHandler() {
		ProMcGames.setSidebar(new SidebarScoreboardUtil("&eClans For Battle") {
			@Override
			public void update() {
				int nextClan = BattleHandler.getNextRandomClan();
				if(nextClan == -1) {
					removeScoresBelow(0);
					setText(new String [] {
						" ",
						"&cNo Clans",
						"&cWaiting for",
						"&cBattle At",
						"&cThis Time",
						"  ",
						"To Look For",
						"A Battle Do",
						"&e/c findBattle",
						"   "
					}, -1);
				} else {
					removeScoresBelow(0);
					setText(new String [] {
						" ",
						"Clan Looking",
						"For Battle:",
						"&b" + ClanHandler.getClan(nextClan).getClanName(),
						"  ",
						"To Battle Do:",
						"&e/c findBattle"
					}, -1);
				}
				super.update();
			}
		});
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().setScoreboard(ProMcGames.getScoreboard());
		ProMcGames.getSidebar().update();
	}
}
