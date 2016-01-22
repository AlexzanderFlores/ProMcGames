package promcgames.anticheat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.PerformanceHandler;
import promcgames.server.util.EventUtil;

public class SpamBotFix implements Listener {
	public SpamBotFix() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(PerformanceHandler.getPing(event.getPlayer()) == 0 && !Ranks.PRO.hasRank(event.getPlayer())) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou must be " + Ranks.PRO.getPrefix() + "&cor above to talk while your ping is 0! (This is to prevent spam bots)");
			event.setCancelled(true);
		}
	}
}
