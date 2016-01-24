package promcgames.server.servers.hub;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.ProMcGames;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.FiveTickTaskEvent;
import promcgames.customevents.timed.TenSecondTaskEvent;
import promcgames.player.scoreboard.SidebarScoreboardUtil;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class ScoreboardHandler implements Listener {
	//private int uniquePlayers = 0;
	private int onlinePlayers = 0;
	private static int dailyVotes = 0;
	private int titleColorCounter = 0;
	private ChatColor [] titleColors = null;
	
	public ScoreboardHandler() {
		titleColors = new ChatColor [] {ChatColor.GREEN, ChatColor.RED, ChatColor.YELLOW};
		ProMcGames.setSidebar(new SidebarScoreboardUtil(" play.ProMcGames.com ") {
			@Override
			public void update() {
				String supporter = RecentPurchaseDisplayer.getRecentCustomers().get(0); 
				try {
					if(!ChatColor.stripColor(getText(-4)).equals(ChatColor.stripColor(getDailyVotes()))) {
						removeScore(-4);
					}
				} catch(Exception e) {
					
				}
				try {
					if(!ChatColor.stripColor(getText(-9)).equals(ChatColor.stripColor(supporter))) {
						removeScore(-9);
					}
				} catch(Exception e) {
					
				}
				//setText(ChatColor.AQUA + "Unique Logins", uniquePlayers);
				setText(ChatColor.AQUA + "Online Players", onlinePlayers);
				setText(ChatColor.AQUA + "Players on hub", Bukkit.getOnlinePlayers().size());
				//setText(new String [] {" ", "Hub #" + Hub.hubNumber});
				setText(new String [] {" ", "&cDaily Vote", "&cGoal:", getDailyVotes(), "&b&l/voteGoal"}, -1);
				setText(new String [] {"  ", "&cMost Recent", "&cDonator:", "&e" + supporter, "&b&l/buy"}, -6);
				super.update();
			}
		});
		loadData();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().setScoreboard(ProMcGames.getScoreboard());
		ProMcGames.getSidebar().update();
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		ProMcGames.getSidebar().update();
	}
	
	@EventHandler
	public void onFiveTick(FiveTickTaskEvent event) {
		if(titleColorCounter >= titleColors.length) {
			titleColorCounter = 0;
		}
		ProMcGames.getSidebar().getObjective().setDisplayName(titleColors[titleColorCounter++] + " play.ProMcGames.com ");
	}
	
	@EventHandler
	public void onTenSecond(TenSecondTaskEvent event) {
		loadData();
	}
	
	private static int getVotes() {
		return DB.PLAYERS_DAILY_VOTES.getInt("day", String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)), "votes");
	}
	
	private static int getMax() {
		int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		return day == 1 || day == 6 || day == 7 ? 125 : 100;
	}
	
	public static String getDailyVotes() {
		return getDailyVotes(false);
	}
	
	public static String getDailyVotes(boolean load) {
		return "&e" + (load ? getVotes() : dailyVotes) + "/" + getMax();
	}
	
	public static boolean voteGoalReached() {
		return getVotes() >= getMax();
	}
	
	private void loadData() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				//uniquePlayers = DB.PLAYERS_ACCOUNTS.getSize();
				int players = 0;
				for(String population : DB.NETWORK_POPULATIONS.getAllStrings("population")) {
					players += Integer.valueOf(population);
				}
				onlinePlayers = players;
				dailyVotes = getVotes();
				ProMcGames.getSidebar().update();
			}
		});
	}
}
