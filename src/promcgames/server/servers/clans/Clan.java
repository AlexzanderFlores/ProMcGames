package promcgames.server.servers.clans;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.servers.clans.ClanHandler.ClanRank;
import promcgames.server.servers.clans.ClanHandler.ClanStatus;
import promcgames.server.util.EventUtil;

public class Clan implements Listener {
	private List<String> users = null;
	private int id = 0;
	private int lastNameChange = 0;
	private String clanName = null;
	private String userInitiatedBattle = null;
	private ChatColor colorTheme = null;
	private ClanStatus clanStatus = null;
	private ClanRank invitePerm = null;
	private boolean forBattle = true;
	
	public Clan(String clanName, Player firstUser, ChatColor colorTheme, ClanStatus clanStatus, ClanRank invitePerm, int id, int lastNameChange) { // firstUser is the user which logged into clans resulting this clan to be loaded.
		this.clanName = clanName;
		users = new ArrayList<String>();
		if(firstUser != null) {
			users.add(firstUser.getName());
		}
		this.colorTheme = colorTheme;
		this.clanStatus = clanStatus;
		this.invitePerm = invitePerm;
		this.id = id;
		this.lastNameChange = lastNameChange;
		EventUtil.register(this);
	}
	
	public Clan(String clanName, ChatColor colorTheme, ClanStatus clanStatus, ClanRank invitePerm, int id, int lastNameChange) {
		this(clanName, null, colorTheme, clanStatus, invitePerm, id, lastNameChange);
	}
	
	public void setUserInitiatedBattle(String user) {
		this.userInitiatedBattle = user;
	}
	
	public void setFounder(String user) {
		if(isInClan(user)) {
			DB.PLAYERS_CLANS.updateString("clan_rank", ClanRank.GENERAL.toString(), new String [] {"clan_rank", "clan_id"}, new String [] {ClanRank.FOUNDER.toString(), getClanID() + ""});
			DB.PLAYERS_CLANS.updateString("clan_rank", ClanRank.FOUNDER.toString(), "uuid", AccountHandler.getUUID(user).toString());
		}
	}
	
	public void kickPlayer(String user) {
		users.remove(user);
		DB.PLAYERS_CLANS.deleteUUID(AccountHandler.getUUID(user));
	}
	
