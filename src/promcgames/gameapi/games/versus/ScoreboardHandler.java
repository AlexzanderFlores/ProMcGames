package promcgames.gameapi.games.versus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.RestartAnnounceEvent;
import promcgames.customevents.ServerRestartEvent;
import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.player.AsyncPlayerJoinEvent;
import promcgames.customevents.player.AsyncPlayerLeaveEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerProTrialExpire;
import promcgames.customevents.player.PlayerViewStatsEvent;
import promcgames.customevents.player.StatsChangeEvent;
import promcgames.customevents.player.StatsChangeEvent.StatsType;
import promcgames.customevents.timed.FiveTickTaskEvent;
import promcgames.gameapi.games.versus.events.KillstreakReachedEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.scoreboard.SidebarScoreboardUtil;
import promcgames.server.DB;
import promcgames.server.util.EventUtil;

public class ScoreboardHandler implements Listener {
	private static Map<String, Integer> wins = null;
	private static Map<String, Integer> streaks = null;
	private ChatColor [] titleColors = null;
	private int titleColorCounter = 0;
	
	public ScoreboardHandler() {
		titleColors = new ChatColor [] {ChatColor.GREEN, ChatColor.RED, ChatColor.YELLOW};
		wins = new HashMap<String, Integer>();
		streaks = new HashMap<String, Integer>();
		ProMcGames.setSidebar(new SidebarScoreboardUtil(ChatColor.GREEN + " play.ProMcGames.com ") {
			@Override
			public void update() {
				super.update();
				for(String key : wins.keySet()) {
					if(wins.get(key) >= 3) {
						setText(key, wins.get(key));
					}
				}
			}
		});
		for(String uuidString : DB.PLAYERS_VERSUS_KILLSTREAKS.getAllStrings("uuid")) {
			UUID uuid = UUID.fromString(uuidString);
			wins.put(getText(uuid), DB.PLAYERS_VERSUS_KILLSTREAKS.getInt("uuid", uuidString, "streak"));
			DB.PLAYERS_VERSUS_KILLSTREAKS.deleteUUID(uuid);
		}
		EventUtil.register(this);
	}
	
	private static String getText(UUID uuid) {
		String text = AccountHandler.getRank(uuid).getColor() + AccountHandler.getName(uuid);
		if(text.length() > 16) {
			text = text.substring(0, 16);
		}
		return text;
	}
	
	private static String getText(Player player) {
		String text = AccountHandler.getRank(player).getColor() + player.getName();
		if(text.length() > 16) {
			text = text.substring(0, 16);
		}
		return text;
	}
	
	private void reset(Player player) {
		player.setLevel(0);
		wins.remove(getText(player));
	}
	
	public static int getWins(Player player) {
		return wins.containsKey(getText(player)) ? wins.get(getText(player)) : 0;
	}
	
	private void remove(Player player) {
		String text = getText(player);
		reset(player);
		ProMcGames.getSidebar().removeText(text);
		ProMcGames.getSidebar().update();
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		streaks.put(player.getName(), DB.PLAYERS_KILLSTREAKS.getInt("uuid", player.getUniqueId().toString(), "streak"));
		ProMcGames.getSidebar().update();
		if(wins.containsKey(getText(player))) {
			MessageHandler.sendMessage(player, "Loading kill streak from before restart...");
		}
	}
	
	@EventHandler
	public void onStatsChange(StatsChangeEvent event) {
		if(event.getType() == StatsType.KILL) {
			Battle battle = BattleHandler.getBattle(event.getPlayer());
			if(battle != null) {
				if(battle.isRanked()) {
					Player player = event.getPlayer();
					String text = getText(player);
					if(wins.containsKey(text)) {
						wins.put(text, wins.get(text) + 1);
					} else {
						wins.put(text, 1);
					}
					int win = wins.get(text);
					if(win > streaks.get(player.getName())) {
						if(win >= 3) {
							MessageHandler.sendLine(player, "&d");
							MessageHandler.sendMessage(player, "&bNEW personal best killstreak: &e" + win);
							MessageHandler.sendLine(player, "&d");
						}
						streaks.put(player.getName(), win);
					}
					ProMcGames.getSidebar().update();
					Bukkit.getPluginManager().callEvent(new KillstreakReachedEvent(player, wins.get(text)));
				} else {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerViewStats(PlayerViewStatsEvent event) {
		Player player = event.getPlayer();
		Player target = Bukkit.getPlayer(event.getTargetUUID());
		if(target != null) {
			MessageHandler.sendMessage(player, "&eBest Killstreak: &c" + streaks.get(target.getName()));
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		Player player = event.getPlayer();
		remove(player);
	}
	
	@EventHandler
	public void onPlayerProTrialExpire(PlayerProTrialExpire event) {
		Player player = event.getPlayer();
		String text = Ranks.PRO.getColor() + player.getName();
		if(text.length() > 16) {
			text = text.substring(0, 16);
		}
		int win = wins.get(text);
		wins.remove(text);
		ProMcGames.getSidebar().removeText(text);
		text = Ranks.PLAYER.getColor() + player.getName();
		if(text.length() > 16) {
			text = text.substring(0, 16);
		}
		wins.put(text, win);
		ProMcGames.getSidebar().update();
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getRealName();
		if(streaks.containsKey(name)) {
			UUID uuid = event.getRealUUID();
			if(streaks.containsKey(name) && streaks.get(name) > 0) {
				int streak = streaks.get(name);
				if(DB.PLAYERS_KILLSTREAKS.isUUIDSet(uuid)) {
					DB.PLAYERS_KILLSTREAKS.updateInt("streak", streak, "uuid", uuid.toString());
				} else {
					DB.PLAYERS_KILLSTREAKS.insert("'" + uuid.toString() + "', '" + streak + "'");
				}
			}
			streaks.remove(name);
		}
	}
	
	@EventHandler
	public void onRestartAnnounce(RestartAnnounceEvent event) {
		if(event.getCounter() > 5) {
			MessageHandler.alert("&6Note: &aKillstreaks are saved through restarts!");
		}
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			String text = getText(player);
			if(wins.containsKey(text)) {
				int win = wins.get(text);
				if(DB.PLAYERS_VERSUS_KILLSTREAKS.isUUIDSet(player.getUniqueId())) {
					DB.PLAYERS_VERSUS_KILLSTREAKS.updateInt("streak", win, "uuid", player.getUniqueId().toString());
				} else {
					DB.PLAYERS_VERSUS_KILLSTREAKS.insert("'" + player.getUniqueId().toString() + "', '" + win + "'");
				}
			}
		}
	}
	
	@EventHandler
	public void onFiveTick(FiveTickTaskEvent event) {
		if(titleColorCounter >= titleColors.length) {
			titleColorCounter = 0;
		}
		ProMcGames.getSidebar().getObjective().setDisplayName(titleColors[titleColorCounter++] + " play.ProMcGames.com ");
	}
}
