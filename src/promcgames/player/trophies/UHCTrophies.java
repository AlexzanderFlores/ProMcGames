package promcgames.player.trophies;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.gameapi.games.uhc.trophies.DiamondHunter1;
import promcgames.gameapi.games.uhc.trophies.DiamondHunter2;
import promcgames.gameapi.games.uhc.trophies.DiamondHunter3;
import promcgames.gameapi.games.uhc.trophies.GappleChugger1;
import promcgames.gameapi.games.uhc.trophies.GappleChugger2;
import promcgames.gameapi.games.uhc.trophies.GappleChugger3;
import promcgames.gameapi.games.uhc.trophies.IronMan;
import promcgames.gameapi.games.uhc.trophies.Top2;
import promcgames.gameapi.games.uhc.trophies.Top4;
import promcgames.gameapi.games.uhc.trophies.Top8;
import promcgames.player.MessageHandler;
import promcgames.server.ProMcGames;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.servers.hub.items.TrophiesItem;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

public class UHCTrophies implements Listener {
	private static ItemStack item = null;
	
	public UHCTrophies() {
		if(ProMcGames.getPlugin() == Plugins.UHC) {
			item = new ItemCreator(Material.GOLD_INGOT).setName("&aTrophies").getItemStack();
		}
		EventUtil.register(this);
	}
	
	public static ItemStack getItem() {
		return item;
	}
	
	public static String getName() {
		return "UHC Trophies";
	}
	
	public static void open(final Player player) {
		if(ProMcGames.getPlugin() == Plugins.UHC) {
			MessageHandler.sendMessage(player, "&cYou can only view these in the hub");
			return;
		}
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				int unlocked = 0;
				int total = 0;
				Inventory inventory = Bukkit.createInventory(player, 9 * 6, getName());
				if(DiamondHunter1.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(DiamondHunter2.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(DiamondHunter3.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(GappleChugger1.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(GappleChugger2.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(GappleChugger3.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(Top8.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(Top4.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(Top2.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(IronMan.getInstance().addToInventory(player, inventory)) {
					++unlocked;
				}
				++total;
				if(ProMcGames.getPlugin() == Plugins.HUB) {
					inventory.setItem(inventory.getSize() - 9, new ItemCreator(Material.ARROW).setName("&bBack").getItemStack());
				}
				inventory.setItem(inventory.getSize() - 1, new ItemCreator(Material.EMERALD).setName("&e" + unlocked + "&7/&e" + total + " &aTrophies Unlocked").getItemStack());
				player.openInventory(inventory);
			}
		});
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		if(ItemUtil.isItem(event.getPlayer().getItemInHand(), item)) {
			open(event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(getName())) {
			Player player = event.getPlayer();
			if(item == null) {
				TrophiesItem.open(player);
			} else {
				player.closeInventory();
			}
			event.setCancelled(true);
		}
	}
}
