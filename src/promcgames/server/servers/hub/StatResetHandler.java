package promcgames.server.servers.hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

public class StatResetHandler implements Listener {
	private List<Plugins> games = null;
	private List<String> lifetime = null;
	private Map<String, Plugins> firstSelection = null;
	
	public StatResetHandler() {
		new CommandBase("statReset", 0, 2) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(sender instanceof Player) {
					if(arguments.length == 0) {
						if(ProMcGames.getPlugin() == Plugins.HUB) {
							Player player = (Player) sender;
							if(!(player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR)) {
								MessageHandler.sendMessage(sender, "&cYou cannot be holding anything in your hand to run this command");
								return true;
							}
							selectLifetime(player);
						} else {
							MessageHandler.sendMessage(sender, "&cYou can only run this command from the hub");
						}
					} else {
						return false;
					}
				} else {
					if(arguments.length == 2) {
						UUID uuid = AccountHandler.getUUID(arguments[0]);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
						} else {
							if(!DB.PLAYERS_STAT_RESETS.isUUIDSet(uuid)) {
								DB.PLAYERS_STAT_RESETS.insert("'" + uuid.toString() + "', '" + (arguments[1].equals("true") ? 1 : 0) + "'");
							}
						}
					} else if(arguments.length == 0) {
						return false;
					}
				}
				return true;
			}
		};
		if(ProMcGames.getPlugin() == Plugins.HUB) {
			games = new ArrayList<Plugins>();
			games.add(Plugins.SURVIVAL_GAMES);
			games.add(Plugins.CLAN_BATTLES);
			games.add(Plugins.KIT_PVP);
			games.add(Plugins.VERSUS);
			games.add(Plugins.SKY_WARS);
			games.add(Plugins.ARCADE);
			firstSelection = new HashMap<String, Plugins>();
			lifetime = new ArrayList<String>();
			EventUtil.register(this);
		}
	}
	
	private void selectLifetime(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9, "Select Type");
		inventory.setItem(2, new ItemCreator(Material.SKULL_ITEM, 3).setName("&bLifetime Stats").getItemStack());
		inventory.setItem(6, new ItemCreator(Material.SKULL_ITEM, 3).setName("&eMonthly Stats").getItemStack());
		player.openInventory(inventory);
	}
	
	private void openInventory(Player player, String title) {
		Inventory inventory = Bukkit.createInventory(player, ItemUtil.getInventorySize(games.size()), ChatColor.stripColor(title));
		int counter = 0;
		for(Plugins game : games) {
			inventory.setItem(counter, new ItemCreator(new ItemStack(Material.STAINED_GLASS, ++counter, (byte) 4)).setName(game.toString().replace("_", " ")).getItemStack());
		}
		player.openInventory(inventory);
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		if(event.getTitle().equals("Select Type")) {
			String item = event.getItemTitle();
			lifetime.remove(player.getName());
			if(item.contains("Lifetime")) {
				lifetime.add(player.getName());
			}
			openInventory(player, "Select your game");
			event.setCancelled(true);
		} else if(event.getTitle().equals("Select your game")) {
			firstSelection.put(player.getName(), Plugins.valueOf(event.getItem().getItemMeta().getDisplayName().replace(" ", "_")));
			openInventory(player, "Confirm your game");
			event.setCancelled(true);
		} else if(event.getTitle().equals("Confirm your game")) {
			Plugins clicked = Plugins.valueOf(event.getItem().getItemMeta().getDisplayName().replace(" ", "_"));
			if(clicked == firstSelection.get(player.getName())) {
				firstSelection.remove(player.getName());
				if(Ranks.isStaff(player) || DB.PLAYERS_STAT_RESETS.isUUIDSet(player.getUniqueId())) {
					boolean selectedLifetime = lifetime.contains(player.getName());
					DB table = null;
					try {
						if(selectedLifetime) {
							table = DB.valueOf("PLAYERS_STATS_" + clicked.toString());
						} else {
							table = DB.valueOf("PLAYERS_STATS_" + clicked.toString() + "_MONTHLY");
						}
					} catch(IllegalArgumentException e) {
						MessageHandler.sendMessage(player, "&cThis game does not support monthly stats");
						player.closeInventory();
						event.setCancelled(true);
						return;
					}
					if(table.isUUIDSet(player.getUniqueId())) {
						Bukkit.getLogger().info(table.toString());
						table.deleteUUID(player.getUniqueId());
						String [] keys = new String [] {"uuid", "unlimited"};
						String [] values = new String [] {player.getUniqueId().toString(), "1"};
						if(DB.PLAYERS_STAT_RESETS.isUUIDSet(player.getUniqueId()) && !DB.PLAYERS_STAT_RESETS.isKeySet(keys, values)) {
							DB.PLAYERS_STAT_RESETS.deleteUUID(player.getUniqueId());
							MessageHandler.sendMessage(player, "&cYour stat reset has been removed.");
							MessageHandler.sendMessage(player, "Get unlimited stat resets: &b/buy");
						}
						MessageHandler.sendMessage(player, "Deleted your " + (selectedLifetime ? "LIFETIME" : "MONTHLY") + " stats for " + clicked.toString().replace("_", " "));
					} else {
						MessageHandler.sendMessage(player, "&cYou have no stats logged for " + clicked.toString().replace("_", " "));
					}
				} else {
					MessageHandler.sendMessage(player, "&cYou do not have pending stat reset passes, get them with &b/buy");
				}
			} else {
				MessageHandler.sendMessage(player, "&cYour two selections do not match");
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
}
