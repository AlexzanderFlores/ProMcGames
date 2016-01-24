package promcgames.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.timed.PlayerOneMinuteConnectedEvent;
import promcgames.gameapi.games.uhc.HostedEvent;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class Friends implements Listener {
	private Map<String, Integer> pages = null;
	private String name = null;
	
	public Friends() {
		pages = new HashMap<String, Integer>();
		name = "Friends";
		new CommandBase("friends", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0) {
					int page = 1;
					if(pages.containsKey(player.getName())) {
						page = pages.get(player.getName());
					}
					open(player, page);
				} else if(arguments.length == 1) {
					String name = arguments[0];
					if(name.equalsIgnoreCase(player.getName())) {
						MessageHandler.sendMessage(player, "&cYou cannot send yourself a friend request");
					} else {
						UUID uuid = AccountHandler.getUUID(name);
						if(uuid == null) {
							MessageHandler.sendMessage(player, "&c" + name + " has never logged in before");
						} else {
							int friends = DB.PLAYERS_FRIENDS.getSize("uuid", player.getUniqueId().toString());
							if(friends >= getSize(player)) {
								MessageHandler.sendMessage(player, "&cYou have the max amount of friends!");
							} else {
								int requests = DB.PLAYERS_FRIEND_REQUESTS.getSize("uuid", player.getUniqueId().toString());
								if(friends + requests >= getSize(player)) {
									MessageHandler.sendMessage(player, "&cYou have too many friends + pending friend requests!");
								} else {
									String [] keys = new String [] {"uuid", "friend"};
									String [] values = new String [] {player.getUniqueId().toString(), uuid.toString()};
									if(DB.PLAYERS_FRIENDS.isKeySet(keys, values)) {
										MessageHandler.sendMessage(player, "&cYou are already friends with " + name);
									} else if(DB.PLAYERS_FRIEND_REQUESTS.isKeySet(new String [] {"friend", "uuid"}, values)) {
										MessageHandler.sendMessage(player, "&cYou already have a pending friend request to " + name);
									} else if(DB.PLAYERS_IGNORES.isKeySet(new String [] {"uuid", "ignored_uuid"}, values)) {
										MessageHandler.sendMessage(player, "&cYou are ignoring " + name);
									} else if(DB.PLAYERS_IGNORES.isKeySet(new String [] {"ignored_uuid", "uuid"}, values)) {
										MessageHandler.sendMessage(player, "&cYou are ignored by " + name);
									} else if(DB.PLAYERS_FRIEND_REQUESTS.getSize("friend", uuid.toString()) >= getSize(player)) {
										MessageHandler.sendMessage(player, "&c" + name + " has too many friend requests currently");
									} else {
										DB.PLAYERS_FRIEND_REQUESTS.insert("'" + uuid.toString() + "', '" + player.getUniqueId().toString() + "'");
										MessageHandler.sendMessage(player, "You've sent a friend request to &e" + name);
									}
								}
							}
						}
					}
				}
				return true;
			}
		}.enableDelay(2);
		EventUtil.register(this);
	}
	
	private int getSize(Player player) {
		Ranks rank = AccountHandler.getRank(player);
		int multiplier = rank == Ranks.PLAYER ? 3 : rank == Ranks.PRO ? 4 : rank == Ranks.PRO_PLUS ? 5 : 6;
		return 9 * multiplier;
	}
	
	private void open(Player player, int page) {
		pages.put(player.getName(), page);
		int size = getSize(player);
		Inventory inventory = Bukkit.createInventory(player, size, name);
		inventory.setItem(2, new ItemCreator(Material.INK_SACK, 10).setName("&aFriends").addLore("&aAdd friends: &f/friend <ign>").getItemStack());
		inventory.setItem(4, new ItemCreator(Material.BOOK_AND_QUILL).setName("&eFriend Requests").addLore("&aAdd friends: &f/friend <ign>").getItemStack());
		inventory.setItem(6, new ItemCreator(Material.BEDROCK).setName("&cIgnored Players").addLore("&aIgnore players with &f/ignore <ign>").getItemStack());
		if(page == 1) {
			inventory.getItem(2).addUnsafeEnchantment(Enchantment.DURABILITY, 1);
			List<String> friends = new ArrayList<String>();
			for(String uuidString : DB.PLAYERS_FRIENDS.getAllStrings("friend", "uuid", player.getUniqueId().toString())) {
				if(friends.size() > size - 9) {
					break;
				} else {
					UUID uuid = UUID.fromString(uuidString);
					Ranks friendRank = AccountHandler.getRank(uuid);
					if(friendRank.isAboveRank(Ranks.HELPER)) {
						friends.add(friendRank.getPrefix() + AccountHandler.getName(uuid) + " &eis &cVANISHED");
					} else {
						if(DB.PLAYERS_LOCATIONS.isUUIDSet(uuid)) {
							friends.add(DB.PLAYERS_LOCATIONS.getString("uuid", uuidString, "location"));
						} else {
							friends.add(friendRank.getPrefix() + AccountHandler.getName(uuid) + " &eis &cOFFLINE");
						}
					}
				}
			}
			if(!friends.isEmpty()) {
				for(int a = 9; a < size && a - 9 < friends.size(); ++a) {
					ItemStack item = new ItemCreator(Material.SKULL_ITEM, 3).addLore("&fJoin: &6Left Click").addLore("&fDelete: &6Right Click").setName(friends.get(a - 9)).getItemStack();
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					String name = friends.get(a - 9);
					if(!name.contains(ChatColor.GRAY.toString())) {
						name = name.split(" ")[1].split(" ")[0];
					}
					meta.setOwner(ChatColor.stripColor(name));
					item.setItemMeta(meta);
					inventory.setItem(a, item);
				}
			}
			friends.clear();
			friends = null;
		} else if(page == 2) {
			inventory.getItem(4).addUnsafeEnchantment(Enchantment.DURABILITY, 1);
			List<String> requests = new ArrayList<String>();
			requests = new ArrayList<String>();
			for(String uuidString : DB.PLAYERS_FRIEND_REQUESTS.getAllStrings("friend", "uuid", player.getUniqueId().toString())) {
				if(requests.size() > size - 9) {
					break;
				} else {
					UUID uuid = UUID.fromString(uuidString);
					Ranks friendRank = AccountHandler.getRank(uuid);
					requests.add(friendRank.getPrefix() + AccountHandler.getName(uuid));
				}
			}
			if(!requests.isEmpty()) {
				for(int a = 9; a <= size && a - 9 < requests.size(); ++a) {
					ItemStack item = new ItemCreator(Material.SKULL_ITEM, 3).setName(requests.get(a - 9)).addLore("&fAdd: &6Left click").addLore("&fDeny: &6Right click").getItemStack();
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					String name = requests.get(a - 9);
					if(!name.contains(ChatColor.GRAY.toString())) {
						if(name.contains("Sr. ")) {
							name = name.replace("Sr. ", "Sr.");
						}
						name = name.split(" ")[1].split(" ")[0];
					}
					meta.setOwner(ChatColor.stripColor(name));
					item.setItemMeta(meta);
					inventory.setItem(a, item);
				}
			}
			requests.clear();
			requests = null;
		} else if(page == 3) {
			inventory.getItem(6).addUnsafeEnchantment(Enchantment.DURABILITY, 1);
			List<UUID> ignores = IgnoreHandler.getIgnores(player);
			if(ignores != null) {
				for(int a = 9; a <= size && a - 9 < ignores.size(); ++a) {
					UUID uuid = ignores.get(a - 9);
					String name = AccountHandler.getName(uuid);
					String prefix = AccountHandler.getRank(uuid).getPrefix() + name;
					String ign = ChatColor.stripColor(prefix.contains(" ") ? prefix.split(" ")[1] : prefix);
					ItemStack item = new ItemCreator(Material.SKULL_ITEM, 3).setName(prefix).addLore("&aUnignore: &d/ignore <" + ign + ">").getItemStack();
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					meta.setOwner(ign);
					item.setItemMeta(meta);
					inventory.setItem(a, item);
				}
			}
			ignores = null;
		}
		player.openInventory(inventory);
	}
	
	@EventHandler
	public void onPlayerOneMinuteConnected(PlayerOneMinuteConnectedEvent event) {
		if(ProMcGames.getPlugin() == Plugins.UHC && HostedEvent.isEvent()) {
			return;
		}
		final String name = event.getPlayer().getName();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Player player = ProPlugin.getPlayer(name);
				int amount = DB.PLAYERS_FRIEND_REQUESTS.getSize("uuid", player.getUniqueId().toString());
				if(amount > 0) {
					MessageHandler.sendMessage(player, "You have &e" + amount + " &apending friend request" + (amount > 1 ? "s" : "") + " &b/friends");
				}
			}
		});
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			if(item.getType() == Material.INK_SACK) {
				open(player, 1);
			} else if(item.getType() == Material.BOOK_AND_QUILL) {
				open(player, 2);
			} else if(item.getType() == Material.BEDROCK) {
				open(player, 3);
			} else {
				String name = event.getItemTitle();
				SkullMeta meta = (SkullMeta) item.getItemMeta();
				if(pages.get(player.getName()) == 1) {
					if(event.getClickType() == ClickType.LEFT) {
						if(name.endsWith("OFFLINE")) {
							MessageHandler.sendMessage(player, "&c" + meta.getOwner() + "&c is offline");
						} else if(name.endsWith("VANISHED")) {
							MessageHandler.sendMessage(player, "&c" + meta.getOwner() + "&c is vanished");
						} else {
							ProPlugin.sendPlayerToServer(player, name.split(ChatColor.RED.toString())[1]);
						}
					} else if(event.getClickType() == ClickType.RIGHT) {
						if(name.contains(ChatColor.GRAY.toString())) {
							name = name.split(" ")[0];
						} else {
							name = name.split(" ")[1].split(" ")[0];
						}
						UUID friendUUID = AccountHandler.getUUID(ChatColor.stripColor(name));
						Bukkit.getLogger().info(ChatColor.stripColor(name));
						String [] values = new String [] {player.getUniqueId().toString(), friendUUID.toString()};
						DB.PLAYERS_FRIENDS.delete(new String [] {"uuid", "friend"}, values);
						DB.PLAYERS_FRIENDS.delete(new String [] {"friend", "uuid"}, values);
						MessageHandler.sendMessage(player, "You have removed " + name + " &aas a friend");
					}
					player.closeInventory();
				} else if(pages.get(player.getName()) == 2) {
					if(event.getClickType() == ClickType.LEFT || event.getClickType() == ClickType.RIGHT) {
						UUID requestUUID = AccountHandler.getUUID(meta.getOwner());
						String [] keys = new String [] {"uuid", "friend"};
						String [] values = new String [] {player.getUniqueId().toString(), requestUUID.toString()};
						DB.PLAYERS_FRIEND_REQUESTS.delete(keys, values);
						if(event.getClickType() == ClickType.LEFT) {
							DB.PLAYERS_FRIENDS.insert("'" + player.getUniqueId().toString() + "', '" + requestUUID.toString() + "'");
							DB.PLAYERS_FRIENDS.insert("'" + requestUUID.toString() + "', '" + player.getUniqueId().toString() + "'");
							MessageHandler.sendMessage(player, "You're now friends with &e" + meta.getOwner());
						} else {
							MessageHandler.sendMessage(player, "You have denied &e" + meta.getOwner() + "&a's &afriend request");
						}
						open(player, 2);
					}
				} else {
					player.closeInventory();
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		pages.remove(event.getPlayer().getName());
	}
}
