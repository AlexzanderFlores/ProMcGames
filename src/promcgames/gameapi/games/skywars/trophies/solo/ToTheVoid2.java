package promcgames.gameapi.games.skywars.trophies.solo;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class ToTheVoid2 extends Trophy {
	private static ToTheVoid2 instance = null;
	private Map<String, String> hitWithProjectile = null;
	private Map<String, Integer> kills = null;
	private int requiredKills = 3;
	
	public ToTheVoid2() {
		super(Plugins.SKY_WARS, 22);
		instance = this;
		if(canRegister()) {
			hitWithProjectile = new HashMap<String, String>();
			kills = new HashMap<String, Integer>();
			EventUtil.register(this);
		}
	}
	
	public static ToTheVoid2 getInstance() {
		if(instance == null) {
			new ToTheVoid2();
		}
		return instance;
	}

	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.SNOW_BALL).setAmount(2).setName("&aTo The Void! 2").
				addLore("&eHit &c" + requiredKills + " &eplayers into the void").addLore("&eWith a projectile in 1 game").getItemStack();
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED && event.getEntity() instanceof Player && event.getDamager() instanceof Projectile) {
			Projectile projectile = (Projectile) event.getDamager();
			if(projectile.getShooter() instanceof Player) {
				Player player = (Player) event.getEntity();
				Player damager = (Player) projectile.getShooter();
				hitWithProjectile.put(player.getName(), damager.getName());
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED && event.getEntity() instanceof Player && event.getCause() == DamageCause.VOID) {
			Player player = (Player) event.getEntity();
			if(hitWithProjectile.containsKey(player.getName())) {
				Player shooter = ProPlugin.getPlayer(hitWithProjectile.get(player.getName()));
				if(shooter != null) {
					int kill = 0;
					if(kills.containsKey(shooter.getName())) {
						kill = kills.get(shooter.getName());
					}
					if(++kill == requiredKills) {
						setAchieved(shooter);
					}
					kills.put(shooter.getName(), kill);
				}
				hitWithProjectile.remove(player.getName());
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		hitWithProjectile.remove(event.getPlayer().getName());
	}
}
