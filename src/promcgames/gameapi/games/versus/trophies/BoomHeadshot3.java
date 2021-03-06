package promcgames.gameapi.games.versus.trophies;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.PlayerHeadshotEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.games.versus.events.BattleEndEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class BoomHeadshot3 extends Trophy {
	private static BoomHeadshot3 instance = null;
	private int amount = 9;
	private Map<String, Integer> headShots = null;
	
	public BoomHeadshot3() {
		super(Plugins.VERSUS, 15);
		instance = this;
		if(canRegister()) {
			headShots = new HashMap<String, Integer>();
			EventUtil.register(this);
		}
	}
	
	public static BoomHeadshot3 getInstance() {
		if(instance == null) {
			new BoomHeadshot3();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.ARROW).setAmount(3).setName("&aBoom Headshot! 3").addLore("&eGet &c" + amount + " &eheadshots in one match").getItemStack();
	}
	
	@EventHandler
	public void onPlayerHeadshotEvent(PlayerHeadshotEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		int amount = 0;
		if(headShots.containsKey(name)) {
			amount = headShots.get(name);
		}
		if(++amount == this.amount) {
			setAchieved(player);
		}
		headShots.put(name, amount);
	}
	
	@EventHandler
	public void onBattleEnd(BattleEndEvent event) {
		if(event.getWinner() != null) {
			headShots.remove(event.getWinner().getName());
		}
		if(event.getLoser() != null) {
			headShots.remove(event.getLoser().getName());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		headShots.remove(event.getPlayer().getName());
	}
}