	public void sendClanMessage(String message) {
		for(String name : users) {
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				MessageHandler.sendMessage(player, message);
			}
		}
	}
	
	public void addPlayer(Player player) {
		String name = player.getName();
		if(!users.contains(name)) {
			users.add(name);
		}
	}
	
	/*
	 * If violation is true, make sure player is not false or the name change won't be logged correctly.
	 */
	public void setClanName(String newName, boolean violation, Player player) {
		if(newName.length() >= 15) {
			newName = newName.substring(0, 14);
		}
		DB.NETWORK_CLANS.updateString("clan_name", newName, "id", id + "");
		for(String user : getUsers()) {
			Player clanUser = ProPlugin.getPlayer(user);
			if(clanUser != null) {
				ScoreboardHandlerx.updateClanName(clanUser, getColorTheme() + newName);
			}
		}
		sendClanMessage("The clan name has been changed to " + getColorTheme() + newName);
		if(violation) {
			String uuid = "Unknown";
			if(player == null) {
				sendClanMessage("It was changed because it was inappropriate");
			} else {
				uuid = player.getUniqueId().toString();
				sendClanMessage("It was changed by " + AccountHandler.getPrefix(player) + " &abecause it was inappropriate");
			}
			DB.NETWORK_CLANS_NAME_CHANGES.insert("'" + getClanName() + "', '" + newName + "', '" + uuid + "'");
		}
		this.clanName = newName;
	}
	
	public void removePlayer(Player player) {
		users.remove(player.getName());
	}
	
	public void setClanStatus(ClanStatus clanStatus) {
		this.clanStatus = clanStatus;
		DB.NETWORK_CLANS.updateString("status", clanStatus.toString(), "clan_name", getClanName());
	}
	
	public void setColorTheme(ChatColor colorTheme) {
		this.colorTheme = colorTheme;
		DB.NETWORK_CLANS.updateString("color", colorTheme.name(), "clan_name", getClanName());
	}
	
	public void setInvitePerm(ClanRank invitePerm) {
		this.invitePerm = invitePerm;
		DB.NETWORK_CLANS.updateString("invite_perm", invitePerm.toString(), "clan_name", getClanName());
	}
	
	public void setRank(String user, ClanRank clanRank) {
		if(isInClan(user)) {
			DB.PLAYERS_CLANS.updateString("clan_rank", clanRank.toString(), "uuid", AccountHandler.getUUID(user).toString());
		}
	}
	
	public void sendInvitePermErrorMessage(Player player) {
		String msg = "";
		if(getInvitePerm() != ClanRank.FOUNDER) {
			msg = "&cYou must be a &b" + getInvitePerm().toString() + " &cor above to invite/uninvite users!";
		} else {
			msg = "&cYou must be a &bFOUNDER &cto invite/uninvite users!";
		}
		MessageHandler.sendMessage(player, msg);
	}
	
	public void joinClan(Player player) {
		if(!ClanHandler.isInClan(player)) {
			DB.PLAYERS_CLANS.insert("'" + player.getUniqueId().toString() + "', '" + getClanID() + "', '" + ClanRank.MEMBER.toString() + "', '0', '0', '0', '0'");
			users.add(player.getName());
		}
	}
	
	public void setForBattle(boolean forBattle) {
		this.forBattle = forBattle;
	}
	
	public void setLastNameChange(int lastNameChange) {
		this.lastNameChange = lastNameChange;
		DB.NETWORK_CLANS.updateInt("last_name_change", lastNameChange, "id", id + "");
	}
	
	public int getUserCount() {
		return DB.PLAYERS_CLANS.getSize("clan_id", getClanID() + "");
	}
	
	public String getUserInitiatedBattle() {
		return this.userInitiatedBattle;
	}
	
	public String getFounderName() {
		String uuidString = DB.PLAYERS_CLANS.getString(new String [] {"clan_id", "clan_rank"}, new String [] {getClanID() + "", ClanRank.FOUNDER.toString()}, "uuid");
		return AccountHandler.getName(UUID.fromString(uuidString));
	}
	
	public List<Player> getMembers() {
		List<Player> players = new ArrayList<Player>();
		for(String name : getUsers()) {
			Player player = ProPlugin.getPlayer(name);
			if(player != null && getRank(player) == ClanRank.MEMBER) {
				players.add(player);
			}
		}
		return players;
	}
	
	public List<Player> getAllWhoCanBattle() {
		List<Player> players = new ArrayList<Player>();
		for(String name : getUsers()) {
			Player player = ProPlugin.getPlayer(name);
			if(player != null && (getRank(player) == ClanRank.GENERAL || getRank(player) == ClanRank.FOUNDER)) {
				players.add(player);
			}
		}
		return players;
	}
	
	public List<Player> getGenerals() {
		List<Player> players = new ArrayList<Player>();
		for(String name : getUsers()) {
			Player player = ProPlugin.getPlayer(name);
			if(player != null && getRank(player) == ClanRank.GENERAL) {
				players.add(player);
			}
		}
		return players;
	}
	
	public Player getFounder() {
		for(String name : getUsers()) {
			Player player = ProPlugin.getPlayer(name);
			if(player != null && getRank(player) == ClanRank.FOUNDER) {
				return player;
			}
		}
		return null;
	}
	
	public String getClanName() {
		return clanName;
	}
	
	/*
	 * This is only intended for online users.
	 */
	public boolean isPlayerInClan(Player player) {
		return users.contains(player.getName());
	}
	
	public boolean isInClan(Player player) {
		return users.contains(player.getName());
	}
	
	public boolean isInClan(String name) {
		try {
			return DB.PLAYERS_CLANS.isKeySet(new String [] {"uuid", "clan_id"}, new String [] {AccountHandler.getUUID(name).toString(), getClanID() + ""});
		} catch(NullPointerException e) {
			return false;
		}
	}
	
	public boolean hasInvitePerm(Player player) {
		return getRank(player) == getInvitePerm() || getRank(player) == ClanRank.FOUNDER || (getInvitePerm() == ClanRank.MEMBER && getRank(player) == ClanRank.GENERAL);
	}
	
	public boolean getForBattle() {
		return forBattle;
	}
	
	public ClanRank getRank(Player player) {
		if(!isInClan(player)) {
			return null;
		}
		ClanRank rank = ClanRank.valueOf(DB.PLAYERS_CLANS.getString("uuid", player.getUniqueId().toString(), "clan_rank"));
		if(rank != null) {
			return rank;
		} else {
			return ClanRank.MEMBER;
		}
	}
	
	public ClanRank getRank(String user) {
		if(!isInClan(user)) {
			return null;
		}
		UUID uuid = AccountHandler.getUUID(user);
		if(uuid != null) {
			ClanRank rank = ClanRank.valueOf(DB.PLAYERS_CLANS.getString("uuid", uuid.toString(), "clan_rank"));
			if(rank != null) {
				return rank;
			}
		}
		return ClanRank.MEMBER;
	}
	
	public ClanRank getInvitePerm() {
		return invitePerm;
	}
	
	public List<String> getUsers() {
		return users;
	}
	
	public ChatColor getColorTheme() {
		return colorTheme;
	}
	
	public ClanStatus getClanStatus() {
		return clanStatus;
	}
	
	public int getClanID() {
		return id;
	}
	
	public int getLastNameChange() {
		return lastNameChange;
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		users.remove(event.getPlayer().getName());
	}
}