package promcgames.server.servers.building;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.games.skywars.islands.IslandHandler;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.scoreboard.SidebarScoreboardUtil;
import promcgames.server.CommandBase;
import promcgames.server.util.ConfigurationUtil;
import promcgames.server.util.FileHandler;
import promcgames.server.util.StringUtil;

public class Building extends ProPlugin {
	public Building() {
		super("Building");
		addGroup("24/7");
		new Events();
		new IslandHandler();
		removeFlags();
		setAllowLeavesDecay(false);
		setAllowDefaultMobSpawning(false);
		new CommandBase("defineFallingChest", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				Player player = (Player) sender;
				ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + player.getWorld().getName() + "/chests.yml");
				int size = 0;
				if(arguments.length == 0) {
					size = config.getConfig().getKeys(false).size() + 1;
				} else {
					try {
						size = Integer.valueOf(arguments[0]);
					} catch(NumberFormatException e) {
						return false;
					}
				}
				Location location = player.getLocation();
				config.getConfig().set(size + ".x", location.getX());
				config.getConfig().set(size + ".y", location.getY());
				config.getConfig().set(size + ".z", location.getZ());
				if(config.save()) {
					MessageHandler.sendMessage(player, "Set chest spawn #" + size);
				} else {
					MessageHandler.sendMessage(player, "&cFailed to set chest spawn #" + size);
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("defineGameSpawn", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				Player player = (Player) sender;
				ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + player.getWorld().getName() + "/spawns.yml");
				int size = 0;
				if(arguments.length == 0) {
					size = config.getConfig().getKeys(false).size() + 1;
				} else {
					try {
						size = Integer.valueOf(arguments[0]);
					} catch(NumberFormatException e) {
						return false;
					}
				}
				Location location = player.getLocation();
				config.getConfig().set(size + ".x", location.getX());
				config.getConfig().set(size + ".y", location.getY());
				config.getConfig().set(size + ".z", location.getZ());
				config.getConfig().set(size + ".yaw", location.getYaw());
				config.getConfig().set(size + ".pitch", location.getPitch());
				if(config.save()) {
					MessageHandler.sendMessage(player, "Set spawn #" + size);
				} else {
					MessageHandler.sendMessage(player, "&cFailed to set spawn #" + size);
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("nameItem", 1, -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				ItemStack item = player.getItemInHand();
				if(item == null || item.getType() == Material.AIR) {
					MessageHandler.sendMessage(player, "&cYou must be holding an item");
					return true;
				}
				ItemMeta meta = item.getItemMeta();
				if(arguments[0].equalsIgnoreCase("lore")) {
					List<String> lores = meta.getLore();
					if(lores == null) {
						lores = new ArrayList<String>();
					}
					String lore = "";
					for(int a = 1; a < arguments.length; ++a) {
						lore += arguments[a] + " ";
					}
					lores.add(StringUtil.color(lore.substring(0, lore.length() - 1)));
					meta.setLore(lores);
				} else {
					String name = "";
					for(int a = 0; a < arguments.length; ++a) {
						name += arguments[a] + " ";
					}
					meta.setDisplayName(StringUtil.color(name.substring(0, name.length() - 1)));
				}
				item.setItemMeta(meta);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("deployHubBuild", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(player.getWorld().getName().equals("newhub")) {
					String path = Bukkit.getWorldContainer().getPath() + "/../resources/maps/hub";
					File file = new File(path);
					if(file.isDirectory()) {
						MessageHandler.sendMessage(player, "Hub build found in Resources, deleting...");
						FileHandler.delete(file);
					}
					FileHandler.copyFolder(Bukkit.getWorldContainer().getPath() + "/" + player.getWorld().getName(), path);
					MessageHandler.sendMessage(player, "Copied your current world to the Resources folder, restart the hubs");
				} else {
					MessageHandler.sendMessage(player, "&cThe world you are in is not valid for deployment");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("deployVersusBuild", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(player.getWorld().getName().equals("versus")) {
					String path = Bukkit.getWorldContainer().getPath() + "/../resources/maps/versus";
					File file = new File(path);
					if(file.isDirectory()) {
						MessageHandler.sendMessage(player, "Versus build found in Resources, deleting...");
						FileHandler.delete(file);
					}
					FileHandler.copyFolder(Bukkit.getWorldContainer().getPath() + "/" + player.getWorld().getName(), path);
					MessageHandler.sendMessage(player, "Copied your current world to the Resources folder, restart versus");
				} else {
					MessageHandler.sendMessage(player, "&cThe world you are in is not valid for deployment");
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		ProMcGames.setSidebar(new SidebarScoreboardUtil(" &5Elite UHC ") {
			@Override
			public void update() {
				setText("Border Radius", 1500);
				setText("Playing", 54);
				setText("&6PVE", 14);
			}
		});
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import uhclobby normal");
		/*UHC.spawnHologram(Bukkit.getWorld("uhclobby"), 8.5, 29, 0.5);
		UHC.spawnHologram(Bukkit.getWorld("uhclobby"), 0.5, 29, -7.5);
		UHC.spawnHologram(Bukkit.getWorld("uhclobby"), -7.5, 29, 0.5);
		UHC.spawnHologram(Bukkit.getWorld("uhclobby"), 0.5, 29, 8.5);*/
	}
	
	@Override
	public void disable() {
		FileHandler.delete(new File(ProMcGames.getInstance().getDataFolder().getPath() + "/../Multiverse-Core/worlds.yml"));
		FileHandler.delete(new File(ProMcGames.getInstance().getDataFolder().getPath() + "/../Essentials/userdata"));
		super.disable();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		super.onPlayerJoin(event);
		event.getPlayer().setScoreboard(ProMcGames.getScoreboard());
		ProMcGames.getSidebar().setText(getText(event.getPlayer()), new Random().nextInt(10) + 1);
		ProMcGames.getSidebar().update();
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		super.onPlayerLeave(event);
		ProMcGames.getSidebar().removeText(getText(event.getPlayer()));
		ProMcGames.getSidebar().update();
	}
	
	private static String getText(Player player) {
		String text = AccountHandler.getRank(player).getColor() + Disguise.getName(player);
		if(text.length() > 16) {
			text = text.substring(0, 16);
		}
		return text;
	}
}
