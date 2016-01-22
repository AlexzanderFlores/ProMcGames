package promcgames.player;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class DyedArmorUtil {
	public static ItemStack getDyedArmor(Material material, Color color) {
		return getDyedArmor(new ItemStack(material), color);
	}
	
	public static ItemStack getDyedArmor(ItemStack itemStack, Color color) {
		LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
		leatherArmorMeta.setColor(color);
		itemStack.setItemMeta(leatherArmorMeta);
		return itemStack;
	}
	
	public static void giveDyedArmor(Player player, ItemStack itemStack, Color color) {
		LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
		leatherArmorMeta.setColor(color);
		itemStack.setItemMeta(leatherArmorMeta);
		switch(itemStack.getType()){
			case LEATHER_HELMET:
				player.getInventory().setHelmet(itemStack);
				break;
			case LEATHER_CHESTPLATE:
				player.getInventory().setChestplate(itemStack);
				break;
			case LEATHER_LEGGINGS:
				player.getInventory().setLeggings(itemStack);
				break;
			case LEATHER_BOOTS:
				player.getInventory().setBoots(itemStack);
				break;
			default:
		}
	}
	
	public static void giveDyedArmor(Player player, ItemStack itemStack, int red, int green, int blue) {
		giveDyedArmor(player, itemStack, Color.fromRGB(red, green, blue));
	}
	
}
