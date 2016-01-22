package promcgames.gameapi.scenarios.scenarios;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TripleOres extends OreMultipliers {
	private static TripleOres instance = null;
	
	public TripleOres() {
		super("TripleOres", 3, new ItemStack(Material.IRON_INGOT, 3));
		instance = this;
		setInfo("Whenever an ore is mined it drops 3x the amount instead.");
	}
	
	public static TripleOres getInstance() {
		if(instance == null) {
			new TripleOres();
		}
		return instance;
	}
}
