package promcgames.staff;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.ProPlugin;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.account.PlaytimeTracker;
import promcgames.player.account.PlaytimeTracker.TimeType;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.TimeUtil;

public class DataChecker implements Listener {
	public DataChecker() {
		new CommandBase("monthlyStaffData") {
			@Override
			public boolean execute(final CommandSender sender, String [] arguments) {
				if(ProMcGames.getPlugin() == Plugins.HUB) {
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							MessageHandler.sendMessage(sender, "Rank - Name - Tickets - Bans - Playtime");
							String date = TimeUtil.getTime().substring(0, 7);
							for(Ranks rank : new Ranks [] {Ranks.HELPER, Ranks.MODERATOR, Ranks.SENIOR_MODERATOR, Ranks.DEV, Ranks.ADMIN, Ranks.OWNER}) {
								for(String uuidString : DB.PLAYERS_ACCOUNTS.getAllStrings("uuid", "rank", rank.toString())) {
									UUID uuid = UUID.fromString(uuidString);
									String name = AccountHandler.getName(uuid);
									String [] values = new String [] {uuidString, date};
									int tickets = DB.STAFF_TICKETS_CLOSED.getInt(new String [] {"uuid", "date_closed"}, values, "amount");
									int bans = DB.STAFF_BAN.getSize(new String [] {"staff_uuid", "date"}, values);
									String lifetimePlaytime = DB.PLAYERS_PLAY_TIME.getString("uuid", uuidString, "play_time");
									String [] split = lifetimePlaytime.split("/");
									lifetimePlaytime = split[0] + "w " + split[1] + "d " + split[2] + "h " + split[3] + "m " + split[4] + "s";
									String [] keys = new String [] {"uuid", "date"};
									String monthlyPlaytime = DB.PLAYERS_MONTHLY_PLAY_TIME.getString(keys, values, "play_time");
									monthlyPlaytime = split[0] + "w " + split[1] + "d " + split[2] + "h " + split[3] + "m " + split[4] + "s";
									MessageHandler.sendMessage(sender, rank.getPrefix() + " &f- " + name + " - " + tickets + " - " + bans + " - " + monthlyPlaytime);
								}
							}
							MessageHandler.sendMessage(sender, "Rank - Name - Tickets - Bans - Playtime");
						}
					});
				} else {
					MessageHandler.sendMessage(sender, "&cYou can only run this command on a hub server");
				}
				return true;
			}
		}.setRequiredRank(Ranks.SENIOR_MODERATOR);
		
		new CommandBase("staffData", 0, 1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				if(ProMcGames.getPlugin() == Plugins.HUB) {
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							Player player = (Player) sender;
							String uuid = "CONSOLE";
							if(arguments.length == 0) {
								uuid = Disguise.getUUID(player).toString();
							} else if(arguments.length == 1 && !arguments[0].equalsIgnoreCase("CONSOLE")) {
								Player target = ProPlugin.getPlayer(arguments[0]);
								if(target == null) {
									UUID targetUUID = AccountHandler.getUUID(arguments[0]);
									if(targetUUID == null) {
										MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in");
										return;
									} else {
										uuid = targetUUID.toString();
									}
								} else {
									uuid = Disguise.getUUID(target).toString();
								}
							}
							Player target = null;
							if(!uuid.equals("CONSOLE")) {
								target = Bukkit.getPlayer(UUID.fromString(uuid));
							}
							Ranks rank = Ranks.HELPER;
							if(target == null) {
								if(uuid.equals("CONSOLE")) {
									rank = Ranks.OWNER;
								} else {
									rank = AccountHandler.getRank(UUID.fromString(uuid));
								}
							} else {
								rank = AccountHandler.getRank(target);
							}
							
							String lifetimePlaytime = null;
							String monthlyPlaytime = null;
							if(!uuid.equals("CONSOLE")) {
								if(target == null) {
									if(DB.PLAYERS_PLAY_TIME.isUUIDSet(UUID.fromString(uuid))) {
										String [] playTime = DB.PLAYERS_PLAY_TIME.getString("uuid", uuid, "play_time").split("-");
										lifetimePlaytime = playTime[0];
										String [] split = lifetimePlaytime.split("/");
										lifetimePlaytime = split[0] + "w " + split[1] + "d " + split[2] + "h " + split[3] + "m " + split[4] + "s";
									} else {
										lifetimePlaytime = "No lifetime play time found";
									}
									if(DB.PLAYERS_MONTHLY_PLAY_TIME.isUUIDSet(UUID.fromString(uuid))) {
										String [] keys = new String [] {"uuid", "date"};
										String [] values = new String [] {uuid, TimeUtil.getTime().substring(0, 7)};
										String [] playTime = DB.PLAYERS_MONTHLY_PLAY_TIME.getString(keys, values, "play_time").split("-");
										monthlyPlaytime = playTime[0];
										String [] split = monthlyPlaytime.split("/");
										monthlyPlaytime = split[0] + "w " + split[1] + "d " + split[2] + "h " + split[3] + "m " + split[4] + "s";
									} else {
										monthlyPlaytime = "No lifetime play time found";
									}
								} else {
									lifetimePlaytime = PlaytimeTracker.getPlayTime(target).getDisplay(TimeType.LIFETIME);
									monthlyPlaytime = PlaytimeTracker.getPlayTime(target).getDisplay(TimeType.MONTHLY);
								}
							}
							Inventory inventory = Bukkit.createInventory(player, 9, "Staff Data - " + (arguments.length == 0 ? sender.getName() : arguments[0]));
							inventory.addItem(new ItemCreator(Material.DIAMOND).setName("&aCurrent Rank: " + rank.getPrefix()).getItemStack());
							inventory.addItem(new ItemCreator(Material.DIAMOND_BOOTS).setName("&aKick Data").setLores(new String [] {"&6Click to view data"}).getItemStack());
							inventory.addItem(new ItemCreator(Material.PUMPKIN).setName("&aMute Data").setLores(new String [] {"&6Click to view data"}).getItemStack());
							inventory.addItem(new ItemCreator(Material.SKULL_ITEM).setName("&aBan Data").setLores(new String [] {"&6Click to view data"}).getItemStack());
							if(!uuid.equals("CONSOLE")) {
								inventory.addItem(new ItemCreator(Material.NAME_TAG).setName("&aTicket Data").setLores(new String [] {"&6Click to view data"}).getItemStack());
								inventory.addItem(new ItemCreator(Material.WATCH).setName("&aLifetime playtime:").setLores(new String [] {"&6" + lifetimePlaytime}).getItemStack());
								inventory.addItem(new ItemCreator(Material.WATCH).setName("&aMonthly playtime:").setLores(new String [] {"&6" + monthlyPlaytime, "&6For &e" + TimeUtil.getTime().substring(0, 7)}).getItemStack());
							}
							player.openInventory(inventory);
						}
					});
				} else {
					MessageHandler.sendMessage(sender, "&cYou can only use this command on a hub server");
				}
				return true;
			}
		}.enableDelay(2);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		String clicked = event.getItem().getItemMeta().getDisplayName();
		if(clicked != null) {
			clicked = ChatColor.stripColor(clicked);
		}
		if(event.getTitle().startsWith("Staff Data - ")) {
			if(event.getItem().getType() == Material.WATCH || event.getItem().getType() == Material.DIAMOND) {
				event.setCancelled(true);
				event.getPlayer().closeInventory();
				return;
			}
			String name = event.getTitle().replace("Staff Data - ", "");
			String uuid = "CONSOLE";
			if(!name.equals("CONSOLE")) {
				Player player = ProPlugin.getPlayer(name);
				if(player == null) {
					uuid = AccountHandler.getUUID(name).toString();
				} else {
					uuid = Disguise.getUUID(player).toString();
				}
			}
			String date = TimeUtil.getTime().substring(0, 7);
			String [] lores = new String [] {"&6For &e" + date};
			Player player = event.getPlayer();
			if(event.getItem().getType() == Material.DIAMOND_BOOTS) {
				if(Ranks.DEV.hasRank(player)) {
					Inventory inventorty = Bukkit.createInventory(event.getPlayer(), 9 * 1, "Kick Data - " + name);
					inventorty.addItem(new ItemCreator(Material.DIAMOND_BOOTS).setName("&aLifetime Kicks: " + DB.STAFF_KICKS.getSize("staff_uuid", uuid)).getItemStack());
					inventorty.addItem(new ItemCreator(Material.DIAMOND_BOOTS).setName("&6Monthly Kicks: " + DB.STAFF_KICKS.getSize(new String [] {"staff_uuid", "date"}, new String [] {uuid, date})).setLores(lores).getItemStack());
					event.getPlayer().openInventory(inventorty);
				} else {
					MessageHandler.sendMessage(player, "To check this data you must have " + Ranks.DEV.getPrefix());
				}
			} else if(event.getItem().getType() == Material.PUMPKIN) {
				if(Ranks.DEV.hasRank(player)) {
					Inventory inventorty = Bukkit.createInventory(event.getPlayer(), 9 * 1, "Mute Data - " + name);
					inventorty.addItem(new ItemCreator(Material.PUMPKIN).setName("&aLifetime Mutes: " + DB.STAFF_MUTES.getSize("staff_uuid", uuid)).getItemStack());
					inventorty.addItem(new ItemCreator(Material.PUMPKIN).setName("&aMonthly Mutes: " + DB.STAFF_MUTES.getSize(new String [] {"staff_uuid", "date"}, new String [] {uuid, date})).setLores(lores).getItemStack());
					inventorty.addItem(new ItemCreator(Material.PUMPKIN).setName("&aLifetime Unmutes: " + DB.STAFF_UNMUTES.getSize("staff_uuid", uuid)).getItemStack());
					inventorty.addItem(new ItemCreator(Material.PUMPKIN).setName("&aMonthly Unmutes: " + DB.STAFF_UNMUTES.getSize(new String [] {"staff_uuid", "date"}, new String [] {uuid, date})).setLores(lores).getItemStack());
					event.getPlayer().openInventory(inventorty);
				} else {
					MessageHandler.sendMessage(player, "To check this data you must have " + Ranks.DEV.getPrefix());
				}
			} else if(event.getItem().getType() == Material.SKULL_ITEM) {
				Inventory inventorty = Bukkit.createInventory(event.getPlayer(), 9 * 1, "Ban Data - " + name);
				inventorty.addItem(new ItemCreator(Material.SKULL_ITEM).setName("&aLifetime Bans: " + DB.STAFF_BAN.getSize(new String [] {"staff_uuid", "active"}, new String [] {uuid, "1"})).getItemStack());
				inventorty.addItem(new ItemCreator(Material.SKULL_ITEM).setName("&aMonthly Bans: " + DB.STAFF_BAN.getSize(new String [] {"staff_uuid", "active", "date"}, new String [] {uuid, "1", date})).setLores(lores).getItemStack());
				inventorty.addItem(new ItemCreator(Material.SKULL_ITEM).setName("&aLifetime Unbans: " + DB.STAFF_BAN.getSize(new String [] {"staff_uuid", "active"}, new String [] {uuid, "1"})).getItemStack());
				inventorty.addItem(new ItemCreator(Material.SKULL_ITEM).setName("&aMonthly Unbans: " + DB.STAFF_BAN.getSize(new String [] {"staff_uuid", "active", "date"}, new String [] {uuid, "1", date})).setLores(lores).getItemStack());
				event.getPlayer().openInventory(inventorty);
			} else if(event.getItem().getType() == Material.NAME_TAG) {
				Inventory inventorty = Bukkit.createInventory(event.getPlayer(), 9 * 1, "Ticket Data - " + name);
				inventorty.addItem(new ItemCreator(Material.NAME_TAG).setName("&aMonthly Tickets Closed: " + DB.STAFF_TICKETS_CLOSED.getInt(new String [] {"uuid", "date_closed"}, new String [] {uuid, date}, "amount")).setLores(lores).getItemStack());
				event.getPlayer().openInventory(inventorty);
			}
			event.setCancelled(true);
		} else if(event.getTitle().startsWith("Kick Data - ")) {
			event.setCancelled(true);
			event.getPlayer().closeInventory();
		} else if(event.getTitle().startsWith("Mute Data - ")) {
			event.setCancelled(true);
			event.getPlayer().closeInventory();
		} else if(event.getTitle().startsWith("Ban Data - ")) {
			event.setCancelled(true);
			event.getPlayer().closeInventory();
		} else if(event.getTitle().startsWith("Ticket Data - ")) {
			event.setCancelled(true);
			event.getPlayer().closeInventory();
		}
	}
}
