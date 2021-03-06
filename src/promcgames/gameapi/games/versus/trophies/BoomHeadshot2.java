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

public class BoomHeadshot2 extends Trophy {
	private static BoomHeadshot2 instance = null;
	private int amount = 6;
	private Map<String, Integer> headShots = null;
	
	public BoomHeadshot2() {
		super(Plugins.VERSUS, 14);
		instance = this;
		if(canRegister()) {
			headShots = new HashMap<String, Integer>();
			EventUtil.register(this);
		}
	}
	
	public static BoomHeadshot2 getInstance() {
		if(instance == null) {
			new BoomHeadshot2();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.ARROW).setAmount(2).setName("&aBoom Headshot! 2").addLore("&eGet &c" + amount + " &eheadshots in one match").getItemStack();
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
