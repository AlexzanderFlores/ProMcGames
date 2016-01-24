package promcgames.gameapi;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.game.GameKillEvent;
import promcgames.customevents.game.GameLossEvent;
import promcgames.customevents.game.GameStatChangeEvent;
import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerSpectateStartEvent;
import promcgames.customevents.player.PlayerViewStatsEvent;
import promcgames.customevents.player.StatsChangeEvent;
import promcgames.customevents.player.StatsChangeEvent.StatsType;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.DoubleUtil;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.TimeUtil;
import promcgames.staff.KillLogger;
import promcgames.staff.StaffMode;

public class StatsHandler implements Listener {
	public static class GameStats {
		int wins = 0;
		int losses = 0;
		int kills = 0;
		int deaths = 0;
		int monthlyWins = 0;
		int monthlyLosses = 0;
		int monthlyKills = 0;
		int monthlyDeaths = 0;
		int originalWins = 0;
		int originalLosses = 0;
		int originalKills = 0;
		int originalDeaths = 0;
		
		public GameStats(Player player) {
			if(table.isUUIDSet(Disguise.getUUID(player))) {
				String uuid = Disguise.getUUID(player).toString();
				wins = table.getInt("uuid", uuid, "wins");
				losses = table.getInt("uuid", uuid, "losses");
				kills = table.getInt("uuid", uuid, "kills");
				deaths = table.getInt("uuid", uuid, "deaths");
				if(monthly != null) {
					String [] keys = new String [] {"uuid", "date"};
					String [] values = new String [] {uuid, TimeUtil.getTime().substring(0, 7)};
					if(monthly.isKeySet(keys, values)) {
						monthlyWins = monthly.getInt(keys, values, "wins");
						monthlyLosses = monthly.getInt(keys, values, "losses");
						monthlyKills = monthly.getInt(keys, values, "kills");
						monthlyDeaths = monthly.getInt(keys, values, "deaths");
					} else {
						String date = TimeUtil.getTime().substring(0, 7);
						monthly.insert("'" + Disguise.getUUID(player).toString() + "', '" + date + "', '0', '0', '0', '0'");
					}
				}
				originalWins = wins;
				originalLosses = losses;
				originalKills = kills;
				originalDeaths = deaths;
			} else {
				table.insert("'" + Disguise.getUUID(player).toString() + "', '0', '0', '0', '0'");
			}
			gameStats.put(Disguise.getName(player), this);
		}
		
		public int getWins() {
			return this.wins;
		}
		
		public void addWins() {
			++wins;
		}
		
		public int getMonthlyWins() {
			return this.monthlyWins;
		}
		
		public void addMonthlyWins() {
			++monthlyWins;
		}
		
		public int getLosses() {
			return this.losses;
		}
		
		public void addLosses() {
			++losses;
		}
		
		public int getMonthlyLosses() {
			return this.monthlyLosses;
		}
		
		public void addMonthlyLosses() {
			++monthlyLosses;
		}
		
		public int getKills() {
			return this.kills;
		}
		
		public void addKills() {
			++kills;
		}
		
		public int getMonthlyKills() {
			return this.monthlyKills;
		}
		
		public void addMonthlyKills() {
			++monthlyKills;
		}
		
		public int getDeaths() {
			return this.deaths;
		}
		
		public void addDeaths() {
			++deaths;
		}
		
		public int getMonthlyDeaths() {
			return this.monthlyDeaths;
		}
		
		public void addMonthlyDeaths() {
			++monthlyDeaths;
		}
		
		public void removeDeath() {
			--deaths;
			--monthlyDeaths;
		}
		
		public int getOriginalWins() {
			return originalWins;
		}
		
		public int getOriginalLosses() {
			return originalLosses;
		}
		
		public int getOriginalKills() {
			return originalKills;
		}
		
		public int getOriginalDeaths() {
			return originalDeaths;
		}
	}
	
	private static Map<String, GameStats> gameStats = null;
	private static Map<String, String> combatTagged = null;
	public static enum StatTypes {RANK, WINS, LOSSES, KILLS, DEATHS};
	private static DB table = null;
	private static DB monthly = null;
	private static DB elo = null;
	private static String wins = null;
	private static String losses = null;
	private static String kills = null;
	private static String deaths = null;
	private static boolean enabled = false;
	private static boolean saveOnQuit = true;
	private static boolean viewOnly = false;
	
	public StatsHandler(DB table) {
		this(table, null);
	}
	
