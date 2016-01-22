package promcgames.staff.ban;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.ProPlugin;
import promcgames.customevents.player.AsyncPostPlayerJoinEvent;
import promcgames.customevents.player.PlayerBanEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.TimeUtil;
import promcgames.staff.Punishment;

public class BanHandler extends Punishment {
	private static List<String> checkedForBanned = null;
	private static List<String> hasBeenBanned = null;
	
	public enum Violations {
		HACKING,
		XRAY,
		BLACK_LISTED_MODS,
		COMBAT_MACROS,
		CHARGING_BACK,
		EXPLOITING_BUGS
	}
	
	public BanHandler() {
		super("Banned");
		checkedForBanned = new ArrayList<String>();
		hasBeenBanned = new ArrayList<String>();
		// Command syntax: /ban <player name> <reason> <proof>
		new CommandBase("ban", 2, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				// See if they are not attaching proof to the command, if they aren't and they aren't an owner don't run the command
				if(arguments.length == 2 && AccountHandler.getRank(sender) != Ranks.OWNER) {
					return false;
				}
				// Use a try/catch to view if the given reason is valid
				try {
					Violations reason = Violations.valueOf(arguments[1].toUpperCase());
					if(reason == Violations.HACKING) {
						// Be sure that the link is a youtube URL, if so remove the &featured part after (not needed)
						if(arguments.length == 3 && !arguments[2].toLowerCase().contains("youtube.com/") && !arguments[2].toLowerCase().contains("youtu.be/")) {
							MessageHandler.sendMessage(sender, "&cYour proof must be a youtube URL");
							return true;
						}
						if(arguments.length == 3 && arguments[2].contains("&")) {
							arguments[2] = arguments[2].split("&")[0];
						}
					}
					// Detect if the command should be activated
					PunishmentExecuteReuslts result = executePunishment(sender, arguments, false);
					if(result.isValid()) {
						UUID uuid = result.getUUID();
						// See if the player is already banned
						String [] keys = new String [] {"uuid", "active"};
						String [] values = new String [] {uuid.toString(), "1"};
						if(DB.STAFF_BAN.isKeySet(keys, values)) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is already " + getName());
							return true;
						}
						// Get the staff data
						String staff = "CONSOLE";
						String staffUUID = staff;
						if(sender instanceof Player) {
							Player player = (Player) sender;
							staff = Disguise.getName(player);
							staffUUID = Disguise.getUUID(player).toString();
						}
						// Compile the message and proof strings
						final String message = getReason(AccountHandler.getRank(sender), arguments, reason.toString(), result);
						String time = TimeUtil.getTime();
						String date = time.substring(0, 7);
						int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
						DB.STAFF_BAN.insert("'" + uuid.toString() + "', 'null', '" + staffUUID + "', 'null', '" + reason + "', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
						int id = DB.STAFF_BAN.getInt(keys, values, "id");
						String proof = (arguments.length == 2 ? "none" : arguments[2]);
						DB.STAFF_BAN_PROOF.insert("'" + id + "', '" + proof + "'");
						//DB.STAFF_BAN.insert("'" + uuid.toString() + "', '" + staffUUID + "', '" + address + "', '" + reason.toString() + "', '" + proof + "', '" + time.substring(0, 7) + "', '" + time + "'");
						// Perform any final execution instructions
						MessageHandler.alert(message);
						Bukkit.getPluginManager().callEvent(new PlayerBanEvent(uuid, sender));
						// Execute the ban if the player is online
						Player player = ProPlugin.getPlayer(arguments[0]);
						if(player != null) {
							//ProPlugin.sendPlayerToServer(player, "slave");
							final String name = player.getName();
							new DelayedTask(new Runnable() {
								@Override
								public void run() {
									Player player = ProPlugin.getPlayer(name);
									if(player != null && player.isOnline()) {
										player.kickPlayer(message);
									}
								}
							}, 15);
						}
						// If they are banned for charging back then ban any account(s) that have had a rank transfered
						if(reason == Violations.CHARGING_BACK && DB.PLAYERS_RANK_TRANSFERS.isUUIDSet(result.getUUID())) {
							UUID target = UUID.fromString(DB.PLAYERS_RANK_TRANSFERS.getString("uuid", result.getUUID().toString(), "target"));
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + AccountHandler.getName(target) + " CHARGING_BACK");
						}
					}
				} catch(IllegalArgumentException e) {
					// Display all the valid options
					MessageHandler.sendMessage(sender, "&c\"" + arguments[1] + "\" is an unknown violation, use one of the following:");
					String reasons = "";
					for(Violations reason : Violations.values()) {
						reasons += "&a" + reason + "&e, ";
					}
					MessageHandler.sendMessage(sender, reasons.substring(0, reasons.length() - 2));
				}
				return true;
			}
		}.setRequiredRank(Ranks.MODERATOR);
		// Command syntax: /SrBan <player name>
		new CommandBase("SrBan", 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				// Detect if the command should be activated
				PunishmentExecuteReuslts result = executePunishment(sender, arguments, false);
				if(result.isValid()) {
					UUID uuid = result.getUUID();
					// See if the player is already banned
					String [] keys = new String [] {"uuid", "active"};
					String [] values = new String [] {uuid.toString(), "1"};
					if(DB.STAFF_BAN.isKeySet(keys, values)) {
						MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is already " + getName());
						return true;
					}
					// Get the staff data
					String staff = "CONSOLE";
					String staffUUID = staff;
					if(sender instanceof Player) {
						Player player = (Player) sender;
						staff = Disguise.getName(player);
						staffUUID = Disguise.getUUID(player).toString();
					}
					// Compile the message and proof strings
					String message = getReason(AccountHandler.getRank(sender), arguments, "HACKING", result);
					String time = TimeUtil.getTime();
					String date = time.substring(0, 7);
					int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
					DB.STAFF_BAN.insert("'" + uuid.toString() + "', 'null', '" + staffUUID + "', 'null', 'HACKING', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
					//DB.STAFF_BANS.insert("'" + uuid.toString() + "', '" + staffUUID + "', '" + address + "', 'HACKING', 'Proof being uploaded soon', '" + time.substring(0, 7) + "', '" + time + "'");
					// Perform any final execution instructions
					MessageHandler.alert(message);
					MessageHandler.sendMessage(sender, "&c&lYOU MUST UPLOAD PROOF AND REBAN");
					Bukkit.getPluginManager().callEvent(new PlayerBanEvent(uuid, sender));
					// Execute the ban if the player is online
					Player player = ProPlugin.getPlayer(arguments[0]);
					if(player != null) {
						player.kickPlayer(message);
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.SENIOR_MODERATOR);
		new CommandBase("isBanned", 1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						UUID uuid = AccountHandler.getUUID(arguments[0]);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
						} else {
							String [] keys = new String [] {"uuid", "active"};
							String [] values = new String [] {uuid.toString(), "1"};
							if(DB.STAFF_BAN.isKeySet(keys, values)) {
								Player player = (Player) sender;
								display(uuid, player);
							} else {
								MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not banned");
							}
						}
					}
				});
				return true;
			}
		}.enableDelay(2);
		new CommandBase("whoUnbanned", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String name = arguments[0];
						UUID uuid = AccountHandler.getUUID(name);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
						} else {
							String [] keys = new String [] {"uuid", "active"};
							String [] values = new String [] {uuid.toString(), "0"};
							if(DB.STAFF_BAN.isKeySet(keys, values)) {
								int counter = 0;
								for(String id : DB.STAFF_BAN.getAllStrings("id", "uuid", uuid.toString())) {
									String time = DB.STAFF_BAN.getString("id", id, "unban_time");
									String staffUUID = DB.STAFF_BAN.getString("id", id, "staff_uuid");
									String staffName = staffUUID;
									if(!staffUUID.equals("CONSOLE")) {
										staffName = AccountHandler.getName(UUID.fromString(staffUUID));
									}
									MessageHandler.sendMessage(sender, "&c#" + ++counter + " &e" + name + " was unbanned at " + time + " by " + staffName);
								}
							} else {
								MessageHandler.sendMessage(sender, "&c" + name + " has never been banned before");
							}
						}
					}
				});
				return true;
			}
		}.enableDelay(2).setRequiredRank(Ranks.MODERATOR);
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(ProMcGames.getPlugin() != Plugins.HUB && ProMcGames.getPlugin() != Plugins.SLAVE && checkForBanned(event.getPlayer())) {
			event.setKickMessage(ChatColor.RED + "Failed to connect: You are banned");
			event.setResult(Result.KICK_OTHER);
		}
	}
	
	@EventHandler
	public void onAsyncPostPlayerJoin(AsyncPostPlayerJoinEvent event) {
		if(ProMcGames.getPlugin() == Plugins.HUB) {
			Player player = event.getPlayer();
			if(checkForBanned(player)) {
				player.getInventory().clear();
				player.closeInventory();
				player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100));
				player.teleport(new Location(player.getWorld(), -123.5, 126, -123.5));
			}
		} else {
			AsyncPostPlayerJoinEvent.getHandlerList().unregister(this);
		}
	}
	
	//@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(ProMcGames.getPlugin() == Plugins.HUB) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(checkForBanned(player)) {
					player.teleport(new Location(player.getWorld(), -123.5, 126, -123.5));
				}
			}
		} else {
			AsyncPostPlayerJoinEvent.getHandlerList().unregister(this);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		checkedForBanned.remove(event.getPlayer().getName());
		hasBeenBanned.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if(ProMcGames.getPlugin() == Plugins.HUB && checkForBanned(event.getPlayer()) && !event.getMessage().toLowerCase().contains("verify")) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(ProMcGames.getPlugin() == Plugins.HUB && checkForBanned(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	public static boolean checkForBanned(Player player) {
		if(Ranks.isStaff(player)) {
			return false;
		}
		if(!checkedForBanned.contains(Disguise.getName(player))) {
			String [] keys = new String [] {"uuid", "active"};
			String [] values = new String [] {Disguise.getUUID(player).toString(), "1"};
			boolean uuidBanned = DB.STAFF_BAN.isKeySet(keys, values);
			if(ProMcGames.getPlugin() != Plugins.HUB && uuidBanned) {
				return true;
			}
			checkedForBanned.add(Disguise.getName(player));
			if(uuidBanned) {
				hasBeenBanned.add(Disguise.getName(player));
				display(Disguise.getUUID(player), player);
			}
		}
		return hasBeenBanned.contains(Disguise.getName(player));
	}
	
	private static void display(final UUID uuid, final Player viewer) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				ResultSet resultSet = null;
				PreparedStatement statement = null;
				try {
					DB table = DB.STAFF_BAN;
					statement = table.getConnection().prepareStatement("SELECT id, reason, time, staff_uuid, attached_uuid, day FROM " + table.getName() + " WHERE uuid = '" + uuid.toString() + "' AND active = 1");
					resultSet = statement.executeQuery();
					if(!resultSet.wasNull()) {
						MessageHandler.sendLine(viewer);
						MessageHandler.sendMessage(viewer, "&a&lThis account has been &c&lBANNED!");
						while(resultSet.next()) {
							String id = resultSet.getString("id");
							String reason = resultSet.getString("reason").replace("_", " ");
							if(reason != null && !reason.equals("null")) {
								MessageHandler.sendMessage(viewer, "Why? &e" + reason);
							}
							int counter = 0;
							for(String proof : DB.STAFF_BAN_PROOF.getAllStrings("proof", "ban_id", id)) {
								MessageHandler.sendMessage(viewer, "Proof #" + (++counter) + " &e" + proof);
							}
							String time = resultSet.getString("time");
							if(time != null && !time.equals("null")) {
								MessageHandler.sendMessage(viewer, "When? &e" + time);
							}
							String attached_uuid = resultSet.getString("attached_uuid");
							if(attached_uuid.equalsIgnoreCase("null")) {
								ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick to explain", "Click to explain", "/banTypes", "&aType of ban? &eDIRECT");
							} else {
								ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick to explain", "Click to explain", "/banTypes", "&aType of ban? &eASSOCIATION");
							}
							MessageHandler.sendMessage(viewer, "");
							ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick to explain", "Click to explain", "/appealInfo", "&aSome ban types are only temporary bans");
							MessageHandler.sendMessage(viewer, "");
							ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick here", "Click to appeal", "/appeal", "&aTo appeal your ban");
							MessageHandler.sendMessage(viewer, "");
							if(Ranks.SENIOR_MODERATOR.hasRank(viewer)) {
								String staff = resultSet.getString("staff_uuid");
								if(!staff.equals("CONSOLE")) {
									staff = AccountHandler.getName(UUID.fromString(staff));
								}
								MessageHandler.sendMessage(viewer, "&c&lThis is ONLY shown to Sr. Mods and above");
								MessageHandler.sendMessage(viewer, "Banned by: " + staff);
							}
						}
						if(Disguise.getUUID(viewer) == uuid) {
							MessageHandler.sendMessage(viewer, "Is this an unfair punishment? Appeal here: &bhttps://promcgames.com/forum/view_forum/?fid=12");
						}
						MessageHandler.sendLine(viewer);
					}
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					DB.close(statement, resultSet);
				}
			}
		});
	}
}
