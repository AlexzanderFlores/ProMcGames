package promcgames.gameapi.games.arcade.games;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.arcade.ArcadeGame;
import promcgames.player.Disguise;
import promcgames.player.account.AccountHandler;
import promcgames.player.bossbar.BossBar;

public class TNTBomber extends ArcadeGame {
	public TNTBomber() {
		super("TNT Bomber");
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			ProMcGames.getSidebar().setText(AccountHandler.getRank(player).getColor() + Disguise.getName(player), (int) player.getHealth());
		}
		BossBar.remove();
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			List<Player> players = ProPlugin.getPlayers();
			getWorld().spawnEntity(players.get(new Random().nextInt(players.size())).getLocation().add(0, 5, 0), EntityType.PRIMED_TNT);
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for(Block block : event.blockList()) {
			if(new Random().nextBoolean()) {
				block.setType(Material.AIR);
			}
		}
	}
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		for(Entity entity : event.getEntity().getNearbyEntities(2, 2, 2)) {
			if(entity instanceof Player) {
				Player player = (Player) entity;
				if(!SpectatorHandler.contains(player)) {
					if(player.getHealth() > 2.0d) {
						player.damage(2.0);
						ProMcGames.getSidebar().setText(AccountHandler.getRank(player).getColor() + Disguise.getName(player), (int) player.getHealth());
						ProMcGames.getSidebar().update();
					} else {
						SpectatorHandler.add(player);
						int left = ProPlugin.getPlayers().size();
						if(left <= 0) {
							disable(null);
						} else if(left == 1) {
							Player winner = ProPlugin.getPlayers().get(0);
							disable(winner);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getCause() == DamageCause.VOID && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(!SpectatorHandler.contains(player)) {
				SpectatorHandler.add(player);
				int left = ProPlugin.getPlayers().size();
				if(left <= 0) {
					disable(null);
				} else if(left == 1) {
					Player winner = ProPlugin.getPlayers().get(0);
					disable(winner);
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		event.setCancelled(true);
	}
}
