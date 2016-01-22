package promcgames.gameapi.games.versus.kits;

import org.bukkit.Material;

import promcgames.server.util.ItemCreator;

public class Ender extends VersusKit {
	public Ender() {
		super("Ender", Material.ENDER_PEARL);
		setArmor(Material.IRON_HELMET);
		setArmor(Material.DIAMOND_CHESTPLATE);
		setArmor(Material.IRON_LEGGINGS);
		setArmor(Material.IRON_BOOTS);
		setItem(0, Material.DIAMOND_SWORD);
		setItem(1, Material.FISHING_ROD);
		setItem(1, Material.FLINT_AND_STEEL);
		setItem(1, new ItemCreator(Material.ENDER_PEARL).setAmount(3).getItemStack());
	}
}
