package promcgames.gameapi.games.uhc.anticheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.FiveMinuteTaskEvent;
import promcgames.gameapi.games.uhc.HostHandler;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class DiamondTracker implements Listener {
	private Map<String, Integer> mined = null;
	private List<String> delayed = null;
	
	public DiamondTracker() {
		mined = new HashMap<String, Integer>();
		delayed = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.getBlock().getType() == Material.DIAMOND_ORE) {
			Player player = event.getPlayer();
			int times = 0;
			if(mined.containsKey(Disguise.getName(player))) {
				times = mined.get(Disguise.getName(player));
			}
			if(++times >= 10 && !delayed.contains(player.getName())) {
				final String name = player.getName();
				delayed.add(name);
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						delayed.remove(name);
					}
				}, 20 * 5);
				for(Player online: Bukkit.getOnlinePlayers()) {
					if(HostHandler.isHost(online.getUniqueId())) {
						MessageHandler.sendMessage(online, AccountHandler.getPrefix(player) + " &cHAS MINED &e&l" + times + " &cDIAMONDS WITHIN THE LAST 5 MINUTES");
					}
				}
			}
			mined.put(Disguise.getName(player), times);
		}
	}
	
	@EventHandler
	public void onFiveMinuteTask(FiveMinuteTaskEvent event) {
		mined.clear();
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		mined.remove(event.getPlayer().getName());
	}
}
