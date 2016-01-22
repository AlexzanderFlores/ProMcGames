package promcgames.anticheat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.ProPlugin;
import promcgames.anticheat.killaura.InventoryKillAuraDetection;
import promcgames.anticheat.killaura.KillAuraSpectatorCheck;
import promcgames.customevents.player.PlayerBanEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.PerformanceHandler;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.StringUtil;
import promcgames.server.util.TimeUtil;
import promcgames.staff.ban.BanHandler;

public class AntiGamingChair {
	private static boolean enabled = true;
	private static List<String> banned = null;
	private String name = null;
	private int maxPing = 135;
	
	public AntiGamingChair() {
		banned = new ArrayList<String>();
		if(ProMcGames.getPlugin() != Plugins.HUB) {
			new SpeedFix();
			new SurvivalFly();
			new InvisibleFireGlitchFix();
			new FenceGlitchFix();
			new FastBowFix();
			new AutoCritFix();
		}
		new KillAuraSpectatorCheck();
		new InventoryKillAuraDetection();
		new HeadlessFix();
		new SpamBotFix();
		new WaterWalkDetection();
		new AutoClicker();
		//new AntiToggleSneak();
		new CommandBase("fakeDisable", 2) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				Player player = ProPlugin.getPlayer(name);
				if(player == null) {
					MessageHandler.sendMessage(sender, "&c" + name + " is not online");
				} else {
					String type = arguments[1];
					if(type.equals("Survival_Fly") || type.equals("Speed")) {
						MessageHandler.sendMessage(player, "&b&lAnti-Cheat&e: \"&a" + type.replace("_", " ") + "&e\" has been disabled due to lag/performance problems");
						MessageHandler.sendMessage(sender, "Told " + AccountHandler.getPrefix(player));
					} else {
						MessageHandler.sendMessage(sender, "&cUnkown hack type! Use: &aSurvival_Fly &cor &aSpeed");
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.HELPER);
	}
	
	public AntiGamingChair(String name) {
		this.name = name;
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	public static void setEnabled(boolean enabled) {
		AntiGamingChair.enabled = enabled;
	}
	
	protected int getMaxPing() {
		return maxPing;
	}
	
	public boolean notIgnored(Player player) {
		int ping = PerformanceHandler.getPing(player);
		return ping > 0 && ping <= maxPing && !BanHandler.checkForBanned(player) && !SpectatorHandler.contains(player);
	}
	
	public void kick(Player player) {
		kick(player, "KICKED");
	}
	
	public void kick(Player player, String action) {
		String message = StringUtil.color("&bAnti Gaming Chair: &f" + Disguise.getName(player) + " &chas been &4" + action + "&c: \"&e" + this.name + "&c\"");
		Bukkit.broadcastMessage(message);
		ProPlugin.sendPlayerToServer(player, "hub");
	}
	
	public void ban(Player player) {
		ban(player, Bukkit.getConsoleSender());
	}
	
	public void ban(Player player, CommandSender sender) {
		if(!BanHandler.checkForBanned(player) && !banned.contains(player.getName())) {
			banned.add(player.getName());
			int playerPing = PerformanceHandler.getPing(player);
			int staffPing = 0;
			if(sender instanceof Player) {
				Player staff = (Player) sender;
				staffPing = PerformanceHandler.getPing(staff);
			}
			if(PerformanceHandler.getTicksPerSecond() < 19) {
				MessageHandler.sendMessage(sender, "&cThere is possible server lag, ban cannot be executed");
			} else if(playerPing >= maxPing) {
				MessageHandler.sendMessage(sender, "&c" + Disguise.getName(player) + " has a ping above " + maxPing + ", ban cannot be executed due to possible lag");
			} else if(staffPing >= maxPing) {
				MessageHandler.sendMessage(sender, "&cYou have a ping above " + maxPing + ", ban cannot be executed due to possible lag");
			} else {
				String information = playerPing + "-" + staffPing + "-" + ProMcGames.getServerName();
				final String message = StringUtil.color("&bAnti Gaming Chair: &f" + Disguise.getName(player) + " &chas been &4BANNED &cfor use of the black-listed modification: \"&e" + this.name + "&c\" " + information);
				Bukkit.broadcastMessage(message);
				String time = TimeUtil.getTime();
				String date = time.substring(0, 7);
				String uuid = "CONSOLE";
				if(sender instanceof Player) {
					Player staff = (Player) sender;
					uuid = Disguise.getUUID(staff).toString();
					MessageHandler.sendMessage(sender, "(Note that this ban will be credited to you)");
				}
				UUID playerUUID = Disguise.getUUID(player);
				Bukkit.getPluginManager().callEvent(new PlayerBanEvent(playerUUID, sender));
				int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
				DB.STAFF_BAN.insert("'" + playerUUID.toString() + "', 'null', '" + uuid + "', 'null', 'HACKING', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
				String proof = this.name + "-" + information;
				String [] keys = new String [] {"uuid", "active"};
				String [] values = new String [] {playerUUID.toString(), "1"};
				int id = DB.STAFF_BAN.getInt(keys, values, "id");
				DB.STAFF_BAN_PROOF.insert("'" + id + "', '" + proof + "'");
				int counter = 0;
				for(String uuidString : DB.PLAYERS_ACCOUNTS.getAllStrings("uuid", "address", AccountHandler.getAddress(playerUUID))) {
					if(!uuidString.equals(uuid.toString())) {
						Player online = Bukkit.getPlayer(UUID.fromString(uuidString));
						if(online != null) {
							online.kickPlayer(ChatColor.RED + "You have been banned due to sharing the IP of " + player.getName());
						}
						DB.STAFF_BAN.insert("'" + uuidString + "', '" + playerUUID.toString() + "', '" + uuid + "', 'null', 'HACKING', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
						keys = new String [] {"uuid", "active"};
						values = new String [] {uuidString, "1"};
						id = DB.STAFF_BAN.getInt(keys, values, "id");
						DB.STAFF_BAN_PROOF.insert("'" + id + "', '" + proof + "'");
						++counter;
					}
				}
				if(counter > 0) {
					MessageHandler.alert("&cBanning &e" + counter + " &caccount" + (counter == 1 ? "" : "s") + " that shared the same IP as &e" + player.getName());
				}
				ProPlugin.sendPlayerToServer(player, "slave");
				final String name = player.getName();
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = ProPlugin.getPlayer(name);
						if(player != null && player.isOnline()) {
							player.kickPlayer(message);
						}
						banned.remove(name);
					}
				}, 15);
			}
		}
	}
}
