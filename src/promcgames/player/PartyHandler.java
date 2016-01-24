package promcgames.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import promcgames.customevents.player.AsyncPlayerJoinEvent;
import promcgames.customevents.player.AsyncPostPlayerJoinEvent;
import promcgames.customevents.player.PartyChangeServerEvent;
import promcgames.customevents.player.PartyDeleteEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerPartyEvent;
import promcgames.customevents.player.PlayerPartyEvent.PartyEvent;
import promcgames.customevents.player.PlayerServerJoinEvent;
import promcgames.player.account.AccountHandler;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class PartyHandler implements Listener {
	public static class Party {
		private int ID = 0;
		private String leader = null;
		private List<String> members = null;
		
		public Party(Player leader) {
			this.leader = leader.getName();
			if(!parties.containsKey(this.leader)) {
				parties.put(this.leader, this);
			}
			final UUID uuid = leader.getUniqueId();
			final Party party = this;
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					if(DB.NETWORK_PARTIES.isKeySet("leader_uuid", uuid.toString())) {
						ID = DB.NETWORK_PARTIES.getInt("leader_uuid", uuid.toString(), "id");
						if(members == null) {
							members = new ArrayList<String>();
						}
						for(String memberUUIDString : DB.NETWORK_PLAYER_PARTIES.getAllStrings("uuid", "party_id", String.valueOf(ID))) {
							String name = AccountHandler.getName(UUID.fromString(memberUUIDString));
							if(!getLeader().getName().equals(name)) {
								members.add(name);
								parties.put(name, party);
							}
						}
					} else {
						DB.NETWORK_PARTIES.insert("'" + uuid.toString() + "'");
						ID = DB.NETWORK_PARTIES.getInt("leader_uuid", uuid.toString(), "id");
					}
					if(DB.NETWORK_PLAYER_PARTIES.isUUIDSet(uuid)) {
						DB.NETWORK_PLAYER_PARTIES.updateInt("party_id", getID(), "uuid", uuid.toString());
					} else {
						DB.NETWORK_PLAYER_PARTIES.insert("'" + uuid.toString() + "', '" + getID() + "'");
					}
				}
			});
		}
		
		public int getID() {
			return ID;
		}
		
		public Player getLeader() {
			return ProPlugin.getPlayer(leader);
		}
		
		public boolean isLeader(Player player) {
			return isLeader(player.getName());
		}
		
		public boolean isLeader(String name) {
			return leader != null && leader.equals(name);
		}
		
		public boolean isMember(String name) {
			return members != null && members.contains(name);
		}
		
		public List<String> getMembers() {
			return this.members;
		}
		
		public List<Player> getPlayers() {
			List<Player> players = new ArrayList<Player>();
			Player leader = getLeader();
			if(leader != null) {
				players.add(leader);
			}
			if(members != null) {
				for(String member : getMembers()) {
					Player player = ProPlugin.getPlayer(member);
					if(player != null) {
						players.add(player);
					}
				}
			}
			return players;
		}
		
		public void sendMessage(String message) {
			for(Player player : getPlayers()) {
				MessageHandler.sendMessage(player, message);
			}
		}
		
		public void addMember(Player member) {
			if(this.members == null) {
				this.members = new ArrayList<String>();
			}
			members.add(member.getName());
			parties.put(member.getName(), this);
			invites.remove(member.getName());
			sendMessage(AccountHandler.getPrefix(member) + " &ehas joined the party");
			final UUID uuid = member.getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					if(DB.NETWORK_PLAYER_PARTIES.isUUIDSet(uuid)) {
						DB.NETWORK_PLAYER_PARTIES.updateInt("party_id", getID(), "uuid", uuid.toString());
					} else {
						DB.NETWORK_PLAYER_PARTIES.insert("'" + uuid.toString() + "', '" + getID() + "'");
					}
				}
			});
		}
		
		public void removeMember(Player player, final LeaveReason leave, boolean fromRemove) {
			if(leave == LeaveReason.KICKED) {
				sendMessage(AccountHandler.getPrefix(player) + " &chas been kicked from the party");
			} else if(leave == LeaveReason.LEAVE){
				sendMessage(AccountHandler.getPrefix(player) + " &chas left the party");
			}
			if(isLeader(player) && members != null && !members.isEmpty()) {
				Player newLeader = ProPlugin.getPlayer(members.get(0));
				if(newLeader != null) {
					promote(newLeader, true);
				}
			}
			if(members != null) {
				members.remove(player.getName());
			}
			if(parties != null) {
				parties.remove(player.getName());
			}
			if(getSize() <= 1 && !fromRemove) {
				remove(leave);
			}
			long delay = leave == LeaveReason.SERVER ? 20 * 5 : 1;
			final UUID uuid = player.getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					if(uuid != null) {
						if(leave == LeaveReason.SERVER && (DB.PLAYERS_LOCATIONS.isUUIDSet(uuid) || DB.STAFF_ONLINE.isUUIDSet(uuid))) {
							return;
						}
						DB.NETWORK_PLAYER_PARTIES.deleteUUID(uuid);
						if(DB.NETWORK_PARTIES.isKeySet("leader_uuid", uuid.toString())) {
							int ID = DB.NETWORK_PARTIES.getInt("leader_uuid", uuid.toString(), "id");
							DB.NETWORK_PLAYER_PARTIES.delete("party_id", String.valueOf(ID));
							DB.NETWORK_PARTIES.delete("leader_uuid", uuid.toString());
						}
					}
				}
			}, delay);
		}
		
		public void removeMember(String name, final LeaveReason leave, boolean fromRemove) {
			Player player = ProPlugin.getPlayer(name);
			if(player == null) {
				if(leave == LeaveReason.KICKED) {
					sendMessage("&c" + name + " has been kicked from the party");
				} else if(leave == LeaveReason.LEAVE){
					sendMessage("&c" + name + " has left the party");
				}
				if(isLeader(name) && members != null && !members.isEmpty()) {
					Player newLeader = ProPlugin.getPlayer(members.get(0));
					if(newLeader != null) {
						promote(newLeader, true);
					}
				}
				members.remove(name);
				parties.remove(name);
				if(getSize() == 1 && !fromRemove) {
					remove(leave);
				}
				long delay = leave == LeaveReason.SERVER ? 20 * 5 : 1;
				final String finalName = name;
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						UUID uuid = AccountHandler.getUUID(finalName);
						if(uuid != null) {
							if(leave == LeaveReason.SERVER && (DB.PLAYERS_LOCATIONS.isUUIDSet(uuid) || DB.STAFF_ONLINE.isUUIDSet(uuid))) {
								return;
							}
							DB.NETWORK_PLAYER_PARTIES.deleteUUID(uuid);
							if(DB.NETWORK_PARTIES.isKeySet("leader_uuid", uuid.toString())) {
								int ID = DB.NETWORK_PARTIES.getInt("id", "uuid", uuid.toString());
								DB.NETWORK_PLAYER_PARTIES.delete("party_id", String.valueOf(ID));
							}
						}
					}
				}, delay);
			} else {
				removeMember(player, leave, fromRemove);
			}
		}
		
		public void promote(Player member, boolean removed) {
			if(!removed) {
				members.add(getLeader().getName());
			}
			members.remove(member.getName());
			leader = member.getName();
			sendMessage(AccountHandler.getPrefix(member) + " &ehas been promoted to the party leader");
		}
		
		public int getSize() {
			List<Player> players = getPlayers();
			return players == null || players.isEmpty() ? 0 : players.size();
		}
		
		public void remove(LeaveReason leave) {
			Bukkit.getPluginManager().callEvent(new PartyDeleteEvent(this));
			sendMessage("&4Your party has been deleted");
			if(members != null) {
				Iterator<String> iterator = members.iterator();
				while(iterator.hasNext()) {
					String member = iterator.next();
					removeMember(member, leave, true);
					invites.remove(member);
				}
				members.clear();
				members = null;
			}
			if(leader != null) {
				removeMember(leader, leave, true);
				invites.remove(leader);
				leader = null;
			}
		}
	}
	
	private static Map<String, Party> parties = null;
	private static Map<String, Party> invites = null;
	private static int requestDelay = 60;
	public static enum LeaveReason {LEAVE, KICKED, SERVER, OTHER};
	
	public PartyHandler() {
		parties = new HashMap<String, Party>();
		invites = new HashMap<String, Party>();
		new CommandBase("party", 0, 2, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length >= 1) {
					Player player = (Player) sender;
					final String playerName = player.getName();
					String option = arguments[0].toLowerCase();
					if(option.equals("invite") && arguments.length >= 2) {
						if(parties.containsKey(player.getName()) && !parties.get(player.getName()).isLeader(player)) {
							MessageHandler.sendMessage(player, "&cOnly Party leaders can invite players to the party");
							return true;
						}
						final String targetName = arguments[1];
						Player target = ProPlugin.getPlayer(targetName);
						if(target == null) {
							MessageHandler.sendMessage(player, "&c" + targetName + " is not online");
						} else if(parties.containsKey(target.getName())){
							MessageHandler.sendMessage(player, AccountHandler.getPrefix(target) + " &cis already in a party");
						} else if(invites.containsKey(target.getName())) {
							MessageHandler.sendMessage(player, AccountHandler.getPrefix(target) + " &calready has a pending party invite");
						} else if(player.getName().equals(target.getName())) {
							MessageHandler.sendMessage(player, "&cYou cannot invite yourself to a party");
						} else {
							if(DB.PLAYERS_DISABLED_PARTY_INVITES.isUUIDSet(target.getUniqueId())) {
								MessageHandler.sendMessage(player, AccountHandler.getPrefix(target) + " &chas party invites disabled");
							} else {
								PlayerPartyEvent event = new PlayerPartyEvent(player, PartyEvent.CREATE);
								Bukkit.getPluginManager().callEvent(event);
								if(!event.isCancelled()) {
									final Party party = parties.containsKey(player.getName()) ? parties.get(player.getName()) : new Party(player);
									event = new PlayerPartyEvent(player, PartyEvent.ADD_PLAYER, party);
									Bukkit.getPluginManager().callEvent(event);
									if(!event.isCancelled()) {
										parties.put(player.getName(), party);
										invites.put(target.getName(), party);
										party.sendMessage(AccountHandler.getPrefix(target) + " &ehas been invited to your party");
										MessageHandler.sendLine(target, "&5");
										MessageHandler.sendMessage(target, "You have been invited to a party by " + AccountHandler.getPrefix(player));
										MessageHandler.sendMessage(target, "You have &c" + requestDelay + " &aseconds to reply");
										ChatClickHandler.sendMessageToRunCommand(target, "&a&lCLICK TO ACCEPT", "Click to accept the invite", "/party accept");
										MessageHandler.sendMessage(target, "");
										ChatClickHandler.sendMessageToRunCommand(target, "&a&lCLICK TO DENY", "Click to deny the invite", "/party deny");
										MessageHandler.sendMessage(target, "");
										ChatClickHandler.sendMessageToRunCommand(target, " &c&lCLICK TO TOGGLE PARTY INVITES", "Click to toggle party invites", "/party toggleInvites", "&cDon't want invites?");
										MessageHandler.sendLine(target, "&5");
										new DelayedTask(new Runnable() {
											@Override
											public void run() {
												if(invites.containsKey(targetName)) {
													if(party.getSize() == 1) {
														party.remove(LeaveReason.OTHER);
													}
													invites.remove(targetName);
													Player player = ProPlugin.getPlayer(playerName);
													if(player != null) {
														MessageHandler.sendMessage(player, "&c" + targetName + "'s party invite has expired");
													}
													Player target = ProPlugin.getPlayer(targetName);
													if(target != null) {
														MessageHandler.sendMessage(target, "&c" + playerName + "'s party invite has expired");
													}
												}
											}
										}, 20 * requestDelay);
									}
								}
							}
						}
						return true;
					} else if(option.equals("accept")) {
						if(invites.containsKey(player.getName())) {
							Party party = invites.get(player.getName());
							party.addMember(player);
						} else {
							MessageHandler.sendMessage(player, "&cYou do not have a pending party invite");
						}
						return true;
					} else if(option.equals("deny")) {
						if(invites.containsKey(player.getName())) {
							Party party = invites.get(player.getName());
							MessageHandler.sendMessage(party.getLeader(), AccountHandler.getPrefix(player) + " &chas denied your party invite");
							if(party.getSize() == 1) {
								party.remove(LeaveReason.OTHER);
							}
							MessageHandler.sendMessage(player, "You have denied the party invite");
							invites.remove(player.getName());
						} else {
							MessageHandler.sendMessage(player, "&cYou do not have a pending party invite");
						}
						return true;
					} else if(option.equals("toggleinvites")) {
						if(DB.PLAYERS_DISABLED_PARTY_INVITES.isUUIDSet(player.getUniqueId())) {
							DB.PLAYERS_DISABLED_PARTY_INVITES.deleteUUID(player.getUniqueId());
							MessageHandler.sendMessage(player, "Party invites &eON");
						} else {
							DB.PLAYERS_DISABLED_PARTY_INVITES.insert("'" + player.getUniqueId().toString() + "'");
							MessageHandler.sendMessage(player, "Party invites &cOFF");
						}
						return true;
					} else if(option.equals("promote") && arguments.length == 2) {
						if(parties.containsKey(player.getName())) {
							Party party = parties.get(player.getName());
							Player target = ProPlugin.getPlayer(arguments[1]);
							if(target == null) {
								MessageHandler.sendMessage(player, "&c" + arguments[1] + " is not online");
							} else if(party.isMember(target.getName())) {
								if(party.isLeader(player)) {
									party.promote(target, false);
								} else {
									MessageHandler.sendMessage(player, "&cYou must be the party leader to promote a member");
								}
							} else {
								MessageHandler.sendMessage(player, AccountHandler.getPrefix(target) + " &cis not in your party");
							}
						} else {
							MessageHandler.sendMessage(player, "&cYou are not in a party");
						}
						return true;
					} else if(option.equals("kick") && arguments.length == 2) {
						if(parties.containsKey(player.getName())) {
							Party party = parties.get(player.getName());
							if(party.isLeader(player)) {
								String target = arguments[1];
								if(party.isMember(target)) {
									party.removeMember(target, LeaveReason.KICKED, false);
								} else {
									MessageHandler.sendMessage(player, "&c" + target + " is not in your party");
								}
							} else {
								MessageHandler.sendMessage(player, "&cYou must be the party leader to kick someone");
							}
						} else {
							MessageHandler.sendMessage(player, "&cYou are not in a party");
						}
						return true;
					} else if(option.equals("leave")) {
						if(parties.containsKey(player.getName())) {
							parties.get(player.getName()).removeMember(player, LeaveReason.LEAVE, false);
						} else {
							MessageHandler.sendMessage(player, "&cYou are not in a party");
						}
						return true;
					} else if(option.equals("disband")) {
						if(parties.containsKey(player.getName())) {
							Party party = parties.get(player.getName());
							if(party.isLeader(player)) {
								party.remove(LeaveReason.OTHER);
							} else {
								MessageHandler.sendMessage(player, "&cYou must be the party leader to disband the party");
							}
						} else {
							MessageHandler.sendMessage(player, "&cYou are not in a party");
						}
						return true;
					} else if(option.equals("list")) {
						if(parties.containsKey(player.getName())) {
							Party party = parties.get(player.getName());
							String message = "Party members: ";
							Player leader = party.getLeader();
							if(leader != null) {
								message += "&c" + party.getLeader().getName();
							}
							if(party.getMembers() != null && !party.getMembers().isEmpty()) {
								for(String member : party.getMembers()) {
									message += "&f, " + member;
								}
							}
							MessageHandler.sendMessage(player, message);
						} else {
							MessageHandler.sendMessage(player, "&cYou are not in a party");
						}
						return true;
					}
				}
				MessageHandler.sendLine(sender);
				MessageHandler.sendMessage(sender, "/party &7- &eDisplays information about the party system");
				MessageHandler.sendMessage(sender, "/party invite <name> &7- &eInvites a player to your party");
				MessageHandler.sendMessage(sender, "/party accept &7- &eAccepts your current party invite");
				MessageHandler.sendMessage(sender, "/party deny &7- &eDenies your current party invite");
				MessageHandler.sendMessage(sender, "/party toggleInvites &7- &eToggles allowing party invites");
				MessageHandler.sendMessage(sender, "/party promote <name> &7- &ePromotes another player to leader");
				MessageHandler.sendMessage(sender, "/party kick <name> &7- &eKick a player from the party");
				MessageHandler.sendMessage(sender, "/party leave &7- &eLeaves your current party");
				MessageHandler.sendMessage(sender, "/party disband &7- &eDeletes your party");
				MessageHandler.sendMessage(sender, "/party list &7- &eLists all players in your current party");
				MessageHandler.sendLine(sender);
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static Party getParty(Player player) {
		if(parties != null && parties.containsKey(player.getName())) {
			return parties.get(player.getName());
		}
		return null;
	}
	
	public static List<Party> getParties() {
		return new ArrayList<Party>(parties.values());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerServerJoin(PlayerServerJoinEvent event) {
		if(!event.isCancelled()) {
			Player player = event.getPlayer();
			if(parties.containsKey(player.getName())) {
				final Party party = parties.get(player.getName());
				if(party.isLeader(player)) {
					PartyChangeServerEvent partyChangeServerEvent = new PartyChangeServerEvent(player, party);
					Bukkit.getPluginManager().callEvent(partyChangeServerEvent);
					if(!partyChangeServerEvent.isCancelled()) {
						final String server = event.getServer();
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								for(Player member : party.getPlayers()) {
									ProPlugin.sendPlayerToServer(member, server, true);
								}
							}
						});
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(DB.NETWORK_PARTIES.isKeySet("leader_uuid", player.getUniqueId().toString())) {
			new Party(player);
		}
	}
	
	@EventHandler
	public void onAsyncPostPlayerJoin(AsyncPostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(!parties.containsKey(player.getName()) && DB.NETWORK_PLAYER_PARTIES.isUUIDSet(player.getUniqueId())) {
			int ID = DB.NETWORK_PLAYER_PARTIES.getInt("uuid", player.getUniqueId().toString(), "party_id");
			for(Party party : parties.values()) {
				if(ID == party.getID()) {
					if(!party.isMember(player.getName())) {
						party.addMember(player);
					}
					parties.put(player.getName(), party);
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(parties.containsKey(player.getName())) {
			parties.get(player.getName()).removeMember(player, LeaveReason.SERVER, false);
		}
		invites.remove(player.getName());
	}
}
