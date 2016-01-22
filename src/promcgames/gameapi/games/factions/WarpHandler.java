package promcgames.gameapi.games.factions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.gameapi.TeleportCoolDown;
import promcgames.gameapi.games.factions.spawn.SpawnHandler;
import promcgames.gameapi.games.factions.spawn.SpawnHandler.WorldLocation;
import promcgames.server.CommandBase;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class WarpHandler implements Listener {
	private String name = null;
	
	public WarpHandler() {
		name = "Warp";
		new CommandBase("warp", -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				Inventory inventory = Bukkit.createInventory(player, 9, name);
				inventory.setItem(3, new ItemCreator(Material.ITEM_FRAME).setName("&aShop").getItemStack());
				inventory.setItem(5, new ItemCreator(Material.CHEST).setName("&aCrates").getItemStack());
				player.openInventory(inventory);
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Player player = event.getPlayer();
			ItemStack item = event.getItem();
			boolean isAtSpawn = SpawnHandler.getSpawnLevel(player.getLocation()) == WorldLocation.SAFEZONE;
			if(item.getType() == Material.ITEM_FRAME) {
				new TeleportCoolDown(player, new Location(Bukkit.getWorlds().get(0), -258.5, 70, 338.5, -180.0f, 0.0f), isAtSpawn ? 0 : 5);
			} else if(item.getType() == Material.CHEST) {
				new TeleportCoolDown(player, new Location(Bukkit.getWorlds().get(0), -224.5, 67, 276.5, -260.0f, 0.0f), isAtSpawn ? 0 : 5);
			}
			event.setCancelled(true);
			player.closeInventory();
		}
	}
}
