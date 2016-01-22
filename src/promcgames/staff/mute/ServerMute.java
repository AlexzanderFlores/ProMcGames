package promcgames.staff.mute;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class ServerMute implements Listener {
	private boolean enabled = false;
	private int muteID = 0;
	private String reason = "";
	
	public ServerMute() {
		new CommandBase("serverMute", 2, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				try {
					int delay = Integer.valueOf(arguments[0]);
					if(delay > 20) {
						MessageHandler.sendMessage(sender, "&cYou cannot mute chat for more than 20 minutes");
					} else {
						reason = "";
						if(enabled) {
							MessageHandler.alert("Chat has been unmuted");
							enabled = false;
						} else {
							final int id = ++muteID;
							for(int a = 1; a < arguments.length; ++a) {
								reason += arguments[a] + " ";
							}
							enabled = true;
							MessageHandler.alert("Chat is now disabled for &e" + delay + " &aminutes for &c" + reason);
							new DelayedTask(new Runnable() {
								@Override
								public void run() {
									if(id == muteID && enabled) {
										enabled = false;
										MessageHandler.alert("Chat has been unmuted");
									}
								}
							}, 20 * 60 * delay);
						}
					}
				} catch(NumberFormatException e) {
					return false;
				}
				return true;
			}
		}.setRequiredRank(Ranks.SENIOR_MODERATOR);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(enabled) {
			MessageHandler.sendMessage(event.getPlayer(), "Chat is currently disabled due to &c" + reason);
			event.setCancelled(true);
		}
	}
}
