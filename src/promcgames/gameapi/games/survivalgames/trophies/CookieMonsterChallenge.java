package promcgames.gameapi.games.survivalgames.trophies;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.Disguise;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class CookieMonsterChallenge extends Trophy {
	private static CookieMonsterChallenge instance = null;
	private List<String> players = null;
	
	public CookieMonsterChallenge() {
		super(Plugins.SURVIVAL_GAMES, 43);
		instance = this;
		if(canRegister()) {
			players = new ArrayList<String>();
			EventUtil.register(this);
		}
	}
	
	public static CookieMonsterChallenge getInstance() {
		if(instance == null) {
			new CookieMonsterChallenge();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.COOKIE).setName("&aCookie Monster Challenge").addLore("&eWin a game without eating non-cookie food").getItemStack();
	}
	
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		String name = Disguise.getName(player);
		if(event.getItem().getType() != Material.COOKIE && !players.contains(name)) {
			players.add(name);
		}
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		Player player = event.getPlayer();
		String name = Disguise.getName(event.getPlayer());
		if(!players.contains(name)) {
			setAchieved(player);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		String name = Disguise.getName(player);
		players.remove(name);
	}
}
