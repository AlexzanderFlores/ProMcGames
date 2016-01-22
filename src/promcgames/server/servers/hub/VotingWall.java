package promcgames.server.servers.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.timed.OneMinuteTaskEvent;
import promcgames.player.account.AccountHandler;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.TimeUtil;

public class VotingWall implements Listener {
	public VotingWall() {
		updateTop8();
		EventUtil.register(this);
	}
	
	private void updateTop8() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				List<UUID> uuids = new ArrayList<UUID>();
				List<Integer> amounts = new ArrayList<Integer>();
				String date = TimeUtil.getTime().substring(0, 7);
				for(String uuid : DB.PLAYERS_VOTES.getOrdered("votes", "uuid", "date", date, 8, true)) {
					uuids.add(UUID.fromString(uuid));
					String [] keys = new String [] {"uuid", "date"};
					String [] values = new String [] {uuid, date};
					amounts.add(DB.PLAYERS_VOTES.getInt(keys, values, "votes"));
				}
				if(uuids.isEmpty()) {
					return;
				}
				World world = Bukkit.getWorlds().get(0);
				Sign sign = (Sign) world.getBlockAt(-103, 127, -137).getState();
				for(int a = 0; a < 4; ++a) {
					sign.setLine(a, AccountHandler.getName(uuids.get(a)));
				}
				sign.update();
				sign = (Sign) world.getBlockAt(-103, 126, -137).getState();
				for(int a = 4; a < 8; ++a) {
					sign.setLine(a - 4, AccountHandler.getName(uuids.get(a)));
				}
				sign.update();
				sign = (Sign) world.getBlockAt(-104, 127, -137).getState();
				for(int a = 0; a < 4; ++a) {
					sign.setLine(a, ChatColor.stripColor(amounts.get(a) + ""));
				}
				sign.update();
				sign = (Sign) world.getBlockAt(-104, 126, -137).getState();
				for(int a = 4; a < 8; ++a) {
					sign.setLine(a - 4, ChatColor.stripColor(amounts.get(a) + ""));
				}
				sign.update();
				uuids.clear();
				uuids = null;
				amounts.clear();
				amounts = null;
			}
		});
	}
	
	@EventHandler
	public void onOneMinuteTask(OneMinuteTaskEvent event) {
		updateTop8();
	}
}
