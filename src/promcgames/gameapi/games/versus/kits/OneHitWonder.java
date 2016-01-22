package promcgames.gameapi.games.versus.kits;

import org.bukkit.Material;

public class OneHitWonder extends VersusKit {
	public OneHitWonder() {
		super("One Hit Wonder", Material.DIAMOND_SWORD);
		setItem(ArmorSlot.HELMET.getSlot(), Material.AIR);
		setItem(ArmorSlot.CHESTPLATE.getSlot(), Material.AIR);
		setItem(ArmorSlot.LEGGINGS.getSlot(), Material.AIR);
		setItem(ArmorSlot.BOOTS.getSlot(), Material.AIR);
		setItem(0, Material.DIAMOND_SWORD);
	}
}
