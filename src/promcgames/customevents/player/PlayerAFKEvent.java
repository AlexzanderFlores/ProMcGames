package promcgames.customevents.player;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.Disguise;
import promcgames.server.util.EventUtil;

public class PlayerAFKEvent extends Event implements Listener {
	private static final HandlerList handlers = new HandlerList();
	private static Map<String, Integer> counters = null;
	private Player player = null;
	private int counter = 0;
	private boolean afk = true;
	
	public PlayerAFKEvent() {
		counters = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	public PlayerAFKEvent(Player player, int counter) {
		this(player, counter, true);
	}
	
	public PlayerAFKEvent(Player player, int counter, boolean afk) {
		this.player = player;
		this.counter = counter;
		this.afk = afk;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public int getCounter() {
		return this.counter;
	}
	
	public boolean getAFK() {
		return this.afk;
	}
	
	public static boolean isAFK(Player player) {
		return counters != null && counters.containsKey(player.getName()) && counters.get(player.getName()) >= 30;
	}
	
	private void unAFK(Player player) {
		if(counters.containsKey(Disguise.getName(player)) && counters.get(Disguise.getName(player)) >= 30) {
			Bukkit.getPluginManager().callEvent(new PlayerAFKEvent(player, 0, false));
		}
		counters.remove(Disguise.getName(player));
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(counters.containsKey(Disguise.getName(player))) {
				counters.put(Disguise.getName(player), counters.get(Disguise.getName(player)) + 1);
				if(player.getTicksLived() >= (20 * 10) && counters.get(Disguise.getName(player)) % 30 == 0) {
					Bukkit.getPluginManager().callEvent(new PlayerAFKEvent(player, counters.get(Disguise.getName(player))));
				}
			} else {
				counters.put(Disguise.getName(player), 1);
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		unAFK(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(event.getTo().getX() != event.getFrom().getX() || event.getTo().getZ() != event.getFrom().getZ()) {
			unAFK(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		unAFK(event.getPlayer());
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		unAFK(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		unAFK(event.getPlayer());
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			unAFK(player);
		}
	}
	
	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent event) {
		unAFK(event.getPlayer());
	}
	
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
