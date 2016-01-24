package promcgames.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerProTrialExpire;
import promcgames.customevents.player.PostPlayerJoinEvent;
import promcgames.customevents.player.timed.PlayerHourOfPlaytimeEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.account.PlaytimeTracker;
import promcgames.player.account.PlaytimeTracker.Playtime;
import promcgames.player.account.PlaytimeTracker.TimeType;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.servers.hub.Parkour;
import promcgames.server.util.CountDownUtil;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

public class ProRankTrial implements Listener {
	private static Map<UUID, CountDownUtil> counters = null;
	
	public ProRankTrial() {
		counters = new HashMap<UUID, CountDownUtil>();
		if(ProMcGames.getPlugin() == Plugins.HUB) {
			new NPCEntity(EntityType.ZOMBIE, Ranks.PRO.getPrefix() + "&6rank trial", new Location(Bukkit.getWorlds().get(0), -102.5, 126, -173.5)) {
				@Override
				public void onInteract(Player player) {
					EffectUtil.playSound(player, Sound.ZOMBIE_IDLE);
					executeTrial(player);
				}
			};
		}
		new CommandBase("proTrial", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = null;
				String name = null;
				if(arguments.length == 0) {
					if(sender instanceof Player) {
						player = (Player) sender;
						name = player.getName();
					} else {
						MessageHandler.sendPlayersOnly(sender);
						return true;
					}
				} else if(arguments.length == 1) {
					name = arguments[0];
					player = ProPlugin.getPlayer(name);
					if(player == null) {
						MessageHandler.sendMessage(sender, "&c" + name + " is not online");
						return true;
					}
				}
				if(displayTimeLeft(sender, player, true) == null) {
					if(name.equals(sender.getName())) {
						MessageHandler.sendMessage(sender, "&cYou currently do not have a trial for " + Ranks.PRO.getPrefix());
					} else {
						MessageHandler.sendMessage(sender, AccountHandler.getPrefix(player, false) + " &ccurrently does not have a trial for " + Ranks.PRO.getPrefix());
					}
				}
				return true;
			}
		};
		new CommandBase("resetProTrial", 1, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String target = arguments[0];
				UUID uuid = AccountHandler.getUUID(target);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + target + " has never logged in before");
				} else if(DB.PLAYERS_PRO_TRIALS.isUUIDSet(uuid)) {
					DB.PLAYERS_PRO_TRIALS.deleteUUID(uuid);
					MessageHandler.sendMessage(sender, target + " can now use the " + Ranks.PRO.getPrefix() + "&arank trial again");
				} else {
					MessageHandler.sendMessage(sender, "&c" + target + " has never used the " + Ranks.PRO.getPrefix() + "&crank trial before");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	private void executeTrial(Player player) {
		int result = canUseTrial(player);
		if(result == 1) {
			if(ProMcGames.getPlugin() == Plugins.HUB && !Parkour.isParkouring(player)) {
				player.setAllowFlight(true);
				player.setFlying(true);
			}
			AccountHandler.setRank(player, Ranks.PRO, true);
			int counter = 60 * 60;
			counters.put(player.getUniqueId(), new CountDownUtil(counter));
			DB.PLAYERS_PRO_TRIALS.insert("'" + player.getUniqueId().toString() + "', '" + counter + "'");
			displayTimeLeft(player, false);
		} else if(result == -1) {
			MessageHandler.sendMessage(player, "&cOnly non-ranked players can use this feature");
		} else if(result == -2) {
			MessageHandler.sendMessage(player, "&cYou must have at least &e5 &chours of playtime to use this");
		} else if(result == -3) {
			MessageHandler.sendMessage(player, "&cYou already have or have had a trial for " + Ranks.PRO.getPrefix());
		}
	}
	
	private CountDownUtil displayTimeLeft(Player player, boolean fromCommand) {
		return displayTimeLeft(player, player, fromCommand);
	}
	
	private CountDownUtil displayTimeLeft(CommandSender sender, Player player, boolean fromCommand) {
		UUID uuid = player.getUniqueId();
		if(counters.containsKey(uuid)) {
			int timeLeft = counters.get(uuid).getCounter();
			if(timeLeft > 0) {
				CountDownUtil countDown = counters.get(uuid);
				MessageHandler.sendMessage(sender, "&6Your " + Ranks.PRO.getPrefix() + "&6rank trial will expire in " + countDown.getCounterAsString());
				if(!fromCommand) {
					MessageHandler.sendMessage(sender, "Check this any time with &6/proTrial");
				}
				MessageHandler.sendMessage(sender, "&6Want to re-use your trial? &bstore.promcgames.com/category/359455");
				return countDown;
			}
		}
		return null;
	}
	
	public static boolean isInTrial(Player player) {
		return counters != null && counters.containsKey(player.getUniqueId());
	}
	
	/*
	 * Return values:
	 * 1 = can use
	 * -1 = already has a rank
	 * -2 = not enough playtime
	 * -3 = already has a trial for pro
	 */
	public static int canUseTrial(Player player) {
		if(AccountHandler.getRank(player) == Ranks.PLAYER) {
			Playtime playtime = PlaytimeTracker.getPlayTime(player);
			if(playtime == null) {
				return -2;
			}
			int weeks = playtime.getWeeks(TimeType.LIFETIME);
			int days = playtime.getDays(TimeType.LIFETIME);
			int hours = playtime.getHours(TimeType.LIFETIME);
			if(weeks >= 1 || days >= 1 || hours >= 5) {
				if(DB.PLAYERS_PRO_TRIALS.isUUIDSet(player.getUniqueId())) {
					MessageHandler.sendMessage(player, "&6Want to re-use your trial? &bstore.promcgames.com/category/359455");
					return -3;
				} else {
					return 1;
				}
			} else {
				return -2;
			}
		} else {
			return -1;
		}
	}
	
	@EventHandler
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(AccountHandler.getRank(player) == Ranks.PRO) {
			int timeLeft = DB.PLAYERS_PRO_TRIALS.getInt("uuid", player.getUniqueId().toString(), "time_left");
			if(timeLeft > 0) {
				counters.put(event.getPlayer().getUniqueId(), new CountDownUtil(timeLeft));
				displayTimeLeft(player, false);
			}
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		for(UUID uuid: counters.keySet()) {
			CountDownUtil countDownUtil = counters.get(uuid);
			countDownUtil.decrementCounter();
			if(countDownUtil.getCounter() <= 0) {
				counters.remove(uuid);
				Player player = Bukkit.getPlayer(uuid);
				if(ProMcGames.getPlugin() == Plugins.HUB && player.getAllowFlight()) {
					player.setFlying(false);
					player.setAllowFlight(false);
				}
				AccountHandler.setRank(player, Ranks.PLAYER, true);
				DB.PLAYERS_PRO_TRIALS.updateInt("time_left", 0, "uuid", uuid.toString());
				MessageHandler.sendLine(player, "&6");
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, "&cYour " + Ranks.PRO.getPrefix() + "&crank trial has expired!");
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendMessage(player, "&6Want &c&l25% &6&loff " + Ranks.PRO.getPrefix() + "&6? Use code \"&e&lProTrial&6\"! &c&l/buy");
				MessageHandler.sendMessage(player, "&6Want to re-use your trial? &bstore.promcgames.com/category/359455");
				MessageHandler.sendMessage(player, "");
				MessageHandler.sendLine(player, "&6");
				Bukkit.getPluginManager().callEvent(new PlayerProTrialExpire(player));
			}
		}
	}
	
	@EventHandler
	public void onPlayerHourOfPlaytime(PlayerHourOfPlaytimeEvent event) {
		Player player = event.getPlayer();
		if(!Disguise.isDisguised(player)) {
			int counter = canUseTrial(player);
			if(counter == 1) {
				MessageHandler.sendMessage(player, "&eYou can try our " + Ranks.PRO.getPrefix() + "&etrial FREE for 1 hour! Click the hub NPC");
			} else if(counter == -3) {
				MessageHandler.sendMessage(player, "&eYou can try our " + Ranks.PRO.getPrefix() + "&etrial FREE for 1 hour! Click the hub NPC");
				MessageHandler.sendMessage(player, "Already used the trial? Reuse it for just &b$0.99 &ahere:");
				MessageHandler.sendMessage(player, "&6http://store.promcgames.com/category/359455");
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(counters.containsKey(player.getUniqueId())) {
			DB.PLAYERS_PRO_TRIALS.updateInt("time_left", counters.get(player.getUniqueId()).getCounter(), "uuid", player.getUniqueId().toString());
			counters.remove(player.getUniqueId());
		}
	}
}
