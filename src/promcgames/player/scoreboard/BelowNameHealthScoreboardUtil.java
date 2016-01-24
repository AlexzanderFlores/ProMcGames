package promcgames.player.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import promcgames.player.Disguise;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.util.UnicodeUtil;

public class BelowNameHealthScoreboardUtil extends BelowNameScoreboardUtil {
	public BelowNameHealthScoreboardUtil() {
		this(ProMcGames.getSidebar().getScoreboard());
	}
	
	public BelowNameHealthScoreboardUtil(Scoreboard scoreboard) {
		super(scoreboard, "showhealth", "health", ChatColor.RED + UnicodeUtil.getHeart());
		for(Player player : ProMcGames.getProPlugin() == null ? Bukkit.getOnlinePlayers() : ProPlugin.getPlayers()) {
			setScore(Disguise.getName(player), (int) player.getHealth());
			player.setHealth(player.getMaxHealth() - 2.0d);
			player.setHealth(player.getMaxHealth());
		}
	}
}
