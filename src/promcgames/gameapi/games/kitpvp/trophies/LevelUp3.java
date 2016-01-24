package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.GameKillEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class LevelUp3 extends Trophy {
	private static LevelUp3 instance = null;
	private int amount = 20;
	
	public LevelUp3() {
		super(Plugins.KIT_PVP, 14);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static LevelUp3 getInstance() {
		if(instance == null) {
			new LevelUp3();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.EXP_BOTTLE).setAmount(amount).setName("&aLevel Up 3").addLore("&eGet &c" + amount + " &elevels").getItemStack();
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGameKill(GameKillEvent event) {
		if(event.getPlayer().getLevel() == amount) {
			setAchieved(event.getPlayer());
		}
	}
}
