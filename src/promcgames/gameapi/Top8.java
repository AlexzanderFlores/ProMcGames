package promcgames.gameapi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneMinuteTaskEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.TimeUtil;

@SuppressWarnings("deprecation")
public class Top8 implements Listener {
	private static List<Top8> top8s = null;
	private static List<String> recentlyClickedReset = null;
	private static List<String> displayedPrizes = null;
	private static int delay = 2;
	//private static boolean alert = true;
	private String name = null;
	private DB table = null;
	private DB monthly = null;
	private String orderBy = null;
	private Sign leftDisplay = null;
	private Sign rightDisplay = null;
	private Sign names = null;
	private Sign winsLosses = null;
	private Sign killsDeaths = null;
	private Sign statsSign = null;
	private int refresh = -1;
	private int counter = 0;
	private boolean displayLifetime = true;
	private List<String> topRanked = null;
	private List<String> topRankedMonthly = null;
	
	public static List<Top8> getTop8s() {
		return top8s;
	}
	
	public Top8(DB table, Location names, Location leftDisplay, Location rightDisplay, Location winsLosses, Location killsDeaths, Location statsSign) {
		this(table, null, "kills", names, leftDisplay, rightDisplay, winsLosses, killsDeaths, statsSign);
	}
	
	public Top8(DB table, String orderBy, Location names, Location leftDisplay, Location rightDisplay, Location winsLosses, Location killsDeaths, Location statsSign) {
		this(table, null, orderBy, names, leftDisplay, rightDisplay, winsLosses, killsDeaths, statsSign);
	}
	
	public Top8(DB table, DB monthly, Location names, Location leftDisplay, Location rightDisplay, Location winsLosses, Location killsDeaths, Location statsSign) {
		this(table, monthly, "kills", names, leftDisplay, rightDisplay, winsLosses, killsDeaths, statsSign);
	}
	
	public Top8(DB table, DB monthly, String orderBy, Location names, Location leftDisplay, Location rightDisplay, Location winsLosses, Location killsDeaths, Location statsSign) {
		setTable(table);
		setMonthlyTable(monthly);
		setOrderBy(orderBy);
		setLeftDisplay(leftDisplay);
		setRightDisplay(rightDisplay);
		setNames(names);
		setWinsLosses(winsLosses);
		setKillsDeaths(killsDeaths);
		setStatsSign(statsSign);
		if(topRanked == null && ProMcGames.getProPlugin().getUseTop8()) {
			EventUtil.register(this);
		}
		topRanked = new ArrayList<String>();
		if(table == null) {
			displayLifetime = false;
		} else {
			topRanked = table.getOrdered(orderBy, "uuid", 5000, true);
		}
		topRankedMonthly = new ArrayList<String>();
		if(monthly != null) {
			topRankedMonthly = monthly.getOrdered(orderBy, "uuid", 5000, true);
		}
		if(ProMcGames.getProPlugin().getUseTop8()) {
			populate();
		}
		if(top8s == null) {
			top8s = new ArrayList<Top8>();
		}
		top8s.add(this);
		if(displayedPrizes == null) {
			displayedPrizes = new ArrayList<String>();
		}
	}
	
	public Top8 setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public Top8 setTable(DB table) {
		this.table = table;
		return this;
	}
	
	public DB getTable() {
		return table;
	}
	
	public Top8 setMonthlyTable(DB monthly) {
		this.monthly = monthly;
		return this;
	}
	
	public DB getMonthlyTable() {
		return monthly;
	}
	
