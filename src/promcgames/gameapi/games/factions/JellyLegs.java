package promcgames.gameapi.games.factions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import promcgames.customevents.player.AsyncPlayerJoinEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class JellyLegs implements Listener {
	private List<UUID> players = null;
	
	public JellyLegs() {
		players = new ArrayList<UUID>();
		new CommandBase("giveJellyLegs", 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				UUID uuid = AccountHandler.getUUID(name);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + name + " is not online");
				} else if(DB.PLAYERS_FACTIONS_JELLY_LEGS.isUUIDSet(uuid)) {
					MessageHandler.sendMessage(sender, "&c" + name + " already has Jelly Legs");
				} else {
					DB.PLAYERS_FACTIONS_JELLY_LEGS.insert("'" + uuid.toString() + "'");
					MessageHandler.sendMessage(sender, "Gave " + name + " Jelly Legs");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("hasJellyLegs", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String target = arguments[0];
						UUID uuid = AccountHandler.getUUID(target);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + target + " has never logged in before");
						} else if(players.contains(uuid)) {
							MessageHandler.sendMessage(sender, target + " does have Jelly Legs");
						} else if(DB.PLAYERS_FACTIONS_JELLY_LEGS.isUUIDSet(uuid)) {
							MessageHandler.sendMessage(sender, target + " does have Jelly Legs");
						} else {
							MessageHandler.sendMessage(sender, "&c" + target + " does NOT have Jelly Legs");
						}
					}
				});
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if(!event.isCancelled() && event.getCause() == DamageCause.FALL && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(players.contains(player.getUniqueId())) {
				event.setCancelled(true);
			} else {
				MessageHandler.sendMessage(player, "&bDon't want fall damage? Get Jelly Legs! &e/buy");
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		if(DB.PLAYERS_FACTIONS_JELLY_LEGS.isUUIDSet(event.getPlayer().getUniqueId())) {
			players.add(event.getPlayer().getUniqueId());
		}
	}
}