	public StatsHandler(DB table, DB monthly) {
		if(ProMcGames.getMiniGame() != null && !ProMcGames.getMiniGame().getStoreStats()) {
			return;
		}
		StatsHandler.table = table;
		StatsHandler.monthly = monthly;
		if(table == null) {
			new CommandBase("stats", -1) {
				@Override
				public boolean execute(CommandSender sender, String[] arguments) {
					MessageHandler.sendMessage(sender, "&cYou can only use this command on a game server");
					return true;
				}
			};
		} else {
			enabled = true;
			wins = "Wins";
			losses = "Losses";
			kills = "Kills";
			deaths = "Deaths";
			World world = ProMcGames.getMiniGame() == null ? Bukkit.getWorlds().get(0) : ProMcGames.getMiniGame().getLobby();
			if(ProMcGames.getPlugin() == Plugins.KIT_PVP) {
				Location leftDisplay = new Location(world, 19, 114, -160);//-7, 84, -16);
				Location rightDisplay = new Location(world, 19, 114, -156);//-3, 84, -16);
				Location names = new Location(world, 19, 114, -159);//-6, 84, -16);
				Location winsLosses = new Location(world, 19, 114, -158);//-5, 84, -16);
				Location killsDeaths = new Location(world, 19, 114, -157);//-4, 84, -16);
				Location statsSign = new Location(world, 19, 113, -160);//-7, 83, -16);
				new Top8(table, monthly, names, leftDisplay, rightDisplay, winsLosses, killsDeaths, statsSign).setRefreshTimer(5);
			} else if(ProMcGames.getPlugin() == Plugins.VERSUS) {
				Location leftDisplay = new Location(world, 12, 6, -2);
				Location rightDisplay = new Location(world, 12, 6, 2);
				Location names = new Location(world, 12, 6, -1);
				Location winsLosses = new Location(world, 12, 6, 0);
				Location killsDeaths = new Location(world, 12, 6, 1);
				Location statsSign = new Location(world, 12, 5, -2);
				new Top8(table, monthly, names, leftDisplay, rightDisplay, winsLosses, killsDeaths, statsSign).setRefreshTimer(5);
				leftDisplay = new Location(world, -2, 6, -12);
				rightDisplay = new Location(world, 2, 6, -12);
				names = new Location(world, -1, 6, -12);
				winsLosses = new Location(world, 0, 6, -12);
				killsDeaths = new Location(world, 1, 6, -12);
				statsSign = new Location(world, -2, 5, -12);
				new Top8(DB.PLAYERS_KILLSTREAKS, "streak", names, leftDisplay, rightDisplay, winsLosses, killsDeaths, statsSign).setRefreshTimer(5).setName("Killstreaks");
				leftDisplay = new Location(world, -12, 6, 2);
				rightDisplay = new Location(world, -12, 6, -2);
				names = new Location(world, -12, 6, 1);
				winsLosses = new Location(world, -12, 6, 0);
				killsDeaths = new Location(world, -12, 6, -1);
				statsSign = new Location(world, -12, 5, 2);
				new Top8(table, "wins", names, leftDisplay, rightDisplay, winsLosses, killsDeaths, statsSign).setRefreshTimer(5).setName("Tournament Wins");
			} else if(ProMcGames.getPlugin() == Plugins.SGHUB) {
				Location leftDisplay = new Location(world, -26, 7, -55);
				Location rightDisplay = new Location(world, -22, 7, -55);
				Location names = new Location(world, -25, 7, -55);
				Location winsLosses = new Location(world, -24, 7, -55);
				Location killsDeaths = new Location(world, -23, 7, -55);
				Location statsSign = new Location(world, -26, 6, -55);
				new Top8(table, names, leftDisplay, rightDisplay, winsLosses, killsDeaths, statsSign).setRefreshTimer(5);
				leftDisplay = new Location(world, -50, 9, -52);
				rightDisplay = new Location(world, -50, 9, -56);
				names = new Location(world, -50, 9, -53);
				winsLosses = new Location(world, -50, 9, -54);
				killsDeaths = new Location(world, -50, 9, -55);
				statsSign = new Location(world, -50, 8, -52);
				new Top8(DB.PLAYERS_ELO_CLANS, "amount", names, leftDisplay, rightDisplay, winsLosses, killsDeaths, statsSign).setRefreshTimer(5).setName("Elo");
			} else {
				Location leftDisplay = new Location(world, 3, 29, -36);
				Location rightDisplay = new Location(world, 3, 29, -32);
				Location names = new Location(world, 3, 29, -35);
				Location winsLosses = new Location(world, 3, 29, -34);
				Location killsDeaths = new Location(world, 3, 29, -33);
				Location statsSign = new Location(world, 3, 28, -36);
				new Top8(table, monthly, "wins", names, leftDisplay, rightDisplay, winsLosses, killsDeaths, statsSign);
				leftDisplay = new Location(world, 2, 29, -30);
				rightDisplay = new Location(world, -2, 29, -30);
				names = new Location(world, 1, 29, -30);
				winsLosses = new Location(world, 0, 29, -30);
				killsDeaths = new Location(world, -1, 29, -30);
				statsSign = new Location(world, 2, 28, -30);
				new Top8(null, monthly, "wins", names, leftDisplay, rightDisplay, winsLosses, killsDeaths, statsSign);
			}
			new CommandBase("stats", 0, 1) {
				@Override
				public boolean execute(CommandSender sender, String[] arguments) {
					String name = "";
					if(arguments.length == 0) {
						if(sender instanceof Player) {
							Player player = (Player) sender;
							name = Disguise.getName(player);
						} else {
							MessageHandler.sendUnknownCommand(sender);
							return true;
						}
					} else {
						name = arguments[0];
					}
					Player player = ProPlugin.getPlayer(name, true);
					if(player == null || (StaffMode.contains(player) && !sender.getName().equals(Disguise.getName(player)))) {
						MessageHandler.sendMessage(sender, "&c" + name + " is not online");
					} else {
						loadStats(player);
						MessageHandler.sendMessage(sender, AccountHandler.getPrefix(player, false) + "'s Statistics:");
						MessageHandler.sendMessage(sender, "Key: &cLifetime Stats &7/ &bMonthly Stats");
						if(Disguise.isDisguised(player)) {
							MessageHandler.sendMessage(sender, "&eRank: &cN/A (Above 5,000");
							MessageHandler.sendMessage(sender, "&e" + wins + ": &c0 &7/ &b0");
							MessageHandler.sendMessage(sender, "&e" + losses + ": &c0 &7/ &b0");
							MessageHandler.sendMessage(sender, "&e" + kills + ": &c0 &7/ &b0");
							MessageHandler.sendMessage(sender, "&e" + deaths + ": &c0 &7/ &b0");
							MessageHandler.sendMessage(sender, "&eKDR: &c0.0 &7/ &b0.0");
						} else {
							int rank = getRank(player);
							int monthlyRank = getMonthlyRank(player);
							String rankString = "&eRank: &c" + (rank == -1 ? "N/A" : rank);
							rankString += " &7/ &b" + (monthlyRank == -1 ? "N/A" : monthlyRank);
							MessageHandler.sendMessage(sender, rankString);
							if(!gameStats.containsKey(player.getName())) {
								loadStats(player);
							}
							GameStats stats = gameStats.get(player.getName());
							MessageHandler.sendMessage(sender, "&e" + wins + ": &c" + stats.getWins() + " &7/ &b" + stats.getMonthlyWins());
							MessageHandler.sendMessage(sender, "&e" + losses + ": &c" + stats.getLosses() + " &7/ &b" + stats.getMonthlyLosses());
							MessageHandler.sendMessage(sender, "&e" + kills + ": &c" + stats.getKills() + " &7/ &b" + stats.getMonthlyKills());
							MessageHandler.sendMessage(sender, "&e" + deaths + ": &c" + stats.getDeaths() + " &7/ &b" + stats.getMonthlyDeaths());
							double kills = (double) gameStats.get(Disguise.getName(player)).getKills();
							double deaths = (double) gameStats.get(Disguise.getName(player)).getDeaths();
							double monthlyKills = (double) gameStats.get(Disguise.getName(player)).getMonthlyKills();
							double monthlyDeaths = (double) gameStats.get(Disguise.getName(player)).getMonthlyDeaths();
							double kdr = (kills == 0 || deaths == 0 ? 0 : DoubleUtil.round(kills / deaths, 2));
							double monthlyKdr = (monthlyKills == 0 || monthlyDeaths == 0 ? 0 : DoubleUtil.round(monthlyKills / monthlyDeaths, 2));
							MessageHandler.sendMessage(sender, "&eKDR: &c" + kdr + " &7/ &b" + monthlyKdr);
						}
						if(sender instanceof Player) {
							Player viewer = (Player) sender;
							Bukkit.getPluginManager().callEvent(new PlayerViewStatsEvent(viewer, player.getUniqueId(), player.getName()));
						}
					}
					return true;
				}
			};
			new KillLogger();
			EventUtil.register(this);
		}
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	public static void setEloDB(DB elo) {
		if(StatsHandler.elo == null) {
			new EloHandler();
		}
		StatsHandler.elo = elo;
	}
	
	public static DB getEloDB() {
		return elo;
	}
	
	public static boolean getSaveOnQuit() {
		return saveOnQuit;
	}
	
	public static boolean getViewOnly() {
		return viewOnly;
	}
	
	public static void setSaveOnQuit(boolean saveOnQuit) {
		StatsHandler.saveOnQuit = saveOnQuit;
	}
	
	public static void loadStats(Player player) {
		if(gameStats == null) {
			gameStats = new HashMap<String, GameStats>();
		}
		if(!gameStats.containsKey(Disguise.getName(player))) {
			new GameStats(ProPlugin.getPlayer(player.getName()));
		}
	}
	
	public static void setViewOnly(boolean viewOnly) {
		StatsHandler.viewOnly = viewOnly;
	}
	
	public static void setWinsString(String wins) {
		StatsHandler.wins = wins;
	}
	
	public static void setLossesString(String losses) {
		StatsHandler.losses = losses;
	}
	
	public static void setKillsString(String kills) {
		StatsHandler.kills = kills;
	}
	
	public static void setDeathsString(String deaths) {
		StatsHandler.deaths = deaths;
	}
	
	private static int getRank(Player player) {
		return getRank(player, 0);
	}
	
	private static int getMonthlyRank(Player player) {
		return getMonthlyRank(player, 0);
	}
	
	private static int getRank(Player player, int index) {
		return Top8.getTop8s().get(index).getRank(player);
	}
	
	private static int getMonthlyRank(Player player, int index) {
		return Top8.getTop8s().get(index).getMonthlyRank(player);
	}
	
	public static int getWins(Player player) {
		loadStats(player);
		return gameStats.get(Disguise.getName(player)).getWins();
	}
	
	public static int getMonthlyWins(Player player) {
		loadStats(player);
		return gameStats.get(Disguise.getName(player)).getMonthlyWins();
	}
	
	public static int getLosses(Player player) {
		loadStats(player);
		return gameStats.get(Disguise.getName(player)).getLosses();
	}
	
	public static int getMonthlyLosses(Player player) {
		loadStats(player);
		return gameStats.get(Disguise.getName(player)).getMonthlyLosses();
	}
	
	public static int getKills(Player player) {
		loadStats(player);
		return gameStats.get(Disguise.getName(player)).getKills();
	}
	
	public static int getMonthlyKills(Player player) {
		loadStats(player);
		return gameStats.get(Disguise.getName(player)).getMonthlyKills();
	}
	
	public static int getDeaths(Player player) {
		loadStats(player);
		return gameStats.get(Disguise.getName(player)).getDeaths();
	}
	
	public static int getMonthlyDeaths(Player player) {
		loadStats(player);
		return gameStats.get(Disguise.getName(player)).getMonthlyDeaths();
	}
	
	public static void addWin(Player player) {
		if(viewOnly) {
			return;
		}
		StatsChangeEvent event = new StatsChangeEvent(player, StatsType.WIN);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled()) {
			loadStats(player);
			gameStats.get(Disguise.getName(player)).addWins();
			gameStats.get(Disguise.getName(player)).addMonthlyWins();
		}
	}
	
