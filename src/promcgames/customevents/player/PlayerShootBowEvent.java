package promcgames.customevents.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

import promcgames.server.util.EventUtil;

public class PlayerShootBowEvent extends Event implements Listener {
	
	private static final HandlerList handlers = new HandlerList();
	
	private Player player = null;
	private Arrow arrow = null;
	private float force = 0F;
	private boolean cancelled = false;
	
	public PlayerShootBowEvent() {
		EventUtil.register(this);
	}
	
	public PlayerShootBowEvent(Player player, Arrow arrow, float force) {
		this.player = player;
		this.arrow = arrow;
		this.force = force;
	}
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Arrow getArrow() {
		return arrow;
	}
	
	public float getForce() {
		return force;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(event.getEntity() instanceof Player && event.getProjectile() instanceof Arrow) {
			Player player = (Player) event.getEntity();
			Arrow arrow = (Arrow) event.getProjectile();
			float force = event.getForce();
			PlayerShootBowEvent playerShootBowEvent = new PlayerShootBowEvent(player, arrow, force);
			Bukkit.getPluginManager().callEvent(playerShootBowEvent);
			if(playerShootBowEvent.isCancelled()) {
				event.setCancelled(true);
			}
		}
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }
	
}