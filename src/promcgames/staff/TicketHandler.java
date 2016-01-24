package promcgames.staff;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import promcgames.customevents.TicketCreatedEvent;
import promcgames.customevents.player.PlayerBanEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.customevents.timed.TenSecondTaskEvent;
import promcgames.gameapi.MiniGame;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.account.PlaytimeTracker;
import promcgames.player.account.PlaytimeTracker.TimeType;
import promcgames.server.AutoBroadcasts;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.TimeUtil;

public class TicketHandler implements Listener {
	public enum TicketReasons {HACKING, FAST_BOW, CHAT_VIOLATION, CHAT_FILTER_DETECTION}
	private Map<Integer, String> openTickets = null;
	private static Map<String, List<Integer>> ticketIDs = null;
	private static List<String> tempBanned = null;
	private int counter = 10;
	
	private class TicketData {
		public TicketData(int id, Player player) {
			boolean open = true;
			String reporting = null;
			UUID reportedUUID = null;
			String reported = null;
			String playTime = null;
			TicketReasons reason = null;
			String server = null;
			String proof = null;
			PreparedStatement statement = null;
			ResultSet resultSet = null;
			DB table = DB.STAFF_TICKETS;
			try {
				statement = table.getConnection().prepareStatement("SELECT * FROM " + table.getName() + " WHERE id = '" + id + "'");
				resultSet = statement.executeQuery();
				while(resultSet.next()) {
					reportedUUID = UUID.fromString(resultSet.getString("reported_uuid"));
					reported = AccountHandler.getName(reportedUUID);
					String uuid = resultSet.getString("uuid");
					if(uuid.equals("CONSOLE")) {
						reporting = "CONSOLE";
					} else {
						reporting = AccountHandler.getName(UUID.fromString(uuid));
					}
					try {
						reason = TicketReasons.valueOf(resultSet.getString("reason"));
						open = resultSet.getBoolean("opened");
						if(ProPlugin.getPlayer(reported) != null && (reason == TicketReasons.HACKING || reason == TicketReasons.FAST_BOW)) {
							if(ticketIDs == null) {
								ticketIDs = new HashMap<String, List<Integer>>();
							}
							List<Integer> ints = ticketIDs.get(reported);
							if(ints == null) {
								ints = new ArrayList<Integer>();
							}
							if(!ints.contains(id)) {
								ints.add(id);
								ticketIDs.put(reported, ints);
							}
						}
						playTime = resultSet.getString("play_time");
						proof = resultSet.getString("proof");
					} catch(IllegalArgumentException e) {
						
					}
				}
			} catch(SQLException e) {
				e.printStackTrace();
			} finally {
				DB.close(statement, resultSet);
			}
			server = DB.PLAYERS_LOCATIONS.getString("uuid", reportedUUID.toString(), "location");
			if(server == null) {
				server = ChatColor.RED + "OFFLINE";
			}
			if(player != null) {
				MessageHandler.sendLine(player, "&e");
				MessageHandler.sendMessage(player, "&eTicket ID #&b" + id + " &eby &b" + reporting + " &efor &b" + reason.toString().replace("_", " "));
				MessageHandler.sendMessage(player, "&eReported: &2" + reported + " &eplaytime: &b" + playTime);
				String actualServer = server.split(ChatColor.RED.toString())[1];
				ChatClickHandler.sendMessageToRunCommand(player, " &c&lCLICK TO JOIN " + actualServer, "Click to teleport to " + actualServer, "/join " + actualServer, "&eCurrent location: &b" + actualServer);
				if(!proof.equals("none")) {
					MessageHandler.sendMessage(player, "&eProof: &b" + proof.replace("_", " "));
				}
				if(Ranks.isStaff(player)) {
					if(reason == TicketReasons.CHAT_FILTER_DETECTION && open) {
						ChatClickHandler.sendMessageToRunCommand(player, " &c&lCLICK TO KICK", "Click to kick this player", "/ticket startKicking " + id, "&eStatus: " + (open ? "&aOPEN" : "&cCLOSED"));
					}
					ChatClickHandler.sendMessageToRunCommand(player, open ? " &c&lCLICK TO CLOSE" : " &c&lCLICK TO RE-CLOSE", "Click to close this ticket", "/ticket close " + id, "&eStatus: " + (open ? "&aOPEN" : "&cCLOSED"));
				}
				MessageHandler.sendLine(player, "&e");
			}
		}
	}
	
