package promcgames.gameapi.games.survivalgames.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.GameKillEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProPlugin;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class KatnissChallenge extends Trophy {
	private static KatnissChallenge instance = null;
	
	public KatnissChallenge() {
		super(Plugins.SURVIVAL_GAMES, 11);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static KatnissChallenge getInstance() {
		if(instance == null) {
			new KatnissChallenge();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.BOW).setName("&aKatniss Challenge").addLore("&eWin a game with a bow").getItemStack();
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		if(ProPlugin.getPlayers().size() == 2 && event.getMessage().contains(" shot ")) {
			setAchieved(event.getPlayer());
		}
	}
}
