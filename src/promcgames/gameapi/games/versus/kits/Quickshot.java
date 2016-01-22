package promcgames.gameapi.games.versus.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import promcgames.server.util.ItemCreator;

public class Quickshot extends VersusKit {
	public Quickshot() {
		super("Quickshot", Material.ARROW);
		setItem(ArmorSlot.HELMET.getSlot(), Material.AIR);
		setItem(ArmorSlot.CHESTPLATE.getSlot(), Material.AIR);
		setItem(ArmorSlot.LEGGINGS.getSlot(), Material.AIR);
		setItem(ArmorSlot.BOOTS.getSlot(), Material.AIR);
		setItem(0, new ItemCreator(Material.BOW).addEnchantment(Enchantment.ARROW_INFINITE).getItemStack());
		setItem(9, Material.ARROW);
	}
}
