package promcgames.server.tasks;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import promcgames.server.ProMcGames;

public class DelayedTask implements Listener {
	public DelayedTask(Runnable runnable) {
		this(runnable, 1);
	}
	
	public DelayedTask(Runnable runnable, long delay) {
		ProMcGames instance = ProMcGames.getInstance();
		if(instance.isEnabled()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(instance, runnable, delay);
		} else {
			runnable.run();
		}
	}
}
