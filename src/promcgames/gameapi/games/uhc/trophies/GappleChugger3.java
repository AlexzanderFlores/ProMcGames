package promcgames.gameapi.games.uhc.trophies;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class GappleChugger3 extends Trophy {
	private static GappleChugger3 instance = null;
	private int amount = 30;
	private Map<String, Integer> counters = null;
	
	public GappleChugger3() {
		super(Plugins.UHC, 15);
		instance = this;
		if(canRegister()) {
			counters = new HashMap<String, Integer>();
			EventUtil.register(this);
		}
	}
	
	public static GappleChugger3 getInstance() {
		if(instance == null) {
			new GappleChugger3();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.GOLDEN_APPLE).setAmount(3).setName("&aGapple Chugger 3").addLore("&eEat &c" + amount + " &eGapples").getItemStack();
	}
	
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if(event.getItem().getType() == Material.GOLDEN_APPLE) {
			Player player = event.getPlayer();
			int eaten = 0;
			if(counters.containsKey(player.getName())) {
				eaten = counters.get(player.getName());
			}
			counters.put(player.getName(), ++eaten);
			if(eaten == amount) {
				setAchieved(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		counters.remove(event.getPlayer().getName());
	}
}
