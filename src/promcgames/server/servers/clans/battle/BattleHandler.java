package promcgames.server.servers.clans.battle;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.FiveSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.ChatClickHandler;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.servers.clans.Clan;
import promcgames.server.servers.clans.ClanHandler;
import promcgames.server.servers.clans.ClanHandler.ClanRank;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class BattleHandler implements Listener {
	private static List<PendingBattle> pendingBattles = null;
	private static List<BattleRequest> battleRequests = null;
	private static int nextRandomClan = -1;
	
	public BattleHandler() {
		pendingBattles = new ArrayList<PendingBattle>();
		battleRequests = new ArrayList<BattleRequest>();
		EventUtil.register(this);
	}
	
	public static List<PendingBattle> getPendingBattles() {
		return pendingBattles;
	}
	
	public static void add(PendingBattle battle) {
		pendingBattles.add(battle);
	}
	
	public static void remove(PendingBattle battle) {
		battle.remove();
		pendingBattles.remove(battle);
	}
	
	public static PendingBattle getPendingBattle(String user) {
		for(PendingBattle pendingBattle : getPendingBattles()) {
			if(pendingBattle.getFounderOne().equalsIgnoreCase(user) || pendingBattle.getFounderTwo().equalsIgnoreCase(user)) {
				return pendingBattle;
			}
		}
		return null;
	}

	public static BattleRequest getBattleRequest(Player player) {
		Clan clan = ClanHandler.getClan(player);
		if(clan != null && clan.getFounderName().equalsIgnoreCase(player.getName())) {
			for(BattleRequest br : battleRequests) {
				if(br.getRequester().getClanName().equalsIgnoreCase(clan.getClanName()) || br.getRequestee().getClanName().equalsIgnoreCase(clan.getClanName())) {
					return br;
				}
			}
		}
		return null;
	}
	
	public static BattleRequest getPendingBattleRequest(String clanName) {
		for(BattleRequest br : battleRequests) {
			if(br.getRequestee().getClanName().equalsIgnoreCase(clanName)) {
				return br;
			}
		}
		return null;
	}
	
	public static BattleRequest getSentBattleRequest(String clanName) {
		for(BattleRequest br : battleRequests) {
			if(br.getRequester().getClanName().equalsIgnoreCase(clanName)) {
				return br;
			}
		}
		return null;
	}
	
	public static int getNextRandomClan() {
		return nextRandomClan;
	}
	
	public static boolean isClanBattleRunning(String clanName) {
		int id = ClanHandler.getClanID(clanName);
		return DB.NETWORK_CLANS_BATTLES.isKeySet("clan_one_id", id + "") || DB.NETWORK_CLANS_BATTLES.isKeySet("clan_two_id", id + "");
	}
	
	/*
	 * The method which is ran when the command /clan findBattle is executed.
	 */
	public static void findBattle(Player player) {
		Clan clan = ClanHandler.getClan(player);
		if(clan != null) {
			if(clan.getRank(player) == ClanRank.FOUNDER || clan.getRank(player) == ClanRank.GENERAL) {
				boolean setString = false;
				if(nextRandomClan == -1) {
					setString = true;
				} else if(nextRandomClan == clan.getClanID()) {
					nextRandomClan = -1;
					MessageHandler.sendMessage(player, "You are no longer looking for a battle");
				} else {
					Clan otherClan = ClanHandler.getClan(nextRandomClan);
					if(otherClan == null) {
						setString = true;
					} else {
						Player otherFounder = ProPlugin.getPlayer(otherClan.getFounderName());
						if(otherFounder != null) {
							new PendingBattle(player.getName(), otherFounder.getName());
							MessageHandler.sendMessage(player, "You have been paired up with the clan " + otherClan.getColorTheme() + otherClan.getClanName());
							MessageHandler.sendMessage(player, "Now looking for a clan battle server. This may take time");
							MessageHandler.sendMessage(otherFounder, "You have been paired up with the clan " + clan.getColorTheme() + clan.getClanName());
							MessageHandler.sendMessage(otherFounder, "Now looking for a clan battle server. This may take time");
						} else {
							setString = true;
						}
					}
				}
				if(setString) {
					if(clan.getUserCount() == 1) {
						MessageHandler.sendMessage(player, "&cYou're the only one in your clan");
						MessageHandler.sendMessage(player, "&cInvite someone before going into battle &b/clan invte");
					} else {
						nextRandomClan = clan.getClanID();
						MessageHandler.sendMessage(player, "You are now looking for a battle");
					}
				}
				ProMcGames.getSidebar().update();
			} else {
				MessageHandler.sendMessage(player, "&cYou must be a clan &bFOUNDER &cor &bGENERAL &cto run this command");
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou aren't in a clan");
		}
	}
	
	/*
	 * The method which is ran when the command /clan openForBattle is executed.
	 */
	public static void openForBattle(Player player) {
		Clan clan = ClanHandler.getClan(player);
		if(clan != null) {
			if(clan.getRank(player) == ClanRank.FOUNDER || clan.getRank(player) == ClanRank.GENERAL) {
				if(clan.getForBattle()) {
					clan.setForBattle(false);
					MessageHandler.sendMessage(player, "You are no longer listed for battle");
				} else {
					if(clan.getUserCount() == 1) {
						MessageHandler.sendMessage(player, "&cYou're the only one in your clan");
						MessageHandler.sendMessage(player, "&cInvite someone before going into battle &b/clan invte");
					} else {
						clan.setForBattle(true);
						MessageHandler.sendMessage(player, "You are now listed for battle");
					}
					//MessageHandler.sendMessage(player, "To not be listed for battle, run this command again");
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be a clan &bFOUNDER &cor &bGENERAL &cto run this command");
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou aren't in a clan");
		}
	}
	
	/*
	 * The method which is ran when the command /clan listBattles is executed.
	 */
	public static void listBattles(Player player) {
		List<Clan> clans = new ArrayList<Clan>(); // clans open for battle
		for(Clan clan : clans) {
			if(clan.getForBattle()) {
				clans.add(clan);
			}
		}
		if(!clans.isEmpty()) {
			MessageHandler.sendMessage(player, "&a&lClans for Battle:");
			for(Clan clan : clans) {
				ChatClickHandler.sendMessageToRunCommand(player, "&6Click to send a battle request", "Click to send a battle request", "/clan battle " + clan.getClanName(), clan.getColorTheme() + clan.getClanName() + " ");
			}
		} else {
			MessageHandler.sendMessage(player, "There are currently no clans available for battle");
		}
	}
	
	/*
	 * The method which is ran when the command /clan battle is executed.
	 */
	public static void battle(Player player, Player opponent) {
		Clan clan = ClanHandler.getClan(player);
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cYou are not in a clan");
		} else {
			if(clan.getRank(player) == ClanRank.FOUNDER || clan.getRank(player) == ClanRank.GENERAL) {
				Clan clanToBattle = ClanHandler.getClan(opponent);
				if(clanToBattle == null) {
					MessageHandler.sendMessage(player, "&b" + AccountHandler.getPrefix(opponent) + " &cdoes not have a clan");
				}
				if(clanToBattle == null) {
					MessageHandler.sendMessage(player, "&cCould not find " + AccountHandler.getPrefix(opponent) + "&c's clan");
				} else {
					if(clan.getClanID() == clanToBattle.getClanID()) {
						MessageHandler.sendMessage(player, "&cYou can't send a battle request to yourself");
					} else {
						if(clan.getUserCount() == 1) {
							MessageHandler.sendMessage(player, "&cYou're the only one in your clan");
							MessageHandler.sendMessage(player, "&cInvite someone before going into battle &b/clan invte");
						} else {
							if(clanToBattle.getForBattle()) {
								if(getPendingBattleRequest(player.getName()) == null) {
									if(getSentBattleRequest(player.getName()) == null) {
										final String name = player.getName();
										final String opponentName = opponent.getName();
										final Clan finalClan = clanToBattle;
										final BattleRequest br = new BattleRequest(clanToBattle, clan, player, opponent);
										battleRequests.add(br);
										new DelayedTask(new Runnable() {
											@Override
											public void run() {
												if(battleRequests.contains(br)) {
													battleRequests.remove(br);
													Player player = ProPlugin.getPlayer(name);
													if(player != null) {
														PendingBattle battle = getPendingBattle(player.getName());
														if(battle == null) {
															remove(battle);
														} else {
															MessageHandler.sendMessage(player, "The battle request sent to " + finalClan.getColorTheme() + finalClan.getClanName() + " &ahas expired");
														}
													}
													Player opponent = ProPlugin.getPlayer(opponentName);
													if(opponent != null) {
														PendingBattle battle = getPendingBattle(opponent.getName());
														if(battle == null) {
															MessageHandler.sendMessage(opponent, "The battle request from " + AccountHandler.getPrefix(player) + " &ahas expired");
														} else {
															remove(battle);
														}
													}
												}
											}
										}, 20 * 60);
										MessageHandler.sendMessage(player, "A battle request has been sent to " + clanToBattle.getColorTheme() + clanToBattle.getClanName());
										MessageHandler.sendMessage(player, "This request will expire in &b60 &aseconds");
										MessageHandler.sendLine(opponent, "&b");
										MessageHandler.sendMessage(opponent, "Battle request from: " + AccountHandler.getPrefix(player));
										MessageHandler.sendMessage(opponent, "Clan: " + clan.getColorTheme() + clan.getClanName());
										ChatClickHandler.sendMessageToRunCommand(opponent, "&6Click to accept", "Click to accept", "/clan acceptBattle " + name);
										MessageHandler.sendMessage(opponent, "  &r&m---------");
										ChatClickHandler.sendMessageToRunCommand(opponent, "&6Click to deny", "Click to deny", "/clan denyBattle");
										MessageHandler.sendMessage(opponent, "This battle request will expire in &b60 &aseconds");
										ChatClickHandler.sendMessageToRunCommand(opponent, "&6Click to toggle battle requests", "Click to toggle battle requests", "/clan openForBattle", "&cDo not want battle requests? ");
										MessageHandler.sendLine(opponent, "&b");
									} else {
										MessageHandler.sendMessage(player, "&cThis clan is currently waiting on a battle request");
									}
								} else {
									MessageHandler.sendMessage(player, "&cThis clan currently has a pending battle request");
								}
							} else {
								MessageHandler.sendMessage(player, "&cThis clan is not open for battle");
							}
						}
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be a clan &bFOUNDER &cor &bGENERAL &cto run this command");
			}
		}
	}
	
	/*
	 * The method which is ran when the command /clan acceptBattle is executed.
	 */
	public static void acceptBattle(Player player, Player challenger) {
		Clan clan = ClanHandler.getClan(player);
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cYou are not in a clan");
		} else {
			if(clan.getRank(player) == ClanRank.FOUNDER || clan.getRank(player) == ClanRank.GENERAL) {
				BattleRequest br = getPendingBattleRequest(clan.getClanName());
				if(br == null) {
					MessageHandler.sendMessage(player, "&cYour clan currently does not have a battle request");
				} else {
					Clan otherClan = br.getRequester();
					if(otherClan == null) {
						MessageHandler.sendMessage(player, "&cAn unexpected error occured");
					} else {
						new PendingBattle(player.getName(), challenger.getName());
						MessageHandler.sendMessage(challenger, "Your battle request to " + clan.getColorTheme() + clan.getClanName() + " &ahas been accepted");
						MessageHandler.sendMessage(challenger, "Now looking for a clan battle server. This may take time");
						MessageHandler.sendMessage(player, "You have accepted the battle request from " + otherClan.getColorTheme() + otherClan.getClanName());
						MessageHandler.sendMessage(player, "Now looking for a clan battle server. This may take time");
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be a clan &bFOUNDER &cto run this command");
			}
		}
	}
	
	/*
	 * The method which is ran when the command /clan denyBattle is executed.
	 */
	public static void denyBattle(Player player) {
		Clan clan = ClanHandler.getClan(player);
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cYou are not in a clan");
		} else {
			if(clan.getFounderName().equalsIgnoreCase(player.getName())) {
				BattleRequest br = getPendingBattleRequest(clan.getClanName());
				if(br == null) {
					MessageHandler.sendMessage(player, "&cYour clan currently does not have a battle request");
				} else {
					battleRequests.remove(br);
					Clan otherClan = br.getRequester();
					if(otherClan == null) {
						MessageHandler.sendMessage(player, "You have denied the battle request");
					} else {
						MessageHandler.sendMessage(player, "You have denied the battle request from " + otherClan.getColorTheme() + otherClan.getClanName());
						Player founder = ProPlugin.getPlayer(otherClan.getFounderName());
						if(founder != null) {
							MessageHandler.sendMessage(founder, "The battle request sent to " + clan.getColorTheme() + clan.getClanName() + " &ahas been denied");
						}
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be a clan &bFOUNDER &cto run this command");
			}
		}
	}
	
	@EventHandler
	public void onFiveSecondTask(FiveSecondTaskEvent event) {
		if(!pendingBattles.isEmpty()) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					List<String> availableServers = DB.NETWORK_CLANS_SETUP.getAllStrings("server_name", "setup_phase", "WAITING");
					int index = 0;
					for(String server : availableServers) {
						final PendingBattle pendingBattle;
						try {
							pendingBattle = pendingBattles.get(index++);
						} catch(IndexOutOfBoundsException e) {
							break;
						}
						Player player1 = ProPlugin.getPlayer(pendingBattle.getFounderOne());
						Player player2 = ProPlugin.getPlayer(pendingBattle.getFounderTwo());
						if(player1 == null || player2 == null) {
							if(player1 != null) {
								MessageHandler.sendMessage(player1, "The pending battle against &b" + pendingBattle.getFounderTwo() + "'s &aclan has been cancelled");
							} else if(player2 != null) {
								MessageHandler.sendMessage(player2, "The pending battle against &b" + pendingBattle.getFounderOne() + "'s &aclan has been cancelled");
							}
							pendingBattles.remove(pendingBattle);
							break;
						} else {
							DB.NETWORK_CLANS_SETUP.updateString("player1", player1.getUniqueId().toString(), "server_name", server.toUpperCase());
							DB.NETWORK_CLANS_SETUP.updateString("player2", player2.getUniqueId().toString(), "server_name", server.toUpperCase());
							ProPlugin.sendPlayerToServer(player1, server);
							ProPlugin.sendPlayerToServer(player2, server);
							new DelayedTask(new Runnable() {
								@Override
								public void run() {
									Player player1 = ProPlugin.getPlayer(pendingBattle.getFounderOne());
									Player player2 = ProPlugin.getPlayer(pendingBattle.getFounderTwo());
									if(player1 == null || player2 == null) {
										pendingBattles.remove(pendingBattle);
									} else {
										MessageHandler.sendMessage(player1, "&cNo open server found, checking again soon...");
										MessageHandler.sendMessage(player2, "&cNo open server found, checking again soon...");
									}
								}
							}, 20 * 3);
							break;
						}
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		Clan clan = ClanHandler.getClan(player);
		if(clan != null && clan.getFounderName().equalsIgnoreCase(player.getName())) {
			clan.setForBattle(false);
			try {
				if(nextRandomClan == clan.getClanID()) {
					nextRandomClan = -1;
					ProMcGames.getSidebar().update();
				}
			} catch(NullPointerException e) {
				
			}
		}
		BattleRequest battleRequest = getBattleRequest(player);
		if(battleRequest != null) {
			String message = null;
			Player founder = null;
			if(battleRequest.getRequestee() == clan) {
				Clan otherClan = battleRequest.getRequester();
				if(otherClan != null) {
					founder = ProPlugin.getPlayer(otherClan.getFounderName());
					if(founder != null) {
						message = "The battle request sent to " + clan.getColorTheme() + clan.getClanName() + " &ahas expired";
					}
				}
			} else if(battleRequest.getRequester() == clan) {
				Clan otherClan = battleRequest.getRequestee();
				if(otherClan != null) {
					founder = ProPlugin.getPlayer(otherClan.getFounderName());
					if(founder != null) {
						message = "The battle request from " + clan.getColorTheme() + clan.getClanName() + " &ahas expired";
					}
				}
			}
			if(message != null && founder != null) {
				MessageHandler.sendMessage(founder, null);
			}
		}
		PendingBattle pendingBattle = getPendingBattle(player.getName());
		if(pendingBattle != null) {
			remove(pendingBattle);
			Player otherFounder = null;
			if(pendingBattle.getFounderOne().equalsIgnoreCase(player.getName())) {
				otherFounder = ProPlugin.getPlayer(pendingBattle.getFounderTwo());
			} else if(pendingBattle.getFounderTwo().equalsIgnoreCase(player.getName())) {
				otherFounder = ProPlugin.getPlayer(pendingBattle.getFounderOne());
			}
			if(otherFounder != null) {
				MessageHandler.sendMessage(otherFounder, "The pending battle between your clan and " + clan.getColorTheme() + clan.getClanName() + " &ahas been cancelled because &b" + player.getName() + " &aleft the server");
			}
		}
	}
}
