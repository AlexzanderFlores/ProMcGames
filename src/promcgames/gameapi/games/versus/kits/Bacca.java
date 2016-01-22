package promcgames.gameapi.games.versus.kits;

import org.bukkit.Material;

public class Bacca extends VersusKit {
	public Bacca() {
		super("Bacca", Material.DIAMOND_AXE);
		setArmor(Material.GOLD_HELMET);
		setArmor(Material.CHAINMAIL_CHESTPLATE);
		setArmor(Material.IRON_LEGGINGS);
		setArmor(Material.CHAINMAIL_BOOTS);
		setItem(0, Material.DIAMOND_AXE);
	}
}
