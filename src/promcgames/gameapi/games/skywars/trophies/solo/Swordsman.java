package promcgames.gameapi.games.skywars.trophies.solo;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class Swordsman extends Trophy {
	private static Swordsman instance = null;
	
	public Swordsman() {
		super(Plugins.SKY_WARS, 10);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static Swordsman getInstance() {
		if(instance == null) {
			new Swordsman();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.DIAMOND_SWORD).setName("&aSwordsman").addLore("&eCraft a diamond sword").getItemStack();
	}
	
	@EventHandler
	public void onCraftItem(CraftItemEvent event) {
		if(event.getWhoClicked() instanceof Player && event.getCurrentItem().getType() == Material.DIAMOND_SWORD) {
			Player player = (Player) event.getWhoClicked();
			setAchieved(player);
		}
	}
}
