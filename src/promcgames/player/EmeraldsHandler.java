package promcgames.player;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.GameStartingEvent;
import promcgames.customevents.player.AsyncPlayerLeaveEvent;
import promcgames.customevents.player.PostPlayerJoinEvent;
import promcgames.customevents.player.timed.PlayerDayOfPlaytimeEvent;
import promcgames.customevents.timed.OneMinuteTaskEvent;
import promcgames.customevents.timed.TenSecondTaskEvent;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class EmeraldsHandler implements Listener {
	public enum EmeraldReason {
		EMERALDS_GIVEN,
		HUB_CHECKPOINT_SET,
		HUB_GIVE_AWAY,
		VOTE,
		HEADSHOT,
		GAME_WIN,
		GAME_PARTICIPATE,
		GAME_KILL,
		SG_SPONSOR,
		PARKOUR_FINISH,
		KIT_PURCHASE,
		EMERALD_TRANSFER,
		CTF_PICK_UP,
		CTF_RETURN,
		CTF_CAPTURE,
		DOM_CAPTURE,
		DAY_OF_PLAYTIME,
		VERSUS_TOURNAMENT_WIN,
		HUB_SPONSOR_PURCHASE,
		KIT_PVP_LEVEL_PURCHASE,
		DAILY_GIFT,
		TOWER_RUSH_SHOP_PURCHASE
	}
	
	private static Map<String, Integer> gained = null;
	private static Map<String, Integer> originalValues = null;
	private static boolean doubleEmeraldsWeekend = false;
	
	public EmeraldsHandler() {
		gained = new HashMap<String, Integer>();
		originalValues = new HashMap<String, Integer>();
		new CommandBase("emeralds", 0, 1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String[] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String target = "";
						int amount = -1;
						if(arguments.length == 0) {
							if(sender instanceof Player) {
								Player player = (Player) sender;
								target = Disguise.getName(player);
								amount = getEmeralds(player);
							}
						} else if(arguments.length == 1) {
							target = arguments[0];
							Player player = ProPlugin.getPlayer(target);
							if(player == null) {
								if(target.equalsIgnoreCase("top")) {
									if(ProMcGames.getPlugin() == Plugins.HUB) {
										if(sender instanceof Player) {
											player = (Player) sender;
											player.teleport(new Location(player.getWorld(), -80.5, 126, -160.5, -90.0f, 0.0f));
										} else {
											MessageHandler.sendPlayersOnly(sender);
										}
									} else {
										MessageHandler.sendMessage(sender, "&cYou can only view the top ranked players on the hub");
									}
								} else {
									UUID uuid = AccountHandler.getUUID(target);
									if(uuid == null) {
										MessageHandler.sendMessage(sender, "&c" + target + " has never logged in before");
									} else {
										amount = DB.PLAYERS_EMERALDS_CURRENCY.getInt("uuid", uuid.toString(), "amount");
									}
								}
							} else {
								target = player.getName();
								amount = getEmeralds(player);
							}
						}
						if(amount > -1) {
							MessageHandler.sendMessage(sender, target + " has &e" + amount + " &2Emeralds");
							if(target.equalsIgnoreCase(sender.getName())) {
								MessageHandler.sendMessage(sender, "&cNote: &eIf you voted you may have to relog");
							}
							MessageHandler.sendMessage(sender, "View the top 8 ranked players: &c/emeralds top");
						}
					}
				});
				return true;
			}
		}.enableDelay(1);
		new CommandBase("addEmeralds", 2, false) {
			@Override
			public boolean execute(final CommandSender sender, final String[] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String target = arguments[0];
						int amount = 0;
						try {
							amount = Integer.valueOf(arguments[1]);
						} catch(NumberFormatException e) {
							MessageHandler.sendMessage(sender, "&f/addEmeralds <name> <amount>");
							return;
						}
						Player player = ProPlugin.getPlayer(target);
						if(player == null) {
							UUID uuid = AccountHandler.getUUID(target);
							if(uuid == null) {
								MessageHandler.sendMessage(sender, "&c" + target + " has never logged in before");
							} else {
								if(DB.PLAYERS_EMERALDS_CURRENCY.isUUIDSet(uuid)) {
									MessageHandler.sendMessage(sender, "Gave " + target + " &e" + amount + " &2Emeralds");
									amount += DB.PLAYERS_EMERALDS_CURRENCY.getInt("uuid", uuid.toString(), "amount");
									MessageHandler.sendMessage(sender, target + " now has &e" + amount + " &2Emeralds");
									DB.PLAYERS_EMERALDS_CURRENCY.updateInt("amount", amount, "uuid", uuid.toString());
								} else {
									DB.PLAYERS_EMERALDS_CURRENCY.insert("'" + uuid.toString() + "', '" + amount + "'");
									MessageHandler.sendMessage(sender, "Gave " + target + " &e" + amount + " &2Emeralds");
									MessageHandler.sendMessage(sender, target + " now has &e" + amount + " &2Emeralds");
								}
							}
						} else {
							addEmeralds(player, amount, EmeraldReason.EMERALDS_GIVEN, false);
							MessageHandler.sendMessage(sender, "Gave " + target + " &e" + amount + " &2Emeralds");
							MessageHandler.sendMessage(sender, target + " now has &e" + getEmeralds(player) + " &2Emeralds");
						}
					}
				});
				return true;
			}
		}.enableDelay(1).setRequiredRank(Ranks.OWNER);
		if(ProMcGames.getPlugin() == Plugins.HUB) {
			updateTop8();
		}
		EventUtil.register(this);
	}
	
	public static int getEmeralds(Player player) {
		String name = Disguise.getName(player);
		UUID uuid = Disguise.getUUID(player);
		if(!originalValues.containsKey(name)) {
			originalValues.put(name, DB.PLAYERS_EMERALDS_CURRENCY.getInt("uuid", uuid.toString(), "amount"));
		}
		int amount = originalValues.get(name);
		if(gained.containsKey(name)) {
			amount += gained.get(name);
		}
		return amount;
	}
	
	public static void addEmeralds(Player player, int amount, EmeraldReason reason, boolean multiply) {
		if(multiply) {
			if(Ranks.ELITE.hasRank(player, true)) {
				amount *= 4;
				MessageHandler.sendMessage(player, AccountHandler.getRank(player).getPrefix() + "&a4x Multiplier taking place!");
			} else if(Ranks.PRO_PLUS.hasRank(player, true)) {
				amount *= 3;
				MessageHandler.sendMessage(player, AccountHandler.getRank(player).getPrefix() + "&a3x Multiplier taking place!");
			} else if(Ranks.PRO.hasRank(player, true)) {
				amount *= 2;
				MessageHandler.sendMessage(player, AccountHandler.getRank(player).getPrefix() + "&a2x Multiplier taking place!");
			}
			if(doubleEmeraldsWeekend) {
				amount *= 2;
				MessageHandler.sendMessage(player, "&2Double Emeralds Weekend: &a2x Emeralds");
			}
		}
		int amountGained = amount;
		String movement = amount <= 0 ? "&clost &a" : "&egained &a";
		String currency = amount == 1 ? " Emerald" : " Emeralds";
		MessageHandler.sendMessage(player, "You have " + movement + amount + currency + " for " + WordUtils.capitalize(reason.toString().replace("_", " ")));
		String name = Disguise.getName(player);
		if(gained.containsKey(name)) {
			amountGained += gained.get(name);
		}
		gained.put(name, amountGained);
	}
	
	public static ItemStack getEmeraldIcon(Player player) {
		return new ItemCreator(Material.EMERALD).setName("&aEmeralds: &e" + getEmeralds(player)).getItemStack();
	}
	
	private void updateTop8() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				List<UUID> uuids = new ArrayList<UUID>();
				List<Integer> amounts = new ArrayList<Integer>();
				for(String uuid : DB.PLAYERS_EMERALDS_CURRENCY.getOrdered("amount", "uuid", 8, true)) {
					uuids.add(UUID.fromString(uuid));
					amounts.add(DB.PLAYERS_EMERALDS_CURRENCY.getInt("uuid", uuid, "amount"));
				}
				if(uuids.isEmpty()) {
					return;
				}
				World world = Bukkit.getWorlds().get(0);
				Sign sign = (Sign) world.getBlockAt(-78, 127, -161).getState();
				for(int a = 0; a < 4; ++a) {
					sign.setLine(a, AccountHandler.getName(uuids.get(a)));
				}
				sign.update();
				sign = (Sign) world.getBlockAt(-78, 126, -161).getState();
				for(int a = 4; a < 8; ++a) {
					sign.setLine(a - 4, AccountHandler.getName(uuids.get(a)));
				}
				sign.update();
				sign = (Sign) world.getBlockAt(-78, 127, -160).getState();
				for(int a = 0; a < 4; ++a) {
					sign.setLine(a, ChatColor.stripColor(amounts.get(a) + ""));
				}
				sign.update();
				sign = (Sign) world.getBlockAt(-78, 126, -160).getState();
				for(int a = 4; a < 8; ++a) {
					sign.setLine(a - 4, ChatColor.stripColor(amounts.get(a) + ""));
				}
				sign.update();
				uuids.clear();
				uuids = null;
				amounts.clear();
				amounts = null;
			}
		});
	}
	
	//@EventHandler
	public void onTenSecondTask(TenSecondTaskEvent event) {
		int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		doubleEmeraldsWeekend = day == 6 || day == 7 || day == 1;
	}
	
	@EventHandler
	public void onOneMinuteTask(OneMinuteTaskEvent event) {
		if(ProMcGames.getPlugin() == Plugins.HUB) {
			updateTop8();
		} else {
			OneMinuteTaskEvent.getHandlerList().unregister(this);
		}
	}
	
	@EventHandler
	public void onPlayerDayOfPlaytime(PlayerDayOfPlaytimeEvent event) {
		addEmeralds(event.getPlayer(), 100, EmeraldReason.DAY_OF_PLAYTIME, true);
	}
	
	@EventHandler
	public void onGameStarting(GameStartingEvent event) {
		ProPlugin proPlugin = ProMcGames.getProPlugin();
		if(doubleEmeraldsWeekend && (proPlugin.getWinEmeralds() > 0 && proPlugin.getKillEmeralds() > 0)) {
			MessageHandler.alertLine();
			MessageHandler.alert("&2Double Emeralds Weekend is &eEnabled");
			MessageHandler.alertLine();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		ProPlugin proPlugin = ProMcGames.getProPlugin();
		if(doubleEmeraldsWeekend && (proPlugin.getWinEmeralds() > 0 && proPlugin.getKillEmeralds() > 0)) {
			MessageHandler.sendLine(event.getPlayer());
			MessageHandler.sendMessage(event.getPlayer(), "&2Double Emeralds Weekend is &eEnabled");
			MessageHandler.sendLine(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getRealUUID();
		String name = event.getRealName();
		if(gained.containsKey(name)) {
			if(DB.PLAYERS_EMERALDS_CURRENCY.isUUIDSet(uuid)) {
				int amount = gained.get(name) + DB.PLAYERS_EMERALDS_CURRENCY.getInt("uuid", uuid.toString(), "amount");
				DB.PLAYERS_EMERALDS_CURRENCY.updateInt("amount", amount, "uuid", uuid.toString());
			} else {
				DB.PLAYERS_EMERALDS_CURRENCY.insert("'" + uuid.toString() + "', '" + gained.get(name) + "'");
			}
			gained.remove(name);
			originalValues.remove(name);
		}
	}
}
