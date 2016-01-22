package promcgames.gameapi.games.versus.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class TNT extends VersusKit {
	public TNT() {
		super("TNT", Material.TNT);
		setArmor(Material.CHAINMAIL_HELMET);
		setArmor(Material.GOLD_CHESTPLATE);
		setArmor(Material.CHAINMAIL_LEGGINGS);
		setArmor(Material.CHAINMAIL_BOOTS);
		for(int a = 0; a < 9; ++a) {
			setItem(a, new ItemStack(Material.TNT, 64));
		}
	}
}
