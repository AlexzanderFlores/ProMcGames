package promcgames.gameapi.games.versus.kits;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import promcgames.player.DyedArmorUtil;
import promcgames.server.util.ItemCreator;

public class Pyro extends VersusKit {
	public Pyro() {
		super("Pyro", Material.FLINT_AND_STEEL);
		setArmor(DyedArmorUtil.getDyedArmor(Material.LEATHER_HELMET, Color.RED));
		setArmor(DyedArmorUtil.getDyedArmor(Material.LEATHER_CHESTPLATE, Color.RED));
		setArmor(DyedArmorUtil.getDyedArmor(Material.LEATHER_LEGGINGS, Color.RED));
		setArmor(DyedArmorUtil.getDyedArmor(Material.LEATHER_BOOTS, Color.RED));
		setItem(0, new ItemCreator(Material.BOW).addEnchantment(Enchantment.ARROW_INFINITE).addEnchantment(Enchantment.ARROW_FIRE).getItemStack());
		for(int a = 1; a < 8; ++a) {
			setItem(a, Material.FLINT_AND_STEEL);
		}
		setItem(8, Material.ARROW);
	}
}
