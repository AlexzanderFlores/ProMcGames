package promcgames.staff.mute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.TimeUtil;
import promcgames.staff.Punishment;

public class MuteHandler extends Punishment {
	private Map<ChatViolations, String> muteLengths = null;
	private static Map<String, MuteData> muteData = null;
	private static List<String> checkedForMuted = null;
	
	public static class MuteData {
		private int id = 0;
		private String player = null;
		private String time = null;
		private String expires = null;
		private String staffName = null;
		private String reason = null;
		private String proof = null;
		
		public MuteData(Player player) {
			this.player = Disguise.getName(player);
			PreparedStatement statement = null;
			ResultSet resultSet = null;
			DB table = DB.STAFF_MUTES;
			try {
				statement = table.getConnection().prepareStatement("SELECT * FROM " + table.getName() + " WHERE uuid = '" + Disguise.getUUID(player).toString() + "'");
				resultSet = statement.executeQuery();
				while(resultSet.next()) {
					id = resultSet.getInt("id") + 1;
					this.time = resultSet.getString("time");
					this.expires = resultSet.getString("expires");
					if(!hasExpired(player)) {
						String uuid = resultSet.getString("staff_uuid");
						if(uuid.equals("CONSOLE")) {
							this.staffName = uuid;
						} else {
							this.staffName = AccountHandler.getName(UUID.fromString(uuid));
						}
						this.reason = resultSet.getString("reason");
						this.proof = resultSet.getString("proof");
					}
				}
				if(muteData == null) {
					muteData = new HashMap<String, MuteData>();
				}
				muteData.put(this.player, this);
			} catch(SQLException e) {
				e.printStackTrace();
			} finally {
				DB.close(statement, resultSet);
			}
		}
		
		public MuteData(Player player, String time, String expires, String staff, String reason, String proof, int id) {
			this.id = id;
			this.player = Disguise.getName(player);
			this.time = time;
			this.expires = expires;
			this.staffName = staff;
			this.reason = reason;
			this.proof = proof;
			if(muteData == null) {
				muteData = new HashMap<String, MuteData>();
			}
			muteData.put(this.player, this);
		}
		
		public void display(Player player) {
			if(this.player.equals(Disguise.getName(player))) {
				MessageHandler.sendLine(player);
				MessageHandler.sendMessage(player, (this.player.equals(Disguise.getName(player)) ? "You have" : this.player + " has") + " been muted: (ID #" + id + ")");
				MessageHandler.sendMessage(player, "Muted by: " + staffName);
				MessageHandler.sendMessage(player, "Muted for: " + reason.replace("_", " ") + " " + proof.replace("_", " "));
				MessageHandler.sendMessage(player, "Muted at: " + time);
				MessageHandler.sendMessage(player, "Expires on: " + expires);
				MessageHandler.sendMessage(player, "Appeal your mute: " + appeal);
				if(reason.equals(Punishment.ChatViolations.SERVER_ADVERTISEMENT.toString())) {
					MessageHandler.sendMessage(player, "&bThis type of mute can be undone through an unmute pass:");
					MessageHandler.sendMessage(player, "&6http://store.promcgames.com/category/359455");
				}
				MessageHandler.sendLine(player);
			}
		}
		
		public boolean hasExpired(Player player) {
			if(!expires.equals("NEVER")) {
				long timeCheck = Long.valueOf(TimeUtil.getTime().split(" ")[0].replace("/", "").replace(":", ""));
				long expiresCheck = Long.valueOf(expires.split(" ")[0].replace("/", "").replace(":", ""));
				if(expiresCheck <= timeCheck) {
					unMute(player, true);
					return true;
				}
			}
			return false;
		}
	}
	
