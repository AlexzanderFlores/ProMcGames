package promcgames.gameapi.games.arcade.games;

import java.util.Random;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.game.PostGameStartEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.arcade.ArcadeGame;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.bossbar.BossBar;
import promcgames.server.util.EffectUtil;

@SuppressWarnings("all")
public class BlockRain extends ArcadeGame {
	public BlockRain() {
		super("Block Rain");
	}
	
	@Override
	public void enable() {
		super.enable();
	}
	
	@Override
	public void disable(Player winner) {
		super.disable(winner);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPostGameStart(PostGameStartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			ProMcGames.getSidebar().setText(AccountHandler.getRank(player).getColor() + Disguise.getName(player), 1);
		}
		MessageHandler.alertLine();
		MessageHandler.alert("");
		MessageHandler.alert("&6&lFalling blocks deal 5 hearts of damage");
		MessageHandler.alert("&6&lFirst to the top or last alive wins!");
		MessageHandler.alert("");
		MessageHandler.alertLine();
		BossBar.remove();
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			for(Player player : ProPlugin.getPlayers()) {
				Location location = player.getLocation();
				location.setY(20);
				byte data = DyeColor.values()[new Random().nextInt(DyeColor.values().length)].getData();
				FallingBlock block = player.getWorld().spawnFallingBlock(location, Material.WOOL, data);
				block.setDropItem(false);
			}
		}
	}
	
	@EventHandler
	public void onEntityChanageBlock(EntityChangeBlockEvent event) {
		int x = event.getBlock().getX();
		int y = event.getBlock().getY();
		int z = event.getBlock().getZ();
		for(Player player : ProPlugin.getPlayers()) {
			Location location = player.getLocation();
			if(location.getBlockX() == x && location.getBlockY() == y && location.getBlockZ() == z) {
				MessageHandler.sendMessage(player, "&c&lYou've been hit by a falling block");
				if(player.getHealth() == 20) {
					player.setHealth(10);
				} else {
					EffectUtil.playSound(player, Sound.COW_HURT);
					SpectatorHandler.add(player);
					if(ProPlugin.getPlayers().size() == 1) {
						disable(ProPlugin.getPlayers().get(0));
					} else if(ProPlugin.getPlayers().isEmpty()) {
						disable(null);
					}
				}
				break;
			}
		}
		event.setCancelled(false);
	}
	
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if(event.getCause() == DamageCause.CUSTOM) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if(!SpectatorHandler.contains(player) && event.getTo().getY() >= 15) {
			PlayerMoveEvent.getHandlerList().unregister(this);
			MessageHandler.alert(AccountHandler.getPrefix(player) + " &6&lhas gotten to the top of the map");
			disable(player);
		}
	}
}
