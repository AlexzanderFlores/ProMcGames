package promcgames.player.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.ProPlugin;
import promcgames.customevents.player.AsyncPlayerLeaveEvent;
import promcgames.customevents.player.NewPlayerJoinEvent;
import promcgames.customevents.player.PlayerAFKEvent;
import promcgames.customevents.player.PlaytimeLoadedEvent;
import promcgames.customevents.player.timed.PlayerDayOfPlaytimeEvent;
import promcgames.customevents.player.timed.PlayerFirstThirtyMinutesOfPlaytimeEvent;
import promcgames.customevents.player.timed.PlayerHourOfPlaytimeEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.TimeUtil;
import promcgames.staff.StaffMode;
import promcgames.staff.ban.BanHandler;

public class PlaytimeTracker implements Listener {
	public static class Playtime {
		private int weeks = 0;
		private int days = 0;
		private int hours = 0;
		private int minutes = 0;
		private int seconds = 0;
		private int monthlyWeeks = 0;
		private int monthlyDays = 0;
		private int monthlyHours = 0;
		private int monthlyMinutes = 0;
		private int monthlySeconds = 0;
		
		public Playtime(UUID uuid) {
			if(DB.PLAYERS_PLAY_TIME.isUUIDSet(uuid)) {
				setPlayTime(DB.PLAYERS_PLAY_TIME.getString("uuid", uuid.toString(), "play_time"), TimeType.LIFETIME);
			} else {
				setWeeks(0, TimeType.LIFETIME);
				setDays(0, TimeType.LIFETIME);
				setHours(0, TimeType.LIFETIME);
				setMinutes(0, TimeType.LIFETIME);
				setSeconds(0, TimeType.LIFETIME);
			}
			String [] keys = new String [] {"uuid", "date"};
			String [] values = new String [] {uuid.toString(), TimeUtil.getTime().substring(0, 7)};
			if(DB.PLAYERS_MONTHLY_PLAY_TIME.isKeySet(keys, values)) {
				setPlayTime(DB.PLAYERS_MONTHLY_PLAY_TIME.getString(keys, values, "play_time"), TimeType.MONTHLY);
			} else {
				setWeeks(0, TimeType.MONTHLY);
				setDays(0, TimeType.MONTHLY);
				setHours(0, TimeType.MONTHLY);
				setMinutes(0, TimeType.MONTHLY);
				setSeconds(0, TimeType.MONTHLY);
			}
			Player player = Bukkit.getPlayer(uuid);
			if(ProMcGames.getPlugin() != Plugins.HUB && ProMcGames.getMiniGame() == null) {
				int required = 10;
				if(weeks == 0 && days == 0 && hours == 0 && minutes < required) {
					Bukkit.getPluginManager().callEvent(new NewPlayerJoinEvent(player));
					/*if(newPlayers == null) {
						newPlayers = new ArrayList<String>();
					}
					newPlayers.add(player.getName());
					String prefix = AccountHandler.getPrefix(player);
					String message = "&4NOTE: " + prefix + " &bhas joined with less than &e" + required + " &bminutes of playtime. They could be hacking";
					for(Player online : Bukkit.getOnlinePlayers()) {
						if(Ranks.isStaff(online)) {
							MessageHandler.sendMessage(online, message);
						}
					}*/
				}
			}
			Bukkit.getPluginManager().callEvent(new PlaytimeLoadedEvent(player));
		}
		
		public void setPlayTime(String string, TimeType type) {
			String playtime = string.split("-")[0];
			String [] data = playtime.split("/");
			setWeeks(Integer.valueOf(data[0]), type);
			setDays(Integer.valueOf(data[1]), type);
			setHours(Integer.valueOf(data[2]), type);
			setMinutes(Integer.valueOf(data[3]), type);
			setSeconds(Integer.valueOf(data[4]), type);
		}
		
		public int getWeeks(TimeType type) {
			if(type == TimeType.LIFETIME) {
				return this.weeks;
			} else {
				return this.monthlyWeeks;
			}
		}
		
