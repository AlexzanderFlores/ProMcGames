package promcgames.server.servers.clans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import promcgames.ProPlugin;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.servers.clans.battle.BattleHandler;
import promcgames.server.servers.clans.battle.BattleHistoryHandler;
import promcgames.server.servers.clans.invites.InviteHandler;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.StringUtil;
import promcgames.staff.ViolationPrevention;

public class ClanHandler {
	private static List<Clan> clans = null;
	private static List<String> disbandList = null;
	private static ChatColor [] invalidChatColors = {ChatColor.BOLD, ChatColor.ITALIC, ChatColor.MAGIC, ChatColor.RESET, ChatColor.STRIKETHROUGH, ChatColor.UNDERLINE};
	
	public static enum ClanRank {FOUNDER, GENERAL, MEMBER};
	public static enum ClanStatus {OPEN, INVITE, CLOSED};
	
	public ClanHandler() {
		clans = new ArrayList<Clan>();
		disbandList = new ArrayList<String>();
		new CommandBase("clan", -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0 || arguments[0].equalsIgnoreCase("help")) {
					int page = 1;
					if(arguments.length == 2) {
						try {
							page = Integer.valueOf(arguments[1]);
						} catch(NumberFormatException e) {
							
						}
					}
					MessageHandler.sendLine(player, "&a");
					MessageHandler.sendMessage(player, "&bClans Help Page (&e" + page + "&b/&e" + 4 + "&b)");
					if(page == 1) {
						MessageHandler.sendMessage(player, "&b/clan help &eDisplays clan info");
						MessageHandler.sendMessage(player, "&b/clan create <name> &eCreates a clan");
						MessageHandler.sendMessage(player, "&b/clan leave &eLeaves your current clan");
						MessageHandler.sendMessage(player, "&b/clan setStatus <open | invite | closed> &eSets availability status");
						MessageHandler.sendMessage(player, "&b/clan invite <player> &eInvites a player to your clan");
						MessageHandler.sendMessage(player, "&b/clan unInvite <player> &eCancels a pending invite to your clan");
						MessageHandler.sendMessage(player, "&b/clan join <clan name> &eJoins the clan specified");
						MessageHandler.sendMessage(player, "&b/clan accept &eAccepts your current clan invite");
						MessageHandler.sendMessage(player, "&b/clan deny &eDenies your current clan invite");
					} else if(page == 2) {
						MessageHandler.sendMessage(player, "&b/clan kick <player> &eKick a player from your clan");
						MessageHandler.sendMessage(player, "&b/clan toggleInvites &eToggles clan invites on/off");
						MessageHandler.sendMessage(player, "&b/clan info [clan name | player name] &eDisplays information on a clan");
						MessageHandler.sendMessage(player, "&b/clan battle <player name> &eRequests a clan battle");
						MessageHandler.sendMessage(player, "&b/clan acceptBattle &eAccept a battle request");
						MessageHandler.sendMessage(player, "&b/clan denyBattle &eDeny a battle request");
						MessageHandler.sendMessage(player, "&b/clan listBattles &eDisplays all clans open to battle");
						MessageHandler.sendMessage(player, "&b/clan findBattle &eOpen/Close your clan to a random clan battle");
						MessageHandler.sendMessage(player, "&b/clan openForBattle &eOpen/Close your clan to battle requests");
					} else if(page == 3) {
						MessageHandler.sendMessage(player, "&b/clan setRank <player> <rank> &eSets the rank of a clan member");
						MessageHandler.sendMessage(player, "&b/clan setFounder <player> &ePasses on the founder rank to another user");
						MessageHandler.sendMessage(player, "&b/clan disband &eDisbands the clan. This is &4&lPERMANENT");
						MessageHandler.sendMessage(player, "&b/clan setInvitePermission <rank> &eSets who can invite users");
						MessageHandler.sendMessage(player, "&b/clan setColorTheme <color theme> &eSets the color theme for your clan");
						MessageHandler.sendMessage(player, "&b/clan setName <name> &eSet the name of your clan");
						MessageHandler.sendMessage(player, "&b/clan viewBattleHistory <ranked|unranked|all> [clan] &eView a clans battle history");
						MessageHandler.sendMessage(player, "&b/clan block <clan> &eBlocks a clan from sending you invites");
						MessageHandler.sendMessage(player, "&b/clan unblock <clan> &eUnblocks a clan from sending you invites");
					} else if(page == 4) {
						MessageHandler.sendMessage(player, "&b/clan viewStats [player] &eShows stats for current clan");
						MessageHandler.sendMessage(player, "&b/clan logo <url> &eSubmut a logo for your clan");
					} else {
						MessageHandler.sendMessage(player, "&cPage &e" + page + " &cdoes not exist");
					}
					MessageHandler.sendMessage(player, "&e/clan help [page]");
				} else if(arguments[0].equalsIgnoreCase("create")){
					if(arguments.length == 2) {
						createClan(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan create <name>");
					}
				} else if(arguments[0].equalsIgnoreCase("leave")){
					leaveClan(player);
				} else if(arguments[0].equalsIgnoreCase("setStatus")){
					if(arguments.length != 2) {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan setStatus <open | invite | closed>");
					} else {
						setStatus(player, arguments[1]);
					}
				} else if(arguments[0].equalsIgnoreCase("invite")){
					if(arguments.length != 2) {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan invite <player>");
					} else {
						InviteHandler.invitePlayer(player, arguments[1]);
					}
				} else if(arguments[0].equalsIgnoreCase("unInvite")){
					if(arguments.length != 2) {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan uninvite <player>");
					} else {
						InviteHandler.uninvitePlayer(player, arguments[1]);
					}
				} else if(arguments[0].equalsIgnoreCase("accept")){
					InviteHandler.accept(player);
				} else if(arguments[0].equalsIgnoreCase("deny")){
					InviteHandler.deny(player);
				} else if(arguments[0].equalsIgnoreCase("toggleInvites")){
					InviteHandler.toggleInvites(player);
				} else if(arguments[0].equalsIgnoreCase("info")){
					if(arguments.length == 1) {
						info(player);
					} else if(arguments.length == 2) {
						info(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan info [clan name | player name]");
					}
				} else if(arguments[0].equalsIgnoreCase("battle")){
					if(arguments.length == 2) {
						Player target = ProPlugin.getPlayer(arguments[1]);
						if(target == null) {
							MessageHandler.sendMessage(player, "&c" + arguments[1] + " is not online");
						} else {
							BattleHandler.battle(player, target);
						}
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan battle <player>");
					}
				} else if(arguments[0].equalsIgnoreCase("listBattles")){
					BattleHandler.listBattles(player);
				} else if(arguments[0].equalsIgnoreCase("findBattle")){
					BattleHandler.findBattle(player);
				} else if(arguments[0].equalsIgnoreCase("setRank")){
					if(arguments.length == 3) {
						setRank(player, arguments[1], arguments[2]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan setRank <player> <rank>");
					}
				} else if(arguments[0].equalsIgnoreCase("infractions")){
					MessageHandler.sendMessage(player, "&cNot done yet");
				} else if(arguments[0].equalsIgnoreCase("setColorTheme")) {
					if(arguments.length == 2) {
						setColorTheme(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan setColorTheme <color>");
					}
				} else if(arguments[0].equalsIgnoreCase("setFounder")) {
					if(arguments.length == 2) {
						setFounder(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan setFounder <player>");
					}
				} else if(arguments[0].equalsIgnoreCase("setInvitePermission")) {
					if(arguments.length == 2) {
						InviteHandler.setInvitePermission(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan setInvitePermission <rank>");
					}
				} else if(arguments[0].equalsIgnoreCase("disband")) {
					disbandClan(player);
				} else if(arguments[0].equalsIgnoreCase("join")) {
					if(arguments.length == 2) {
						joinClan(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &e/clan join <clan name>");
					}
				} else if(arguments[0].equalsIgnoreCase("kick")) {
					if(arguments.length == 2) {
						kickPlayer(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &e/clan kick <player>");
					}
				} else if(arguments[0].equalsIgnoreCase("openForBattle")) {
					BattleHandler.openForBattle(player);
				} else if(arguments[0].equalsIgnoreCase("acceptBattle") && arguments.length == 2) {
					Player challenger = ProPlugin.getPlayer(arguments[1]);
					if(challenger == null) {
						MessageHandler.sendMessage(player, "&c" + arguments[1] + " is not online");
					} else {
						BattleHandler.acceptBattle(player, challenger);
					}
				} else if(arguments[0].equalsIgnoreCase("denyBattle")) {
					BattleHandler.denyBattle(player);
				} else if(arguments[0].equalsIgnoreCase("setName")) {
					if(arguments.length == 2) {
						setClanName(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan setName <new clan name>");
					}
				} else if(arguments[0].equalsIgnoreCase("renameClan")) {
					if(Ranks.SENIOR_MODERATOR.hasRank(player)) {
						if(arguments.length == 3) {
							renameClan(player, arguments[1], arguments[2]);
						} else {
							MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan renameClan <clan name> <new clan name>");
						}
					} else {
						MessageHandler.sendMessage(sender, "&cUnknown clan command. Do &b/clan help");
					}
				} else if(arguments[0].equalsIgnoreCase("viewBattleHistory")) {
					if(arguments.length == 3) {
						int filter = -1;
						if(arguments[1].equalsIgnoreCase("all")) {
							filter = 0;
						} else if(arguments[1].equalsIgnoreCase("ranked")) {
							filter = 1;
						} else if(arguments[1].equalsIgnoreCase("unranked")) {
							filter = 2;
						} else {
							MessageHandler.sendMessage(player, "&cInvalid filter. &bFilters include: all, ranked, unranked");
						}
						if(filter > -1) {
							BattleHistoryHandler.viewBattleHistory(player, arguments[2], filter);
						}
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan viewBattleHistory <ranked|unranked|all> [clan]");
					}
				} else if(arguments[0].equalsIgnoreCase("block")) {
					if(arguments.length == 2) {
						blockClan(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan block <clan>");
					}
				} else if(arguments[0].equalsIgnoreCase("unblock")) {
					if(arguments.length == 2) {
						unblockClan(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan unblock <clan>");
					}
				} else if(arguments[0].equalsIgnoreCase("viewstats")) {
					if(arguments.length == 1) {
						StatsHandler.viewStats(player);
					} else if(arguments.length == 2) {
						StatsHandler.viewStats(player, arguments[1]);
					} else {
						MessageHandler.sendMessage(player, "&cIncorrect usage. &b/clan viewStats [player]");
					}
				} else if(arguments[0].equalsIgnoreCase("logo")) {
					LogoHandler.handleCommand(sender, arguments);
				} else {
					MessageHandler.sendMessage(player, "&cUnknown clan command. Do &b/clan help");
				}
				return true;
			}
		};
	}
	
	public static List<Clan> getClans() {
		return clans;
	}
	
	/*
	 * Checks if the clan name is valid for use.
	 */
	private static boolean isClanNameValid(Player player, String clan) {
		for(byte b : clan.getBytes()) {
			if(b <= 47 || (b >= 58 && b <= 64) || (b >= 91 && b <= 96) || b >= 123) {
				MessageHandler.sendMessage(player, "&cYou can only have the following in your clan name: &b[a-z] [A-Z] [0-9]");
				return false;
			}
		}
		if(ViolationPrevention.contains(clan)) {
			MessageHandler.sendMessage(player, "&cYour clan name contains possibly rude or offensive text");
			return false;
		}
		if(clan.length() >= 15) {
			MessageHandler.sendMessage(player, "&cYour clan name is too long. The limit is &b14 characters");
			return false;
		}
		return true;
	}
	
	public static boolean isClanBlocked(UUID uuid, Clan clan) {
		return DB.PLAYERS_CLANS_BLOCKED.isKeySet(new String [] {"uuid", "blocked_clan_id"}, new String [] {uuid.toString(), clan.getClanID() + ""});
	}
	
	public static String getClanPrefix(Player player) {
		Clan clan = getClan(player);
		if(clan != null) {
			String stars = "";
			ClanRank rank = clan.getRank(player);
			if(rank == ClanRank.GENERAL) {
				stars = "*";
			} else if(rank == ClanRank.FOUNDER) {
				stars = "**";
			}
			String prefix = "&b[&1" + stars + clan.getColorTheme() + clan.getClanName() + "&b]";
			return StringUtil.color(prefix);
		}
		return "";
	}
	
	public static String getClanPrefix(String user) {
		String clanName = getClanName(user);
		if(clanName != null) {
			Clan clan = getClan(clanName);
			if(clan != null) {
				String stars = "";
				ClanRank rank = clan.getRank(user);
				if(rank == ClanRank.GENERAL) {
					stars = "*";
				} else if(rank == ClanRank.FOUNDER) {
					stars = "**";
				}
				String prefix = "&b[&1" + stars + clan.getColorTheme() + clan.getClanName() + "&b]";
				return StringUtil.color(prefix);
			}
		}
		return "";
	}
	
	public static int getClanBattleWins(String clanName) {
		if(DB.NETWORK_CLANS.isKeySet("clan_name", clanName)) {
			return DB.NETWORK_CLANS.getInt("clan_name", clanName, "battle_wins");
		} else {
			return -1;
		}
	}
	
	public static int getClanBattleLosses(String clanName) {
		if(DB.NETWORK_CLANS.isKeySet("clan_name", clanName)) {
			return DB.NETWORK_CLANS.getInt("clan_name", clanName, "battle_losses");
		} else {
			return -1;
		}
	}
	
	public static int getClanID(String clanName) {
		if(DB.NETWORK_CLANS.isKeySet("clan_name", clanName)) {
			return DB.NETWORK_CLANS.getInt("clan_name", clanName, "id");
		} else {
			return -1;
		}
	}
	
	public static int getLastNameChange(String clanName) {
		if(DB.NETWORK_CLANS.isKeySet("clan_name", clanName)) {
			return DB.NETWORK_CLANS.getInt("clan_name", clanName, "last_name_change");
		} else {
			return -1;
		}
	}
	
	/*
	 * Gets the Clan object from the name of the clan. If the Clan object doesn't exist yet, it will load it.
	 */
	public static Clan getClan(String clanName) {
		for(Clan clan : clans) {
			if(clan.getClanName().equalsIgnoreCase(clanName)) {
				return clan;
			}
		}
		return loadClan(clanName);
	}
	
	/*
	 * Gets the Clan a Player is in.
	 */
	public static Clan getClan(Player player) {
		if(clans == null) {
			clans = new ArrayList<Clan>();
		}
		if(isInClan(player)) {
			String clanName = getClanName(player);
			if(clanName != null) {
				return getClan(clanName);
			}
		}
		return null;
	}
	
	public static Clan getClan(int id) {
		for(Clan clan : clans) {
			if(clan.getClanID() == id) {
				return clan;
			}
		}
		return loadClan(getClanName(id));
	}
	
	/*
	 * Will load the clan from the database into memory. If the clan does not exist, null will be returned.
	 */
	public static Clan loadClan(String clanName) {
		return loadClan(null, clanName);
	}
	
	public static Clan loadClan(Player firstPlayer, String clanName) {
		if(doesClanExist(clanName)) {
			Clan clan = new Clan(clanName, firstPlayer, getClanColorTheme(clanName), getClanStatus(clanName), InviteHandler.getInvitePerm(clanName), getClanID(clanName), getLastNameChange(clanName));
			if(clans == null) {
				clans = new ArrayList<Clan>();
			}
			clans.add(clan);
			return clan;
		}
		return null;
	}
	
	/*
	 * If a clan is returned with a red D afterwards in parenthesis, the clan has been disbanded.
	 */
	public static String getClanName(int id) {
		if(DB.NETWORK_CLANS.isKeySet("id", id + "")) {
			return DB.NETWORK_CLANS.getString("id", id + "", "clan_name");
		} else if(DB.NETWORK_CLANS_DISBANDED.isKeySet("id", id + "")) {
			return DB.NETWORK_CLANS_DISBANDED.getString("id", id + "", "clan_name") + ChatColor.RED + " (D)";
		}
		return null;
	}
	
	/*
	 * Gets the name of the clan a player is in from the database.
	 */
	public static String getClanName(Player player) {
		if(isInClan(player)) {
			return getClanName(DB.PLAYERS_CLANS.getInt("uuid", player.getUniqueId().toString(), "clan_id"));
		} else {
			return null;
		}
	}
	
	public static String getClanName(String user) {
		if(isInClan(user)) {
			return getClanName(DB.PLAYERS_CLANS.getInt("uuid", AccountHandler.getUUID(user).toString(), "clan_id"));
		} else {
			return null;
		}
	}
	
	/*
	 * Checks if the player is the founder of a clan.
	 */
	public static boolean isRank(Player player, ClanRank rank) {
		return DB.PLAYERS_CLANS.isKeySet(new String [] {"uuid", "clan_rank"}, new String [] {player.getUniqueId().toString(), rank.toString()});
	}
	
	/*
	 * Checks if the Player is in a clan, according to the database.
	 */
	public static boolean isInClan(Player player) {
		return isInClan(player.getUniqueId());
	}
	
	public static boolean isInClan(String user) {
		try {
			return DB.PLAYERS_CLANS.isKeySet("uuid", AccountHandler.getUUID(user).toString());
		} catch(NullPointerException e) {
			return false;
		}
	}
	
	public static boolean isInClan(UUID uuid) {
		return DB.PLAYERS_CLANS.isKeySet("uuid", uuid.toString());
	}
	
	/*
	 * Checks if the clan exists using a clan name, according to the database.
	 */
	public static boolean doesClanExist(String clan) {
		return DB.NETWORK_CLANS.isKeySet("clan_name", clan);
	}
	
	/*
	 * Checks the status of the clan from the clan name.
	 */
	public static ClanStatus getClanStatus(String clanName) {
		if(doesClanExist(clanName)) {
			ClanStatus clanStatus = ClanStatus.valueOf(DB.NETWORK_CLANS.getString("clan_name", clanName, "status"));
			if(clanStatus != null) {
				return clanStatus;
			}
		}
		return ClanStatus.CLOSED;
	}
	
	/*
	 * Checks the color theme of the clan from the clan name.
	 */
	public static ChatColor getClanColorTheme(String clanName) {
		if(doesClanExist(clanName)) {
			ChatColor chatColor = ChatColor.valueOf(DB.NETWORK_CLANS.getString("clan_name", clanName, "color"));
			if(chatColor != null) {
				return chatColor;
			}
		}
		return ChatColor.WHITE;
	}
	
	/*
	 * The method which is ran when /clan disband is executed.
	 */
	public static void disbandClan(final Player player) {
		Clan clan = getClan(player);
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cYou are not in a clan");
		} else {
			if(clan.getFounderName().equalsIgnoreCase(player.getName())) {
				if(disbandList.contains(player.getName())) {
					for(String uuidS : DB.PLAYERS_CLANS.getAllStrings("uuid", "clan_id", clan.getClanID() + "")) {
						UUID uuid = UUID.fromString(uuidS);
						if(uuid != null) {
							String name = AccountHandler.getName(uuid);
							if(name != null) {
								StatsHandler.backupClanStats(name);
							}
						}
					}
					disbandList.remove(player.getName());
					clan.sendClanMessage(AccountHandler.getPrefix(player) + " &ajust disbanded " + clan.getColorTheme() + clan.getClanName());
					for(String user : clan.getUsers()) {
						Player puser = ProPlugin.getPlayer(user);
						if(puser != null) {
							ScoreboardHandlerx.updateClanName(puser, "&fNone");
						}
					}
					Bukkit.getLogger().info("id: " + clan.getClanID());
					Bukkit.getLogger().info("clan name: " + clan.getClanName());
					//DB.NETWORK_CLANS_DISBANDED.insert("'" + clan.getClanID() + "', '" + clan.getClanName() + "'");
					DB.NETWORK_CLANS_DISBANDED.execute("INSERT INTO " + DB.NETWORK_CLANS_DISBANDED.getName() + " (`id`, `clan_name`) VALUES ('" + clan.getClanID() + "', '" + clan.getClanName() + "')");
					DB.PLAYERS_CLANS.delete("clan_id", clan.getClanID() + "");
					DB.NETWORK_CLANS.delete("id", clan.getClanID() + "");
					clans.remove(clan);
				} else {
					if(BattleHandler.isClanBattleRunning(clan.getClanName())) {
						MessageHandler.sendMessage(player, "&cYour clan is currently in a battle. Wait for it to end");
					} else {
						MessageHandler.sendLine(player, "&b");
						MessageHandler.sendMessage(player, "Are you sure you want to disband your clan?");
						MessageHandler.sendMessage(player, "This will delete &4&lEVERYTHING &r&aexcept battle history");
						MessageHandler.sendMessage(player, "If you don't want to be &bFOUNDER &aanymore, pass it on:");
						MessageHandler.sendMessage(player, "&b/clan setFounder <name>");
						//MessageHandler.sendMessage(player, "If you are 100% sure, run this command again");
						ChatClickHandler.sendMessageToRunCommand(player, "&6Click here to confirm disband", "Click here to confirm disband", "/clan disband");
						MessageHandler.sendMessage(player, "In 30 seconds, this will expire");
						MessageHandler.sendLine(player, "&b");
						disbandList.add(player.getName());
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								if(disbandList.contains(player.getName())) {
									disbandList.remove(player.getName());
									if(player.isOnline()) {
										MessageHandler.sendMessage(player, "The clan disband has expired");
									}
								}
							}
						}, 20 * 30);
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be the clan &bFOUNDER &cto use this command");
			}
		}
	}
	
	/*
	 * The method which is ran when /clan create <name> is executed.
	 */
	public static void createClan(Player player, String clanName) {
		if(isInClan(player)) {
			MessageHandler.sendMessage(player, "&cYou are already in a clan, use &b/clan leave &cto leave");
		} else {
			if(isClanNameValid(player, clanName)) {
				if(doesClanExist(clanName)) {
					MessageHandler.sendMessage(player, "&cThe clan " + getClanColorTheme(clanName) + clanName + " &calready exists");
				} else {
					DB.NETWORK_CLANS.insert("'" + clanName + "', '0', '" + ClanStatus.INVITE.toString() + "', '" + ClanRank.FOUNDER.toString() + "', '0', '0', '" + ChatColor.WHITE.name() + "'");
					int clanID = getClanID(clanName);
					DB.PLAYERS_CLANS.insert("'" + player.getUniqueId().toString() + "', '" + clanID + "', '" + ClanRank.FOUNDER.toString() + "', '0', '0', '0', '0'");
					clans.add(new Clan(clanName, player, ChatColor.WHITE, ClanStatus.INVITE, ClanRank.FOUNDER, clanID, 0));
					MessageHandler.alert(AccountHandler.getPrefix(player) + " &acreated the clan &f" + clanName);
					ScoreboardHandlerx.updateClanName(player, ChatColor.WHITE + clanName);
				}
			}
		}
	}
	
	/*
	 * The method which is ran when /clan leave is executed.
	 */
	public static void leaveClan(Player player) {
		Clan clan = getClan(player);
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cYou are not in a clan");
		} else {
			if(clan.getRank(player) == ClanRank.FOUNDER) {
				MessageHandler.sendMessage(player, "&cYou are the clan &bFOUNDER");
				MessageHandler.sendMessage(player, "&cYou must either pass on this rank or disband the clan");
				MessageHandler.sendMessage(player, "&b/clan setFounder <player> &cor &b/clan disband");
			} else {
				StatsHandler.backupClanStats(player.getName());
				DB.PLAYERS_CLANS.deleteUUID(player.getUniqueId());
				removeFromClan(player);
				MessageHandler.sendMessage(player, "You have left the clan " + clan.getColorTheme() + clan.getClanName());
				clan.sendClanMessage(AccountHandler.getPrefix(player) + " &ahas left the clan");
				ScoreboardHandlerx.updateClanName(player, "&fNone");
			}
		}
	}
	
	/*
	 * The method which is ran when /clan status is executed.
	 */
	public static void setStatus(Player player, String status) {
		Clan clan = getClan(player);
		if(clan != null) {
			if(clan.getRank(player) == ClanRank.FOUNDER) {
				ClanStatus clanStatus = ClanStatus.valueOf(status.toUpperCase());
				if(clanStatus != null) {
					clan.setClanStatus(clanStatus);
					String color;
					if(clanStatus == ClanStatus.CLOSED) {
						color = "&4";
					} else if(clanStatus == ClanStatus.INVITE) {
						color = "&b";
					} else {
						color = "&2";
					}
					clan.sendClanMessage("The clan status has been updated to " + color + clanStatus.toString());
				} else {
					MessageHandler.sendMessage(player, "&cInvalid status. Valid statuses are: &bopen&c, &binvite&c, or &bclosed");
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be the clan &bFOUNDER &cto use this command");
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou are not in a clan");
		}
	}
	
	/*
	 * Runs the info method by using the player's own clan.
	 */
	public static void info(Player player) {
		if(isInClan(player)) {
			info(player, getClan(player).getClanName());
		} else {
			MessageHandler.sendMessage(player, "&cYou are not in a clan");
		}
	}
	
	/*
	 * The method which is ran when the command /clan info is executed.
	 */
	public static void info(Player player, String clanName) {
		Clan clan = getClan(clanName);
		if(clan == null) {
			Player target = ProPlugin.getPlayer(clanName);
			if(target != null) {
				clan = getClan(target);
			}
		}
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cThe clan &b" + clanName + " &cdoesn't exist");
		} else {
			Inventory inv = Bukkit.createInventory(player, 9, clan.getClanName() + " Info");
			try {
				inv.addItem(new ItemCreator(Material.SKULL_ITEM).setName("&aFounder: ").addLore(AccountHandler.getPrefix(clan.getFounderName())).getItemStack());
			} catch(NullPointerException e) {
				MessageHandler.sendMessage(player, "Could not display this clan's information");
				player.closeInventory();
				return;
			}
			inv.addItem(new ItemCreator(Material.SKULL_ITEM, 1).setName("&aTotal Members: &b" + clan.getUserCount()).getItemStack());
			int data = 0;
			String status = "";
			ClanStatus clanStatus = clan.getClanStatus();
			if(clanStatus == ClanStatus.OPEN) {
				data = 5;
				status = "&2OPEN";
			} else if(clanStatus == ClanStatus.INVITE) {
				data = 3;
				status = "&bINVITE";
			} else if(clanStatus == ClanStatus.CLOSED) {
				data = 14;
			}
			inv.addItem(new ItemCreator(Material.STAINED_GLASS, data).setName("&aClan Status: " + status).getItemStack());
			inv.addItem(new ItemCreator(Material.DIAMOND_SWORD).setName("&aBattle Stats").addLore("")
					.addLore("&bWins: " + DB.NETWORK_CLANS.getInt("clan_name", clan.getClanName(), "battle_wins"))
					.addLore("&bLosses: " + DB.NETWORK_CLANS.getInt("clan_name", clan.getClanName(), "battle_losses")).getItemStack());
			inv.addItem(new ItemCreator(Material.STAINED_GLASS, clan.getForBattle() ? 5 : 14).setName("&aBattle Requests: " + (clan.getForBattle() ? "&bON" : "&cOFF")).getItemStack());
			inv.addItem(new ItemCreator(Material.BOOK).setName("&aView Battle History").addLore("").addLore("&6Click to view battle history").getItemStack());
			inv.setItem(8, new ItemCreator(Material.ARROW).setName("&bClick to go back").getItemStack());
			if(player.getOpenInventory() != null) {
				player.closeInventory();
			}
			player.openInventory(inv);
		}
	}
	
	/*
	 * The method which is ran when the command /clan setRank is executed.
	 */
	public static void setRank(Player player, String user, String rank) {
		Clan clan = getClan(player);
		if(clan != null) {
			if(clan.getRank(player) == ClanRank.FOUNDER) {
				if(player.getName().equalsIgnoreCase(user)) {
					MessageHandler.sendMessage(player, "&cYou can't set your own rank. If you don't want to be &bFOUNDER &canymore, use &b/clan setFounder");
				} else {
					if(clan.isInClan(user)) {
						ClanRank clanRank = null;
						try {
							clanRank = ClanRank.valueOf(rank.toUpperCase());
						} catch(IllegalArgumentException e) {
							clanRank = null;
						}
						if(clanRank == null) {
							MessageHandler.sendMessage(player, "&b" + rank + "&c is an invalid rank. Valid ranks are: &bMEMBER, GENERAL");
						} else {
							if(clanRank == ClanRank.FOUNDER) {
								MessageHandler.sendMessage(player, "&cTo pass on ownership of the clan, use &b/clan setFounder");
							} else {
								clan.setRank(user, clanRank);
								MessageHandler.sendMessage(player, AccountHandler.getPrefix(user) + " &ahas been set to a &b" + clanRank.toString());
								Player userP = ProPlugin.getPlayer(user);
								if(userP != null) {
									MessageHandler.sendMessage(userP, "Your rank has been set to &b" + clanRank.toString() + " &afor the clan " + clan.getColorTheme() + clan.getClanName());
								}
							}
						}
					} else {
						MessageHandler.sendMessage(player, AccountHandler.getPrefix(user) + "&c is not in your clan");
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be a clan &bFOUNDER &cto run this command");
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou aren't in a clan");
		}
	}
	
	/*
	 * The method which is ran when the command /clan setColorTheme is executed.
	 */
	public static void setColorTheme(Player player, String color) {
		Clan clan = getClan(player);
		if(clan != null) {
			if(clan.getRank(player) == ClanRank.FOUNDER) {
				ChatColor chatColor = null;
				try {
					chatColor = ChatColor.valueOf(color.toUpperCase());
				} catch(IllegalArgumentException e) {
					chatColor = null;
				}
				if(isInvalidColor(chatColor)) {
					MessageHandler.sendMessage(player, "&b" + color + " &cis an invalid color. Valid colors are:");
					for(ChatColor colors : ChatColor.values()) {
						if(!isInvalidColor(colors)) {
							MessageHandler.sendMessage(player, colors + colors.name());
						}
					}
				} else {
					clan.setColorTheme(chatColor);
					clan.sendClanMessage("The color theme has been updated to " + chatColor + chatColor.name());
					for(String user : clan.getUsers()) {
						Player member = ProPlugin.getPlayer(user);
						if(member != null) {
							ScoreboardHandlerx.updateClanName(member, clan.getColorTheme() + clan.getClanName());
						}
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be a clan &bFOUNDER &cto run this command");
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou aren't in a clan");
		}
	}
	
	/*
	 * The method which is ran when the command /clan setFounder is executed.
	 */
	public static void setFounder(Player player, String user) {
		Clan clan = getClan(player);
		if(clan != null) {
			if(clan.getRank(player) == ClanRank.FOUNDER) {
				if(user.equalsIgnoreCase(player.getName())) {
					MessageHandler.sendMessage(player, "&cYou can't run this command on yourself");
				} else {
					if(clan.isInClan(user)) {
						clan.setFounder(user);
						clan.sendClanMessage("Ownership of the clan has been passed on to " + AccountHandler.getPrefix(user));
						MessageHandler.sendMessage(player, "You have been set to &bGENERAL");
					} else {
						MessageHandler.sendMessage(player, AccountHandler.getPrefix(user) + " &cis not in your clan");
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be a clan &bFOUNDER &cto run this command");
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou aren't in a clan");
		}
	}
	
	/*
	 * The method which is ran when the command /clan join is executed.
	 */
	public static void joinClan(Player player, String clanName) {
		Clan clan = getClan(player);
		if(clan != null) {
			if(clan.getClanName().equalsIgnoreCase(clanName)) {
				MessageHandler.sendMessage(player, "&cYou are already in this clan");
			} else {
				MessageHandler.sendMessage(player, "&cYou are already in a clan. Use &b/clan leave &cbefore joining another clan");
			}
		} else {
			Clan clanToJoin = getClan(clanName);
			if(clanToJoin == null) {
				MessageHandler.sendMessage(player, "&b" + clanToJoin + " &cdoes not exist");
			} else {
				ClanStatus clanStatus = clanToJoin.getClanStatus();
				if(clanStatus == ClanStatus.OPEN) {
					clanToJoin.sendClanMessage(AccountHandler.getPrefix(player) + " &ahas joined the clan");
					clanToJoin.joinClan(player);
					MessageHandler.sendMessage(player, "You have successfully joined " + clanToJoin.getColorTheme() + clanToJoin.getClanName());
					ScoreboardHandlerx.updateClanName(player, clanToJoin.getColorTheme() + clanToJoin.getClanName());
				} else if(clanStatus == ClanStatus.INVITE) {
					MessageHandler.sendMessage(player, "&cThis clan is currently on &bINVITE &cstatus. Request an invite first");
				} else {
					MessageHandler.sendMessage(player, "&cThis clan is currently &4CLOSED");
				}
			}
		}
	}
	
	/*
	 * The method which is ran when the command /clan kick is executed.
	 */
	public static void kickPlayer(Player player, String user) {
		Clan clan = getClan(player);
		if(clan != null) {
			if(clan.hasInvitePerm(player)) {
				if(user.equalsIgnoreCase(player.getName())) {
					MessageHandler.sendMessage(player, "&cYou can't kick yourself");
				} else {
					if(clan.isInClan(user)) {
						clan.kickPlayer(user);
						MessageHandler.sendMessage(player, AccountHandler.getPrefix(user) + " &ahas been kicked by " + AccountHandler.getPrefix(player));
						Player playerToKick = ProPlugin.getPlayer(user);
						if(playerToKick != null) {
							ScoreboardHandlerx.updateClanName(playerToKick, "&fNone");
							MessageHandler.sendMessage(playerToKick, "You have been kicked from " + clan.getColorTheme() + clan.getClanName() + " &aby " + AccountHandler.getPrefix(player));
						}
					} else {
						MessageHandler.sendMessage(player, "&b" + user + " &cis not in your clan");
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou do not have the correct clan rank to do this");
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou aren't in a clan");
		}
	}
	
	public static void setClanName(Player player, String newName) {
		Clan clan = getClan(player);
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cYou are not in a clan");
		} else {
			if(clan.getFounderName().equalsIgnoreCase(player.getName())) {
				if(Ranks.PRO_PLUS.hasRank(player)) {
					int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
					if(clan.getLastNameChange() == 0 || clan.getLastNameChange() != currentMonth) {
						if(isClanNameValid(player, newName)) {
							Clan other = getClan(newName);
							if(other != null) {
								MessageHandler.sendMessage(player, "&cThis name is already taken. Choose another name");
							} else {
								clan.setClanName(newName, false, null);
								clan.setLastNameChange(currentMonth);
							}
						}
					} else {
						MessageHandler.sendMessage(player, "&cYou can change a clan name once a month");
					}
				} else {
					MessageHandler.sendMessage(player, "You must be " + Ranks.PRO_PLUS.getPrefix() + " &cor above to rename a clan");
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be a clan &bFOUNDER &cto run this command");
			}
		}
	}
	
	public static void renameClan(Player player, String clanName, String newName) {
		Clan clan = getClan(clanName);
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cThis clan does not exist");
		} else {
			if(isClanNameValid(player, newName)) {
				Clan other = getClan(newName);
				if(other == null) {
					clan.setClanName(newName, true, player);
					MessageHandler.sendMessage(player, "The clan has been renamed to " + clan.getColorTheme() + clan.getClanName());
				} else {
					MessageHandler.sendMessage(player, "&cThis name is already taken. Choose another name");
				}
			}
		}
	}
	
	public static void blockClan(Player player, String clanName) {
		Clan clan = getClan(clanName);
		if(clan == null) {
			Player target = ProPlugin.getPlayer(clanName);
			if(target != null) {
				clan = getClan(target);
			}
		}
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cThis clan doesn't exist");
		} else {
			Clan ownClan = getClan(player);
			if(ownClan != null && ownClan.getClanID() == clan.getClanID()) {
				MessageHandler.sendMessage(player, "&cYou can't block the clan you are in");
			} else if(isClanBlocked(player.getUniqueId(), clan)) {
				MessageHandler.sendMessage(player, "&cThis clan is already blocked");
				MessageHandler.sendMessage(player, "To unblock a clan do &f/clan unblock <clan>");
			} else {
				DB.PLAYERS_CLANS_BLOCKED.insert("'" + player.getUniqueId().toString() + "', '" + clan.getClanID() + "'");
				MessageHandler.sendMessage(player, clan.getColorTheme() + clan.getClanName() + " &ahas been blocked");
			}
		}
	}
	
	public static void unblockClan(Player player, String clanName) {
		Clan clan = getClan(clanName);
		if(clan == null) {
			Player target = ProPlugin.getPlayer(clanName);
			if(target != null) {
				clan = getClan(target);
			}
		}
		if(clan == null) {
			MessageHandler.sendMessage(player, "&cThis clan doesn't exist");
		} else {
			if(isClanBlocked(player.getUniqueId(), clan)) {
				DB.PLAYERS_CLANS_BLOCKED.delete(new String [] {"uuid", "blocked_clan_id"}, new String [] {player.getUniqueId().toString(), "" + clan.getClanID()});
				MessageHandler.sendMessage(player, clan.getColorTheme() + clan.getClanName() + " &ahas been unblocked");
			} else {
				MessageHandler.sendMessage(player, "&cYou don't have this clan blocked");
			}
		}
	}
	
	/*
	 * Removes the player from the List of users of the clan they are in.
	 */
	public static void removeFromClan(Player player) {
		for(Clan clan : clans) {
			if(clan.isInClan(player)) {
				clan.removePlayer(player);
			}
		}
	}
	
	/*
	 * Checks if the ChatColor is invalid for use in a clan name.
	 */
	private static boolean isInvalidColor(ChatColor chatColor) {
		if(chatColor == null) {
			return true;
		}
		for(ChatColor color : invalidChatColors) {
			if(color == chatColor) {
				return true;
			}
		}
		return false;
	}
}
