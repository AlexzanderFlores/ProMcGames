package promcgames.gameapi.games.factions;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.timed.TenSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class CreeperEggGiver implements Listener {
	public CreeperEggGiver() {
		new CommandBase("giveCreeperEgg", 2) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				UUID uuid = AccountHandler.getUUID(name);
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
				} else {
					try {
						int amount = Integer.valueOf(arguments[1]);
						if(DB.PLAYERS_FACTIONS_CREEPER_EGGS.isUUIDSet(uuid)) {
							amount += DB.PLAYERS_FACTIONS_CREEPER_EGGS.getInt("uuid", uuid.toString(), "amount");
							DB.PLAYERS_FACTIONS_CREEPER_EGGS.updateInt("amount", amount, "uuid", uuid.toString());
						} else {
							DB.PLAYERS_FACTIONS_CREEPER_EGGS.insert("'" + uuid.toString() + "', '" + amount + "'");
						}
						MessageHandler.sendMessage(sender, "Dispatched to give " + name + " " + amount + " more Creeper Eggs");
					} catch(NumberFormatException e) {
						return false;
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTenSecondTask(TenSecondTaskEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				for(String uuidString : DB.PLAYERS_FACTIONS_CREEPER_EGGS.getAllStrings("uuid")) {
					UUID uuid = UUID.fromString(uuidString);
					Player player = Bukkit.getPlayer(uuid);
					if(player != null) {
						int amount = DB.PLAYERS_FACTIONS_CREEPER_EGGS.getInt("uuid", uuidString, "amount");
						boolean given = false;
						for(ItemStack item : player.getInventory().getContents()) {
							if(item == null || item.getType() == Material.AIR) {
								player.getInventory().addItem(new ItemStack(Material.MONSTER_EGG, amount, (byte) 50));
								given = true;
								break;
							}
						}
						if(given) {
							MessageHandler.sendMessage(player, "&e&lYou have been given &b&l" + amount + " &e&lCreeper Eggs!");
							DB.PLAYERS_FACTIONS_CREEPER_EGGS.deleteUUID(uuid);
						} else {
							MessageHandler.sendMessage(player, "&c&lYou have Creeper Eggs waiting for you but you have no room in your inventory!");
						}
					}
				}
			}
		});
	}
}
