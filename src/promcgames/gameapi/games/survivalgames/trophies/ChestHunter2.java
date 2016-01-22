package promcgames.gameapi.games.survivalgames.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.ChestOpenEvent;
import promcgames.gameapi.games.survivalgames.ChestLogger;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class ChestHunter2 extends Trophy {
	private static ChestHunter2 instance = null;
	private int amount = 20;
	
	public ChestHunter2() {
		super(Plugins.SURVIVAL_GAMES, 29);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static ChestHunter2 getInstance() {
		if(instance == null) {
			new ChestHunter2();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.CHEST).setName("&aChest Hunter 2").addLore("&eOpen &c" + amount + " &echests in one game").getItemStack();
	}
	
	@EventHandler
	public void onChestOpen(ChestOpenEvent event) {
		if(ChestLogger.getNumberOfChestsOpened(event.getPlayer()) == amount) {
			setAchieved(event.getPlayer());
		}
	}
}
