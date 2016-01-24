package promcgames.gameapi.games.uhc;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scoreboard.Team;

import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.player.PostPlayerJoinEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.CommandBase;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.StringUtil;

public class HealthHandler implements Listener {
	public HealthHandler() {
		new CommandBase("health", 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = ProPlugin.getPlayer(arguments[0]);
				if(player == null) {
					MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online");
				} else {
					MessageHandler.sendMessage(sender, AccountHandler.getPrefix(player) + " &ahas &c" + (int) player.getHealth() + " &ahealth");
					updateHealth(player);
				}
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static void updateHealth() {
		for(Player player : ProPlugin.getPlayers()) {
			updateHealth(player);
		}
	}
	
	public static void updateHealth(Player player) {
		String color = AccountHandler.getRank(player).getColor().toString();
		Team team = TeamHandler.getTeam(player);
		if(team != null) {
			color = team.getPrefix();
		}
		//Bukkit.getLogger().info("Prefix: \"" + color + "\"");
		String name = StringUtil.color(color) + player.getName();
		name += " " + ChatColor.WHITE + (int) player.getHealth();
		if(name.length() > 16) {
			name = name.substring(0, 11) + " " + ChatColor.WHITE + (int) player.getHealth();
		}
		try {
			player.setPlayerListName(name);
		} catch(IllegalArgumentException e) {
			
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		updateHealth();
	}
	
	@EventHandler
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		if(!SpectatorHandler.contains(event.getPlayer())) {
			updateHealth(event.getPlayer());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player && !event.isCancelled()) {
			Player player = (Player) event.getEntity();
			final String name = player.getName();
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					updateHealth(ProPlugin.getPlayer(name));
				}
			});
		}
	}
	
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if(event.getEntity() instanceof Player && !event.isCancelled()) {
			Player player = (Player) event.getEntity();
			final String name = player.getName();
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					updateHealth(ProPlugin.getPlayer(name));
				}
			});
		}
	}
}
