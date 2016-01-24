package promcgames.gameapi.games.uhc.anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.games.uhc.HostHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.ProPlugin;
import promcgames.server.util.CountDownUtil;
import promcgames.server.util.EventUtil;

public class WaterBucketLogging implements Listener {
	private Map<String, Integer> counters = null;
	private List<String> enabled = null;
	private int counter = 0;
	
	public WaterBucketLogging() {
		new CommandBase("bucket", 0, 1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					if(!Ranks.HELPER.hasRank(player) && !HostHandler.isHost(player.getUniqueId())) {
						MessageHandler.sendUnknownCommand(player);
						return true;
					}
				}
				if(arguments.length == 0) {
					if(sender instanceof Player) {
						if(enabled.contains(sender.getName())) {
							enabled.remove(sender.getName());
							MessageHandler.sendMessage(sender, "Bucket alerts are now &cOFF");
						} else {
							enabled.add(sender.getName());
							MessageHandler.sendMessage(sender, "Bucket alerts are now &eON");
						}
					} else {
						MessageHandler.sendPlayersOnly(sender);
					}
				} else {
					String name = arguments[0].toLowerCase();
					if(counters.containsKey(name)) {
						CountDownUtil countDown = new CountDownUtil(counter - counters.get(name));
						MessageHandler.sendMessage(sender, name + " placed a water bucket down " + countDown.getCounterAsString() + " &aago");
					} else {
						MessageHandler.sendMessage(sender, "&c" + name + " has not placed a bucket");
					}
				}
				return true;
			}
		};
		counters = new HashMap<String, Integer>();
		enabled = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if(event.getBucket() == Material.WATER_BUCKET) {
			counters.put(event.getPlayer().getName().toLowerCase(), counter);
			for(String name : enabled) {
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					MessageHandler.sendMessage(player, "&6&lBucket: " + AccountHandler.getPrefix(event.getPlayer()) + " &ehas placed a water bucket");
				}
			}
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		++counter;
	}
}
