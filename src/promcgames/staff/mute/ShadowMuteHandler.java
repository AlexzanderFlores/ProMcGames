package promcgames.staff.mute;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import promcgames.ProPlugin;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class ShadowMuteHandler implements Listener {
	private List<String> checkedForShadowMuted = null;
	private static List<String> shadowMuted = null;
	
	public ShadowMuteHandler() {
		checkedForShadowMuted = new ArrayList<String>();
		shadowMuted = new ArrayList<String>();
		new CommandBase("shadowMute", 1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = (Player) sender;
						if(Disguise.getUUID(player).toString().equalsIgnoreCase("c5f7f0fe-b3f7-443b-850d-dd2561caea71")) {
							Player target = ProPlugin.getPlayer(arguments[0]);
							String name = null;
							UUID uuid = null;
							if(target == null) {
								name = arguments[0];
								uuid = AccountHandler.getUUID(name);
							} else {
								name = target.getName();
								uuid = Disguise.getUUID(target);
							}
							if(DB.STAFF_SHADOW_MUTES.isUUIDSet(uuid)) {
								DB.STAFF_SHADOW_MUTES.deleteUUID(uuid);
								shadowMuted.remove(name);
								MessageHandler.sendMessage(sender, name + " is no longer shadow muted");
							} else {
								DB.STAFF_SHADOW_MUTES.insert("'" + uuid.toString() + "'");
								shadowMuted.add(name);
								MessageHandler.sendMessage(sender, name + " is now shadow muted");
							}
						} else {
							MessageHandler.sendUnknownCommand(sender);
						}
					}
				});
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	public static boolean contains(Player player) {
		return shadowMuted != null && shadowMuted.contains(Disguise.getName(player));
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if(!checkedForShadowMuted.contains(Disguise.getName(player))) {
			checkedForShadowMuted.add(Disguise.getName(player));
			if(DB.STAFF_SHADOW_MUTES.isUUIDSet(Disguise.getUUID(player))) {
				shadowMuted.add(Disguise.getName(player));
			}
		}
		if(shadowMuted.contains(Disguise.getName(player))) {
			for(Player online : Bukkit.getOnlinePlayers()) {
				if(!online.getName().equals(Disguise.getName(player))) {
					event.getRecipients().remove(online);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		checkedForShadowMuted.remove(event.getPlayer().getName());
		shadowMuted.remove(event.getPlayer().getName());
	}
}
