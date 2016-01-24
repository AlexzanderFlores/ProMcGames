package promcgames.server;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.player.timed.PlayerFiveMinuteConnectedEvent;
import promcgames.customevents.player.timed.PlayerFiveSecondConnectedOnceEvent;
import promcgames.customevents.player.timed.PlayerOneHourConnectedEvent;
import promcgames.customevents.player.timed.PlayerOneMinuteConnectedEvent;
import promcgames.customevents.player.timed.PlayerTenSecondConnectedEvent;
import promcgames.customevents.timed.FifteenTickTaskEvent;
import promcgames.customevents.timed.FiveMinuteTaskEvent;
import promcgames.customevents.timed.FiveSecondTaskEvent;
import promcgames.customevents.timed.FiveTickTaskEvent;
import promcgames.customevents.timed.OneAndAHalfSecondTask;
import promcgames.customevents.timed.OneMinuteTaskEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.customevents.timed.OneTickTaskEvent;
import promcgames.customevents.timed.SevenSecondTaskEvent;
import promcgames.customevents.timed.TenSecondTaskEvent;
import promcgames.customevents.timed.TenTickTaskEvent;
import promcgames.customevents.timed.ThirtySecondTaskEvent;
import promcgames.customevents.timed.ThreeSecondTaskEvent;
import promcgames.customevents.timed.ThreeTickTaskEvent;
import promcgames.customevents.timed.TwentyMinuteTaskEvent;
import promcgames.customevents.timed.TwoMinuteTaskEvent;
import promcgames.customevents.timed.TwoSecondTaskEvent;
import promcgames.customevents.timed.TwoTickTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.util.EventUtil;

public class PerformanceHandler implements Listener {
	private int counter = 0;
	private static double ticksPerSecond = 0;
	private long seconds = 0;
	private long currentSecond = 0;
	private int ticks = 0;
	private static int uptimeCounter = 0;
	
