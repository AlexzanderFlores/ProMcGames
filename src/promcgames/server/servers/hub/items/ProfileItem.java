package promcgames.server.servers.hub.items;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.CommunityLevelHandler;
import promcgames.player.Disguise;
import promcgames.player.EmeraldsHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.PlaytimeTracker;
import promcgames.player.account.PlaytimeTracker.Playtime;
import promcgames.player.account.PlaytimeTracker.TimeType;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

@SuppressWarnings("deprecation")
public class ProfileItem extends HubItemBase {
	private static HubItemBase instance = null;
	
	public ProfileItem() {
		super(new ItemCreator(Material.SKULL_ITEM, 3).setName("&aProfile"), 6);
		instance = this;
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}

	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		giveItem(event.getPlayer());
	}

	@Override
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(isItem(player)) {
			final String name = player.getName();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					Player player = ProPlugin.getPlayer(name);
					Inventory inventory = Bukkit.createInventory(player, 9 * 3, ChatColor.stripColor(getName()));
					UUID uuid = player.getUniqueId();
					String prefix = AccountHandler.getRank(player).getPrefix().replace(" ", "");
					Playtime playtime = PlaytimeTracker.getPlayTime(player);
					String lifetimePlaytime = ChatColor.GREEN + playtime.getDisplay(TimeType.LIFETIME);
					String monthlyPlaytime = ChatColor.GREEN + playtime.getDisplay(TimeType.MONTHLY);
					int communityLevel = CommunityLevelHandler.getCommunityLevel(player);
					int trophies = DB.PLAYERS_ACHIEVEMENTS.getSize("uuid", uuid.toString());
					int kitsUnlocked = DB.PLAYERS_KITS.getSize("uuid", uuid.toString());
					inventory.setItem(10, new ItemCreator(Material.DIAMOND).setName("&aRank: " + prefix).getItemStack());
					inventory.setItem(11, new ItemCreator(Material.WATCH).setName("&aLifetime Playtime").addLore(lifetimePlaytime).getItemStack());
					inventory.setItem(12, new ItemCreator(Material.WATCH).setName("&aMonthly Playtime").addLore(monthlyPlaytime).getItemStack());
					inventory.setItem(13, new ItemCreator(Material.EMERALD).setName("&aEmeralds: &2" + EmeraldsHandler.getEmeralds(player)).getItemStack());
					inventory.setItem(14, new ItemCreator(Material.NAME_TAG).setName("&aCommunity Level: &6" + communityLevel).getItemStack());
					inventory.setItem(15, new ItemCreator(Material.GOLD_INGOT).setName("&aTrophies Unlocked: &6" + trophies).getItemStack());
					inventory.setItem(16, new ItemCreator(Material.ENDER_PEARL).setName("&aKits Unlocked: &6" + kitsUnlocked).getItemStack());
					player.openInventory(inventory);
				}
			});
			event.getPlayer().updateInventory();
			event.setCancelled(true);
		}
	}

	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(isItem(event.getPlayer())) {
			event.getPlayer().closeInventory();
			event.setCancelled(true);
		}
	}
	
	@Override
	public void giveItem(Player player) {
		player.getInventory().setItem(getSlot(), ItemUtil.getSkull(Disguise.getName(player), getItem().clone()));
	}
}