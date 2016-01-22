package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class Enchanted extends Trophy {
	private static Enchanted instance = null;
	
	public Enchanted() {
		super(Plugins.KIT_PVP, 11);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static Enchanted getInstance() {
		if(instance == null) {
			new Enchanted();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.ENCHANTMENT_TABLE).setName("&aEnchanted").addLore("&eUse the killstreak selector").getItemStack();
	}
}
