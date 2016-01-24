package promcgames.gameapi.games.versus;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.PlayerViewStatsEvent;
import promcgames.gameapi.games.versus.events.BattleEndEvent;
import promcgames.gameapi.games.versus.kits.VersusKit;
import promcgames.player.MessageHandler;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.DoubleUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class VersusStats implements Listener {
	public VersusStats() {
		new CommandBase("versusStats", 0, 1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					if(!LobbyHandler.isInLobby(player)) {
						MessageHandler.sendMessage(player, "&cYou cannot open this while in a match");
						return true;
					}
				}
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						Player target = null;
						UUID uuid = null;
						if(arguments.length == 0) {
							if(sender instanceof Player) {
								Player player = (Player) sender;
								target = player;
								uuid = player.getUniqueId();
							} else {
								MessageHandler.sendPlayersOnly(sender);
								return;
							}
						} else if(arguments.length == 1) {
							String name = arguments[0];
							target = ProPlugin.getPlayer(name);
							if(target == null) {
								MessageHandler.sendMessage(sender, "&c" + name + " is not online");
								return;
							} else {
								uuid = target.getUniqueId();
							}
						}
						Player player = (Player) sender;
						String [] keys = new String [] {"uuid", "kit"};
						Inventory inventory = LobbyHandler.getKitSelectorInventory(player, "Versus Stats - " + target.getName(), false);
						List<VersusKit> kits = VersusKit.getKits();
						for(int a = 0; a < kits.size(); ++a) {
							VersusKit kit = kits.get(a);
							String [] values = new String [] {uuid.toString(), kit.getName()};
							int wins = DB.PLAYERS_VERSUS_STATS.getInt(keys, values, "wins");
							int losses = DB.PLAYERS_VERSUS_STATS.getInt(keys, values, "losses");
							inventory.setItem(a, new ItemCreator(inventory.getItem(a)).setAmount(1).setLores(new String [] {
								"&eWins: &c" + wins,
								"&eLosses: &c" + losses,
								"&eTotal: &c" + (wins + losses),
								"&eWLR: &c" + (wins == 0 || losses == 0 ? 0 : DoubleUtil.round(wins / losses, 2)),
								/*"",
								"&eElo: &c" + VersusElo.getElo(target, kit)*/
							}).getItemStack());
						}
						player.openInventory(inventory);
					}
				});
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerViewStats(PlayerViewStatsEvent event) {
		Player player = event.getPlayer();
		ChatClickHandler.sendMessageToRunCommand(player, " &cClick here", "Click to view versus stats", "/versusStats", "&aView kit stats: &f/versusStats [name] &aor");
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().startsWith("Versus Stats - ")) {
			event.getPlayer().closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBattleEnd(BattleEndEvent event) {
		if(event.getWinner() != null && event.getLoser() != null && event.getKit() != null) {
			final UUID winner = event.getWinner().getUniqueId();
			final UUID loser = event.getLoser().getUniqueId();
			final String kit = event.getKit().getName();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String [] keys = new String [] {"uuid", "kit"};
					String [] values = new String [] {winner.toString(), kit};
					if(DB.PLAYERS_VERSUS_STATS.isKeySet(keys, values)) {
						int wins = DB.PLAYERS_VERSUS_STATS.getInt(keys, values, "wins") + 1;
						DB.PLAYERS_VERSUS_STATS.updateInt("wins", wins, keys, values);
					} else {
						DB.PLAYERS_VERSUS_STATS.insert("'" + winner.toString() + "', '" + kit + "', '1', '0'");
					}
					values[0] = loser.toString();
					if(DB.PLAYERS_VERSUS_STATS.isKeySet(keys, values)) {
						int losses = DB.PLAYERS_VERSUS_STATS.getInt(keys, values, "losses") + 1;
						DB.PLAYERS_VERSUS_STATS.updateInt("losses", losses, keys, values);
					} else {
						DB.PLAYERS_VERSUS_STATS.insert("'" + loser.toString() + "', '" + kit + "', '0', '1'");
					}
				}
			});
		}
	}
}
