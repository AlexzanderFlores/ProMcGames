package promcgames.gameapi.games.uhc.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.games.uhc.events.DiamondMineEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class DiamondHunter1 extends Trophy {
	private static DiamondHunter1 instance = null;
	private int amount = 25;
	
	public DiamondHunter1() {
		super(Plugins.UHC, 10);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static DiamondHunter1 getInstance() {
		if(instance == null) {
			new DiamondHunter1();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.DIAMOND).setName("&aDiamond Hunter 1").addLore("&eMine &c" + amount + " &eDiamonds").getItemStack();
	}
	
	@EventHandler
	public void onDiamondMine(DiamondMineEvent event) {
		if(event.getAmount() == amount) {
			setAchieved(event.getPlayer());
		}
	}
}
