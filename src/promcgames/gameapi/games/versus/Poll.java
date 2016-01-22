package promcgames.gameapi.games.versus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.ServerRestartEvent;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.gameapi.games.versus.events.BattleEndEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

public class Poll implements Listener {
	private Map<UUID, Boolean> votes = null;
	private String title = null;
	
	public Poll() {
		votes = new HashMap<UUID, Boolean>();
		title = "Poll";
		new CommandBase("poll", 0, 1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 0) {
					MessageHandler.sendLine(player);
					MessageHandler.sendMessage(player, "");
					MessageHandler.sendMessage(player, "NOTE: ELO IS TEMP DISABLED DUE TO LAG");
					MessageHandler.sendMessage(player, "To fix this we may move to a global elo value.");
					MessageHandler.sendMessage(player, "This means all kits will share the same elo.");
					MessageHandler.sendMessage(player, "Some of our more \"fun\" kits won't have elo enabled:");
					MessageHandler.sendMessage(player, "One hit wonder, Quickshot, TNT, etc.");
					MessageHandler.sendMessage(player, "");
					MessageHandler.sendMessage(player, "Do you want one elo value for all kits?");
					MessageHandler.sendMessage(player, "");
					/*MessageHandler.sendMessage(player, "If this is voted yes, then we are not sure on:");
					MessageHandler.sendMessage(player, "");
					MessageHandler.sendMessage(player, "1) Resetting all elo to 1,000 (default value)");
					MessageHandler.sendMessage(player, "2) Player's new elo becomes their highest previous elo");
					MessageHandler.sendMessage(player, "We'll have a poll about this after");*/
					MessageHandler.sendMessage(player, "&cTo vote do &6/poll yes &cor &6/poll no");
					MessageHandler.sendLine(player);
				} else if(arguments.length == 1) {
					String option = arguments[0].toLowerCase();
					if(option.equals("yes")) {
						votes.put(player.getUniqueId(), true);
						MessageHandler.sendMessage(player, "Thank you for your input!");
					} else if(option.equals("no")) {
						votes.put(player.getUniqueId(), false);
						MessageHandler.sendMessage(player, "Thank you for your input!");
						MessageHandler.sendMessage(player, "If you'd like to share why you dislike this idea please do so here:");
						MessageHandler.sendMessage(player, "https://twitter.com/ProMcGames");
					} else if(Ranks.DEV.hasRank(player)) {
						int yesVotes = 0;
						int noVotes = 0;
						for(UUID uuid : votes.keySet()) {
							if(votes.get(uuid)) {
								++yesVotes;
							} else {
								++noVotes;
							}
						}
						MessageHandler.sendMessage(player, "Votes for &eYes: &a" + yesVotes);
						MessageHandler.sendMessage(player, "Votes for &cNo: &a" + noVotes);
					} else {
						player.chat("/poll");
					}
				}
				return true;
			}
		};
		for(String uuidString : DB.NETWORK_POLL_VOTES.getAllStrings("uuid")) {
			UUID uuid = UUID.fromString(uuidString);
			votes.put(uuid, DB.NETWORK_POLL_VOTES.getBoolean("uuid", uuid.toString(), "vote"));
		}
		Bukkit.getLogger().info("Loaded " + votes.size() + " previous votes");
		EventUtil.register(this);
	}
	
	private void remind(Player player) {
		if(!votes.containsKey(player.getUniqueId())) {
			MessageHandler.sendLine(player);
			MessageHandler.sendMessage(player, "");
			MessageHandler.sendMessage(player, "&c&lYou have not voted in our current poll! &6&l/poll");
			MessageHandler.sendMessage(player, "");
			MessageHandler.sendLine(player);
			EffectUtil.playSound(player, Sound.ZOMBIE_HURT);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		remind(event.getPlayer());
	}
	
	@EventHandler
	public void onBattleEnd(BattleEndEvent event) {
		remind(event.getWinner());
		remind(event.getLoser());
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(title)) {
			Player player = event.getPlayer();
			String title = ChatColor.stripColor(event.getItemTitle());
			if(title.equalsIgnoreCase("Yes")) {
				votes.put(player.getUniqueId(), true);
			} else {
				votes.put(player.getUniqueId(), false);
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		for(UUID uuid : votes.keySet()) {
			if(DB.NETWORK_POLL_VOTES.isUUIDSet(uuid)) {
				DB.NETWORK_POLL_VOTES.updateBoolean("vote", votes.get(uuid), "uuid", uuid.toString());
			} else {
				DB.NETWORK_POLL_VOTES.insert("'" + uuid.toString() + "', '" + votes.get(uuid) + "'");
			}
		}
	}
}
