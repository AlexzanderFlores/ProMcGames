package promcgames.gameapi.games.survivalgames.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import promcgames.customevents.game.GameKillEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class BrokenLegsChallenge extends Trophy {
	private static BrokenLegsChallenge instance = null;
	
	public BrokenLegsChallenge() {
		super(Plugins.SURVIVAL_GAMES, 31);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static BrokenLegsChallenge getInstance() {
		if(instance == null) {
			new BrokenLegsChallenge();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.DIAMOND_LEGGINGS).setName("&aBroken Legs Challenge").addLore("&eKill someone while your legs are broken").getItemStack();
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		if(event.getPlayer().hasPotionEffect(PotionEffectType.SLOW)) {
			setAchieved(event.getPlayer());
		}
	}
}
