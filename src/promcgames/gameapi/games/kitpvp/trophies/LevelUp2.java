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

public class LevelUp2 extends Trophy {
	private static LevelUp2 instance = null;
	private int amount = 15;
	
	public LevelUp2() {
		super(Plugins.KIT_PVP, 13);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static LevelUp2 getInstance() {
		if(instance == null) {
			new LevelUp2();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.EXP_BOTTLE).setAmount(amount).setName("&aLevel Up 2").addLore("&eGet &c" + amount + " &elevels").getItemStack();
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onGameKill(GameKillEvent event) {
		if(event.getPlayer().getLevel() == amount) {
			setAchieved(event.getPlayer());
		}
	}
}
