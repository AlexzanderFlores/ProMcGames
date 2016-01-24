package promcgames.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.AutoBroadcasts;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.TimeUtil;

public class VotingHandler implements Listener {
	private static String name = null;
	
	public VotingHandler() {
		AutoBroadcasts.addAlert("Vote for us daily to get cool stuff &b/vote");
		new CommandBase("vote", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0) {
					MessageHandler.sendMessage(player, "Vote at &ehttp://minecraftservers.org/server/141907");
					MessageHandler.sendMessage(player, "Want to view all of your voting perks? &c/vote perks");
				} else if(arguments[0].equalsIgnoreCase("top")) {
					if(ProMcGames.getPlugin() == Plugins.HUB) {
						player.teleport(new Location(player.getWorld(), -102.5, 126, -138.5, -360.0f, 0.0f));
					} else {
						MessageHandler.sendMessage(player, "&cYou can only run this command in the hub, &e/hub");
					}
				} else if(arguments[0].equalsIgnoreCase("perks")) {
					display(player);
				}
				return true;
			}
		};
		new CommandBase("addVotePass", 2, false) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						UUID uuid = AccountHandler.getUUID(arguments[0]);
						try {
							int amount = Integer.valueOf(arguments[1]);
							if(uuid == null) {
								MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
							} else if(DB.PLAYERS_VOTES.isUUIDSet(uuid)){
								amount += DB.PLAYERS_VOTES.getInt("uuid", uuid.toString(), "votes");
								DB.PLAYERS_VOTES.updateInt("votes", amount, "uuid", uuid.toString());
							} else {
								DB.PLAYERS_VOTES.insert("'" + uuid.toString() + "', '" + TimeUtil.getTime() + "', '" + amount + "'");
							}
							Player player = Bukkit.getPlayer(uuid);
							if(player != null) {
								MessageHandler.sendMessage(player, "&a+&c" + arguments[1] + " &aVote Passes! You now have &c" + amount);
							}
						} catch(NumberFormatException e) {
							return;
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	public static void display(Player player) {
		if(name == null) {
			name = "Voting Perks";
		}
		Inventory inventory = Bukkit.createInventory(player, 9 * 4, name);
		inventory.setItem(1, new ItemCreator(Material.DIAMOND).setName("&eYou get all of these!").setLores(new String [] {
			"&aVoting just once gives you all",
			"&aof the perks listed below!"
		}).getItemStack());
		inventory.setItem(3, new ItemCreator(Material.IRON_INGOT).setAmount(2).setName("&eVote Link").setLores(new String [] {
			"&aClick to get the voting link"
		}).getItemStack());
		inventory.setItem(5, new ItemCreator(Material.EMERALD).setAmount(3).setName("&eDaily Vote Goal").setLores(new String [] {
			"&aClick for info about our",
			"&adaily vote goal"
		}).getItemStack());
		inventory.setItem(7, new ItemCreator(Material.GOLD_INGOT).setAmount(4).setName("&eVoting Top 8").setLores(new String [] {
			"&aClick to view the top 8",
			"&aplayers in terms of voting"
		}).getItemStack());
		inventory.setItem(19, new ItemCreator(Material.REDSTONE_TORCH_ON).setName("&e+1 &aHub Sponsor").setLores(new String [] {
			"&aVoting gives you &e1 &aHub Sponsor!"
		}).getItemStack());
		inventory.setItem(21, new ItemCreator(Material.GOLDEN_APPLE).setName("&e+10 &aAuto Regen Kit PVP Passes").setLores(new String [] {
			"&aVoting gives you &e10 &aauto",
			"&aregen Kit PVP passes!"
		}).getItemStack());
		inventory.setItem(23, new ItemCreator(Material.FEATHER).setName("&e+20 &aHub Parkour Checkpoints").setLores(new String [] {
			"&aVoting gives you &e20 &ahub",
			"&aparkour checkpoints!"
		}).getItemStack());
		inventory.setItem(25, new ItemCreator(Material.REDSTONE_TORCH_ON).setName("&e+3 &aAuto Sponsor SG Passes").setLores(new String [] {
			"&aVoting gives you &e3 &aauto",
			"&asponsor passes for",
			"&aSurvival Games!"
		}).getItemStack());
		inventory.setItem(27, new ItemCreator(Material.MONSTER_EGG, 50).setName("&e+5 &aMap Voting Passes").setLores(new String [] {
			"Voting gives you &e5 &amap voting",
			"passes! Used to vote on unlisted maps!"
		}).getItemStack());
		inventory.setItem(29, new ItemCreator(Material.NAME_TAG).setName("&e+1 &aFactions Voting Key").setLores(new String [] {
			"&aVoting gives you &e1 &aFactions",
			"&avoting key!"
		}).getItemStack());
		inventory.setItem(31, new ItemCreator(Material.GOLD_INGOT).setName("&e+50 &aFactions Coins").setLores(new String [] {
			"&aVoting gives you &e50 &aFactions coins!"
		}).getItemStack());
		inventory.setItem(33, new ItemCreator(Material.MONSTER_EGG).setName("&e+5 &aFactions Pokeballs").setLores(new String [] {
			"&aVoting gives you &e5 &aFactions",
			"&aPokeballs, used to capture & move",
			"&afriendly mobs!"
		}).getItemStack());
		inventory.setItem(35, new ItemCreator(Material.BEDROCK).setName("&7Coming soon").getItemStack());
		player.openInventory(inventory);
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(name)) {
			Material type = event.getItem().getType();
			if(type == Material.IRON_INGOT) {
				event.getPlayer().closeInventory();
				MessageHandler.sendMessage(event.getPlayer(), "Vote at &ehttp://minecraftservers.org/server/141907");
			} else if(type == Material.EMERALD) {
				event.getPlayer().closeInventory();
				event.getPlayer().chat("/voteGoal");
			} else if(type == Material.GOLD_INGOT && event.getItem().getAmount() == 4) {
				event.getPlayer().closeInventory();
				event.getPlayer().chat("/vote top");
			} else {
				display(event.getPlayer());
			}
			event.setCancelled(true);
		}
	}
}
