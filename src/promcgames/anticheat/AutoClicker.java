package promcgames.anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.customevents.timed.TenSecondTaskEvent;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class AutoClicker extends AntiGamingChair implements Listener {
	private Map<String, Integer> clicks = null;
	//private Map<String, Integer> rightClcks = null;
	private Map<String, Integer> logs = null;
	private List<String> delayed = null;
	private int delay = 1;
	
	public AutoClicker() {
		super("Auto Clicker");
		clicks = new HashMap<String, Integer>();
		//rightClcks = new HashMap<String, Integer>();
		logs = new HashMap<String, Integer>();
		delayed = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(isEnabled()) {
			clicks.clear();
			//rightClcks.clear();
		}
	}
	
	@EventHandler
	public void onTenSecondTask(TenSecondTaskEvent event) {
		if(isEnabled()) {
			logs.clear();
		}
	}
	
	/*@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(isEnabled()) {
			if(event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();
				int click = 0;
				if(leftClicks.containsKey(damager.getName())) {
					click = leftClicks.get(damager.getName());
				}
				leftClicks.put(damager.getName(), ++click);
				if(click >= 20 && !delayed.contains(damager.getName())) {
					final String name = damager.getName();
					delayed.add(name);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							delayed.remove(name);
						}
					}, 20 * delay);
					if(click >= 30) {
						int log = 0;
						if(logs.containsKey(name)) {
							log = logs.get(name);
						}
						if(++log >= 3) {
							ban(damager);
						} else {
							logs.put(name, log);
						}
					} else {
						for(Player player : Bukkit.getOnlinePlayers()) {
							if(Ranks.HELPER.hasRank(player)) {
								MessageHandler.sendMessage(player, AccountHandler.getPrefix(damager) + " &chas reached &e" + click + " &cCPS");
							}
						}
					}
				}
			}
		}
	}*/
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(isEnabled() && event.getAction() != Action.PHYSICAL) {
			Player player = event.getPlayer();
			int click = 0;
			if(clicks.containsKey(player.getName())) {
				click = clicks.get(player.getName());
			}
			clicks.put(player.getName(), ++click);
			if(click >= 30 && !delayed.contains(player.getName())) {
				final String name = player.getName();
				delayed.add(name);
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						delayed.remove(name);
					}
				}, 20 * delay);
				int log = 0;
				if(logs.containsKey(name)) {
					log = logs.get(name);
				}
				if(++log >= 3) {
					ban(player);
				} else {
					logs.put(name, log);
				}
			}
		}
	}
}
