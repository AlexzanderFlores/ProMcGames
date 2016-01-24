package promcgames.staff;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.staff.mute.ShadowMuteHandler;

public class StaffPager implements Listener {
	private List<String> delayed = null;
	private int delay = 30;
	
	public StaffPager() {
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(!ShadowMuteHandler.contains(event.getPlayer())) {
			Player target = ProPlugin.getPlayer(event.getMessage().split(" ")[0]);
			if(!event.isCancelled() && target != null && Ranks.isStaff(target) && !StaffMode.contains(target)) {
				if(!Disguise.isDisguised(event.getPlayer()) && !Disguise.isDisguised(target)) {
					if(delayed == null || !delayed.contains(target.getName())) {
						if(delayed == null) {
							delayed = new ArrayList<String>();
						}
						if(!delayed.contains(target.getName())) {
							delayed.add(target.getName());
							EffectUtil.playSound(target, Sound.ANVIL_LAND);
							MessageHandler.sendMessage(target, AccountHandler.getPrefix(event.getPlayer(), false) + " &5has paged you by saying your name in chat");
							MessageHandler.sendMessage(event.getPlayer(), "&5You paged " + AccountHandler.getPrefix(target));
							if(!event.getRecipients().contains(target)) {
								event.getRecipients().add(target);
							}
							final String name = target.getName();
							new DelayedTask(new Runnable() {
								@Override
								public void run() {
									delayed.remove(name);
								}
							}, 20 * delay);
						}
					}
				}
			}
		}
	}
}
