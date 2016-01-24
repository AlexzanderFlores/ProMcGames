package promcgames.gameapi.games.factions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.player.AsyncPlayerLeaveEvent;
import promcgames.gameapi.TeleportCoolDown;
import promcgames.player.MessageHandler;
import promcgames.server.CommandBase;
import promcgames.server.ProMcGames;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.ConfigurationUtil;
import promcgames.server.util.EventUtil;

public class HomeHandler implements Listener {
	private Map<String, Map<Integer, Location>> homes = null;
	
	public HomeHandler() {
		homes = new HashMap<String, Map<Integer, Location>>();
		new CommandBase("setHome", 1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				try {
					Player player = (Player) sender;
					final int index = Integer.valueOf(arguments[0]);
					if(VIPHandler.isVIPPlus(player) && index > 5) {
						MessageHandler.sendMessage(player, "&cVIP+ players can only have up to 5 homes");
						return true;
					} else if(VIPHandler.isVIP(player) && !VIPHandler.isVIPPlus(player) && index > 2) {
						MessageHandler.sendMessage(player, "&cVIP players can only have up to 2 homes");
						return true;
					} else if(!VIPHandler.hasRank(player) && index > 1) {
						MessageHandler.sendMessage(player, "&cNon-VIP and non-VIP+ players can only have 1 home");
						return true;
					}
					Map<Integer, Location> home = homes.get(player.getName());
					if(home == null) {
						home = new HashMap<Integer, Location>();
					}
					home.put(index, player.getLocation());
					homes.put(player.getName(),  home);
					MessageHandler.sendMessage(player, "Home &e#" + index + " &aset");
				} catch(NumberFormatException e) {
					return false;
				}
				return true;
			}
		}.enableDelay(1);
		new CommandBase("home", 0, 1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = (Player) sender;
						int index = 1;
						if(arguments.length == 1) {
							try {
								index = Integer.valueOf(arguments[0]);
							} catch(NumberFormatException e) {
								MessageHandler.sendMessage(player, "&cYour home index must be a number");
								return;
							}
						}
						if(index <= 0) {
							MessageHandler.sendMessage(player, "&cHome numbers must be positive");
							return;
						} else if(VIPHandler.isVIPPlus(player) && index > 5) {
							MessageHandler.sendMessage(player, "&cVIP+ players can only have up to 5 homes");
							return;
						} else if(VIPHandler.isVIP(player) && !VIPHandler.isVIPPlus(player) && index > 2) {
							MessageHandler.sendMessage(player, "&cVIP players can only have up to 2 homes");
							return;
						} else if(!VIPHandler.hasRank(player) && index > 1) {
							MessageHandler.sendMessage(player, "&cNon-VIP and non-VIP+ players can only have 1 home");
							return;
						}
						Location location = null;
						Map<Integer, Location> home = homes.get(player.getName());
						if(home == null) {
							home = new HashMap<Integer, Location>();
							homes.put(player.getName(), home);
						}
						if(home.containsKey(index)) {
							location = home.get(index);
						} else {
							ConfigurationUtil config = new ConfigurationUtil(ProMcGames.getInstance().getDataFolder().getPath() + "/homes.yml");
							String key = player.getUniqueId().toString() + "." + index + ".";
							String worldName = config.getConfig().getString(key + ".world");
							if(worldName == null) {
								MessageHandler.sendMessage(player, "&cHome &e#" + index + " &cis not set");
								MessageHandler.sendMessage(player, "Set homes with &b/setHome [index]");
							} else {
								World world = Bukkit.getWorld(worldName);
								if(world == null) {
									MessageHandler.sendMessage(player, "&cThe world for this home no longer exists");
								} else {
									double x = config.getConfig().getDouble(key + ".x");
									double y = config.getConfig().getDouble(key + ".y");
									double z = config.getConfig().getDouble(key + ".z");
									location = new Location(world, x, y, z);
									home.put(index, location);
									homes.put(player.getName(), home);
								}
							}
						}
						if(location == null) {
							MessageHandler.sendMessage(player, "&cFailed to teleport to home");
						} else {
							new TeleportCoolDown(player, location, 5);
						}
					}
				});
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		String name = event.getRealName();
		UUID uuid = event.getUUID();
		if(homes.containsKey(name)) {
			Map<Integer, Location> home = homes.get(name);
			if(home != null) {
				ConfigurationUtil config = new ConfigurationUtil(ProMcGames.getInstance().getDataFolder().getPath() + "/homes.yml");
				for(int index : home.keySet()) {
					Location location = home.get(index);
					String key = uuid.toString() + "." + index + ".";
					config.getConfig().set(key + ".world", location.getWorld().getName());
					config.getConfig().set(key + ".x", location.getBlockX());
					config.getConfig().set(key + ".y", location.getBlockY());
					config.getConfig().set(key + ".z", location.getBlockZ());
				}
				config.save();
				home.clear();
			}
			homes.remove(name);
		}
	}
}