		public void setWeeks(int weeks, TimeType type) {
			if(type == TimeType.LIFETIME) {
				this.weeks = weeks;
			} else {
				this.monthlyWeeks = weeks;
			}
		}
		
		public int getDays(TimeType type) {
			if(type == TimeType.LIFETIME) {
				return this.days;
			} else {
				return this.monthlyDays;
			}
		}
		
		public void setDays(int days, TimeType type) {
			if(type == TimeType.LIFETIME) {
				this.days = days;
			} else {
				this.monthlyDays = days;
			}
		}
		
		public int getHours(TimeType type) {
			if(type == TimeType.LIFETIME) {
				return this.hours;
			} else {
				return this.monthlyHours;
			}
		}
		
		public void setHours(int hours, TimeType type) {
			if(type == TimeType.LIFETIME) {
				this.hours = hours;
			} else {
				this.monthlyHours = hours;
			}
		}
		
		public int getMinutes(TimeType type) {
			if(type == TimeType.LIFETIME) {
				return this.minutes;
			} else {
				return this.monthlyMinutes;
			}
		}
		
		public void setMinutes(int minutes, TimeType type) {
			if(type == TimeType.LIFETIME) {
				this.minutes = minutes;
			} else {
				this.monthlyMinutes = minutes;
			}
		}
		
		public int getSeconds(TimeType type) {
			if(type == TimeType.LIFETIME) {
				return this.seconds;
			} else {
				return this.monthlySeconds;
			}
		}
		
		public int setSeconds(int seconds, TimeType type) {
			if(type == TimeType.LIFETIME) {
				return this.seconds = seconds;
			} else {
				return this.monthlySeconds = seconds;
			}
		}
		
		public String getString(TimeType type) {
			return getWeeks(type) + "/" + getDays(type) + "/" + getHours(type) + "/" + getMinutes(type) + "/" + getSeconds(type);
		}
		
		public String getDisplay(TimeType type) {
			return getWeeks(type) + "w " + getDays(type) + "d " + getHours(type) + "h " + getMinutes(type) + "m " + getSeconds(type) + "s";
		}
		
		public void addSecond(Player player) {
			if(++seconds >= 60) {
				seconds = 0;
				++minutes;
				if(minutes == 30 && hours == 0 && days == 0 && weeks == 0) {
					Bukkit.getPluginManager().callEvent(new PlayerFirstThirtyMinutesOfPlaytimeEvent(player));
				}
				if(minutes >= 60) {
					minutes = 0;
					Bukkit.getPluginManager().callEvent(new PlayerHourOfPlaytimeEvent(player));
					MessageHandler.sendMessage(player, "You passed another hour of playtime!");
					++hours;
					if(hours >= 24) {
						hours = 0;
						Bukkit.getPluginManager().callEvent(new PlayerDayOfPlaytimeEvent(player));
						MessageHandler.sendMessage(player, "You passed another day of playtime!");
						if(++days >= 7) {
							days = 0;
							++weeks;
						}
					}
				}
			}
			if(++monthlySeconds >= 60) {
				monthlySeconds = 0;
				if(++monthlyMinutes >= 60) {
					monthlyMinutes = 0;
					if(++monthlyHours >= 24) {
						monthlyHours = 0;
						if(++monthlyDays >= 7) {
							monthlyDays = 0;
							++monthlyWeeks;
						}
					}
				}
			}
		}
	}
	
	private static Map<String, Playtime> playtime = null;
	private static List<String> newPlayers = null;
	private static List<String> queue = null;
	private List<String> afk = null;
	public enum TimeType {LIFETIME, MONTHLY}
	
