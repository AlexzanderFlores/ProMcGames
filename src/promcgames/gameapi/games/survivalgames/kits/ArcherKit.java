package promcgames.gameapi.games.survivalgames.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.ItemCreator;

public class ArcherKit extends KitBase implements Listener {
	private static int arrows = 5;
	private static int price = 500;
	
	public ArcherKit() {
		super(new ItemCreator(Material.BOW).setName("Archer").setLores(new String [] {
			"&bPrice: " + price,
			"",
			"&6Start with a bow & &e" + arrows + " &6arrows"
		}).getItemStack(), 10);
	}
	
	@Override
	public String getPermission() {
		return "survival_games.archer";
	}

	@Override
	public void execute() {
		for(Player player : getPlayers()) {
			player.getInventory().addItem(new ItemStack(Material.BOW));
			player.getInventory().addItem(new ItemStack(Material.ARROW, arrows));
		}
	}

	@Override
	public void execute(Player player) {
		
	}
}