	public static void addLoss(Player player) {
		if(viewOnly) {
			return;
		}
		StatsChangeEvent event = new StatsChangeEvent(player, StatsType.LOSS);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled()) {
			loadStats(player);
			gameStats.get(Disguise.getName(player)).addLosses();
			gameStats.get(Disguise.getName(player)).addMonthlyLosses();
		}
	}
	
	public static void addKill(Player player) {
		if(viewOnly) {
			return;
		}
		StatsChangeEvent event = new StatsChangeEvent(player, StatsType.KILL);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled()) {
			loadStats(player);
			gameStats.get(Disguise.getName(player)).addKills();
			gameStats.get(Disguise.getName(player)).addMonthlyKills();
		}
	}
	
	public static void addDeath(Player player) {
		if(viewOnly) {
			return;
		}
		StatsChangeEvent event = new StatsChangeEvent(player, StatsType.DEATH);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled()) {
			loadStats(player);
			gameStats.get(Disguise.getName(player)).addDeaths();
			gameStats.get(Disguise.getName(player)).addMonthlyDeaths();
		}
	}
	
	public static void removeDeath(Player player) {
		if(viewOnly) {
			return;
		}
		loadStats(player);
		gameStats.get(Disguise.getName(player)).removeDeath();
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(event.getPlayer() != null) {
			addWin(event.getPlayer());
		} else if(event.getTeam() != null) {
			for(OfflinePlayer offlinePlayer : event.getTeam().getPlayers()) {
				Player player = offlinePlayer.getPlayer();
				if(player != null) {
					addWin(player);
				}
			}
			for(String died : TeamHandler.getDied().get(event.getTeam().getName())) {
				Player player = ProPlugin.getPlayer(died);
				if(player != null) {
					addWin(player);
				}
			}
		}
	}
	
	@EventHandler
	public void onGameLoss(GameLossEvent event) {
		if(event.getPlayer() != null) {
			addLoss(event.getPlayer());
		} else if(event.getTeam() != null) {
			for(String died : TeamHandler.getDied().get(event.getTeam().getName())) {
				Player player = ProPlugin.getPlayer(died);
				if(player != null) {
					addLoss(player);
				}
			}
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		addKill(event.getPlayer());
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		addDeath(event.getPlayer());
		EffectUtil.displayDeath(event.getPlayer().getLocation());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(!event.isCancelled() && event.getEntity() instanceof Player && (event.getDamager() instanceof Player || event.getDamager() instanceof Projectile)) {
			Player attacker = null;
			if(event.getDamager() instanceof Player) {
				attacker = (Player) event.getDamager();
			} else if(event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				if(projectile.getShooter() instanceof Player) {
					attacker = (Player) projectile.getShooter();
				}
			}
			if(attacker != null && !SpectatorHandler.contains(attacker)) {
				final Player player = (Player) event.getEntity();
				if(!SpectatorHandler.contains(player)) {
					if(combatTagged == null) {
						combatTagged = new HashMap<String, String>();
					}
					combatTagged.put(Disguise.getName(player), attacker.getName());
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							combatTagged.remove(Disguise.getName(player));
						}
					}, 20 * 5);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerSpectateStart(PlayerSpectateStartEvent event) {
		if(combatTagged != null) {
			combatTagged.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(!SpectatorHandler.contains(player) && combatTagged != null && combatTagged.containsKey(Disguise.getName(player))) {
			if(ProMcGames.getMiniGame() != null && ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
				return;
			}
			Player attacker = ProPlugin.getPlayer(combatTagged.get(Disguise.getName(player)));
			if(attacker != null) {
				addKill(attacker);
				MessageHandler.sendMessage(attacker, "Given 1 kill due to " + Disguise.getName(player) + " combat logging");
			}
			if(ProMcGames.getPlugin() != Plugins.KIT_PVP && ProMcGames.getPlugin() != Plugins.VERSUS) {
				addLoss(player);
			}
			addDeath(player);
			combatTagged.remove(Disguise.getName(player));
		}
		if(gameStats != null && gameStats.containsKey(Disguise.getName(player))) {
			if(saveOnQuit) {
				GameStats stats = gameStats.get(Disguise.getName(player));
				String uuid = Disguise.getUUID(player).toString();
				table.updateInt("wins", stats.getWins(), "uuid", uuid);
				table.updateInt("losses", stats.getLosses(), "uuid", uuid);
				table.updateInt("kills", stats.getKills(), "uuid", uuid);
				table.updateInt("deaths", stats.getDeaths(), "uuid", uuid);
				if(monthly != null) {
					String [] keys = new String [] {"uuid", "date"};
					String [] values = new String [] {uuid, TimeUtil.getTime().substring(0, 7)};
					monthly.updateInt("wins", stats.getMonthlyWins(), keys, values);
					monthly.updateInt("losses", stats.getMonthlyLosses(), keys, values);
					monthly.updateInt("kills", stats.getMonthlyKills(), keys, values);
					monthly.updateInt("deaths", stats.getMonthlyDeaths(), keys, values);
				}
				Bukkit.getPluginManager().callEvent(new GameStatChangeEvent(stats, player));
			}
			gameStats.remove(Disguise.getName(player));
		}
	}
}
