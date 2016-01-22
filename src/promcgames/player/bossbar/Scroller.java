package promcgames.player.bossbar;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.player.PlayerAFKEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.TwoTickTaskEvent;
import promcgames.player.Disguise;
import promcgames.server.util.EventUtil;

public class Scroller implements Listener {
	private List<String> afkPlayers = null;
	private static String text = null;
	private static String spaces = null;
	private int counter = 0;
	private static int max = 64;
	
	public Scroller() {
		afkPlayers = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	public static void setText(String newText) {
		if(spaces == null) {
			spaces = "";
			for(int a = 0; a < max; ++a) {
				spaces += " ";
			}
		}
		text = spaces + newText + spaces;
	}
	
	@EventHandler
	public void onTwoTickTask(TwoTickTaskEvent event) {
		if(text != null) {
			try {
				for(Player player : Bukkit.getOnlinePlayers()) {
					if(!afkPlayers.contains(Disguise.getName(player))) {
						BossBar.display(player, "&b" + text.substring(counter, counter + max));
					}
				}
				++counter;
			} catch(IndexOutOfBoundsException e) {
				counter = 0;
			}
		}
	}
	
	@EventHandler
	public void onPlayerAFK(PlayerAFKEvent event) {
		if(event.getAFK()) {
			if(!afkPlayers.contains(event.getPlayer())) {
				afkPlayers.add(event.getPlayer().getName());
			}
			BossBar.remove(event.getPlayer());
		} else {
			afkPlayers.remove(event.getPlayer().getName());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		afkPlayers.remove(event.getPlayer().getName());
	}
}
