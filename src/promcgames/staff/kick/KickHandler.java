package promcgames.staff.kick;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import promcgames.ProPlugin;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.TimeUtil;
import promcgames.staff.Punishment;

public class KickHandler extends Punishment {
	private Map<ChatViolations, Integer> kicksToTriggerMute = null;
	
	public KickHandler() {
		super("KICKED");
		// Set the amount of kicks required to mute a player
		// Note that a kick would make a player reach this amount a mute will be issued instead of the kick
		kicksToTriggerMute = new HashMap<ChatViolations, Integer>();
		kicksToTriggerMute.put(ChatViolations.DISRESPECT, 2);
		kicksToTriggerMute.put(ChatViolations.RACISM, 2);
		kicksToTriggerMute.put(ChatViolations.DEATH_COMMENTS, 1);
		kicksToTriggerMute.put(ChatViolations.INAPPROPRIATE_COMMENTS, 3);
		kicksToTriggerMute.put(ChatViolations.SPAMMING, 3);
		kicksToTriggerMute.put(ChatViolations.SOCIAL_MEDIA_ADVERTISEMENT, 3);
		kicksToTriggerMute.put(ChatViolations.WEBSITE_ADVERTISEMENT, 3);
		kicksToTriggerMute.put(ChatViolations.SERVER_ADVERTISEMENT, 1);
		kicksToTriggerMute.put(ChatViolations.HACKUSATIONS, 2);
		kicksToTriggerMute.put(ChatViolations.DDOS_THREATS, 1);
		// Command syntax: /kick <player name> <reason> <proof>
		new CommandBase("kick", 3) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				// Use a try/catch to view if the given reason is valid
				try {
					ChatViolations reason = ChatViolations.valueOf(arguments[1].toUpperCase());
					// Detect if the command should be activated
					PunishmentExecuteReuslts result = executePunishment(sender, arguments, false);
					if(result.isValid()) {
						// See if the player is muted
						if(DB.STAFF_MUTES.isUUIDSet(result.getUUID())) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is already muted");
							return true;
						}
						// Get the staff data
						String staff = "CONSOLE";
						String staffUUID = staff;
						if(sender instanceof Player) {
							Player player = (Player) sender;
							staff = Disguise.getName(player);
							staffUUID = Disguise.getUUID(player).toString();
							// Close the ticket if this kick is from a ticket
							int id = DB.STAFF_TICKETS.getInt(new String [] {"reported_uuid", "proof", "opened"}, new String [] {result.getUUID().toString(), arguments[2], "1"}, "id");
							if(id > 0) {
								player.chat("/ticket close " + id + " PUNISHMENT_ISSUED That player has been punished");
							}
						}
						// Detect if the punishment should be another kick or a mute
						if(DB.STAFF_KICKS.getSize("uuid", result.getUUID().toString()) >= 2 || DB.STAFF_KICKS.getSize(new String [] {"uuid", "reason"}, new String [] {result.getUUID().toString(), reason.toString()}) + 1 >= kicksToTriggerMute.get(reason)) {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mute " + arguments[0] + " " + reason.toString() + " " + arguments[2] + " " + staff);
						} else {
							// See if the player has been kicked for that screen shot before
							if(DB.STAFF_KICKS.isKeySet(new String [] {"uuid", "proof"}, new String [] {result.getUUID().toString(), arguments[2]})) {
								MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has already been " + getName() + " with that proof");
								return true;
							}
							// Compile the message string
							String message = getReason(AccountHandler.getRank(sender), arguments, reason.toString(), result);
							// If the player is currently online then kick them from the server
							final Player player = ProPlugin.getPlayer(arguments[0]);
							if(player != null) {
								final String kickMessage = message.replace(arguments[2], "\n" + ChatColor.AQUA + "To view the proof log in and do \"/viewKicks\"\n" + ChatColor.RED + "Invalid kick? Log in and do /appeal");
								try {
									player.kickPlayer(kickMessage);
								} catch(IllegalStateException e) {
									new DelayedTask(new Runnable() {
										@Override
										public void run() {
											player.kickPlayer(kickMessage);
										}
									});
								}
							}
							// Update the database
							UUID uuid = result.getUUID();
							String time = TimeUtil.getTime();
							DB.STAFF_KICKS.insert("'" + uuid.toString() + "', '" + staffUUID + "', '" + reason + "', '" + arguments[2] + "', '" + time.substring(0, 7) + "', '" + time + "'");
							// Perform any final execution instructions
							MessageHandler.alert(message);
						}
					}
				} catch(IllegalArgumentException e) {
					e.printStackTrace();
					// Display all the valid options
					MessageHandler.sendMessage(sender, "&c\"" + arguments[1] + "\" is an unknown chat violation, use one of the following:");
					String reasons = "";
					for(ChatViolations reason : ChatViolations.values()) {
						reasons += "&a" + reason + "&e, ";
					}
					MessageHandler.sendMessage(sender, reasons.substring(0, reasons.length() - 2));
				}
				return true;
			}
		}.setRequiredRank(Ranks.HELPER);
		new CommandBase("viewKicks", 0, 1) {
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
						PreparedStatement statement = null;
						ResultSet resultSet = null;
						DB table = DB.STAFF_KICKS;
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
									MessageHandler.sendMessage(sender, "&c#" + (++counter) + ": &eReason: " + reason + " Proof: " + proof + " Time: " + time + " ID #" + id);
								}
								if(target.equalsIgnoreCase(sender.getName())) {
									MessageHandler.sendMessage(sender, "Do you have an unfair kick? Appeal it to get it removed here:");
									MessageHandler.sendMessage(sender, Punishment.appeal);
								}
							} else {
								MessageHandler.sendMessage(sender, "&c" + target + " has never been kicked before");
							}
							MessageHandler.sendMessage(sender, "To view their deleted kicks do &b/viewDeletedKicks");
						} catch(SQLException e) {
							e.printStackTrace();
						} finally {
							DB.close(statement, resultSet);
						}
					}
				});
				return true;
			}
		}.enableDelay(2);
		new CommandBase("viewDeletedKicks", 0, 1) {
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
						PreparedStatement statement = null;
						ResultSet resultSet = null;
						DB table = DB.STAFF_DELETED_KICKS;
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
									MessageHandler.sendMessage(sender, "&c#" + (++counter) + ": &eReason: " + reason + " Proof: " + proof + " Time: " + time + " ID #" + id);
								}
								if(target.equalsIgnoreCase(sender.getName())) {
									MessageHandler.sendMessage(sender, "Do you have an unfair kick? Appeal it to get it removed here:");
									MessageHandler.sendMessage(sender, Punishment.appeal);
								}
							} else {
								MessageHandler.sendMessage(sender, "&c" + target + " has never been kicked before");
							}
							MessageHandler.sendMessage(sender, "To view their current kicks do &b/viewKicks");
						} catch(SQLException e) {
							e.printStackTrace();
						} finally {
							DB.close(statement, resultSet);
						}
					}
				});
				return true;
			}
		}.enableDelay(2);
	}
}
