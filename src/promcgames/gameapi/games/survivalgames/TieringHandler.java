package promcgames.gameapi.games.survivalgames;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import promcgames.ProMcGames;
import promcgames.gameapi.MiniGame;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.survivalgames.kits.premium.TrackerKit;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.Loading;

public class TieringHandler {
	public static List<Location> tierOne = null;
	private List<Location> announced = null;
	private static int percentage = 25;
	
	public TieringHandler() {
		new Loading("Chest Tiers", "/tiering");
		tierOne = new ArrayList<Location>();
		String map = SurvivalGames.arenaCenter.getWorld().getName().replace("new_", "");
		int size = DB.NETWORK_SG_CHESTS.getSize("map_name", map);
		for(String location : DB.NETWORK_SG_CHESTS.getOrdered("times_opened", "location", "map_name", map, Math.round(percentage * 100.0 / size))) {
			String [] split = location.split(",");
			double x = Double.valueOf(split[0]);
			double y = Double.valueOf(split[1]);
			double z = Double.valueOf(split[2]);
			Block block = new Location(SurvivalGames.arenaCenter.getWorld(), x, y, z).getBlock();
			if(block.getRelative(0, 1, 0).getType() == Material.AIR) {
				tierOne.add(block.getLocation());
			} else {
				DB.NETWORK_SG_CHESTS.delete(new String [] {"location", "map_name"}, new String [] {location, map});
			}
		}
		if(tierOne.size() >= 10) {
			announced = new ArrayList<Location>();
			for(Player player : MiniGame.getPlayers()) {
				if(announced.size() >= 5) {
					break;
				} else if(Ranks.ELITE.hasRank(player)) {
					Location toAnnounce = null;
					do {
						toAnnounce = tierOne.get(new Random().nextInt(tierOne.size()));
					} while(announced.contains(toAnnounce));
					announced.add(toAnnounce);
					MessageHandler.alert(Ranks.ELITE.getPrefix() + "&cperk: Located a tier 1 chest!");
				}
			}
			MessageHandler.alert("&eRun \"&c/tiering&e\" to view the locations!");
		}
		new CommandBase("tiering", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0) {
					explain(player);
					if(announced != null && !announced.isEmpty()) {
						MessageHandler.sendMessage(player, "&eTier 1 locations found due to " + announced.size() + " " + Ranks.ELITE.getPrefix() + "&ebeing in this game");
						for(int a = 0; a < announced.size(); ++a) {
							String place = getLocation(announced.get(a));
							if(SpectatorHandler.contains(player)) {
								MessageHandler.sendMessage(player, place);
							} else {
								ChatClickHandler.sendMessageToRunCommand(player, " &c&lCLICK TO TRACK", "Click to track", "/tiering " + a, place);
							}
						}
					}
				} else if(TrackerKit.isEnabled()) {
					MessageHandler.sendMessage(player, "&cCannot track while the \"&eTracker Kit&c\" is enabled");
				} else {
					try {
						int id = Integer.valueOf(arguments[0]);
						if(announced.get(id) == null) {
							MessageHandler.sendMessage(player, "&cThere is no tier 1 ID \"&e" + id + "&c\"");
							MessageHandler.sendMessage(player, "&cTo track a tier 1 chest do \"&e/tiering&c\"");
						} else {
							if(!player.getInventory().contains(Material.COMPASS)) {
								player.getInventory().addItem(new ItemCreator(Material.COMPASS).setName("&fChest Tracker").getItemStack());
							}
							Location location = announced.get(id);
							player.setCompassTarget(location);
							MessageHandler.sendMessage(player, "Your compass is now pointing to:");
							MessageHandler.sendMessage(player, getLocation(location));
						}
					} catch(NumberFormatException e) {
						MessageHandler.sendMessage(player, "&f/tiering <tier 1 ID>");
					}
				}
				return true;
			}
		};
		ProMcGames.getSidebar().setText(new String [] {"  ",
				"&aTier1 Count:",
				"&c" + tierOne.size() + " Chest" + (tierOne.size() == 1 ? "" : "s"),
				"&b/tiering"}, -4);
	}
	
	public static void explain(Player player) {
		MessageHandler.sendLine(player, "&6");
		MessageHandler.sendMessage(player, "Our Tiering system is different from most servers.");
		MessageHandler.sendMessage(player, "Tier locations change to keep things different.");
		MessageHandler.sendMessage(player, "The least opened &e" + percentage + "% &aof chests are tier 1, the rest are tier 2");
		MessageHandler.sendMessage(player, "Tier 1 chests contain Iron Armor, Diamonds, Iron Ingots and Golden Apples");
		MessageHandler.sendLine(player, "&6");
	}
	
	private String getLocation(Location location) {
		return "&b" + location.getBlockX() + "&e, &b" + location.getBlockY() + "&e, &b" + location.getBlockZ();
	}
}
