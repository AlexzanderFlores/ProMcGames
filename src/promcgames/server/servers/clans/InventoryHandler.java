package promcgames.server.servers.clans;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.MessageHandler;
import promcgames.server.ProPlugin;
import promcgames.server.servers.clans.ClanHandler.ClanRank;
import promcgames.server.servers.clans.ClanHandler.ClanStatus;
import promcgames.server.servers.clans.battle.BattleHandler;
import promcgames.server.servers.clans.invites.InviteHandler;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class InventoryHandler implements Listener {
	private String name = null;
	private static ItemStack item = null;
	
	public InventoryHandler() {
		name = "Clans Main Menu";
		item = new ItemCreator(Material.NETHER_STAR).setName("&a" + name).getItemStack();
		EventUtil.register(this);
	}
	
	public static ItemStack getItem() {
		return item;
	}
	
	private void openMainMenu(Player player) {
		player.openInventory(getMenuInventory(player));
	}
	
	private Inventory getMenuInventory(Player player) {
		int size = 9;
		Clan clan = ClanHandler.getClan(player);
		if(clan != null) {
			size = 18;
			if(clan.getRank(player) == ClanRank.GENERAL) {
				size = 27;
			} else if(clan.getRank(player) == ClanRank.FOUNDER) {
				size = 36;
			}
		}
		Inventory inventory = Bukkit.createInventory(player, size, name);
		inventory.setItem(0, new ItemCreator(Material.PAPER).setName("&bClan Options").getItemStack());
		if(InviteHandler.areInvitesOn(player)) {
			inventory.setItem(1, new ItemCreator(Material.STAINED_GLASS, 5).setName("&bClan Invites").addLore("").addLore("&6Currently &aON").addLore("&6Click to toggle").getItemStack());
		} else {
			inventory.setItem(1, new ItemCreator(Material.STAINED_GLASS, 14).setName("&bClan Invites").addLore("").addLore("&6Currently &cOFF").addLore("&6Click to toggle").getItemStack());
		}
		inventory.setItem(2, new ItemCreator(Material.TNT).setName("&bModify Clan Block List").addLore("").addLore("&6/clan block <Clan name>").addLore("&6/clan unBlock <Clan name>").getItemStack());
		if(size == 9) {
			inventory.addItem(new ItemCreator(Material.SKULL_ITEM).setName("&bCreate a Clan").addLore("").addLore("&6Click to create a clan").getItemStack());
		}
		inventory.setItem(8, new ItemCreator(Material.BOOK).setName("&bClan Help Book").addLore("").addLore("&6For all commands, do:").addLore("&6/clan help").getItemStack());
		if(size >= 18) {
			inventory.setItem(9, new ItemCreator(Material.PAPER).setName("&bClan Member Options").getItemStack());
			inventory.setItem(10, new ItemCreator(Material.BOOK).setName("&bView Current Clan Info").addLore("").addLore("&6Click to view current clan info").getItemStack());
			inventory.setItem(11, new ItemCreator(Material.BOOK).setName("&bView Clan Stats").addLore("").addLore("&6View your current clan stats")
					.addLore("").addLore("&6To view another user's clan").addLore("&6stats, do").addLore("&b/clan viewStats <player>").getItemStack());
			if(clan.getInvitePerm() == ClanRank.MEMBER) {
				inventory.setItem(12, new ItemCreator(Material.SKULL_ITEM, 3).setName("&bInvite a User").addLore("").addLore("&6/clan invite <player name>").getItemStack());
			}
			inventory.setItem(17, new ItemCreator(Material.FIRE).setName("&bLeave Clan").getItemStack());
		}
		if(size >= 27) {
			inventory.setItem(18, new ItemCreator(Material.PAPER).setName("&bClan General Options").getItemStack());
			inventory.setItem(19, new ItemCreator(Material.DIAMOND_SWORD).setName("&bBattle a Clan").getItemStack());
			if(clan.getForBattle()) {
				inventory.setItem(20, new ItemCreator(Material.STAINED_GLASS, 5).setName("&bBattle Requests").addLore("").addLore("&6Currently &aON").addLore("&6Click to toggle").getItemStack());
			} else {
				inventory.setItem(20, new ItemCreator(Material.STAINED_GLASS, 14).setName("&bBattle Requests").addLore("").addLore("&6Currently &cOFF").addLore("&6Click to toggle").getItemStack());
			};
			if(clan.getInvitePerm() == ClanRank.GENERAL) {
				inventory.setItem(21, new ItemCreator(Material.SKULL_ITEM, 3).setName("&bInvite a User").addLore("").addLore("&6/clan invite <player name>").getItemStack());
			}
		}
		if(size >= 36) {
			inventory.setItem(27, new ItemCreator(Material.PAPER).setName("&bClan Founder Options").getItemStack());
			if(BattleHandler.getNextRandomClan() == clan.getClanID()) {
				inventory.setItem(28, new ItemCreator(Material.STAINED_GLASS, 5).setName("&bRandom Clan Battle Search").addLore("").addLore("&6Currently &aON").addLore("")
						.addLore("&6When turned on, this will look").addLore("&6for a random clan to battle").addLore("").addLore("&6Click to toggle").getItemStack());
			} else {
				inventory.setItem(28, new ItemCreator(Material.STAINED_GLASS, 14).setName("&bRandom Clan Battle Search").addLore("").addLore("&6Currently &cOFF").addLore("")
						.addLore("&6When turned on, this will look").addLore("&6for a random clan to battle").addLore("").addLore("&6Click to toggle").getItemStack());
			}
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
				status = "&4CLOSED";
			}
			inventory.setItem(29, new ItemCreator(Material.STAINED_GLASS, data).setName("&bCurrent Clan Status").addLore("").addLore("&6Current: " + status).addLore("&6Click to change clan status").getItemStack());
			ClanRank invitePerm = clan.getInvitePerm();
			Material mat = null;
			if(invitePerm == ClanRank.FOUNDER) {
				mat = Material.DIAMOND;
			} else if(invitePerm == ClanRank.GENERAL) {
				mat = Material.IRON_INGOT;
			} else {
				mat = Material.CLAY_BRICK;
			}
			String perm = invitePerm.toString().toUpperCase();
			inventory.setItem(30, new ItemCreator(mat).setName("&bCurrent Invite Permission").addLore("").addLore("&6Currently: &b" + perm).addLore("").addLore("&6This shows what rank can").addLore("&6invite users to the clan")
					.addLore("").addLore("&6Click to change invite permission").getItemStack());
			inventory.setItem(31, new ItemCreator(Material.TNT).setName("&bKick a User").addLore("").addLore("&6/clan kick <player name>").getItemStack());
			if(clan.getInvitePerm() == ClanRank.FOUNDER) {
				inventory.setItem(32, new ItemCreator(Material.SKULL_ITEM, 3).setName("&bInvite a User").addLore("").addLore("&6/clan invite <player name>").getItemStack());
			}
			inventory.setItem(35, new ItemCreator(Material.FIRE).setName("&bDisband Clan").getItemStack());
		}
		return inventory;
	}
	
	private void openChangeInvitePermInventory(Player player) {
		player.openInventory(getChangeInvitePermInventory(player));
	}
	
	private Inventory getChangeInvitePermInventory(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9, "Choose a new invite permission");
		inventory.setItem(0, new ItemCreator(Material.DIAMOND).setName("&bFOUNDER").addLore("").addLore("&6Only the FOUNDER can invite users").getItemStack());
		inventory.setItem(1, new ItemCreator(Material.IRON_INGOT).setName("&bGENERAL").addLore("").addLore("&6GENERAL and above can invite users").getItemStack());
		inventory.setItem(2, new ItemCreator(Material.CLAY_BRICK).setName("&bMEMBER").addLore("").addLore("&6MEMBER and above can invite users").getItemStack());
		inventory.setItem(8, new ItemCreator(Material.ARROW).setName("&bClick to go back").getItemStack());
		return inventory;
	}
	
	private void openStatusInventory(Player player) {
		player.openInventory(getStatusInventory(player));
	}
	
	private Inventory getStatusInventory(Player player){ 
		Inventory inventory = Bukkit.createInventory(player, 9, "Choose a new clan status");
		inventory.addItem(new ItemCreator(Material.STAINED_GLASS, 5).setName("&2OPEN").addLore("").addLore("&6Anyone can join").getItemStack());
		inventory.addItem(new ItemCreator(Material.STAINED_GLASS, 3).setName("&bINVITE").addLore("").addLore("&6Must receive an invite to join").getItemStack());
		inventory.addItem(new ItemCreator(Material.STAINED_GLASS, 14).setName("&4CLOSED").addLore("").addLore("&6Nobody can join").getItemStack());
		inventory.setItem(8, new ItemCreator(Material.ARROW).setName("&bClick to go back").getItemStack());
		return inventory;
	}
	
	private void openBattlePage(Player player, int page) {
		player.openInventory(getBattlePage(player, page));
	}
	
	private Inventory getBattlePage(Player player, int page) {
		Inventory inventory = Bukkit.createInventory(player, 9 * 6, "Clans For Battle - Page " + page);
		inventory.setItem(4, new ItemCreator(Material.ARROW).setName("&bClick To Go Back").getItemStack());
		List<Clan> clans = new ArrayList<Clan>();
		for(Clan clan : ClanHandler.getClans()) {
			if(!clan.getUsers().contains(player.getName()) && clan.getForBattle()) {
				clans.add(clan);
			}
		}
		if(clans.isEmpty()) {
			inventory.setItem(13, new ItemCreator(Material.SKULL_ITEM, 1).setName("&bNo Clans Open For Battle").addLore("").addLore("&6Try again later").getItemStack());
		} else {
			if(page != 1) {
				inventory.setItem(0, new ItemCreator(Material.PAPER).setName("&6Previous Page").getItemStack());
			}
			if(clans.size() > page * 45) {
				inventory.setItem(8, new ItemCreator(Material.PAPER).setName("&6Next Page").getItemStack());
			}
			for(int index = 9; index <= 53; index++) {
				Clan clan = null;
				try {
					clan = clans.get((index - 9) + (page - 1) * 45);
				} catch(IndexOutOfBoundsException e) {
					break;
				}
				inventory.setItem(index, new ItemCreator(Material.SKULL_ITEM).setName(clan.getColorTheme() + clan.getClanName()).addLore("").addLore("&6Click to battle").getItemStack());
			}
		}
		return inventory;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		HubItemBase.giveOriginalHotBar(event.getPlayer());
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if(item != null) {
			if(item.getType() == Material.NETHER_STAR) {
				openMainMenu(player);
			} else if(item.getType() == Material.DIAMOND_SWORD) {
				player.getInventory().remove(Material.DIAMOND_SWORD);
				ProPlugin.sendPlayerToServer(player, item.getItemMeta().getLore().get(3).replace(ChatColor.AQUA + "/join ", ""));
			}
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals("Clans Main Menu")) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			if(event.getSlot() == 0 || event.getSlot() == 9 || event.getSlot() == 18 || event.getSlot() == 27) {
				openMainMenu(player);
			}
			if(event.getSlot() == 1) { // toggle clan invites
				if(event.getItem().getData().getData() == 5) {
					player.getOpenInventory().setItem(1, new ItemCreator(Material.STAINED_GLASS, 14).setName("&bClan Invites: &cOFF")
							.addLore("").addLore("&6Click to toggle").getItemStack());
				} else {
					player.getOpenInventory().setItem(1, new ItemCreator(Material.STAINED_GLASS, 5).setName("&bClan Invites: &aON")
							.addLore("").addLore("&6Click to toggle").getItemStack());
				}
				player.chat("/clan toggleinvites");
				openMainMenu(player);
			} else if(event.getItemTitle().equals(ChatColor.AQUA + "Create a Clan")) {
				player.closeInventory();
				MessageHandler.sendMessage(player, "To make a clan, do &b/clan create <name>");
			} else if(event.getSlot() == 8) {
				openMainMenu(player);
			} else if(event.getSlot() == 20) { // toggle battle invites
				if(event.getItem().getData().getData() == 5) {
					player.getOpenInventory().setItem(20, new ItemCreator(Material.STAINED_GLASS, 14).setName("&bBattle Requests: &cOFF")
							.addLore("").addLore("&6Click to toggle").getItemStack());
					player.chat("/clan openForBattle");
					openMainMenu(player);
				} else {
					Clan clan = ClanHandler.getClan(player);
					if(clan != null) {
						if(clan.getUserCount() == 1) {
							player.closeInventory();
							MessageHandler.sendMessage(player, "&cYou're the only one in your clan");
							MessageHandler.sendMessage(player, "&cInvite someone before going into battle &b/clan invite");
						} else {
							player.getOpenInventory().setItem(20, new ItemCreator(Material.STAINED_GLASS, 5).setName("&bBattle Requests: &aON")
									.addLore("").addLore("&6Click to toggle").getItemStack());
							player.chat("/clan openForBattle");
							openMainMenu(player);
						}
					}
				}
			} else if(event.getSlot() == 19) { // battle clan
				openBattlePage(player, 1);
			} else if(event.getSlot() == 28) { // battle random clan
				if(event.getItem().getData().getData() == 5) {
					player.getOpenInventory().setItem(29, new ItemCreator(Material.STAINED_GLASS, 14).setName("&bRandom Clan Battle Search")
							.addLore("").addLore("&6Currently &cOFF").addLore("&6Click to toggle").getItemStack());
					player.chat("/clan findBattle");
					openMainMenu(player);
				} else {
					Clan clan = ClanHandler.getClan(player);
					if(clan != null) {
						if(clan.getUserCount() == 1) {
							player.closeInventory();
							MessageHandler.sendMessage(player, "&cYou're the only one in your clan");
							MessageHandler.sendMessage(player, "&cInvite someone before going into battle &b/clan invite");
						} else {
							player.getOpenInventory().setItem(29, new ItemCreator(Material.STAINED_GLASS, 5).setName("&bRandom Clan Battle Search")
									.addLore("").addLore("&6Currently: &aON").addLore("&6Click to toggle").getItemStack());
							player.chat("/clan findBattle");
							openMainMenu(player);
						}
					}
				}
			} else if(event.getSlot() == 17) { // leave clan
				player.closeInventory();
				player.chat("/clan leave");
			} else if(event.getSlot() == 35) { // disband clan
				player.closeInventory();
				player.chat("/clan disband");
			} else if(event.getSlot() == 29) { // set clan status
				openStatusInventory(player);
			} else if(event.getSlot() == 30) { // change invite perm
				openChangeInvitePermInventory(player);
			} else if(event.getSlot() == 2) { // open modify clans block list menu
				openMainMenu(player);
			} else if(event.getItemTitle().equals(ChatColor.AQUA + "Invite a User")) {
				openMainMenu(player);
			} else if(event.getSlot() == 31) {
				openMainMenu(player);
			} else if(event.getSlot() == 32) {
				openMainMenu(player);
			} else if(event.getSlot() == 10) {
				player.chat("/clan info");
			} else if(event.getSlot() == 11) {
				player.closeInventory();
				player.chat("/clan viewstats");
			}
		} else if(event.getTitle().startsWith("Clans For Battle - Page ")) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			int page = Integer.valueOf(event.getTitle().replace("Clans For Battle - Page ", ""));
			if(event.getItem().getType() == Material.SKULL_ITEM) {
				if(event.getItem().getData().getData() == (byte) 0) {
					player.closeInventory();
					String name = ChatColor.stripColor(event.getItemTitle());
					Clan clan = ClanHandler.getClan(name);
					if(clan == null) {
						MessageHandler.sendMessage(player, "&c" + name + " is not a valid clan");
					} else {
						List<Player> online = clan.getAllWhoCanBattle();
						if(online == null || online.isEmpty()) {
							MessageHandler.sendMessage(player, "&c" + name + " has currently no one online who can battle");
						} else {
							player.chat("/clan battle " + online.get(0).getName());
						}
					}
				} else {
					openBattlePage(player, page);
				}
			} else if(event.getSlot() == 0 || event.getSlot() == 8) {
				page += event.getSlot() == 0 ? -1 : 1;
				openBattlePage(player, page);
			} else if(event.getSlot() == 4) {
				openMainMenu(player);
			}
		} else if(event.getTitle().equals("Choose a new clan status")) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			byte data = event.getItem().getData().getData();
			if(data == (byte) 5) {
				player.chat("/clan setstatus open");
			} else if(data == (byte) 3) {
				player.chat("/clan setstatus invite");
			} else if(data == (byte) 14) {
				player.chat("/clan setstatus closed");
			}
			openMainMenu(player);
		} else if(event.getTitle().contains(" Info")) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			String clan = event.getTitle().replace(" Info", "");
			if(event.getItemTitle().contains("View Battle History")) {
				player.chat("/clan viewBattleHistory all " + clan);
			} else if(event.getSlot() == 8) {
				openMainMenu(player);
			} else {
				ClanHandler.info(player, clan);
			}
		} else if(event.getTitle().equals("Choose a new invite permission")) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			if(event.getSlot() == 0) {
				player.chat("/clan setinvitepermission founder");
			} else if(event.getSlot() == 1) {
				player.chat("/clan setinvitepermission general");
			} else if(event.getSlot() == 2) {
				player.chat("/clan setinvitepermission member");
			}
			openMainMenu(player);
		} else if(event.getTitle().contains("'s Battle History")) {
			event.setCancelled(true);
			if(event.getSlot() == 4) {
				Player player = event.getPlayer();
				player.chat("/clan info " + event.getTitle().replace("'s Battle History", ""));
			}
		} else if(event.getTitle().contains("'s Clan Stats")) {
			event.setCancelled(true);
			if(event.getSlot() == 8) {
				Player player = event.getPlayer();
				openMainMenu(player);
			}
		}
	}
}
