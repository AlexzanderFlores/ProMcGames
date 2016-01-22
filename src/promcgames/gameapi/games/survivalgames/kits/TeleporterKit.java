package promcgames.gameapi.games.survivalgames.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;

public class TeleporterKit extends KitBase {
	private static int price = 500;
	private static int amount = 1;
	
	public TeleporterKit() {
		super(new ItemCreator(Material.ENDER_PEARL).setName("Teleporter").setLores(new String [] {
			"&bPrice: " + price,
			"",
			"&6Get &e" + amount + " &6ender pearl after &e" + 10 + " &6seconds",
			"",
			"&eNote: &bUseful for escaping spawn quickly"
		}).getItemStack(), 14);
	}
	
	@Override
	public String getPermission() {
		return "survival_games.teleporter";
	}

	@Override
	public void execute() {
		if(!getPlayers().isEmpty()) {
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					for(Player player : getPlayers()) {
						player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, amount));
					}
				}
			}, 20 * 10);
		}
	}

	@Override
	public void execute(Player player) {
		
	}
}
