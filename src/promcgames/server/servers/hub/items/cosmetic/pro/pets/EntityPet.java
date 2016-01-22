package promcgames.server.servers.hub.items.cosmetic.pro.pets;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public interface EntityPet {
	public abstract void onSpawn(Player player);
	public abstract void walkTo(Player player, float speed);
	public abstract Inventory getOptionsInventory(Player player, Inventory inventory);
	public abstract void clickedOnCustomOption(Player player, ItemStack clicked);
	public abstract void wornBy(Player player);
	public abstract Vector tossedBy(Player player);
	public abstract void togglePetStaying(Player player);
	public abstract void togglePetSounds(Player player);
	public abstract void makeSound(Player player);
	public abstract void makeHurtSound(Player player);
	public abstract void remove(Player player);
}
