package promcgames.gameapi.games.survivalgames.trophies;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.Disguise;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class BaccaChallenge extends Trophy {
	private static BaccaChallenge instance = null;
	private List<String> players = null;
	
	public BaccaChallenge() {
		super(Plugins.SURVIVAL_GAMES, 14);
		instance = this;
		if(canRegister()) {
			players = new ArrayList<String>();
			EventUtil.register(this);
		}
	}
	
	public static BaccaChallenge getInstance() {
		if(instance == null) {
			new BaccaChallenge();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.STONE_AXE).setName("&aBacca Challenge").setLores(new String [] {
			"&eWin a game where you only:",
			"&e- Ate raw food",
			"&e- Damaged players with axes"
		}).getItemStack();
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			if(event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();
				if(!players.contains(Disguise.getName(damager)) && !damager.getItemInHand().getType().toString().endsWith("_AXE")) {
					players.add(Disguise.getName(damager));
				}
			} else if(event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				if(projectile.getShooter() instanceof Player) {
					Player damager = (Player) projectile.getShooter();
					if(!players.contains(Disguise.getName(damager))) {
						players.add(Disguise.getName(damager));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		for(Material types : new Material [] {Material.COOKED_FISH, Material.GRILLED_PORK, Material.COOKED_CHICKEN, Material.BAKED_POTATO, Material.BREAD, Material.PUMPKIN_PIE,}) {
			if(types == event.getItem().getType()) {
				players.add(Disguise.getName(event.getPlayer()));
			}
		}
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(!players.contains(Disguise.getName(event.getPlayer()))) {
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
