package promcgames.server.servers.hub.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.player.MessageHandler;
import promcgames.player.trophies.KitPVPTrophies;
import promcgames.player.trophies.SkyWarsTeamsTrophies;
import promcgames.player.trophies.SkyWarsTrophies;
import promcgames.player.trophies.SurvivalGamesTrophies;
import promcgames.player.trophies.UHCTrophies;
import promcgames.player.trophies.VersusTrophies;
import promcgames.server.util.ItemCreator;

public class TrophiesItem extends HubItemBase {
	private static HubItemBase instance = null;
	
	public TrophiesItem() {
		super(new ItemCreator(Material.GOLD_INGOT).setName("&aTrophies"), 2);
		instance = this;
		new SurvivalGamesTrophies();
		new VersusTrophies();
		new KitPVPTrophies();
		new UHCTrophies();
		new SkyWarsTrophies();
		new SkyWarsTeamsTrophies();
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}
	
	public static void open(Player player) {
		Inventory inventory = ServerSelectorItem.openInventory(player, ChatColor.stripColor(getInstance().getName()));
		player.openInventory(inventory);
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
			open(player);
			event.setCancelled(true);
		}
	}
	
	@Override
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals("Trophies")) {
			Player player = event.getPlayer();
			Material type = event.getItem().getType();
			if(type == Material.DIAMOND_SWORD) {
				SurvivalGamesTrophies.open(player);
			} else if(type == Material.FISHING_ROD) {
				VersusTrophies.open(player);
			} else if(type == Material.IRON_SWORD) {
				KitPVPTrophies.open(player);
			} else if(type == Material.GOLDEN_APPLE) {
				UHCTrophies.open(player);
			} else if(type == Material.GRASS) {
				openSkyWarsInventory(player);
			} else if(type == Material.SKULL_ITEM) {
				if(event.getItemTitle().contains("Solo")) {
					SkyWarsTrophies.open(player);
				} else {
					SkyWarsTeamsTrophies.open(player);
				}
			} else {
				MessageHandler.sendMessage(player, "&cNot done yet");
				player.closeInventory();
			}
			event.setCancelled(true);
		}
	}
	
	private void openSkyWarsInventory(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.stripColor(getInstance().getName()));
		inventory.setItem(3, new ItemCreator(Material.SKULL_ITEM, 3).setName("&aSolo").getItemStack());
		inventory.setItem(5, new ItemCreator(Material.SKULL_ITEM, 3).setAmount(2).setName("&aTeams of Two").getItemStack());
		player.openInventory(inventory);
	}
}