	public MuteHandler() {
		super("MUTED");
		// Command syntax: /mute <player name> <reason> <proof> <staff name>
		new CommandBase("mute", 3, 4, false) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					public void run() {
						// Use a try/catch to view if the given reason is valid
						try {
							ChatViolations reason = ChatViolations.valueOf(arguments[1].toUpperCase());
							// Detect if the command should be activated
							PunishmentExecuteReuslts result = executePunishment(sender, arguments, false);
							if(result.isValid()) {
								// See if the player is already muted
								if(DB.STAFF_MUTES.isUUIDSet(result.getUUID())) {
									MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is already " + getName());
									return;
								}
								// Get the staff data
								String staff = arguments[3];
								String staffUUID = staff;
								Ranks rank = Ranks.OWNER;
								if(!staff.equals("CONSOLE")) {
									rank = AccountHandler.getRank(AccountHandler.getUUID(staff));
									staffUUID = AccountHandler.getUUID(staff).toString();
								}
								// Compile the message and proof strings
								String message = getReason( rank, arguments, reason.toString(), result);
								// Update the database
								UUID uuid = result.getUUID();
								String time = TimeUtil.getTime();
								// Set times for temporary mutes, note that being muted twice for any reason(s) will result in a lifetime mute
								// Key: DAYS/HOURS
								if(muteLengths == null) {
									muteLengths = new HashMap<ChatViolations, String>();
									muteLengths.put(ChatViolations.DISRESPECT, "0/3");
									muteLengths.put(ChatViolations.RACISM, "0/5");
									muteLengths.put(ChatViolations.DEATH_COMMENTS, "5/0");
									muteLengths.put(ChatViolations.INAPPROPRIATE_COMMENTS, "0/1");
									muteLengths.put(ChatViolations.SPAMMING, "0/1");
									muteLengths.put(ChatViolations.SOCIAL_MEDIA_ADVERTISEMENT, "0/1");
									muteLengths.put(ChatViolations.WEBSITE_ADVERTISEMENT, "0/1");
									muteLengths.put(ChatViolations.SERVER_ADVERTISEMENT, "1/0");
									muteLengths.put(ChatViolations.HACKUSATIONS, "0/1");
									muteLengths.put(ChatViolations.DDOS_THREATS, "3/0");
								}
								String expires = "NEVER";
								if(!muteLengths.get(reason).equals("NEVER")) {
									int days = Integer.valueOf(muteLengths.get(reason).split("/")[0]);
									int hours = Integer.valueOf(muteLengths.get(reason).split("/")[1]);
									int previousMutes = DB.STAFF_UNMUTES.getSize(new String [] {"uuid", "reason"}, new String [] {uuid.toString(), "Mute expired"});
									if(previousMutes > 0) {
										days *= previousMutes;
										hours *= previousMutes;
									}
									expires = TimeUtil.addDate(days, hours);
								}
								String address = null;
								Player player = ProPlugin.getPlayer(arguments[0]);
								if(player == null) {
									address = AccountHandler.getAddress(uuid);
								} else {
									address = player.getAddress().getAddress().getHostAddress();
									uuid = Disguise.getUUID(player);
								}
								DB.STAFF_MUTES.insert("'" + uuid.toString() + "', '" + staffUUID + "', '" + address + "', '" + reason.toString() + "', '" + arguments[2] + "', '" + time.substring(0, 7) + "', '" + time + "', '" + expires + "'");
								// Perform any final execution instructions
								MessageHandler.alert(message);
								// Execute the mute if the player is online
								if(player != null) {
									new MuteData(player, time, expires, sender.getName(), reason.toString(), arguments[2], DB.STAFF_MUTES.getInt("uuid", result.getUUID().toString(), "id"));
									muteData.get(Disguise.getName(player)).display(player);
								}
							}
						} catch(IllegalArgumentException e) {
							e.printStackTrace();
							// Display all the valid options
							MessageHandler.sendMessage(sender, "&c\"" + arguments[1] + "\" is an unknown chat violatoin, use one of the following:");
							String reasons = "";
							for(ChatViolations reason : ChatViolations.values()) {
								reasons += "&a" + reason + "&e, ";
							}
							MessageHandler.sendMessage(sender, reasons.substring(0, reasons.length() - 2));
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("isMuted", 1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						UUID uuid = AccountHandler.getUUID(arguments[0]);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
						} else if(DB.STAFF_MUTES.isUUIDSet(uuid)) {
							PreparedStatement statement = null;
							ResultSet resultSet = null;
							DB table = DB.STAFF_MUTES;
							try {
								statement = table.getConnection().prepareStatement("SELECT * FROM " + table.getName() + " WHERE uuid = '" + uuid.toString() + "'");
								resultSet = statement.executeQuery();
								Player player = (Player) sender;
								MessageHandler.sendLine(player);
								if(Disguise.getUUID(player) == uuid) {
									MessageHandler.sendMessage(player, "You have been MUTED:");
								} else {
									MessageHandler.sendMessage(player, "They have been MUTED:");
								}
								while(resultSet.next()) {
									MessageHandler.sendMessage(player, "Mute ID #" + (resultSet.getInt("id") + 1));
									MessageHandler.sendMessage(player, "Expires at: " + resultSet.getString("expires"));
									String proof = resultSet.getString("proof");
									String reason = resultSet.getString("reason");
									if(proof.equals("none")) {
										MessageHandler.sendMessage(player, "Muted for: " + reason.replace("_", " "));
									} else {
										MessageHandler.sendMessage(player, "Muted for: " + reason.replace("_", " ") + " " + proof.replace("_", " "));
									}
									if(Ranks.SENIOR_MODERATOR.hasRank(sender)) {
										MessageHandler.sendMessage(sender, "&c&lThe following is ONLY displayed to Sr. Mods and above");
										String staffUUID = resultSet.getString("staff_uuid");
										if(!staffUUID.equals("CONSOLE")) {
											staffUUID = AccountHandler.getName(UUID.fromString(staffUUID));
										}
										MessageHandler.sendMessage(sender, "Muted by: " + staffUUID);
									}
								}
								MessageHandler.sendLine(player);
							} catch(SQLException e) {
								e.printStackTrace();
							} finally {
								DB.close(statement, resultSet);
							}
						} else {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not muted");
						}
					}
				});
				return true;
			}
		}.enableDelay(2);
		new CommandBase("viewMutes", 0, 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String target = sender.getName();
						String uuid = null;
						if(arguments.length == 0) {
							if(sender instanceof Player) {
								Player player = (Player) sender;
								target = Disguise.getName(player);
								uuid = Disguise.getUUID(player).toString();
							} else {
								MessageHandler.sendPlayersOnly(sender);
								return;
							}
						} else {
							if(Ranks.isStaff(sender)) {
								target = arguments[0];
								UUID targetUUID = AccountHandler.getUUID(target);
								if(targetUUID == null) {
									MessageHandler.sendMessage(sender, "&c" + target + " has never logged in before");
									return;
								} else {
									uuid = targetUUID.toString();
								}
							} else {
								MessageHandler.sendMessage(sender, Ranks.HELPER.getNoPermission());
								return;
							}
						}
						for(DB table : new DB [] {DB.STAFF_MUTES, DB.STAFF_UNMUTES}) {
							PreparedStatement statement = null;
							ResultSet resultSet = null;
							try {
								statement = table.getConnection().prepareStatement("SELECT COUNT(id) FROM " + table.getName() + " WHERE uuid = '" + uuid + "'");
								resultSet = statement.executeQuery();
								if(resultSet.next() && resultSet.getInt(1) > 0) {
									int size = resultSet.getInt(1);
									DB.close(statement, resultSet);
									statement = table.getConnection().prepareStatement("SELECT id, reason, proof, time FROM " + table.getName() + " WHERE uuid = '" + uuid + "'");
									resultSet = statement.executeQuery();
									MessageHandler.sendMessage(sender, "Displaying &e" + size + " &aKick" + (size == 1 ? ":" : "s:"));
									int counter = 0;
									while(resultSet.next()) {
										int id = resultSet.getInt("id");
										String reason = resultSet.getString("reason");
										String proof = resultSet.getString("proof").replace("_", " ");
										String time = resultSet.getString("time");
										String message = "&c#" + (++counter) + ": &eReason: " + reason + " Proof: " + proof + " Time: " + time + " ID #" + id;
										if(table == DB.STAFF_MUTES) {
											message += " &7(&4CURRENT MUTE&7)";
										} else {
											message += " &7(&4PAST MUTE&7)";
										}
										MessageHandler.sendMessage(sender, message);
									}
									if(target.equalsIgnoreCase(sender.getName())) {
										MessageHandler.sendMessage(sender, "Do you have an unfair kick? Appeal it to get it removed here:");
										MessageHandler.sendMessage(sender, Punishment.appeal);
									}
								} else {
									MessageHandler.sendMessage(sender, "&c" + target + " has never been muted before");
								}
								MessageHandler.sendMessage(sender, "To view their deleted kicks do &b/viewDeletedKicks");
							} catch(SQLException e) {
								e.printStackTrace();
							} finally {
								DB.close(statement, resultSet);
							}
						}
					}
				});
				return true;
			}
		};
	}
	
	public static boolean checkMute(Player player) {
		if(Ranks.isStaff(player)) {
			return false;
		} else {
			if(checkedForMuted == null) {
				checkedForMuted = new ArrayList<String>();
			}
			if(!checkedForMuted.contains(Disguise.getName(player))) {
				checkedForMuted.add(Disguise.getName(player));
				String address = player.getAddress().getAddress().getHostAddress();
				if(DB.STAFF_MUTES.isUUIDSet(Disguise.getUUID(player))) {
					new MuteData(player);
					DB.STAFF_MUTES.updateString("address", address, "uuid", Disguise.getUUID(player).toString());
				}
			}
			return muteData != null && muteData.containsKey(Disguise.getName(player));
		}
	}
	
	public static void remove(Player player) {
		if(checkedForMuted != null && checkedForMuted.contains(Disguise.getName(player))) {
			checkedForMuted.remove(Disguise.getName(player));
		}
		if(muteData != null && muteData.containsKey(Disguise.getName(player))) {
			muteData.remove(Disguise.getName(player));
		}
	}
	
	public static void unMute(Player player, boolean editDatabase) {
		MessageHandler.sendLine(player);
		player.sendMessage(ChatColor.YELLOW + "Your mute has expired! Be sure to follow all rules please!");
		player.sendMessage(ChatColor.AQUA + "/rules");
		MessageHandler.sendLine(player);
		remove(player);
		String time = TimeUtil.getTime();
		if(editDatabase) {
			DB.STAFF_UNMUTES.insert("'" + Disguise.getUUID(player).toString() + "', 'CONSOLE', 'Mute expired', '" + time.substring(0, 7) + "', '" + time + "'");
			DB.STAFF_MUTES.deleteUUID(Disguise.getUUID(player));
		}
	}
	
	public static void display(Player player) {
		if(checkMute(player)) {
			muteData.get(Disguise.getName(player)).display(player);
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if(checkMute(player) && muteData != null && !muteData.get(Disguise.getName(player)).hasExpired(player)) {
			muteData.get(Disguise.getName(player)).display(player);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
}