	public Top8 setOrderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}
	
	public String getOrderBy() {
		return orderBy;
	}
	
	public Top8 setLeftDisplay(Location location) {
		if(location.getBlock().getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) location.getBlock().getState();
			setLeftDisplay(sign);
		} else {
			Bukkit.getLogger().info("Location (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getZ() + ") is not a sign");
		}
		return this;
	}
	
	public Top8 setLeftDisplay(Sign sign) {
		this.leftDisplay = sign;
		return this;
	}
	
	public Sign getLeftDisplay() {
		return leftDisplay;
	}
	
	public Top8 setRightDisplay(Location location) {
		if(location.getBlock().getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) location.getBlock().getState();
			setRightDisplay(sign);
		} else {
			Bukkit.getLogger().info("Location (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getZ() + ") is not a sign");
		}
		return this;
	}
	
	public Top8 setRightDisplay(Sign sign) {
		this.rightDisplay = sign;
		return this;
	}
	
	public Sign getRightDisplay() {
		return rightDisplay;
	}
	
	public Top8 setNames(Location location) {
		if(location.getBlock().getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) location.getBlock().getState();
			setNames(sign);
		} else {
			Bukkit.getLogger().info("Location (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getZ() + ") is not a sign");
		}
		return this;
	}
	
	public Top8 setNames(Sign names) {
		this.names = names;
		return this;
	}
	
	public Sign getNames() {
		return names;
	}
	
	public Top8 setWinsLosses(Location location) {
		if(location.getBlock().getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) location.getBlock().getState();
			setWinsLosses(sign);
		} else {
			Bukkit.getLogger().info("Location (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getZ() + ") is not a sign");
		}
		return this;
	}
	
	public Top8 setWinsLosses(Sign winsLosses) {
		this.winsLosses = winsLosses;
		return this;
	}
	
	public Sign getWinsLosses() {
		return winsLosses;
	}
	
	public Top8 setKillsDeaths(Location location) {
		if(location.getBlock().getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) location.getBlock().getState();
			setKillsDeaths(sign);
		} else {
			Bukkit.getLogger().info("Location (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getZ() + ") is not a sign");
		}
		return this;
	}
	
	public Top8 setKillsDeaths(Sign killsDeaths) {
		this.killsDeaths = killsDeaths;
		return this;
	}
	
	public Sign getKillsDeaths() {
		return killsDeaths;
	}
	
	public Top8 setStatsSign(Location location) {
		if(location.getBlock().getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) location.getBlock().getState();
			setStatsSign(sign);
		} else {
			Bukkit.getLogger().info("Location (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getZ() + ") is not a sign");
		}
		return this;
	}
	
	public Top8 setStatsSign(Sign statsSign) {
		this.statsSign = statsSign;
		return this;
	}
	
	public Sign getStatsSign() {
		return statsSign;
	}
	
	public Top8 setRefreshTimer(int refresh) {
		this.refresh = refresh;
		return this;
	}
	
	public int getRefresh() {
		return refresh;
	}
	
	public List<String> getTopRanked() {
		return topRanked;
	}
	
	public int getRank(Player player) {
		String uuid = player.getUniqueId().toString();
		if(topRanked != null && topRanked.contains(uuid)) {
			return topRanked.indexOf(uuid) + 1;
		} else {
			return -1;
		}
	}
	
	public int getMonthlyRank(Player player) {
		String uuid = player.getUniqueId().toString();
		if(topRankedMonthly != null && topRankedMonthly.contains(uuid)) {
			return topRankedMonthly.indexOf(uuid) + 1;
		} else {
			return -1;
		}
	}
	
	public void populate() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				DB db = table;
				if(db == null || (!displayLifetime && monthly != null)) {
					db = monthly;
				}
				if(db == null) {
					return;
				}
				DB oldTable = db;
				String orderBy = getOrderBy();
				if(db != StatsHandler.getEloDB() && ProMcGames.getPlugin() == Plugins.SGHUB) {
					setTable(DB.NETWORK_CLANS);
					db = getTable();
					orderBy = "battle_wins";
				}
				List<String> uuids = null;
				List<String> names = new ArrayList<String>();
				if(ProMcGames.getPlugin() == Plugins.SGHUB) {
					if(db == StatsHandler.getEloDB()) {
						uuids = db.getOrdered(orderBy, "uuid", 8, true);
						for(String uuid : uuids) {
							names.add(AccountHandler.getName(UUID.fromString(uuid)));
						}
					} else {
						uuids = db.getOrdered(orderBy, "id", 8, true);
						for(String id : uuids) {
							names.add(DB.NETWORK_CLANS.getString("id", id, "clan_name"));
						}
					}
				} else {
					if(displayLifetime) {
						uuids = db.getOrdered(orderBy, "uuid", 8, true);
					} else {
						uuids = db.getOrdered(orderBy, "uuid", "date", TimeUtil.getTime().substring(0, 7), 8, true);
					}
					for(String uuid : uuids) {
						names.add(AccountHandler.getName(UUID.fromString(uuid)));
					}
				}
				if(names.isEmpty() || names.size() < 8) {
					return;
				}
				List<String> wins = null;
				List<String> losses = null;
				List<String> kills = null;
				List<String> deaths = null;
				if(ProMcGames.getPlugin() == Plugins.VERSUS && orderBy.equals("streak")) {
					wins = db.getOrdered(orderBy, "streak", 8, true);
					losses = new ArrayList<String>();
					kills = new ArrayList<String>();
					deaths = new ArrayList<String>();
					for(int a = 0; a < 8; ++a) {
						losses.add("0");
						kills.add("0");
						deaths.add("0");
					}
				} else if(ProMcGames.getPlugin() == Plugins.SGHUB) {
					if(db == StatsHandler.getEloDB()) {
						wins = db.getOrdered(orderBy, "amount", 8, true);
						losses = new ArrayList<String>();
						kills = new ArrayList<String>();
						deaths = new ArrayList<String>();
						for(int a = 0; a < 8; ++a) {
							losses.add("0");
							kills.add("0");
							deaths.add("0");
						}
					} else {
						wins = db.getOrdered(orderBy, "battle_wins", 8, true);
						losses = db.getOrdered(orderBy, "battle_losses", 8, true);
						kills = new ArrayList<String>();
						deaths = new ArrayList<String>();
						for(int a = 0; a < 8; ++a) {
							kills.add("0");
							deaths.add("0");
						}
					}
				} else {
					if(displayLifetime) {
						wins = db.getOrdered(orderBy, "wins", 8, true);
						losses = db.getOrdered(orderBy, "losses", 8, true);
						kills = db.getOrdered(orderBy, "kills", 8, true);
						deaths = db.getOrdered(orderBy, "deaths", 8, true);
					} else {
						String date = TimeUtil.getTime().substring(0, 7);
						wins = db.getOrdered(orderBy, "wins", "date", date, 8, true);
						losses = db.getOrdered(orderBy, "losses", "date", date, 8, true);
						kills = db.getOrdered(orderBy, "kills", "date", date, 8, true);
						deaths = db.getOrdered(orderBy, "deaths", "date", date, 8, true);
					}
				}
				Sign namesSignTop = getNames();
				for(int a = 0; a < 4; ++a) {
					namesSignTop.setLine(a, names.get(a));
				}
				namesSignTop.update();
				Sign namesSignBottom = (Sign) getNames().getLocation().getBlock().getRelative(0, -1, 0).getState();
				for(int a = 0; a < 4; ++a) {
					namesSignBottom.setLine(a, names.get(a + 4));
				}
				namesSignBottom.update();
				Sign winsLossesTop = getWinsLosses();
				for(int a = 0; a < 4; ++a) {
					winsLossesTop.setLine(a, wins.get(a) + " - " + losses.get(a));
				}
				winsLossesTop.update();
				Sign winsLossesBottom = (Sign) getWinsLosses().getBlock().getRelative(0, -1, 0).getState();
				for(int a = 0; a < 4; ++a) {
					winsLossesBottom.setLine(a, wins.get(a + 4) + " - " + losses.get(a + 4));
				}
				winsLossesBottom.update();
				Sign killsDeathsTop = getKillsDeaths();
				for(int a = 0; a < 4; ++a) {
					killsDeathsTop.setLine(a, kills.get(a) + " - " + deaths.get(a));
				}
				killsDeaths.update();
				Sign killsDeathsBottom = (Sign) killsDeathsTop.getBlock().getRelative(0, -1, 0).getState();
				for(int a = 0; a < 4; ++a) {
					killsDeathsBottom.setLine(a, kills.get(a + 4) + " - " + deaths.get(a + 4));
				}
				killsDeathsBottom.update();
				/*if(alert) {
					alert = false;
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							alert = true;
						}
					}, 20 * 5);
					MessageHandler.alertLine();
					if(getName() == null) {
						MessageHandler.alert("&cRanks have been updated on the top 8 wall");
					} else {
						MessageHandler.alert("&cRanks for \"&b" + getName() + "&c\" have been updated on the top 8 wall");
					}
					if(displayLifetime) {
						getLeftDisplay().setLine(2, "(Lifetime)");
						getLeftDisplay().update();
						getRightDisplay().setLine(2, "(Lifetime)");
						getRightDisplay().update();
						MessageHandler.alert("&aDisplaying &cLifetime &aranks on the Top 8");
					} else {
						getLeftDisplay().setLine(2, "(Monthly)");
						getLeftDisplay().update();
						getRightDisplay().setLine(2, "(Monthly)");
						getRightDisplay().update();
						MessageHandler.alert("&aDisplaying &cMonthly &aranks on the Top 8");
						MessageHandler.alert("&6Prizes go to Top 3 players at the end of the month! &f/prizes");
					}
					MessageHandler.alertLine();
				}*/
				if(displayLifetime) {
					setTable(oldTable);
				}
				if(monthly == null) {
					displayLifetime = true;
				} else {
					displayLifetime = !displayLifetime;
				}
			}
		});
	}
	
	@EventHandler
	public void onOneMinuteTask(OneMinuteTaskEvent event) {
		if(ProMcGames.getPlugin() == Plugins.KIT_PVP || ProMcGames.getPlugin() == Plugins.VERSUS || ProMcGames.getPlugin() == Plugins.SGHUB) {
			if(++counter % getRefresh() == 0) {
				populate();
			}
		} else {
			OneMinuteTaskEvent.getHandlerList().unregister(this);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();
		if(action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			if(block.getType() == Material.WALL_SIGN) {
				int x1 = block.getX();
				int y1 = block.getY();
				int z1 = block.getZ();
				int x2 = getStatsSign().getX();
				int y2 = getStatsSign().getY();
				int z2 = getStatsSign().getZ();
				if(x1 == x2 && y1 == y2 && z1 == z2) {
					player.chat("/stats");
					event.setCancelled(true);
				} else {
					Sign sign = (Sign) block.getState();
					if(sign.getLine(1).contains("To reset your") && sign.getLine(2).contains("statistics")) {
						final String name = player.getName();
						if(recentlyClickedReset == null) {
							recentlyClickedReset = new ArrayList<String>();
						}
						if(!recentlyClickedReset.contains(name)) {
							recentlyClickedReset.add(name);
							new DelayedTask(new Runnable() {
								@Override
								public void run() {
									recentlyClickedReset.remove(name);
								}
							}, 20 * delay);
							MessageHandler.sendMessage(player, "http://store.promcgames.com/category/359455");
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(monthly != null) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!displayedPrizes.contains(player.getName()) && player.getWorld().getName().equals(winsLosses.getWorld().getName())) {
					if(player.getLocation().toVector().isInSphere(winsLosses.getLocation().toVector(), 5)) {
						Block block = player.getTargetBlock(null, 4);
						if(block != null && block.getType() == Material.WALL_SIGN) {
							displayedPrizes.add(player.getName());
							player.chat("/prizes");
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		displayedPrizes.remove(event.getPlayer().getName());
	}
}
