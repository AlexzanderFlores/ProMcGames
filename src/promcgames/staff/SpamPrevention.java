package promcgames.staff;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.server.util.EventUtil;

public class SpamPrevention implements Listener {
	private Map<String, String> lastMessages = null;
	
	public SpamPrevention() {
		lastMessages = new HashMap<String, String>();
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(!event.isCancelled()) {
			Player player = event.getPlayer();
			String msg = event.getMessage();
			if(lastMessages.containsKey(Disguise.getName(player)) && lastMessages.get(Disguise.getName(player)).equals(msg)) {
				for(Player online : Bukkit.getOnlinePlayers()) {
					if(!Disguise.getName(player).equals(online.getName())) {
						event.getRecipients().remove(online);
					}
				}
			} else if(msg.length() >= 2) {
				for(int a = 3; a < msg.length(); ++a) {
					if(msg.charAt(a - 3) == msg.charAt(a) && msg.charAt(a - 2)  == msg.charAt(a) && msg.charAt(a - 1) == msg.charAt(a)) {
						String spam = "";
						for(int b = 0; b < 4; ++b) {
							spam += msg.charAt(a);
						}
						MessageHandler.sendMessage(player, "&cThis message is considered spam: \"" + spam + "\"");
						event.setCancelled(true);
						return;
					}
				}
			}
			lastMessages.put(Disguise.getName(player), event.getMessage());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		lastMessages.remove(event.getPlayer().getName());
	}
}
