package promcgames.server.servers.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.timed.OneMinuteTaskEvent;
import promcgames.player.account.AccountHandler;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class CommunityLevelTop8 implements Listener {
	public CommunityLevelTop8() {
		updateTop8();
		EventUtil.register(this);
	}
	
	private void updateTop8() {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				List<UUID> uuids = new ArrayList<UUID>();
				List<Integer> times = new ArrayList<Integer>();
				for(String uuid : DB.PLAYERS_COMMUNITY_LEVELS.getOrdered("level", "uuid", 8, true)) {
					uuids.add(UUID.fromString(uuid));
					times.add(DB.PLAYERS_COMMUNITY_LEVELS.getInt("uuid", uuid, "level"));
				}
				World world = Bukkit.getWorlds().get(0);
				Sign sign = (Sign) world.getBlockAt(-138, 127, -161).getState();
				for(int a = 0; a < 4; ++a) {
					sign.setLine(a, AccountHandler.getName(uuids.get(a)));
				}
				sign.update();
				sign = (Sign) world.getBlockAt(-138, 126, -161).getState();
				for(int a = 4; a < 8; ++a) {
					sign.setLine(a - 4, AccountHandler.getName(uuids.get(a)));
				}
				sign.update();
				sign = (Sign) world.getBlockAt(-138, 127, -162).getState();
				for(int a = 0; a < 4; ++a) {
					sign.setLine(a, times.get(a) + "");
				}
				sign.update();
				sign = (Sign) world.getBlockAt(-138, 126, -162).getState();
				for(int a = 4; a < 8; ++a) {
					sign.setLine(a - 4, times.get(a) + "");
				}
				sign.update();
				uuids.clear();
				uuids = null;
				times.clear();
				times = null;
			}
		});
	}
	
	@EventHandler
	public void onOneMinuteTask(OneMinuteTaskEvent event) {
		updateTop8();
	}
}
