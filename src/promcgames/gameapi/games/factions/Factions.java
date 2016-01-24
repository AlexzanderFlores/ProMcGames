package promcgames.gameapi.games.factions;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.gameapi.StatsHandler;
import promcgames.gameapi.games.factions.spawn.SpawnHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.AutoBroadcasts;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.StringUtil;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class Factions extends ProPlugin {
	public Factions() {
		super("Factions");
		addGroup("24/7");
		removeFlags();
		new SpawnHandler();
		new KitHandler();
		new Events();
		new CoinHandler();
		new MobSpawnerHandler();
		new VIPHandler();
		new HomeHandler();
		new Teleportation();
		new JellyLegs();
		new ObsidianDestroyer();
		new WarpHandler();
		new CreeperEggGiver();
		new Pokeball();
		new BorderHandler();
		new StatsHandler(DB.PLAYERS_STATS_FACTIONS);
		new CommandBase("feed", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.setFoodLevel(20);
				return true;
			}
		}.setRequiredRank(Ranks.PRO_PLUS);
		new CommandBase("setFactionsRank", 2, false) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String target = arguments[0];
						UUID uuid = AccountHandler.getUUID(target);
						if(uuid == null) {
							MessageHandler.sendMessage(sender, "&c" + target + " has never logged in before");
						} else {
							String rank = arguments[1].toUpperCase();
							Player player = ProPlugin.getPlayer(target);
							boolean online = player != null && ProMcGames.getPlugin() == Plugins.FACTIONS;
							if(rank.equals("VIP")) {
								if(online) {
									VIPHandler.setVIP(player);
								}
								if(DB.PLAYERS_FACTIONS_RANKS.isUUIDSet(uuid)) {
									DB.PLAYERS_FACTIONS_RANKS.updateString("rank", rank, "uuid", uuid.toString());
								} else {
									DB.PLAYERS_FACTIONS_RANKS.insert("'" + uuid.toString() + "', '" + rank + "'");
								}
								MessageHandler.sendMessage(sender, "Set " + target + " to " + rank);
							} else if(rank.equals("VIP_PLUS")) {
								if(online) {
									VIPHandler.setVIPPlus(player);
								}
								if(DB.PLAYERS_FACTIONS_RANKS.isUUIDSet(uuid)) {
									DB.PLAYERS_FACTIONS_RANKS.updateString("rank", rank, "uuid", uuid.toString());
								} else {
									DB.PLAYERS_FACTIONS_RANKS.insert("'" + uuid.toString() + "', '" + rank + "'");
								}
								MessageHandler.sendMessage(sender, "Set " + target + " to " + rank);
							} else if(rank.equals("NONE")) {
								if(online) {
									VIPHandler.remove(player);
								}
								DB.PLAYERS_FACTIONS_RANKS.deleteUUID(uuid);
								MessageHandler.sendMessage(sender, "Removed " + target + "'s factions rank");
							} else {
								MessageHandler.sendMessage(sender, "&cUnknown rank! Use &aVIP &cor &aVIP_PLUS");
							}
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		spawnHologram(-258.5, 282.5);
		spawnHologram(-238.5, 303.5);
		spawnHologram(-258.5, 324.5);
		spawnHologram(-280.5, 303.5);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				for(World world : Bukkit.getWorlds()) {
					world.setGameRuleValue("doDaylightCycle", "true");
				}
			}
		}, 20 * 2);
		AutoBroadcasts.addAlert("Want more perks? Get " + VIPHandler.getVIPPrefix() + " &aor " + VIPHandler.getVIPPlusPrefix() + " &b/buy");
	}
	
	private void spawnHologram(double x, double z) {
		World world = Bukkit.getWorlds().get(0);
		Hologram hologram = HologramsAPI.createHologram(ProMcGames.getInstance(), new Location(world, x, 73, z));
		hologram.appendTextLine(StringUtil.color("&c=-=-=-=-=-=-=-=-="));
		hologram.appendTextLine(StringUtil.color(""));
		hologram.appendTextLine(StringUtil.color("&eWelcome to &aProMc &eFactions!"));
		hologram.appendTextLine(StringUtil.color(""));
		hologram.appendTextLine(StringUtil.color("&eVote each day for cool perks! &b/vote"));
		hologram.appendTextLine(StringUtil.color("&eGet awesome benefits! &b/buy"));
		hologram.appendTextLine(StringUtil.color(""));
		hologram.appendTextLine(StringUtil.color("&c=-=-=-=-=-=-=-=-="));
	}
}