	public TicketHandler() {
		tempBanned = new ArrayList<String>();
		AutoBroadcasts.addAlert("Found a hacker? &b/ticket create <name> HACKING");
		new CommandBase("ticket", 1, -1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String option = arguments[0];
						Player player = null;
						if(sender instanceof Player) {
							player = (Player) sender;
						} else if(!option.equalsIgnoreCase("create") && !option.equalsIgnoreCase("close")) {
							MessageHandler.sendPlayersOnly(sender);
							return;
						}
						if(option.equalsIgnoreCase("create")) {
							Plugins plugin = ProMcGames.getPlugin();
							MiniGame game = ProMcGames.getMiniGame();
							if(plugin == Plugins.HUB || plugin == Plugins.SGHUB) {
								MessageHandler.sendMessage(player, "&cYou cannot create a ticket on this server");
								return;
							} else if(game != null && game.getGameState() != GameStates.STARTING && game.getGameState() != GameStates.STARTED) {
								MessageHandler.sendMessage(player, "&cYou cannot create a ticket before the game starts or is starting");
								return;
							}
							if(arguments.length == 3 || arguments.length == 4) {
								String name = arguments[1];
								Player target = ProPlugin.getPlayer(name);
								if(name.equalsIgnoreCase(sender.getName()) && target != null && !Ranks.OWNER.hasRank(target)) {
									MessageHandler.sendMessage(player, "&cYou cannot report yourself");
								} else if(target != null && Ranks.isStaff(target) && !Ranks.OWNER.hasRank(target)) {
									MessageHandler.sendMessage(player, "&cYou cannot report a staff this way, please report staff here:");
									MessageHandler.sendMessage(player, "http://forum.promcgames.com/forums/complaints-about-staff.12/");
								} else {
									if(player != null && DB.STAFF_TICKETS.isKeySet(new String [] {"uuid", "opened"}, new String [] {Disguise.getUUID(player).toString(), "1"})) {
										MessageHandler.sendMessage(player, "&cYou already have an open ticket deployed.");
										return;
									}
									UUID uuid = AccountHandler.getUUID(name);
									if(uuid == null) {
										MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
									} else {
										try {
											TicketReasons reason = TicketReasons.valueOf(arguments[2].toUpperCase());
											if(reason == TicketReasons.CHAT_FILTER_DETECTION && sender instanceof Player) {
												MessageHandler.sendMessage(sender, "&cPlayers cannot create a ticket for this reason");
												return;
											}
											String proof = (arguments.length == 4 ? arguments[3] : "none");
											if(proof.equals("none")) {
												if(reason == TicketReasons.CHAT_VIOLATION) {
													MessageHandler.sendMessage(sender, "&cYou must attach proof for chat violations");
													return;
												} else if(target == null) {
													MessageHandler.sendMessage(sender, "&cYou must attach proof for reports of offline players");
													return;
												}
											} else {
												if(reason != TicketReasons.CHAT_FILTER_DETECTION && !Pattern.compile("http[s]{0,1}://[a-zA-Z0-9\\./\\?=_%&#-+$@'\"\\|,!*]*").matcher(proof).find()) {
													MessageHandler.sendMessage(sender, "&cYour proof must be a URL to a FULL SCREEN screen shot");
													return;
												} else if(proof.contains("gyazo.com")) {
													MessageHandler.sendMessage(sender, "&cGyazo links cannot be used as proof");
													return;
												} else if(reason == TicketReasons.HACKING || reason == TicketReasons.FAST_BOW) {
													if(!proof.toLowerCase().contains("youtube.com/") && !proof.toLowerCase().contains("youtu.be/")) {
														MessageHandler.sendMessage(sender, "&cYour proof must be a youtube URL");
														return;
													}
													if(proof.contains("&")) {
														proof = proof.split("&")[0];
													}
												}
											}
											String reportingUUID = "CONSOLE";
											if(player != null) {
												reportingUUID = Disguise.getUUID(player).toString();
											}
											DB.STAFF_TICKETS.insert("'" + reportingUUID + "', '" + uuid.toString() + "', '" + null + "', '" + reason.toString() + "', '" + null + "', '" + null + "', '" + PlaytimeTracker.getPlayTime(target).getDisplay(TimeType.LIFETIME) + "', '" + proof + "', '" + TimeUtil.getTime() + "', '" + null + "', '" + null + "', '1'");
											String staffUUID = "CONSOLE";
											if(player != null) {
												staffUUID = Disguise.getUUID(player).toString();
											}
											String [] keys = new String [] {"reported_uuid", "uuid", "opened"};
											String [] values = new String [] {Disguise.getUUID(target).toString(), staffUUID, "1"};
											int id = DB.STAFF_TICKETS.getInt(keys, values, "id");
											MessageHandler.sendMessage(sender, "Your ticket has been created (ID# " + id + ")! Staff will be notified of it every 10 seconds until it is closed. Thank you for the report!");
											if(reason == TicketReasons.HACKING || reason == TicketReasons.FAST_BOW) {
												if(ticketIDs == null) {
													ticketIDs = new HashMap<String, List<Integer>>();
												}
												List<Integer> ints = ticketIDs.get(target.getName());
												if(ints == null) {
													ints = new ArrayList<Integer>();
												}
												ints.add(id);
												ticketIDs.put(target.getName(), ints);
											}
											if(PlaytimeTracker.isNew(target) && !tempBanned.contains(target.getName())) {
												if(ticketIDs != null && ticketIDs.get(target.getName()).size() >= 2) {
													tempBanned.add(target.getName());
													ProPlugin.sendPlayerToServer(player, "hub");
												}
											}
											Bukkit.getPluginManager().callEvent(new TicketCreatedEvent(target));
										} catch(IllegalArgumentException e) {
											MessageHandler.sendMessage(sender, "&c\"" + arguments[2] + "\" is an unknown ticket reason, use one of the following:");
											String reasons = "";
											for(TicketReasons reason : TicketReasons.values()) {
												reasons += "&a" + reason + "&e, ";
											}
											MessageHandler.sendMessage(sender, reasons.substring(0, reasons.length() - 2));
										}
									}
								}
							} else {
								MessageHandler.sendMessage(player, "&f/ticket create <player name> <reason> [proof]");
							}
						} else if(option.equalsIgnoreCase("view")) {
							if(arguments.length == 2 && arguments[1].equalsIgnoreCase("open")) {
								if(Ranks.isStaff(sender)) {
									if(openTickets == null || openTickets.isEmpty()) {
										MessageHandler.sendMessage(player, "&cThere are no tickets to display currently.");
										MessageHandler.sendMessage(player, "&cChecking for more tickets in &e" + counter + "&c seconds");
									} else {
										MessageHandler.sendLine(player, "&b");
										MessageHandler.sendMessage(player, "&aOpen ticket IDs: &c&l&nCLICK THE ID TO OPEN");
										for(int id : openTickets.keySet()) {
											ChatClickHandler.sendMessageToRunCommand(player, openTickets.get(id) + "&l" + id, "Click to open", "/ticket view " + id);
										}
										MessageHandler.sendMessage(player, "&cRed &aticket IDs are non-ranked players (More likely to hack)");
										MessageHandler.sendMessage(player, "&bBlue &aticket IDs are possible server advertisement");
										MessageHandler.sendLine(player, "&b");
									}
								} else {
									MessageHandler.sendMessage(player, Ranks.HELPER.getNoPermission());
								}
								return;
							} else if(arguments.length >= 2) {
								try {
									int id = Integer.valueOf(arguments[1]);
									if(DB.STAFF_TICKETS.isKeySet("id", arguments[1])) {
										new TicketData(id, player);
									} else {
										MessageHandler.sendMessage(sender, "&cThere is no ticket for ID #" + id + " (Deleted or never existed)");
									}
									return;
								} catch(NumberFormatException e) {
									
								}
							}
							MessageHandler.sendMessage(sender, "&f/ticket view <ticket ID>");
							if(Ranks.isStaff(sender)) {
								MessageHandler.sendMessage(sender, "&f/ticket view open");
							}
						} else if(option.equalsIgnoreCase("close")) {
							if(Ranks.isStaff(sender)) {
								if(arguments.length >= 2) {
									String id = arguments[1];
									if(DB.STAFF_TICKETS.isKeySet("id", id)) {
										String staffUUID = "CONSOLE";
										if(player != null) {
											staffUUID = Disguise.getUUID(player).toString();
										}
										String date = TimeUtil.getTime().substring(0, 7);
										String [] keys = new String [] {"uuid", "date_closed"};
										String [] values = new String [] {staffUUID, date};
										if(DB.STAFF_TICKETS_CLOSED.isKeySet(keys, values)) {
											int amount = DB.STAFF_TICKETS_CLOSED.getInt(keys, values, "amount") + 1;
											DB.STAFF_TICKETS_CLOSED.updateInt("amount", amount, keys, values);
										} else {
											DB.STAFF_TICKETS_CLOSED.insert("'" + staffUUID + "', '" + date + "', '1'");
										}
										DB.STAFF_TICKETS.delete("id", id);
										MessageHandler.sendMessage(sender, "You have closed ticket #" + id);
									} else {
										MessageHandler.sendMessage(sender, "&cThere is no ticket for ID #" + id + " (Deleted or never existed)");
									}
									return;
								}
								MessageHandler.sendMessage(sender, "&f/ticket close <ticket ID>");
							} else {
								MessageHandler.sendMessage(player, Ranks.HELPER.getNoPermission());
							}
						} else if(option.equalsIgnoreCase("startKicking")) {
							if(Ranks.isStaff(sender)) {
								if(arguments.length == 2) {
									try {
										int id = Integer.valueOf(arguments[1]);
										MessageHandler.sendLine(player, "&e");
										MessageHandler.sendMessage(player, "&a&lOPTIONS TO KICK");
										ChatClickHandler.sendMessageToRunCommand(player, "&c&lCLICK TO KICK FOR (SERVER_ADVERTISEMENT)", "Click to kick for SERVER_ADVERTISEMENT", "/ticket startKicking confirm " + id + " SERVER_ADVERTISEMENT");
										MessageHandler.sendMessage(player, "");
										ChatClickHandler.sendMessageToRunCommand(player, "&c&lCLICK TO KICK FOR (SOCIAL_MEDIA_ADVERTISEMENT)", "Click to kick for SOCIAL_MEDIA_ADVERTISEMENT", "/ticket startKicking confirm " + id + " SOCIAL_MEDIA_ADVERTISEMENT");
										MessageHandler.sendMessage(player, "");
										ChatClickHandler.sendMessageToRunCommand(player, "&c&lCLICK TO KICK FOR (RACISM)", "Click to kick for RACISM", "/ticket startKicking confirm " + id + " RACISM");
										MessageHandler.sendMessage(player, "");
										ChatClickHandler.sendMessageToRunCommand(player, "&c&lCLICK TO KICK FOR (DISRESPECT)", "Click to kick for DISRESPECT", "/ticket startKicking confirm " + id + " DISRESPECT");
										MessageHandler.sendMessage(player, "");
										ChatClickHandler.sendMessageToRunCommand(player, "&c&lCLICK TO KICK FOR (SUICIDE_COMMENTS)", "Click to kick for SUICIDE_COMMENTS", "/ticket startKicking confirm " + id + " SUICIDE_COMMENTS");
										MessageHandler.sendMessage(player, "");
										ChatClickHandler.sendMessageToRunCommand(player, "&c&lCLICK TO KICK FOR (INAPPROPRIATE_COMMENTS)", "Click to kick for INAPPROPRIATE_COMMENTS", "/ticket startKicking confirm " + id + " INAPPROPRIATE_COMMENTS");
										MessageHandler.sendLine(player, "&e");
									} catch(NumberFormatException e) {
										return;
									}
								} else if(arguments.length >= 3) {
									try {
										int id = Integer.valueOf(arguments[2]);
										String name = AccountHandler.getName(UUID.fromString(DB.STAFF_TICKETS.getString("id", String.valueOf(id), "reported_uuid")));
										String proof = DB.STAFF_TICKETS.getString("id", String.valueOf(id), "proof");
										if(name == null) {
											MessageHandler.sendMessage(sender, "&cCould not load the name of the player for this ticket");
										} else if(proof == null) {
											MessageHandler.sendMessage(sender, "&cCould not load the proof for this ticket");
										} else {
											ChatClickHandler.sendMessageToRunCommand(player, "&a&lCLICK TO KICK FOR " + arguments[3], "Click to kick", "/kick " + name + " " + arguments[3] + " " + proof);
										}
									} catch(NumberFormatException e) {
										return;
									}
								} else {
									return;
								}
							} else {
								MessageHandler.sendMessage(player, Ranks.HELPER.getNoPermission());
							}
						} else {
							if(arguments.length == 2) {
								if(arguments[1].equalsIgnoreCase("players")) {
									MessageHandler.sendLine(player);
									MessageHandler.sendMessage(player, "Ticket Commands:");
									MessageHandler.sendMessage(player, "&bCreate a ticket report:");
									MessageHandler.sendMessage(player, "/ticket create <player name> <reason> [proof]");
									MessageHandler.sendMessage(player, "&bView a ticket's status:");
									MessageHandler.sendMessage(player, "/ticket view <ticket ID>");
									MessageHandler.sendLine(player);
									return;
								} else if(arguments[1].equalsIgnoreCase("staff")) {
									MessageHandler.sendLine(player);
									MessageHandler.sendMessage(player, "Staff Ticket Commands:");
									MessageHandler.sendMessage(player, "&bClose a ticket:");
									MessageHandler.sendMessage(player, "/ticket close <ticket ID>");
									MessageHandler.sendMessage(player, "&bView open tickets:");
									MessageHandler.sendMessage(player, "/ticket view open");
									MessageHandler.sendLine(player);
									return;
								}
							}
							MessageHandler.sendMessage(player, "You must specify player or staff commands, examples:");
							MessageHandler.sendMessage(player, "/ticket commands players");
							MessageHandler.sendMessage(player, "/ticket commands staff");
						}
					}
				});
				return true;
			}
		};
		EventUtil.register(this);
		checkForTickets();
	}
	
	private void notifyOfOpenTickets(Player player) {
		if(openTickets.size() == 1) {
			ChatClickHandler.sendMessageToRunCommand(player, "&c&lCLICK TO VIEW IT", "Click to view the open ticket", "/ticket view open", ChatColor.translateAlternateColorCodes('&', "&e&lThere is &c&l1 &e&lopen ticket! "));
		} else {
			ChatClickHandler.sendMessageToRunCommand(player, "&c&lCLICK TO VIEW THEM", "Click to view the open tickets", "/ticket view open", ChatColor.translateAlternateColorCodes('&', "&e&lThere are &c&l" + openTickets.size() + " &e&lopen tickets! "));
		}
		if(!Ranks.OWNER.hasRank(player)) {
			EffectUtil.playSound(player, Sound.CHICKEN_EGG_POP);
		}
	}
	
	private void checkForTickets() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				counter = 10;
				if(openTickets == null) {
					openTickets = new HashMap<Integer, String>();
				} else {
					openTickets.clear();
				}
				PreparedStatement statement = null;
				ResultSet resultSet = null;
				DB table = DB.STAFF_TICKETS;
				try {
					statement = table.getConnection().prepareStatement("SELECT id, reported_uuid, reason FROM " + table.getName() + " WHERE opened = '1'");
					resultSet = statement.executeQuery();
					while(resultSet.next()) {
						String reason = resultSet.getString("reason");
						if(reason.equals(TicketReasons.CHAT_FILTER_DETECTION.toString()) || reason.equals(TicketReasons.CHAT_VIOLATION)) {
							openTickets.put(resultSet.getInt("id"), "&b");
						} else {
							Ranks rank = AccountHandler.getRank(UUID.fromString(resultSet.getString("reported_uuid")));
							openTickets.put(resultSet.getInt("id"), rank == Ranks.PLAYER ? "&c" : "&e");
						}
					}
				} catch(SQLException e) {
					e.printStackTrace();
				} finally {
					DB.close(statement, resultSet);
				}
				if(openTickets != null && !openTickets.isEmpty() && ProMcGames.getMiniGame() == null) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(Ranks.isStaff(player)) {
							notifyOfOpenTickets(player);
						}
					}
				}
			}
		});
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(tempBanned.contains(event.getPlayer().getName())) {
			event.setKickMessage(ChatColor.RED + "You are temporarily not allowed to join this server");
			event.setResult(Result.KICK_OTHER);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		String name = event.getPlayer().getName();
		if(ticketIDs != null && ticketIDs.containsKey(name)) {
			if(ticketIDs.get(name) != null) {
				for(int id : ticketIDs.get(name)) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ticket close " + id + " INVALID_REPORT Player has logged off of that server");
				}
				ticketIDs.get(name).clear();
			}
			ticketIDs.remove(name);
		}
	}
	
	@EventHandler
	public void onPlayerBan(PlayerBanEvent event) {
		Player player = Bukkit.getPlayer(event.getUUID());
		if(player != null && player.isOnline()) {
			CommandSender staff = event.getStaff();
			if(ticketIDs != null && ticketIDs.containsKey(player.getName())) {
				if(ticketIDs.get(player.getName()) != null) {
					for(int id : ticketIDs.get(player.getName())) {
						String command = "ticket close " + id + " PUNISHMENT_ISSUED Player has been banned";
						if(staff instanceof Player) {
							Player staffPlayer = (Player) staff;
							staffPlayer.chat("/" + command);
						} else {
							Bukkit.dispatchCommand(staff, command);
						}
					}
					ticketIDs.get(player.getName()).clear();
				}
				ticketIDs.remove(player.getName());
			}
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		--counter;
	}
	
	@EventHandler
	public void onTenSecondTask(TenSecondTaskEvent event) {
		checkForTickets();
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(!event.isCancelled() && !Ranks.isStaff(event.getPlayer())) {
			String msg = ChatColor.stripColor(event.getMessage().toLowerCase().replace("!", "").replace(".", "").replace("?", ""));
			String regex = "([h]+[\\W]*[a|4|@|q]+[\\W]*(x|k|ck)+[\\W]*(s)*+(([0|e]+[\\W]*[r]+[\\W]*[s]*)*|([i|1]+[\\W]*[n]+[\\W]*[g]*)))+";
			if(msg.toLowerCase().matches(regex)) {
				for(Player online : Bukkit.getOnlinePlayers()) {
					if(!event.getPlayer().getName().equals(online.getName())) {
						event.getRecipients().remove(online);
					}
				}
				display(event.getPlayer());
			} else {
				for(String word : event.getMessage().split(" ")) {
					if(word.toLowerCase().matches(regex)) {
						display(event.getPlayer());
						event.setCancelled(true);
						break;
					}
				}
			}
		}
	}
	
	private void display(Player player) {
		MessageHandler.sendLine(player);
		MessageHandler.sendMessage(player, "&cWe have an on-server reporting system:");
		MessageHandler.sendMessage(player, "Example: /ticket create <player name> HACKING");
		MessageHandler.sendLine(player);
	}
}
