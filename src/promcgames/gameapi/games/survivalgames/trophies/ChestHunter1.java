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

public class ChestHunter1 extends Trophy {
	private static ChestHunter1 instance = null;
	private int amount = 15;
	
	public ChestHunter1() {
		super(Plugins.SURVIVAL_GAMES, 28);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static ChestHunter1 getInstance() {
		if(instance == null) {
			new ChestHunter1();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.CHEST).setName("&aChest Hunter 1").addLore("&eOpen &c" + amount + " &echests in one game").getItemStack();
	}
	
	@EventHandler
	public void onChestOpen(ChestOpenEvent event) {
		if(ChestLogger.getNumberOfChestsOpened(event.getPlayer()) == amount) {
			setAchieved(event.getPlayer());
		}
	}
}
