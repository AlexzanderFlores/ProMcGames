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

public class NoChestChallenge extends Trophy {
	private static NoChestChallenge instance = null;
	
	public NoChestChallenge() {
		super(Plugins.SURVIVAL_GAMES, 32);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static NoChestChallenge getInstance() {
		if(instance == null) {
			new NoChestChallenge();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.CHEST).setName("&aNo Chest Challenge").addLore("&eWin a game without opening a chest").getItemStack();
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(ChestLogger.getNumberOfChestsOpened(event.getPlayer()) == 0) {
			setAchieved(event.getPlayer());
		}
	}
}
