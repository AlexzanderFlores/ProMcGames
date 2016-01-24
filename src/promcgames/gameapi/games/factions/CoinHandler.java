package promcgames.gameapi.games.factions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.player.AsyncPlayerLeaveEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.AutoBroadcasts;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class CoinHandler implements Listener {
	private static Map<String, Integer> gained = null;
	private static Map<String, Integer> originalValues = null;
	
	public CoinHandler() {
		AutoBroadcasts.addAlert("View your coins with &b/coins [player name]");
		AutoBroadcasts.addAlert("Give coins with &b/coins give <name> <amount>");
		gained = new HashMap<String, Integer>();
		originalValues = new HashMap<String, Integer>();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				new CommandBase("coins", 0, 3) {
					@Override
					public boolean execute(CommandSender sender, String [] arguments) {
						if(arguments.length == 3) {
							if(sender instanceof Player) {
								if(arguments[0].equalsIgnoreCase("give")) {
									Player target = ProPlugin.getPlayer(arguments[1]);
									if(target == null) {
										MessageHandler.sendMessage(sender, "&c" + arguments[1] + " is not online");
									} else {
										try {
											int amount = Integer.valueOf(arguments[2]);
											Player player = (Player) sender;
											if(getCoins(player) >= amount) {
												addCoins(player, amount * -1);
												addCoins(target, amount);
												MessageHandler.sendMessage(target, "You were given &e" + amount + " &aCoins by " + AccountHandler.getPrefix(player));
												MessageHandler.sendMessage(player, "You gave " + AccountHandler.getPrefix(target) + " &e" + amount + " &aCoins");
 											} else {
 												MessageHandler.sendMessage(player, "&cYou do not have &e" + amount + " &cCoins");
 											}
										} catch(NumberFormatException e) {
											return false;
										}
									}
								} else {
									return false;
								}
							} else {
								MessageHandler.sendPlayersOnly(sender);
							}
						} else {
							Player player = null;
							if(arguments.length == 0) {
								if(sender instanceof Player) {
									player = (Player) sender;
								} else {
									MessageHandler.sendPlayersOnly(sender);
									return true;
								}
							} else if(arguments.length == 1) {
								player = ProPlugin.getPlayer(arguments[0]);
								if(player == null) {
									MessageHandler.sendMessage(sender, "&c" + arguments[0] + " is not online");
									return true;
								}
							}
							MessageHandler.sendMessage(sender, AccountHandler.getPrefix(player) + " &ahas &e" + getCoins(player) + " &aCoins");
							MessageHandler.sendMessage(sender, "Want to give coins? &e/coins give <name> <amount>");
						}
						return true;
					}
				};
			}
		}, 20);
		EventUtil.register(this);
	}
	
	public static int getCoins(Player player) {
		String name = player.getName();
		if(!originalValues.containsKey(name)) {
			originalValues.put(name, DB.PLAYERS_FACTIONS_COINS.getInt("uuid", player.getUniqueId().toString(), "coins"));
		}
		int amount = originalValues.get(name);
		if(gained.containsKey(name)) {
			amount += gained.get(name);
		}
		return amount;
	}
	
	public static void addCoins(Player player, int amount) {
		int amountGained = amount;
		String movement = amount <= 0 ? "&clost &a" : "&egained &a";
		String currency = amount == 1 ? " Coin" : " Coins";
		String name = player.getName();
		if(gained.containsKey(name)) {
			amountGained += gained.get(name);
		}
		gained.put(name, amountGained);
		MessageHandler.sendMessage(player, "You have " + movement + amount + currency + " going to &e" + getCoins(player) + " Coins");
	}
	
	@EventHandler
	public void onAsyncPlayerLeave(AsyncPlayerLeaveEvent event) {
		UUID uuid = event.getRealUUID();
		String name = event.getRealName();
		if(gained.containsKey(name)) {
			if(DB.PLAYERS_FACTIONS_COINS.isUUIDSet(uuid)) {
				int amount = gained.get(name) + DB.PLAYERS_FACTIONS_COINS.getInt("uuid", uuid.toString(), "coins");
				DB.PLAYERS_FACTIONS_COINS.updateInt("coins", amount, "uuid", uuid.toString());
			} else {
				DB.PLAYERS_FACTIONS_COINS.insert("'" + uuid.toString() + "', '" + gained.get(name) + "'");
			}
			gained.remove(name);
		}
		originalValues.remove(name);
	}
}
