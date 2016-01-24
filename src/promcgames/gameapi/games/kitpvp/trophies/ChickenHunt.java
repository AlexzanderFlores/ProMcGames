package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class ChickenHunt extends Trophy {
	private static ChickenHunt instance = null;
	
	public ChickenHunt() {
		super(Plugins.KIT_PVP, 10);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static ChickenHunt getInstance() {
		if(instance == null) {
			new ChickenHunt();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.EGG).setName("&aChicken Hunt").addLore("&eFind the hidden Chicken NPC").getItemStack();
	}
}
