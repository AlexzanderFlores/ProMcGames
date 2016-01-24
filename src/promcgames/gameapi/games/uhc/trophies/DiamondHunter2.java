package promcgames.gameapi.games.uhc.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.games.uhc.events.DiamondMineEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class DiamondHunter2 extends Trophy {
	private static DiamondHunter2 instance = null;
	private int amount = 50;
	
	public DiamondHunter2() {
		super(Plugins.UHC, 11);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static DiamondHunter2 getInstance() {
		if(instance == null) {
			new DiamondHunter2();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.DIAMOND).setAmount(2).setName("&aDiamond Hunter 2").addLore("&eMine &c" + amount + " &eDiamonds").getItemStack();
	}
	
	@EventHandler
	public void onDiamondMine(DiamondMineEvent event) {
		if(event.getAmount() == amount) {
			setAchieved(event.getPlayer());
		}
	}
}
