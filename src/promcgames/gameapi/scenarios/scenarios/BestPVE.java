package promcgames.gameapi.scenarios.scenarios;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameKillEvent;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.games.uhc.HealthHandler;
import promcgames.gameapi.games.uhc.HostHandler;
import promcgames.gameapi.games.uhc.events.PlayerTimeOutEvent;
import promcgames.gameapi.scenarios.Scenario;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;

public class BestPVE extends Scenario {
	private static BestPVE instance = null;
	private static List<UUID> bestPVE = null;
	private static int counter = -1;
	private static final int minutes = 10;
	
	public BestPVE() {
		super("BestPVE", Material.GOLDEN_APPLE);
		instance = this;
		setInfo("At the start of the game all players are added to a list. Every " + minutes + " minutes after the game starts all players on that list gain 1 max heart and get healed 1 heart. If you take damage from any source you are removed from this list. The only way to be added back to this list is to kill another player.");
		bestPVE = new ArrayList<UUID>();
		new CommandBase("bestPVE", 0, 3) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(ProMcGames.getPlugin() == Plugins.UHC && sender instanceof Player) {
					Player player = (Player) sender;
					if(!HostHandler.isHost(player.getUniqueId()) && !Ranks.isStaff(player)) {
						MessageHandler.sendUnknownCommand(player);
						return true;
					}
				} else if(!Ranks.isStaff(sender)) {
					MessageHandler.sendUnknownCommand(sender);
					return true;
				}
				if(arguments.length == 0) {
					if(bestPVE.isEmpty()) {
						MessageHandler.sendMessage(sender, "&cThere are no players in the Best PVE list");
					} else {
						String players = "";
						int counter = 0;
						for(Player player : ProPlugin.getPlayers()) {
							if(bestPVE.contains(player.getUniqueId())) {
								players += AccountHandler.getPrefix(player) + ", ";
								++counter;
							}
						}
						MessageHandler.sendMessage(sender, "Players in the Best PVE list: (&e" + counter + "&a): " + players.substring(0, players.length() - 2));
					}
				} else if(arguments[0].equalsIgnoreCase("add") && arguments.length == 2) {
					String name = arguments[1];
					Player player = ProPlugin.getPlayer(name);
					if(player == null) {
						MessageHandler.sendMessage(sender, "&c" + name + " is not online");
					} else {
						add(player);
						MessageHandler.sendMessage(sender, "Added " + AccountHandler.getPrefix(player) + " &ato the Best PVE list");
					}
					return true;
				} else if(arguments[0].equalsIgnoreCase("remove") && arguments.length == 2) {
					String name = arguments[1];
					Player player = ProPlugin.getPlayer(name);
					if(player == null) {
						MessageHandler.sendMessage(sender, "&c" + name + " is not online");
					} else {
						remove(player);
						MessageHandler.sendMessage(sender, "&cRemoved " + AccountHandler.getPrefix(player) + " &cfrom the Best PVE list");
					}
					return true;
				}
				MessageHandler.sendMessage(sender, "&f/bestPVE &a- &eLists all players in the list");
				MessageHandler.sendMessage(sender, "&f/bestPVE add <name> &a- &eAdds a player to the list");
				MessageHandler.sendMessage(sender, "&f/bestPVE remove <name> &a- &eRemoves a player to the list");
				return true;
			}
		};
	}
	
	public static BestPVE getInstance() {
		if(instance == null) {
			new BestPVE();
		}
		return instance;
	}
	
	private void add(Player player) {
		if(!bestPVE.contains(player.getUniqueId())) {
			bestPVE.add(player.getUniqueId());
			MessageHandler.sendMessage(player, "&eYou have been added to the \"Best PVE\" list &f/sInfo");
		}
	}
	
	private void remove(Player player) {
		if(bestPVE.contains(player.getUniqueId())) {
			bestPVE.remove(player.getUniqueId());
			MessageHandler.sendMessage(player, "&cYou have been removed from the \"Best PVE\" list &f/sInfo");
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			add(player);
		}
		counter = 0;
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(counter > -1 && ++counter % (60 * minutes) == 0) {
			for(Player player : ProPlugin.getPlayers()) {
				if(bestPVE.contains(player.getUniqueId())) {
					player.setMaxHealth(player.getMaxHealth() + 2.0d);
					double newHealth = player.getHealth() + 2.0d;
					if(newHealth > player.getMaxHealth()) {
						player.setHealth(player.getMaxHealth());
					} else {
						player.setHealth(newHealth);
					}
					HealthHandler.updateHealth(player);
					MessageHandler.sendMessage(player, "&eYou have given 1 max heart and have been healed 1 heart due to being on the Best PVE list &f/sInfo");
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if(!event.isCancelled() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			remove(player);
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		add(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerTimeOut(PlayerTimeOutEvent event) {
		bestPVE.remove(event.getPlayer());
	}
}
