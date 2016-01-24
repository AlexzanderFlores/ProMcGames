package promcgames.gameapi.games.survivalgames;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.GameStartEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.util.EventUtil;

public class SpawnPointSelector implements Listener {
	public SpawnPointSelector() {
		for(Player player : ProPlugin.getPlayers()) {
			if(Ranks.ELITE.hasRank(player, true)) {
				player.getInventory().setItem(8, new ItemStack(Material.SNOW_BALL, 4));
			} else if(Ranks.PRO_PLUS.hasRank(player, true)) {
				player.getInventory().setItem(8, new ItemStack(Material.SNOW_BALL, 3));
			} else if(Ranks.PRO.hasRank(player, true)) {
				player.getInventory().setItem(8, new ItemStack(Material.SNOW_BALL, 2));
			} else {
				player.getInventory().setItem(8, new ItemStack(Material.SNOW_BALL));
			}
		}
		EventUtil.register(this);
	}
	
	private void display(Player player) {
		MessageHandler.sendMessage(player, "&cCannot throw snowball! You need to have " + Ranks.PRO.getPrefix() + "&cor above! &b/buy");
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Snowball && event.getEntity() instanceof Player) {
			Snowball snowball = (Snowball) event.getDamager();
			if(snowball.getShooter() instanceof Player) {
				Player player = (Player) event.getEntity();
				if(!SpectatorHandler.contains(player)) {
					Player shooter = (Player) snowball.getShooter();
					if(ProMcGames.getMiniGame() != null && !SurvivalGames.getSnowballsAlwaysWork() && Ranks.ELITE.hasRank(player, true)) {
						MessageHandler.sendMessage(shooter, "&cYou cannot use snowballs on an " + Ranks.ELITE.getPrefix() + "&cplayer");
						if(shooter.getInventory().contains(Material.SNOW_BALL)) {
							shooter.getInventory().addItem(new ItemStack(Material.SNOW_BALL));
						} else {
							shooter.getInventory().setItem(8, new ItemStack(Material.SNOW_BALL));
						}
					} else {
						Location shooterLocation = shooter.getLocation();
						shooter.teleport(player);
						player.teleport(shooterLocation);
						MessageHandler.sendMessage(player, AccountHandler.getPrefix(shooter, false) + "&a has hit you with a snowball to swap locations!");
						display(player);
						MessageHandler.sendMessage(shooter, "Swapped locations with " + AccountHandler.getPrefix(player, false));
						Events.spawnPointHandler.swapLocations(player, shooter);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if(item != null && item.getType() == Material.SNOW_BALL) {
			if(Ranks.PRO.hasRank(event.getPlayer(), true)) {
				event.setCancelled(false);
			} else {
				display(event.getPlayer());
			}
		}
	}
	
	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
		if(item != null && item.getType() == Material.SNOW_BALL) {
			MessageHandler.sendMessage(player, "Throw this snowball at another player to swap locations!");
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		HandlerList.unregisterAll(this);
	}
}
