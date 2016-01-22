package promcgames.server.servers.hub.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.MouseClickEvent.ClickType;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class HubGamesItem extends HubItemBase {
	private static HubItemBase instance = null;
	
	public HubGamesItem() {
		super(new ItemCreator(Material.DIAMOND_BOOTS).setName("&aHub Games"), 8);
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
			Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.stripColor(getName()));
			inventory.setItem(1, new ItemCreator(Material.DIAMOND_BOOTS).setName("&aParkour").getItemStack());
			inventory.setItem(3, new ItemCreator(Material.STAINED_GLASS, 14).setName("&aColor Egg").getItemStack());
			inventory.setItem(5, new ItemCreator(Material.SNOW_BLOCK).setName("&aSpleef").getItemStack());
			inventory.setItem(7, new ItemCreator(Material.LADDER).setName("&aKing of the Ladder").getItemStack());
			player.openInventory(inventory);
			if(event.getClickType() == ClickType.RIGHT_CLICK) {
				player.updateInventory();
			}
			event.setCancelled(true);
		}
	}

	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals("Hub Games")) {
			Player player = event.getPlayer();
			Material type = event.getItem().getType();
			if(type == Material.DIAMOND_BOOTS) {
				player.teleport(new Location(player.getWorld(), -34.5, 127.0, -171.5, -53.24f, 0.74f));
			} else if(type == Material.STAINED_GLASS) {
				player.teleport(new Location(player.getWorld(), -131.5, 126, -148.5, -305.0f, 13.18f));
			} else if(type == Material.SNOW_BLOCK) {
				player.teleport(new Location(player.getWorld(), -55.5, 126, -197.5, -110.0f, -2.7f));
			} else if(type == Material.LADDER) {
				player.teleport(new Location(player.getWorld(), -45.5, 126, -159.5, -45.5f, -0.6f));
			}
			event.setCancelled(true);
			player.closeInventory();
		}
	}
}
