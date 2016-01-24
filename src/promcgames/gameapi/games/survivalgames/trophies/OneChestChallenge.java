package promcgames.gameapi.games.survivalgames.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.GameWinEvent;
import promcgames.gameapi.games.survivalgames.ChestLogger;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class OneChestChallenge extends Trophy {
	private static OneChestChallenge instance = null;
	
	public OneChestChallenge() {
		super(Plugins.SURVIVAL_GAMES, 33);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static OneChestChallenge getInstance() {
		if(instance == null) {
			new OneChestChallenge();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.CHEST).setName("&aOne Chest Challenge").addLore("&eWin a game while only opening 1 chest").getItemStack();
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(ChestLogger.getNumberOfChestsOpened(event.getPlayer()) == 1) {
			setAchieved(event.getPlayer());
		}
	}
}
