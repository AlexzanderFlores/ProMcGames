package promcgames.gameapi.games.uhc.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.games.uhc.events.DiamondMineEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class DiamondHunter3 extends Trophy {
	private static DiamondHunter3 instance = null;
	private int amount = 75;
	
	public DiamondHunter3() {
		super(Plugins.UHC, 12);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static DiamondHunter3 getInstance() {
		if(instance == null) {
			new DiamondHunter3();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.DIAMOND).setAmount(3).setName("&aDiamond Hunter 3").addLore("&eMine &c" + amount + " &eDiamonds").getItemStack();
	}
	
	@EventHandler
	public void onDiamondMine(DiamondMineEvent event) {
		if(event.getAmount() == amount) {
			setAchieved(event.getPlayer());
		}
	}
}
