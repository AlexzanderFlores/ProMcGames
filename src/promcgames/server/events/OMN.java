package promcgames.server.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.AlertHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.Tweeter;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

public class OMN implements Listener {
	private boolean canRun = true;
	private Map<UUID, String> hosts = null;
	private List<String> names = null;
	private Map<String, Integer> votes = null;
	private Map<String, String> playerVotes = null;
	private String name = null;
	
	public OMN() {
		hosts = new HashMap<UUID, String>();
		names = new ArrayList<String>();
		votes = new HashMap<String, Integer>();
		playerVotes = new HashMap<String, String>();
		name = "Open Mic Night Voting";
		for(String uuidString : DB.NETWORK_OMN_HOSTS.getAllStrings("uuid")) {
			UUID uuid = UUID.fromString(uuidString);
			if(uuid == null) {
				DB.NETWORK_OMN_HOSTS.delete("uuid", uuidString);
				continue;
			}
			String prefix = AccountHandler.getPrefix(uuid);
			if(prefix == null) {
				DB.NETWORK_OMN_HOSTS.delete("uuid", uuidString);
				continue;
			}
			hosts.put(uuid, prefix);
		}
		new CommandBase("omn", 0, 3) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				if(arguments.length == 0 || arguments[0].equalsIgnoreCase("help")) {
					MessageHandler.sendMessage(sender, "/omn alert");
					MessageHandler.sendMessage(sender, "/omn promote <name>");
					MessageHandler.sendMessage(sender, "/omn demote <name>");
					MessageHandler.sendMessage(sender, "/omn list");
					MessageHandler.sendMessage(sender, "/omn vote <name>");
					MessageHandler.sendMessage(sender, "/omn vote <add | remove> <name>");
					MessageHandler.sendMessage(sender, "/omn vote end");
				} else {
					String command = arguments[0];
					if(command.equalsIgnoreCase("alert")) {
						if(ProMcGames.getServerName().equals("HUB1")) {
							new AsyncDelayedTask(new Runnable() {
								@Override
								public void run() {
									if(sender instanceof Player) {
										Player player = (Player) sender;
										if(DB.NETWORK_OMN_HOSTS.isUUIDSet(player.getUniqueId())) {
											int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
											if(day == 6 || day == 7) {
												if(canRun) {
													canRun = false;
													String prize = "Winner gets 10 Hub Sponsors";
													String tweet = "Come join us for Open Mic Night on our Teamspeak server! " + prize + "\n\nts.ProMcGames.com";
													String alert = "Come join us for Open Mic Night on our TS server! Run /ts. " + prize;
													long id = Tweeter.tweet(tweet, "omn.jpg");
													if(id == -1) {
														MessageHandler.sendMessage(sender, "&cFailed to send tweet. Possible duplicate tweet");
													} else {
														AlertHandler.alert(alert + " &ehttps://twitter.com/ProMcGames/status/" + id);
													}
												} else {
													MessageHandler.sendMessage(sender, "&cYou cannot run this command currently");
												}
											} else {
												MessageHandler.sendMessage(sender, "&cYou cannot run this command today");
											}
										} else {
											MessageHandler.sendUnknownCommand(sender);
										}
									} else {
										MessageHandler.sendUnknownCommand(sender);
									}
								}
							});
						} else {
							MessageHandler.sendMessage(sender, "&cYou can only run this command on HUB1 &e/hub 1");
						}
					} else if(command.equalsIgnoreCase("promote")) {
						if(Ranks.SENIOR_MODERATOR.hasRank(sender)) {
							new AsyncDelayedTask(new Runnable() {
								@Override
								public void run() {
									String name = arguments[1];
									UUID uuid = AccountHandler.getUUID(name);
									if(uuid == null) {
										MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
									} else if(hosts.containsKey(uuid) || DB.NETWORK_OMN_HOSTS.isUUIDSet(uuid)) {
										MessageHandler.sendMessage(sender, "&c" + name + " is already a OMN host");
									} else {
										String prefix = AccountHandler.getPrefix(uuid);
										if(prefix == null) {
											MessageHandler.sendMessage(sender, "&cAn error occurred when trying to add " + name + " as a OMN host");
										} else {
											DB.NETWORK_OMN_HOSTS.insert("'" + uuid.toString() + "'");
											hosts.put(uuid, prefix);
											MessageHandler.sendMessage(sender, "You have promoted " + prefix + " &ato OMN host");
										}
									}
								}
							});
						} else {
							MessageHandler.sendMessage(sender, Ranks.OWNER.getNoPermission());
						}
					} else if(command.equalsIgnoreCase("demote")) {
						if(Ranks.SENIOR_MODERATOR.hasRank(sender)) {
							new AsyncDelayedTask(new Runnable() {
								@Override
								public void run() {
									String name = arguments[1];
									UUID uuid = AccountHandler.getUUID(name);
									if(uuid == null) {
										MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
									} else if(hosts.containsKey(uuid) || DB.NETWORK_OMN_HOSTS.isUUIDSet(uuid)) {
										DB.NETWORK_OMN_HOSTS.deleteUUID(uuid);
										hosts.remove(uuid);
										MessageHandler.sendMessage(sender, "You have &cdemoted &a" + name + " from OMN host");
									} else {
										MessageHandler.sendMessage(sender, "&c" + name + " is not a OMN host");
									}
								}
							});
						} else {
							MessageHandler.sendMessage(sender, Ranks.OWNER.getNoPermission());
						}
					} else if(command.equalsIgnoreCase("list")) {
						String message = "";
						for(UUID uuid : hosts.keySet()) {
							message += hosts.get(uuid) + ", ";
						}
						MessageHandler.sendMessage(sender, "OMN Hosts: &e(&a" + hosts.size() + "&e) &a" + message.substring(0, message.length() - 2));
					} else if(command.equalsIgnoreCase("vote")) {
						if(ProMcGames.getServerName().equals("HUB1")) {
							new AsyncDelayedTask(new Runnable() {
								@Override
								public void run() {
									if(sender instanceof Player) {
										Player player = (Player) sender;
										if(arguments.length == 3 && DB.NETWORK_OMN_HOSTS.isUUIDSet(player.getUniqueId())) {
											String option = arguments[1];
											String name = arguments[2].toLowerCase();
											UUID uuid = AccountHandler.getUUID(name);
											if(uuid == null) {
												MessageHandler.sendMessage(player, "&c" + name + " has never logged in before");
											} else {
												if(option.equalsIgnoreCase("add")) {
													if(names.contains(name)) {
														MessageHandler.sendMessage(player, "&c" + name + " is already a voting option");
													} else {
														names.add(name);
														MessageHandler.sendMessage(player, "Added " + name + " to the voting list");
													}
												} else if(option.equalsIgnoreCase("remove")) {
													if(names.contains(name)) {
														names.remove(name);
														votes.remove(name);
														MessageHandler.sendMessage(player, "Removed " + name + " from the voting option");
													} else {
														MessageHandler.sendMessage(player, "&c" + name + " is not a voting option");
													}
												} else {
													MessageHandler.sendMessage(player, "&cUnknown option \"&e" + option + "&c\". Must be \"&eadd&c\" or \"&eremove&c\"");
												}
											}
										} else if(arguments.length == 2) {
											String name = arguments[1].toLowerCase();
											if(name.equalsIgnoreCase("end")) {
												if(names.isEmpty()) {
													MessageHandler.sendMessage(player, "&cThere is no vote currently active");
												} else {
													if(DB.NETWORK_OMN_HOSTS.isUUIDSet(player.getUniqueId())) {
														int topScore = 0;
														for(String option : names) {
															if(votes.containsKey(option) && votes.get(option) >= topScore) {
																topScore = votes.get(option);
															}
														}
														List<String> winners = new ArrayList<String>();
														for(String option : names) {
															if(votes.containsKey(option) && votes.get(option) == topScore) {
																winners.add(option);
															}
														}
														if(winners.size() == 1) {
															MessageHandler.alert("The winner of Open Mic Night is &e" + winners.get(0) + "&a!");
														} else {
															MessageHandler.sendMessage(sender, "There is a tie!");
															for(String winner : winners) {
																MessageHandler.sendMessage(sender, winner);
															}
														}
														names.clear();
														votes.clear();
														playerVotes.clear();
													} else {
														MessageHandler.sendUnknownCommand(player);
													}
												}
											} else {
												vote(player, name);
											}
										} else if(arguments.length == 1) {
											if(names.isEmpty()) {
												MessageHandler.sendMessage(player, "&cThere is no vote currently active");
											} else {
												Inventory inventory = Bukkit.createInventory(player, ItemUtil.getInventorySize(names.size()), name);
												for(String name : names) {
													int vote = 0;
													if(votes.containsKey(name)) {
														vote = votes.get(name);
													}
													inventory.addItem(new ItemCreator(ItemUtil.getSkull(name)).addLore("&aVotes: &e" + vote).setName(name).getItemStack());
												}
												player.openInventory(inventory);
											}
										}
									} else {
										MessageHandler.sendUnknownCommand(sender);
									}
								}
							});
						} else {
							MessageHandler.sendMessage(sender, "&cYou can only run this command on HUB1 &e/hub 1");
						}
					} else {
						return false;
					}
				}
				return true;
			}
		}.enableDelay(2);
		if(ProMcGames.getServerName().equals("HUB1")) {
			new Tweeter("XwNjW1ZJTQadiRfkJwvSjd5l5", "fo3P1xcrAWtaGItKPQnuChd3CVJJhu0n0B1DhWmeMe0Y90VUJa", "2395173859-N85rJm20JEN0uVTFzdVFHRQGtU94GRjmnXXusUt", "UB8b9VoCfpurco5y6pv5q7CW2uF4eoCeyMq0DsXwXMkbV");
		}
		EventUtil.register(this);
	}
	
	private void vote(Player player, String name) {
		if(names.isEmpty()) {
			MessageHandler.sendMessage(player, "&cThere is no vote currently active");
		} else {
			name = name.toLowerCase();
			if(names.contains(name)) {
				//private Map<String, Integer> votes = null;
				//private Map<String, String> playerVotes = null;
				String old = playerVotes.get(player.getName());
				if(old != null && votes.containsKey(old)) {
					votes.put(old, votes.get(old) - 1);
					MessageHandler.sendMessage(player, "&c-1 &avotes for " + old);
				}
				playerVotes.put(player.getName(), name);
				int vote = 0;
				if(votes.containsKey(name)) {
					vote = votes.get(name);
				}
				votes.put(name, ++vote);
				MessageHandler.sendMessage(player, "&e+1 &avotes for " + name);
			} else {
				MessageHandler.sendMessage(player, "&c" + name + " is not an option to vote for");
			}
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			String name = event.getItemTitle();
			vote(player, name);
			player.closeInventory();
			event.setCancelled(true);
		}
	}
}
