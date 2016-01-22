package promcgames.server.servers.hub.items.cosmetic.elite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader.Perk;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class HatItem extends HubItemBase {
	private static HubItemBase instance = null;
	private Map<String, Integer> pages = null;
	private List<Integer> toIgnore = null;

	public HatItem() {
		super(new ItemCreator(Material.SKULL_ITEM, 3).setName(Ranks.ELITE.getColor() + "Hats"), 2);
		instance = this;
		pages = new HashMap<String, Integer>();
		toIgnore = new ArrayList<Integer>();
		toIgnore.add(6);
		toIgnore.add(8);
		toIgnore.add(9);
		toIgnore.add(10);
		toIgnore.add(11);
		toIgnore.add(27);
		toIgnore.add(28);
		toIgnore.add(31);
		toIgnore.add(32);
		toIgnore.add(37);
		toIgnore.add(38);
		toIgnore.add(39);
		toIgnore.add(40);
		toIgnore.add(50);
		toIgnore.add(51);
		toIgnore.add(60);
		toIgnore.add(62);
		toIgnore.add(69);
		toIgnore.add(76);
		toIgnore.add(77);
		toIgnore.add(90);
		toIgnore.add(97);
		toIgnore.add(98);
		toIgnore.add(111);
		toIgnore.add(119);
		toIgnore.add(127);
		toIgnore.add(131);
		toIgnore.add(140);
		toIgnore.add(141);
		toIgnore.add(142);
		toIgnore.add(143);
		toIgnore.add(154);
		toIgnore.add(157);
		toIgnore.add(165);
		toIgnore.add(166);
		toIgnore.add(167);
		toIgnore.add(168);
		toIgnore.add(169);
		for(int a = 175; a <= 396; ++a) {
			toIgnore.add(a);
		}
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}

	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(Ranks.ELITE.hasRank(player)) {
			PerkLoader.addPerkToQueue(player, Perk.HATS);
		}
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = (Player) event.getPlayer();
		if(isItem(player)) {
			if(Ranks.ELITE.hasRank(player)) {
				open(player);
			} else {
				MessageHandler.sendMessage(player, Ranks.ELITE.getNoPermission());
			}
			event.setCancelled(true);
		}
	}

	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(ChatColor.stripColor(getName()))) {
			Player player = event.getPlayer();
			String name = event.getItemTitle();
			if(name != null) {
				if(name.contains("Page ")) {
					pages.put(player.getName(), event.getItem().getAmount());
					open(player);
				} else if(name.endsWith("No Hat")) {
					player.getInventory().setHelmet(new ItemStack(Material.AIR));
					player.closeInventory();
					final UUID uuid = player.getUniqueId();
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							DB.HUB_HATS.deleteUUID(uuid);
						}
					});
				}
			} else {
				player.getInventory().setHelmet(event.getItem());
				player.closeInventory();
				final UUID uuid = player.getUniqueId();
				final String item = event.getItem().getTypeId() + ":" + event.getItem().getData().getData();
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						if(DB.HUB_HATS.isUUIDSet(uuid)) {
							DB.HUB_HATS.updateString("hat", item, "uuid", uuid.toString());
						} else {
							DB.HUB_HATS.insert("'" + uuid.toString() + "', '" + item + "'");
						}
					}
				});
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		pages.remove(event.getPlayer().getName());
	}
	
	private void add(Inventory inventory, int id) {
		add(inventory, id, 0);
	}
	
	private void add(Inventory inventory, int id, int data) {
		ItemStack item = new ItemStack(id, 1, (byte) data);
		if(!inventory.contains(item)) {
			try {
				inventory.addItem(item);
			} catch(Exception e) {
				Bukkit.getLogger().info("ID: " + id);
			}
		}
	}
	
	private void add(Inventory inventory, int id, int data, int dataStop) {
		for(int dataStart = data; dataStart <= dataStop; ++dataStart) {
			add(inventory, id, dataStart);
		}
	}
	
	private void open(Player player) {
		int page = 1;
		if(pages.containsKey(player.getName())) {
			page = pages.get(player.getName());
		}
		Inventory inventory = Bukkit.createInventory(player, 9 * 6, ChatColor.stripColor(getName()));
		if(page == 1) {
			for(int id = 1; id <= 35; ++id) {
				if(toIgnore.contains(id)) {
					continue;
				}
				if(id == 3) {
					add(inventory, id, 0);
					add(inventory, id, 2);
				} else if(id == 5) {
					add(inventory, id, 0, 5);
				} else if(id == 12) {
					add(inventory, id, 0, 1);
				} else if(id == 17) {
					add(inventory, id, 0, 3);
				} else if(id == 18) {
					add(inventory, id, 0, 3);
				} else if(id == 35) {
					add(inventory, id, 0, 8);
				} else {
					add(inventory, id);
				}
			}
		} else if(page == 2) {
			for(int id = 35; id <= 88; ++id) {
				if(toIgnore.contains(id)) {
					continue;
				}
				if(id == 35) {
					add(inventory, id, 9, 15);
				} else if(id == 44) {
					add(inventory, id, 0, 1);
					add(inventory, id, 3, 7);
				}
				else {
					add(inventory, id);
				}
			}
		} else if(page == 3) {
			for(int id = 89; id <= 159; ++id) {
				if(toIgnore.contains(id)) {
					continue;
				}
				if(id == 159) {
					add(inventory, id, 0, 1);
				} else {
					add(inventory, id);
				}
			}
		} else if(page == 4) {
			for(int id = 159; id <= 171; ++id) {
				if(toIgnore.contains(id)) {
					continue;
				}
				if(id == 159) {
					add(inventory, id, 1, 15);
				} else if(id == 160) {
					add(inventory, id, 0, 15);
				} else if(id == 161) {
					add(inventory, id, 0);
					add(inventory, id, 1);
				} else if(id == 162) {
					add(inventory, id, 0);
					add(inventory, id, 1);
				} else if(id == 171) {
					add(inventory, id, 0, 6);
				}
				else {
					add(inventory, id);
				}
			}
		} else if(page == 5) {
			for(int id = 171; id <= 397; ++id) {
				if(toIgnore.contains(id)) {
					continue;
				}
				if(id == 171) {
					add(inventory, id, 6, 15);
				} else if(id == 397) {
					add(inventory, id, 0, 4);
				}
				else {
					add(inventory, id);
				}
			}
		}
		inventory.setItem(inventory.getSize() - 9, new ItemCreator(Material.SKULL_ITEM, 3).setName("&cNo Hat").getItemStack());
		if(page == 1) {
			inventory.setItem(inventory.getSize() - 7, new ItemCreator(Material.SKULL_ITEM, 3).setName("&bPage 1/5").getItemStack());
		} else {
			inventory.setItem(inventory.getSize() - 7, new ItemCreator(Material.SKULL_ITEM).setName("&bPage 1/5").getItemStack());
		}
		if(page == 2) {
			inventory.setItem(inventory.getSize() - 6, new ItemCreator(Material.SKULL_ITEM, 3).setAmount(2).setName("&bPage 2/5").getItemStack());
		} else {
			inventory.setItem(inventory.getSize() - 6, new ItemCreator(Material.SKULL_ITEM).setAmount(2).setName("&bPage 2/5").getItemStack());
		}
		if(page == 3) {
			inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.SKULL_ITEM, 3).setAmount(3).setName("&bPage 3/5").getItemStack());
		} else {
			inventory.setItem(inventory.getSize() - 5, new ItemCreator(Material.SKULL_ITEM).setAmount(3).setName("&bPage 3/5").getItemStack());
		}
		if(page == 4) {
			inventory.setItem(inventory.getSize() - 4, new ItemCreator(Material.SKULL_ITEM, 3).setAmount(4).setName("&bPage 4/5").getItemStack());
		} else {
			inventory.setItem(inventory.getSize() - 4, new ItemCreator(Material.SKULL_ITEM).setAmount(4).setName("&bPage 4/5").getItemStack());
		}
		if(page == 5) {
			inventory.setItem(inventory.getSize() - 3, new ItemCreator(Material.SKULL_ITEM, 3).setAmount(5).setName("&bPage 5/5").getItemStack());
		} else {
			inventory.setItem(inventory.getSize() - 3, new ItemCreator(Material.SKULL_ITEM).setAmount(5).setName("&bPage 5/5").getItemStack());
		}
		player.openInventory(inventory);
	}
}