	public PlaytimeTracker() {
		playtime = new HashMap<String, Playtime>();
		queue = new ArrayList<String>();
		new CommandBase("playTime", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = null;
				if(arguments.length == 0) {
					if(sender instanceof Player ){
						player = (Player) sender;
					} else {
						MessageHandler.sendPlayersOnly(sender);
						return true;
					}
				} else if(arguments.length == 1) {
					player = ProPlugin.getPlayer(arguments[0]);
				}
				if(player == null || (StaffMode.contains(player) && !sender.getName().equals(player.getName()))) {
					MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online");
				} else {
					Playtime playtime = getPlayTime(player);
					if(playtime == null) {
						MessageHandler.sendMessage(sender, "&cPlaytime is not loaded yet, try again soon");
					} else {
						MessageHandler.sendMessage(sender, AccountHandler.getPrefix(player, false) + "'s lifetime play time: " + playtime.getDisplay(TimeType.LIFETIME));
						MessageHandler.sendMessage(sender, AccountHandler.getPrefix(player, false) + "'s monthly play time: " + playtime.getDisplay(TimeType.MONTHLY));
						MessageHandler.sendMessage(sender, "Monthly playtime is for " + TimeUtil.getTime().substring(0, 7));
					}
				}
				return true;
			}
		}.enableDelay(1);
		EventUtil.register(this);
	}
	
	public static boolean isNew(Player player) {
		return newPlayers != null && newPlayers.contains(player.getName());
	}
	
	public static Playtime getPlayTime(Player player) {
		if(!playtime.containsKey(Disguise.getName(player))) {
			final String name = player.getName();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					Player player = ProPlugin.getPlayer(name);
					if(player != null) {
						playtime.put(Disguise.getName(player), new Playtime(Disguise.getUUID(player)));
					}
				}
			});
			return null;
		}
		return playtime.get(Disguise.getName(player));
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if((afk != null && afk.contains(Disguise.getName(player))) || player.getVehicle() != null || player.getTicksLived() <= 40) {
				continue;
			}
			if(ProMcGames.getPlugin() == Plugins.HUB && BanHandler.checkForBanned(player)) {
				continue;
			}
			if(playtime.containsKey(player.getName())) {
				Playtime playtime = getPlayTime(player);
				if(playtime != null) {
					playtime.addSecond(player);
				}
			} else if(!queue.contains(player.getName())) {
				queue.add(player.getName());
			}
		}
		if(!queue.isEmpty()) {
			String name = queue.get(0);
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				getPlayTime(player);
			}
			queue.remove(0);
		}
	}
	
	@EventHandler
	public void onPlayerAFKEvent(PlayerAFKEvent event) {
		if(event.getAFK()) {
			if(afk == null) {
				afk = new ArrayList<String>();
			}
			if(!afk.contains(event.getPlayer().getName())) {
				afk.add(event.getPlayer().getName());
			}
		} else if(afk != null && afk.contains(event.getPlayer().getName())) {
			afk.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getRealUUID();
		String name = event.getRealName();
		if(playtime.containsKey(name)) {
			Playtime time = playtime.get(name);
			if(DB.PLAYERS_PLAY_TIME.isUUIDSet(uuid)) {
				DB.PLAYERS_PLAY_TIME.updateString("play_time", time.getString(TimeType.LIFETIME), "uuid", uuid.toString());
			} else {
				DB.PLAYERS_PLAY_TIME.insert("'" + uuid.toString() + "', '" + time.getString(TimeType.LIFETIME) + "'");
			}
			String date = TimeUtil.getTime().substring(0, 7);
			String [] keys = new String [] {"uuid", "date"};
			String [] values = new String [] {uuid.toString(), date};
			if(DB.PLAYERS_MONTHLY_PLAY_TIME.isKeySet(keys, values)) {
				DB.PLAYERS_MONTHLY_PLAY_TIME.updateString("play_time", time.getString(TimeType.MONTHLY), keys, values);
			} else {
				DB.PLAYERS_MONTHLY_PLAY_TIME.insert("'" + uuid.toString() + "', '" + date + "', '" + time.getString(TimeType.MONTHLY) + "'");
			}
			playtime.remove(name);
		}
		if(afk != null && afk.contains(name)) {
			afk.remove(name);
		}
		if(newPlayers != null) {
			newPlayers.remove(name);
		}
		queue.remove(name);
	}
}
