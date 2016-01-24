package promcgames.gameapi.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.game.PostGameStartEvent;
import promcgames.customevents.player.AsyncPostPlayerJoinEvent;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.EmeraldsHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.servers.slave.Voting;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

public class KitShop implements Listener {
	private static List<String> delayed = null;
	private static int delay = 1;
	private static KitShop recentShop = null;
	private String title = null;
	private ItemStack item = null;
	private int minimumPrice = 0;
	private int size = 9 * 6;
	private boolean giveShopItem = true;
	
	public KitShop() {
		this("Kit Shop");
	}
	
	public KitShop(String title) {
		this(title, 9 * 6);
	}
	
	public KitShop(String title, int size) {
		this(title, size, Material.EMERALD);
	}
	
	public KitShop(String title, int size, Material material) {
		this(title, size, new ItemStack(material));
	}
	
	public KitShop(String title, int size, ItemStack item) {
		this.title = title == null ? "Kit Shop" : title;
		this.size = size;
		this.item = new ItemCreator(item).setName("&b" + getTitle()).getItemStack();
		EventUtil.register(this);
		recentShop = this;
	}

	public void open(Player player) {
		if(delayed == null) {
			delayed = new ArrayList<String>();
		}
		final String name = player.getName();
		if(!delayed.contains(name)) {
			delayed.add(name);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					delayed.remove(name);
				}
			}, 20 * delay);
			final KitShop shop = this;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					Player player = ProPlugin.getPlayer(name);
					Inventory inventory = Bukkit.createInventory(player, size, getTitle());
					boolean sentMessage = false;
					boolean tellEmeralds = false;
					for(KitBase kit : KitBase.getKits()) {
						if(kit.getShop() == shop) {
							if(kit.getPrice() > -1) {
								tellEmeralds = true;
							}
							if(minimumPrice == 0 || kit.getPrice() < minimumPrice) {
								minimumPrice = kit.getPrice();
							}
							if(!sentMessage && EmeraldsHandler.getEmeralds(player) < minimumPrice) {
								sentMessage = true;
								MessageHandler.sendMessage(player, "&cYou seem to have less emeralds than the cheapest kit");
								MessageHandler.sendMessage(player, "&cGet &2" + Voting.emeraldsForVoting + " Emeralds &cby voting! &b/vote");
								if(AccountHandler.getRank(player) == Ranks.PLAYER) {
									MessageHandler.sendMessage(player, "&eRanks also give you emerald multipliers! &b/buy");
								}
							}
							ItemStack item = kit.getIcon();
							inventory.setItem(kit.getSlot(), item);
						}
					}
					if(tellEmeralds) {
						inventory.setItem(inventory.getSize() - 9, EmeraldsHandler.getEmeraldIcon(player));
					}
					player.openInventory(inventory);
				}
			};
			runnable.run();
		}
	}
	
	public static KitShop getMostRecentShop() {
		return recentShop;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void give(Player player) {
		player.getInventory().addItem(item);
	}
	
	public void give(Player player, int slot) {
		player.getInventory().setItem(slot, item);
	}
	
	public boolean giveShopItem() {
		return this.giveShopItem;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public void toggleGiveShopItem() {
		this.giveShopItem = !this.giveShopItem;
	}
	
	private void remove(Player player) {
		for(KitBase kit : KitBase.getKits()) {
			if(kit.getShop() == this) {
				kit.remove(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(giveShopItem && ProMcGames.getMiniGame() != null && ProMcGames.getMiniGame().getJoiningPreGame()) {
			event.getPlayer().getInventory().addItem(getItem());
		}
	}
	
	@EventHandler
	public void onAsyncPostPlayerJoin(AsyncPostPlayerJoinEvent event) {
		if(giveShopItem || ProMcGames.getPlugin() == Plugins.KIT_PVP) {
			Player player = event.getPlayer();
			if(/*Ranks.ELITE.hasRank(player) && */DB.PLAYERS_STORED_KITS.isUUIDSet(player.getUniqueId())) {
				String storedKit = DB.PLAYERS_STORED_KITS.getString("uuid", player.getUniqueId().toString(), "kit");
				for(KitBase kit : KitBase.getKits()) {
					if(kit.getName().equals(storedKit)) {
						/*MessageHandler.sendLine(player, Ranks.ELITE.getColor() + "");
						MessageHandler.sendMessage(player, Ranks.ELITE.getPrefix() + "&aperk: Loading kit...");
						MessageHandler.sendLine(player, Ranks.ELITE.getColor() + "");*/
						kit.use(player, true);
						break;
					}
				}
				//DB.PLAYERS_STORED_KITS.deleteUUID(player.getUniqueId());
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		if(ProMcGames.getPlugin() != Plugins.KIT_PVP) {
			remove(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(ItemUtil.isItem(player.getItemInHand(), item)) {
			open(player);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(final InventoryItemClickEvent event) {
		if(event.getTitle().equals(getTitle())) {
			final Player player = event.getPlayer();
			for(final KitBase kit : KitBase.getKits()) {
				if(kit.getIcon().getItemMeta().getDisplayName().equals(event.getItem().getItemMeta().getDisplayName())) {
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							kit.use(player);
						}
					});
					break;
				}
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getOpenInventory() != null && player.getOpenInventory().getTitle().contains(getTitle())) {
				player.closeInventory();
			}
		}
	}
	
	@EventHandler
	public void onPostGameStart(PostGameStartEvent event) {
		HandlerList.unregisterAll(this);
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(KitBase kit : KitBase.getKits()) {
					if(kit.getPlayers().size() > 0) {
						int amount = 0;
						if(DB.PLAYERS_SELECTED_KITS.isKeySet("kit", kit.getName())) {
							amount = DB.PLAYERS_SELECTED_KITS.getInt("kit", kit.getName(), "amount");
							DB.PLAYERS_SELECTED_KITS.updateInt("amount", amount + 1, "kit", kit.getName());
						} else {
							DB.PLAYERS_SELECTED_KITS.insert("'" + kit.getName() + "', '1'");
						}
					}
					kit.execute();
				}
			}
		});
	}
}
