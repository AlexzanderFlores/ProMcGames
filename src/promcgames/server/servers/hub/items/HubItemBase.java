package promcgames.server.servers.hub.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.server.servers.clans.HubItem;
import promcgames.server.servers.clans.InventoryHandler;
import promcgames.server.servers.clans.SGTrophiesItems;
import promcgames.server.servers.hub.SnowballFight;
import promcgames.server.servers.hub.items.cosmetic.EliteItem;
import promcgames.server.servers.hub.items.cosmetic.MainMenuItem;
import promcgames.server.servers.hub.items.cosmetic.ProItem;
import promcgames.server.servers.hub.items.cosmetic.ProPlusItem;
import promcgames.server.servers.uhc.BlockRunning;
import promcgames.server.servers.uhc.UHCTrophiesItem;
import promcgames.server.servers.uhc.WaterBucket;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public abstract class HubItemBase implements Listener {
	public ItemCreator item = null;
	public int slot = -1;
	
	public HubItemBase(ItemCreator item, int slot) {
		this.item = item;
		this.slot = slot;
		EventUtil.register(this);
	}
	
	public void giveItem(Player player) {
		player.getInventory().setItem(slot, item.getItemStack());
	}
	
	public ItemStack getItem() {
		return item.getItemStack();
	}
	
	public void setItem(ItemStack itemStack) {
		this.item.setItemStack(itemStack);
	}
	
	public String getName() {
		return item.getName();
	}
	
	public void setName(String name) {
	}
	
	public int getSlot() {
		return slot;
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}
	
	public boolean isItem(Player player) {
		ItemStack item = player.getItemInHand();
		return item.getType() != Material.AIR && item.getItemMeta().getDisplayName() != null && getName().startsWith(item.getItemMeta().getDisplayName());
	}
	
	public static void giveOriginalHotBar(final Player player) {
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(0);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				if(ProMcGames.getPlugin() == Plugins.HUB) {
					ServerSelectorItem.getInstance().giveItem(player);
					CosmeticsItem.getInstance().giveItem(player);
					TrophiesItem.getInstance().giveItem(player);
					SnowballFight.getInstance().giveItem(player);
					ProfileItem.getInstance().giveItem(player);
					HubSponsor.getInstance().giveItem(player);
					HubGamesItem.getInstance().giveItem(player);
				} else if(ProMcGames.getPlugin() == Plugins.SGHUB) {
					ServerSelectorItem.getInstance().giveItem(player);
					CosmeticsItem.getInstance().giveItem(player);
					player.getInventory().setItem(2, InventoryHandler.getItem());
					SGTrophiesItems.getInstance().giveItem(player);
					HubItem.getInstance().giveItem(player);
				} else if(ProMcGames.getPlugin() == Plugins.UHCHUB) {
					ServerSelectorItem.getInstance().giveItem(player);
					CosmeticsItem.getInstance().giveItem(player);
					WaterBucket.getInstance().giveItem(player);
					BlockRunning.getInstance().giveItem(player);
					UHCTrophiesItem.getInstance().giveItem(player);
					HubItem.getInstance().giveItem(player);
				}
				player.updateInventory();
			}
		});
	}
	
	public static void giveCosmeticItems(final Player player) {
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(1);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				MainMenuItem.getInstance().giveItem(player);
				ProItem.getInstance().giveItem(player);
				ProPlusItem.getInstance().giveItem(player);
				EliteItem.getInstance().giveItem(player);
				player.updateInventory();
			}
		});
	}
	
	public abstract void onPlayerJoin(PlayerJoinEvent event);
	public abstract void onMouseClick(MouseClickEvent event);
	public abstract void onInventoryItemClick(InventoryItemClickEvent event);
}
