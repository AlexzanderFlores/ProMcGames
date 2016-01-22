package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameKillEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class LevelUp1 extends Trophy {
	private static LevelUp1 instance = null;
	private int amount = 10;
	
	public LevelUp1() {
		super(Plugins.KIT_PVP, 12);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static LevelUp1 getInstance() {
		if(instance == null) {
			new LevelUp1();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.EXP_BOTTLE).setAmount(amount).setName("&aLevel Up 1").addLore("&eGet &c" + amount + " &elevels").getItemStack();
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGameKill(GameKillEvent event) {
		if(event.getPlayer().getLevel() == amount) {
			setAchieved(event.getPlayer());
		}
	}
}
