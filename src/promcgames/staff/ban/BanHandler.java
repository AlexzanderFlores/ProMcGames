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
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
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
	public enum Violations {HACKING, XRAY, BLACK_LISTED_MODS, COMBAT_MACROS, CHARGING_BACK, EXPLOITING_BUGS, FAKE_PROOF}
	
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
						// Perform any final execution instructions
						MessageHandler.alert(message);
						Bukkit.getPluginManager().callEvent(new PlayerBanEvent(uuid, sender));
						// Ban other accounts attached to the IP
						int counter = 0;
						for(String uuidString : DB.PLAYERS_ACCOUNTS.getAllStrings("uuid", "address", AccountHandler.getAddress(uuid))) {
							if(!uuidString.equals(uuid.toString())) {
								Player player = Bukkit.getPlayer(UUID.fromString(uuidString));
								if(player != null) {
									player.kickPlayer(ChatColor.RED + "You have been banned due to sharing the IP of " + arguments[0]);
								}
								DB.STAFF_BAN.insert("'" + uuidString + "', '" + uuid.toString() + "', '" + staffUUID + "', 'null', '" + reason + "', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
								keys = new String [] {"uuid", "active"};
								values = new String [] {uuidString, "1"};
								id = DB.STAFF_BAN.getInt(keys, values, "id");
								DB.STAFF_BAN_PROOF.insert("'" + id + "', '" + proof + "'");
								++counter;
							}
						}
						if(counter > 0) {
							MessageHandler.alert("&cBanning &e" + counter + " &caccount" + (counter == 1 ? "" : "s") + " that shared the same IP as &e" + arguments[0]);
						}
						// Execute the ban if the player is online
						Player player = ProPlugin.getPlayer(arguments[0]);
						if(player != null) {
							ProPlugin.sendPlayerToServer(player, "slave");
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
		// Command syntax: /addProof <player name> <proof>
		new CommandBase("addProof", 2) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				UUID uuid = AccountHandler.getUUID(name);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
				} else {
					String [] keys = new String [] {"uuid", "active"};
					String [] values = new String [] {uuid.toString(), "1"};
					if(DB.STAFF_BAN.isKeySet(keys, values)) {
						int id = DB.STAFF_BAN.getInt(keys, values, "id");
						DB.STAFF_BAN_PROOF.insert("'" + id + "', '" + arguments[1] + "'");
						MessageHandler.sendMessage(sender, "Added &e" + arguments[1] + " &ato proof for the ban of " + name);
					} else {
						MessageHandler.sendMessage(sender, "&c" + name + " is not banned");
					}
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
					final String message = getReason(AccountHandler.getRank(sender), arguments, "HACKING", result);
					String time = TimeUtil.getTime();
					String date = time.substring(0, 7);
					int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
					DB.STAFF_BAN.insert("'" + uuid.toString() + "', 'null', '" + staffUUID + "', 'null', 'HACKING', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
					// Perform any final execution instructions
					MessageHandler.alert(message);
					MessageHandler.sendMessage(sender, "&c&lYOU MUST UPLOAD PROOF AND REBAN");
					Bukkit.getPluginManager().callEvent(new PlayerBanEvent(uuid, sender));
					// Ban other accounts attached to the IP
					int counter = 0;
					for(String uuidString : DB.PLAYERS_ACCOUNTS.getAllStrings("uuid", "address", AccountHandler.getAddress(uuid))) {
						if(!uuidString.equals(uuid.toString())) {
							Player player = Bukkit.getPlayer(UUID.fromString(uuidString));
							if(player != null) {
								player.kickPlayer(ChatColor.RED + "You have been banned due to sharing the IP of " + arguments[0]);
							}
							DB.STAFF_BAN.insert("'" + uuidString + "', '" + uuid.toString() + "', '" + staffUUID + "', 'null', 'HACKING', '" + date + "', '" + time + "', 'null', 'null', '" + day + "', '1'");
							++counter;
						}
					}
					if(counter > 0) {
						MessageHandler.alert("&cBanning &e" + counter + " &caccount" + (counter == 1 ? "" : "s") + " that shared the same IP as &e" + arguments[0]);
					}
					// Execute the ban if the player is online
					Player player = ProPlugin.getPlayer(arguments[0]);
					if(player != null) {
						ProPlugin.sendPlayerToServer(player, "slave");
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
		new CommandBase("banTypes") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "");
				MessageHandler.sendMessage(sender, "&c&lDIRECT &a&lBans:");
				MessageHandler.sendMessage(sender, "A direct ban is when the account banned was the account found builty of the violation.");
				MessageHandler.sendMessage(sender, "");
				MessageHandler.sendMessage(sender, "&c&lASSOCIATION &a&lBans:");
				MessageHandler.sendMessage(sender, "An association ban is placed on all accounts that share an IP address with a &c&lDIRECTLY &abanned account. You must have &cVALID &areason to be unbanned and good proof to back up your reasoning. The likeyhood of one of these bans being lifted, without solid proof that it should NOT have happened, is very low.");
				MessageHandler.sendMessage(sender, "");
				return true;
			}
		};
		new CommandBase("appealInfo") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "");
				MessageHandler.sendMessage(sender, "&b&lX-Ray Bans (1st offence) &eAfter 30 days you may appeal and get unbanned even if your ban was valid");
				MessageHandler.sendMessage(sender, "");
				MessageHandler.sendMessage(sender, "&b&lAnti Cheat Bans via Console &eAfter 30 days you may appeal and get unbanned even if your ban was valid. Note that you &cMUST &eprovide as much detail as possible about what happened leading up to your ban. This is to help us improve our anti cheat.");
				MessageHandler.sendMessage(sender, "");
				MessageHandler.sendMessage(sender, "&b&lOther &eAny appeals for other ban types will &cONLY &eresult in an unban if the proof is invalid");
				MessageHandler.sendMessage(sender, "");
				return true;
			}
		};
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
								MessageHandler.sendMessage(viewer, "Associated with? &e" + AccountHandler.getName(UUID.fromString(attached_uuid)));
							}
							ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick to explain", "Click to explain", "/appealInfo", "&aSome ban types are only temporary bans");
							ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick here", "Click to appeal", "/appeal", "&aTo appeal your ban");
							String staff = resultSet.getString("staff_uuid");
							if(staff.equals("CONSOLE") || reason.equals("XRAY")) {
								int day = resultSet.getInt("day") - Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
								if(day <= 0) {
									ChatClickHandler.sendMessageToRunCommand(viewer, " &bClick here", "Click to appeal", "/appeal", "&aThis player &eMAY &aappeal at this time");
								} else {
									MessageHandler.sendMessage(viewer, "This player &cMAY NOT &aappeal at this time");
									MessageHandler.sendMessage(viewer, "They must wait &e" + day + " &amore day" + (day == 1 ? "" : "s"));
								}
							}
							if(!staff.equals("CONSOLE")) {
								staff = AccountHandler.getName(UUID.fromString(staff));
							}
							if(Ranks.SENIOR_MODERATOR.hasRank(viewer)) {
								MessageHandler.sendMessage(viewer, "&c&lThis is ONLY shown to Sr. Mods and above");
								MessageHandler.sendMessage(viewer, "Who banned? &e" + staff);
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
