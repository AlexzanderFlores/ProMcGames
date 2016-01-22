package promcgames.gameapi.games.survivalgames.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;

public class SwordsmanKit extends KitBase {
	private static int price = 500;
	
	public SwordsmanKit() {
		super(new ItemCreator(Material.STONE_SWORD).setName("Swordsman").setLores(new String [] {
			"&bPrice: " + price,
			"",
			"&6Start with &e2 &6cobble stone and",
			"&e1 &6stick after &e30 &6seconds"
		}).getItemStack(), 12);
	}
	
	@Override
	public String getPermission() {
		return "survival_games.swordsman";
	}

	@Override
	public void execute() {
		if(!getPlayers().isEmpty()) {
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					for(Player player : getPlayers()) {
						player.getInventory().addItem(new ItemStack(Material.COBBLESTONE, 2));
						player.getInventory().addItem(new ItemStack(Material.STICK));
					}
				}
			}, 20 * 30);
		}
	}

	@Override
	public void execute(Player player) {
		
	}
}