	public PerformanceHandler() {
		new CommandBase("lag") {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					String perm = "bukkit.command.tps";
					PermissionAttachment permission = player.addAttachment(ProMcGames.getInstance());
					permission.setPermission(perm, true);
					player.chat("/tps");
					permission.unsetPermission(perm);
					permission.remove();
					permission = null;
					MessageHandler.sendMessage(sender, "&bPing: &c" + getPing(player));
				} else {
					Bukkit.dispatchCommand(sender, "tps");
				}
				int averagePing = 0;
				if(Bukkit.getOnlinePlayers().size() > 0) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						averagePing += getPing(player);
					}
					averagePing /= Bukkit.getOnlinePlayers().size();
				}
				//MessageHandler.sendMessage(sender, "&bTicks per second: &c" + ticksPerSecond);
				MessageHandler.sendMessage(sender, "&bAverage ping: &c" + averagePing);
				MessageHandler.sendMessage(sender, "&bConnected clients: &c" + Bukkit.getOnlinePlayers().size());
				MessageHandler.sendMessage(sender, "&bUsed memory: &c" + getMemory(!Ranks.DEV.hasRank(sender)) + "%");
				MessageHandler.sendMessage(sender, "&bUptime: &c" + getUptimeString());
				MessageHandler.sendMessage(sender, "&eFor more server performance info run /bungeeInfo");
				return true;
			}
		};
		new CommandBase("ping", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(arguments.length == 0) {
					if(sender instanceof Player) {
						Player player = (Player) sender;
						MessageHandler.sendMessage(player, "Your ping is " + getPing(player));
					} else {
						MessageHandler.sendPlayersOnly(sender);
					}
				} else if(arguments.length == 1) {
					Player player = ProPlugin.getPlayer(arguments[0]);
					if(player == null) {
						MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online");
					} else {
						MessageHandler.sendMessage(sender, player.getName() + "'s ping is " + getPing(player));
					}
				}
				return true;
			}
		};
		Bukkit.getScheduler().runTaskTimer(ProMcGames.getInstance(), new Runnable() {
			@Override
			public void run() {
				++counter;
				if(counter % (20 * 60 * 20) == 0) {
					Bukkit.getPluginManager().callEvent(new TwentyMinuteTaskEvent());
				}
				if(counter % (20 * 60 * 5) == 0) {
					Bukkit.getPluginManager().callEvent(new FiveMinuteTaskEvent());
				}
				if(counter % (20 * 60 * 2) == 0) {
					Bukkit.getPluginManager().callEvent(new TwoMinuteTaskEvent());
				}
				if(counter % (20 * 60) == 0) {
					Bukkit.getPluginManager().callEvent(new OneMinuteTaskEvent());
				}
				if(counter % (20 * 30) == 0) {
					Bukkit.getPluginManager().callEvent(new ThirtySecondTaskEvent());
				}
				if(counter % (20 * 10) == 0) {
					Bukkit.getPluginManager().callEvent(new TenSecondTaskEvent());
				}
				if(counter % (20 * 7) == 0) {
					Bukkit.getPluginManager().callEvent(new SevenSecondTaskEvent());
				}
				if(counter % (20 * 5) == 0) {
					Bukkit.getPluginManager().callEvent(new FiveSecondTaskEvent());
				}
				if(counter % (20 * 3) == 0) {
					Bukkit.getPluginManager().callEvent(new ThreeSecondTaskEvent());
				}
				if(counter % (20 * 2) == 0) {
					Bukkit.getPluginManager().callEvent(new TwoSecondTaskEvent());
				}
				if(counter % 30 == 0) {
					Bukkit.getPluginManager().callEvent(new OneAndAHalfSecondTask());
				}
				if(counter % 20 == 0) {
					Bukkit.getPluginManager().callEvent(new OneSecondTaskEvent());
				}
				if(counter % 15 == 0) {
					Bukkit.getPluginManager().callEvent(new FifteenTickTaskEvent());
				}
				if(counter % 10 == 0) {
					Bukkit.getPluginManager().callEvent(new TenTickTaskEvent());
				}
				if(counter % 5 == 0) {
					Bukkit.getPluginManager().callEvent(new FiveTickTaskEvent());
				}
				if(counter % 3 == 0) {
					Bukkit.getPluginManager().callEvent(new ThreeTickTaskEvent());
				}
				if(counter % 2 == 0) {
					Bukkit.getPluginManager().callEvent(new TwoTickTaskEvent());
				}
				Bukkit.getPluginManager().callEvent(new OneTickTaskEvent());
			}
		}, 1, 1);
		EventUtil.register(this);
	}
	
	public static int getPing(Player player) {
		CraftPlayer craftPlayer = (CraftPlayer) player;
		return craftPlayer.getHandle().ping / 2;
	}
	
	public static double getTicksPerSecond() {
		return ticksPerSecond;
	}
	
	public static double getMemory() {
		return getMemory(true);
	}
	
	public static double getMemory(boolean round) {
		double total = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		double allocated = Runtime.getRuntime().maxMemory() / (1024 * 1024);
		double value = (total * 100) / allocated;
		return round ? new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue() : value;
	}
	
	public static String getUptimeString() {
		String uptime = null;
		if(uptimeCounter < 60) {
			uptime = uptimeCounter + " second(s)";
		} else if(uptimeCounter < (60 * 60)) {
			int minutes = getAbsoluteValue((uptimeCounter / 60));
			int seconds = getAbsoluteValue((uptimeCounter % 60));
			uptime = minutes + " minute(s) and " + seconds + " second(s)";
		} else {
			int hours = getAbsoluteValue((uptimeCounter / 60 / 60));
			int minutes = getAbsoluteValue((hours * 60) - (uptimeCounter / 60));
			int seconds = getAbsoluteValue((uptimeCounter % 60));
			uptime = hours + " hour(s) and " + minutes + " minute(s) and " + seconds + " second(s)";
		}
		return uptime;
	}
	
	public static int getUptime() {
		return uptimeCounter;
	}
	
	private static int getAbsoluteValue(int value) {
		if(value < 0) {
			value *= -1;
		}
		return value;
	}
	
	@EventHandler
	public void onOneTickTask(OneTickTaskEvent event) {
		seconds = (System.currentTimeMillis() / 1000);
		if(currentSecond == seconds) {
			++ticks;
		} else {
			currentSecond = seconds;
			ticksPerSecond = (ticksPerSecond == 0 ? ticks : ((ticksPerSecond + ticks) / 2));
			if(ticksPerSecond < 19.0d) {
				++ticksPerSecond;
			}
			if(ticksPerSecond > 20.0d) {
				ticksPerSecond = 20.0d;
			}
			ticksPerSecond = new BigDecimal(ticksPerSecond).setScale(2, RoundingMode.HALF_UP).doubleValue();
			ticks = 0;
		}
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getTicksLived() % (20 * 60 * 60) == 0) {
				Bukkit.getPluginManager().callEvent(new PlayerOneHourConnectedEvent(player));
			}
			if(player.getTicksLived() % (20 * 60 * 5) == 0) {
				Bukkit.getPluginManager().callEvent(new PlayerFiveMinuteConnectedEvent(player));
			}
			if(player.getTicksLived() % (20 * 60) == 0) {
				Bukkit.getPluginManager().callEvent(new PlayerOneMinuteConnectedEvent(player));
			}
			if(player.getTicksLived() % (20 * 10) == 0) {
				Bukkit.getPluginManager().callEvent(new PlayerTenSecondConnectedEvent(player));
			}
			if(player.getTicksLived() == (20 * 5)) {
				Bukkit.getPluginManager().callEvent(new PlayerFiveSecondConnectedOnceEvent(player));
			}
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		++uptimeCounter;
	}
}
