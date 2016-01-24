package promcgames.gameapi.games.survivalgames.trophies;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.Disguise;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class SecondariesChallenge extends Trophy {
	private static SecondariesChallenge instance = null;
	private List<String> players = null;
	
	public SecondariesChallenge() {
		super(Plugins.SURVIVAL_GAMES, 13);
		instance = this;
		if(canRegister()) {
			players = new ArrayList<String>();
			EventUtil.register(this);
		}
	}
	
	public static SecondariesChallenge getInstance() {
		if(instance == null) {
			new SecondariesChallenge();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.FLINT_AND_STEEL).setName("&aSecondaries Challenge").addLore("&eWin a game without using a sword").getItemStack();
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player && !event.isCancelled()) {
			Player player = (Player) event.getDamager();
			if(player.getItemInHand().getType().toString().endsWith("_SWORD") && !players.contains(Disguise.getName(player))) {
				players.add(Disguise.getName(player));
			}
		}
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(players == null || !players.contains(Disguise.getName(event.getPlayer()))) {
			setAchieved(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(players != null) {
			players.remove(Disguise.getName(event.getPlayer()));
		}
	}
}
