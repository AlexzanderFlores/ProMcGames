package promcgames.server.servers.clans.invites;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.ChatClickHandler;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.servers.clans.Clan;
import promcgames.server.servers.clans.ClanHandler;
import promcgames.server.servers.clans.ClanHandler.ClanRank;
import promcgames.server.servers.clans.ClanHandler.ClanStatus;
import promcgames.server.servers.clans.ScoreboardHandlerx;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class InviteHandler implements Listener {
	private static List<Invite> invites = null;
	
	public InviteHandler() {
		invites = new ArrayList<Invite>();
		EventUtil.register(this);
	}
	
	/*
	 * The method which is ran when the command /clan setInvitePermission is executed.
	 */
	public static void setInvitePermission(Player player, String rank) {
		Clan clan = ClanHandler.getClan(player);
		if(clan != null) {
			if(clan.getRank(player) == ClanRank.FOUNDER) {
				ClanRank clanRank = null;
				try {
					clanRank = ClanRank.valueOf(rank.toUpperCase());
				} catch(IllegalArgumentException e) {
					clanRank = null;
				}
				if(clanRank != null) {
					if(clan.getInvitePerm() == clanRank) {
						MessageHandler.sendMessage(player, "&cThe invite permission is already set to &b" + clanRank.toString());
					} else {
						clan.setInvitePerm(clanRank);
						MessageHandler.sendMessage(player, "The invite permission has been set to &b" + clanRank.toString());
					}
				} else {
					MessageHandler.sendMessage(player, "&b" + rank + "&c is not a valid rank. Valid ranks are: &bMEMBER, GENERAL, FOUNDER");
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be a clan &bFOUNDER &cto run this command");
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou are not in a clan");
		}
	}
	
	/*
	 * Gets the invite permission of a clan from the clan name.
	 */
	public static ClanRank getInvitePerm(String clanName) {
		if(ClanHandler.doesClanExist(clanName)) {
			ClanRank clanRank = ClanRank.valueOf(DB.NETWORK_CLANS.getString("clan_name", clanName, "invite_perm"));
			if(clanRank != null) {
				return clanRank;
			}
		}
		return ClanRank.FOUNDER;
	}
	
	/*
	 * Gets the Invite object associated with a Player. The Player is the invitee.
	 */
	public static Invite getInvite(Player player) {
		for(Invite invite : invites) {
			if(invite.getInvitee().equalsIgnoreCase(player.getName())) {
				return invite;
			}
		}
		return null;
	}
	
	/*
	 * Checks if the Player has a pending invite.
	 */
	public static boolean hasInvite(Player player) {
		return getInvite(player) != null;
	}
	
	/*
	 * Checks from the database if the Player has invites toggled on.
	 */
	public static boolean areInvitesOn(Player player) {
		if(DB.PLAYERS_CLANS_INVITES.isKeySet("uuid", player.getUniqueId().toString())) {
			return DB.PLAYERS_CLANS_INVITES.getBoolean("uuid", player.getUniqueId().toString(), "invitesOn");
		} else {
			DB.PLAYERS_CLANS_INVITES.insert("'" + player.getUniqueId().toString() + "', '1'");
			return true;
		}
	}
	
	/*
	 * The method which is ran when /clan invite <player> is executed.
	 */
	public static void invitePlayer(Player player, final String user) {
		final Clan clan = ClanHandler.getClan(player);
		if(clan != null) {
			if(clan.getClanStatus() == ClanStatus.OPEN) {
				MessageHandler.sendMessage(player, "The clan status is &2&lOPEN");
				MessageHandler.sendMessage(player, "Anyone can do &b/clan join " + clan.getClanName() + " &ato join your clan");
			} else {
				if(clan.hasInvitePerm(player)) {
					if(clan.getClanStatus() == ClanStatus.CLOSED) {
						MessageHandler.sendMessage(player, "&cThe clan status is currently &4&lCLOSED");
						MessageHandler.sendMessage(player, "&cThe clan status must be &b&lINVITE &r&cto invite players");
					} else {
						Player invitee = ProPlugin.getPlayer(user);
						if(invitee != null) {
							if(clan.isInClan(invitee)) {
								MessageHandler.sendMessage(player, AccountHandler.getPrefix(invitee) + " &cis already in your clan");
							} else if(areInvitesOn(invitee)) {
								if(!ClanHandler.isClanBlocked(invitee.getUniqueId(), clan)) {
									if(hasInvite(invitee)) {
										MessageHandler.sendMessage(player, AccountHandler.getPrefix(invitee) + " &cstill has an invite they need to respond to. Try again later");
									} else {
										invites.add(new Invite(player.getName(), user, clan.getClanName()));
										final String name = player.getName();
										final String inviteeName = invitee.getName();
										new DelayedTask(new Runnable() {
											@Override
											public void run() {
												Player invitee = ProPlugin.getPlayer(inviteeName);
												if(invitee != null) {
													Invite invite = getInvite(invitee);
													if(invite != null && invite.getClanName().equalsIgnoreCase(clan.getClanName())) {
														invites.remove(invite);
														if(invitee.isOnline()) {
															MessageHandler.sendMessage(invitee, "The invite from " + clan.getColorTheme() + clan.getClanName() + " &ahas expired");
														}
														Player player = ProPlugin.getPlayer(name);
														if(player != null) {
															MessageHandler.sendMessage(player, "The invite you sent to " + AccountHandler.getPrefix(invitee) + " &ahas expired");
														}
													}
												}
											}
										}, 20 * 60);
										MessageHandler.sendMessage(player, "An invite has been sent to " + AccountHandler.getPrefix(invitee));
										MessageHandler.sendLine(invitee, "&b");
										MessageHandler.sendMessage(invitee, "Invite from: " + AccountHandler.getPrefix(player));
										MessageHandler.sendMessage(invitee, "Clan: " + clan.getColorTheme() + clan.getClanName());
										ChatClickHandler.sendMessageToRunCommand(invitee, "&6Click to accept", "Click to accept", "/clan accept");
										MessageHandler.sendMessage(invitee, "  &r&m---------");
										ChatClickHandler.sendMessageToRunCommand(invitee, "&6Click to deny", "Click to deny", "/clan deny");
										MessageHandler.sendMessage(invitee, "You have &b60 &aseconds to respond");
										ChatClickHandler.sendMessageToRunCommand(invitee, "&6Click to toggle invites", "Click to toggle invites", "/clan toggleinvites", "&cDo not want clan invites? ");
										MessageHandler.sendLine(invitee, "&b");
									}
								} else {
									MessageHandler.sendMessage(player, "&cThis user has your clan blocked");
								}
							} else {
								MessageHandler.sendMessage(player, "&cThis user has invites turned off");
							}
						} else {
							MessageHandler.sendMessage(player, "&cThis user is not online");
						}
					}
				} else {
					clan.sendInvitePermErrorMessage(player);
				}
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou are not in a clan");
		}
	}
	
	/*
	 * The method which is ran when the uninvite command is executed.
	 */
	public static void uninvitePlayer(Player player, String user) {
		Clan clan = ClanHandler.getClan(player);
		if(clan != null) {
			if(clan.hasInvitePerm(player)) {
				Player invitee = ProPlugin.getPlayer(user);
				if(invitee != null) {
					Invite invite = getInvite(invitee);
					if(invite != null && invite.getClanName().equalsIgnoreCase(clan.getClanName())) {
						invites.remove(invite);
						MessageHandler.sendMessage(player, AccountHandler.getPrefix(invitee) + "'s &ainvite has been cancelled");
						MessageHandler.sendMessage(invitee, "The invite from clan " + clan.getColorTheme() + clan.getClanName() + " &ahas been cancelled");
					} else {
						MessageHandler.sendMessage(player, AccountHandler.getPrefix(invitee) + " has no pending invites to cancel");
					}
				} else {
					MessageHandler.sendMessage(player, "&cThis user is not online");
				}
			} else {
				clan.sendInvitePermErrorMessage(player);
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou are not in a clan");
		}
	}
	
	/*
	 * The method which is ran when the command /clan accept is executed.
	 */
	public static void accept(Player player) {
		Invite invite = getInvite(player);
		if(invite != null) {
			if(ClanHandler.isInClan(player)) {
				MessageHandler.sendMessage(player, "&cYou are already in a clan. Use &b/clan leave &cbefore accepting this invite");
			} else {
				Clan clan = ClanHandler.getClan(invite.getClanName());
				clan.sendClanMessage(AccountHandler.getPrefix(player) + " &ahas joined the clan via invite from &b" + AccountHandler.getPrefix(invite.getInviter()));
				clan.joinClan(player);
				MessageHandler.sendMessage(player, "You have successfully joined the clan " + clan.getColorTheme() + clan.getClanName());
				invites.remove(invite);
				ScoreboardHandlerx.updateClanName(player, clan.getColorTheme() + clan.getClanName());
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou do not have any pending invites");
		}
	}
	/*
	 * The method which is ran when the command /clan deny is executed.
	 */
	public static void deny(Player player) {
		Invite invite = getInvite(player);
		if(invite != null) {
			invites.remove(invite);
			Clan clan = ClanHandler.getClan(invite.getClanName());
			MessageHandler.sendMessage(player, "You have denied the invite from the clan " + clan.getColorTheme() + clan.getClanName());
			Player inviter = ProPlugin.getPlayer(invite.getInviter());
			if(inviter != null) {
				MessageHandler.sendMessage(inviter, AccountHandler.getPrefix(player) + " &ahas denied the invite");
			}
		} else {
			MessageHandler.sendMessage(player, "&cYou do not have any pending invites");
		}
	}
	
	/*
	 * The method which is ran when the command /clan toggleInvites is executed.
	 */
	public static void toggleInvites(Player player) {
		boolean newValue = false;
		if(!areInvitesOn(player)) {
			newValue = true;
		}
		DB.PLAYERS_CLANS_INVITES.updateBoolean("invitesOn", newValue, "uuid", player.getUniqueId().toString());
		MessageHandler.sendMessage(player, newValue ? "You will now receive invites for clans" : "You will no longer receive invites for clans");
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		Invite invite = getInvite(player);
		if(invite != null) {
			invites.remove(invite);
		}
	}
}
