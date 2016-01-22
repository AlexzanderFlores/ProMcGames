package promcgames.gameapi.games.kitpvp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.game.GameKillEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.Particles.ParticleTypes;
import promcgames.player.account.AccountHandler;
import promcgames.server.DB;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class AutoRegenHandler implements Listener {
	private Map<String, Integer> regenPasses = null;
	private List<String> alertDelayed = null;
	private List<String> delayed = null;
	private int alertDelay = 60;
	private int delay = 3;
	
	public AutoRegenHandler(double x, double y, double z) {
		regenPasses = new HashMap<String, Integer>();
		alertDelayed = new ArrayList<String>();
		delayed = new ArrayList<String>();
		World world = Bukkit.getWorlds().get(0);
		Location location = new Location(world, x, y, z);
		new NPCEntity(EntityType.ZOMBIE, "&aLoad Auto Regen Passes", location, Material.GOLDEN_APPLE) {
			@Override
			public void onInteract(final Player player) {
				if(delayed.contains(Disguise.getName(player))) {
					MessageHandler.sendMessage(player, "&cYou can only click on this every &e" + delay + " &cseconds");
				} else {
					delayed.add(Disguise.getName(player));
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							delayed.remove(Disguise.getName(player));
						}
					}, 20 * delay);
					if(regenPasses.containsKey(Disguise.getName(player))) {
						updateAndRemove(player);
						MessageHandler.sendMessage(player, "Disabled auto regen passes for you");
					} else {
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								if(DB.PLAYERS_KIT_PVP_AUTO_REGEN.isUUIDSet(Disguise.getUUID(player))) {
									int amount = DB.PLAYERS_KIT_PVP_AUTO_REGEN.getInt("uuid", Disguise.getUUID(player).toString(), "amount");
									regenPasses.put(Disguise.getName(player), amount);
									if(alertDelayed.contains(Disguise.getName(player))) {
										MessageHandler.sendMessage(player, "&bLoaded regen passes");
									} else {
										alertDelayed.add(Disguise.getName(player));
										new DelayedTask(new Runnable() {
											@Override
											public void run() {
												alertDelayed.remove(Disguise.getName(player));
											}
										}, 20 * alertDelay);
										MessageHandler.alert(AccountHandler.getRank(player).getColor() + Disguise.getName(player) + " &ehas loaded regen passes gotten from &c/vote");
									}
								} else {
									MessageHandler.sendMessage(player, "&cYou have no auto regen passes! You can get some with &e/vote &cand &e/vote shop");
								}
							}
						});
					}
				}
			}
		};
		EventUtil.register(this);
	}
	
	private void updateAndRemove(Player player) {
		final String name = Disguise.getName(player);
		if(regenPasses.containsKey(name)) {
			final int amount = regenPasses.get(name);
			final UUID uuid = Disguise.getUUID(player);
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					boolean isInTable = DB.PLAYERS_KIT_PVP_AUTO_REGEN.isUUIDSet(uuid);
					if(amount > 0 && isInTable) {
						DB.PLAYERS_KIT_PVP_AUTO_REGEN.updateInt("amount", amount, "uuid", uuid.toString());
					} else if(amount <= 0 && isInTable){
						DB.PLAYERS_KIT_PVP_AUTO_REGEN.deleteUUID(uuid);
					}
					regenPasses.remove(name);
				}
			});
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		Player killer = event.getPlayer();
		if(regenPasses.containsKey(killer.getName()) && regenPasses.get(killer.getName()) > 0) {
			int amount = regenPasses.get(killer.getName()) - 1;
			regenPasses.put(killer.getName(), amount);
			killer.setHealth(killer.getMaxHealth());
			ParticleTypes.FLAME.displaySpiral(killer.getLocation());
			MessageHandler.sendMessage(killer, "&eYou now have &c" + amount + " &eauto regen passes left");
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		updateAndRemove(event.getPlayer());
	}
}