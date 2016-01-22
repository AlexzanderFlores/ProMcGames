package promcgames.gameapi.games.uhcbattles;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.MiniGame;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.games.uhc.border.BorderHandler;
import promcgames.player.MessageHandler;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

public class Events implements Listener {
	public Events() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			MiniGame miniGame = ProMcGames.getMiniGame();
			if(miniGame.getCounter() <= 0) {
				OneSecondTaskEvent.getHandlerList().unregister(this);
				//TODO: Start 1v1s
			} else {
				if(miniGame.canDisplay()) {
					EffectUtil.playSound(Sound.CLICK);
				}
				ProMcGames.getSidebar().update("In Game " + miniGame.getCounterAsString());
			}
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		ProMcGames.getProPlugin().resetFlags();
		ProMcGames.getMiniGame().setAllowEntityDamage(false);
		ProMcGames.getMiniGame().setAllowEntityDamageByEntities(false);
		ProMcGames.getMiniGame().setCounter(60 * 20 * 10);
		String command = "spreadPlayers 0 0 100 " + (BorderHandler.getOverworldBorder().getRadius() / 2) + " false ";
		for(Player player : ProPlugin.getPlayers()) {
			player.teleport(WorldHandler.getWorld().getSpawnLocation());
			player.setNoDamageTicks(20 * 15);
			command += player.getName() + " ";
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			Player damager = null;
			if(event.getDamager() instanceof Player) {
				damager = (Player) event.getDamager();
			} else if(event.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) event.getDamager();
				if(projectile.getShooter() instanceof Player) {
					damager = (Player) projectile.getShooter();
				}
			}
			if(damager != null) {
				MessageHandler.sendMessage(damager, "&cPVP is not enabled until 1v1s");
				event.setCancelled(true);
			}
		}
	}
}
