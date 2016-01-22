package promcgames.gameapi.games.factions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import promcgames.customevents.game.SpawnTNTBlocksEvent;
import promcgames.customevents.player.PlayerHeadshotEvent;
import promcgames.customevents.player.PlayerStaffModeEvent;
import promcgames.customevents.player.PlayerStaffModeEvent.StaffModeEventType;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.AntiAboveNether;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class Events implements Listener {
	private List<String> pearlDelay = null;
	
	public Events() {
		pearlDelay = new ArrayList<String>();
		new AntiAboveNether();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onSpawnTNTBlock(SpawnTNTBlocksEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerHeadshot(PlayerHeadshotEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if(event.getEntity() instanceof EnderPearl) {
			EnderPearl pearl = (EnderPearl) event.getEntity();
			if(pearl.getShooter() instanceof Player) {
				Player player = (Player) pearl.getShooter();
				final String name = player.getName();
				if(pearlDelay.contains(name)) {
					MessageHandler.sendMessage(player, "&cYou cannot throw ender pearls that fast");
					event.setCancelled(true);
				} else {
					pearlDelay.add(name);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							pearlDelay.remove(name);
						}
					}, 20 * 2);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerStaffMode(PlayerStaffModeEvent event) {
		if(event.getType() == StaffModeEventType.ENABLE && AccountHandler.getRank(event.getPlayer()) == Ranks.HELPER) {
			MessageHandler.sendMessage(event.getPlayer(), Ranks.MODERATOR.getNoPermission());
			event.setCancelled(true);
		}
	}
}
