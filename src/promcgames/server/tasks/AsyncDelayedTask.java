package promcgames.server.tasks;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import promcgames.server.ProMcGames;

public class AsyncDelayedTask implements Listener {
	public AsyncDelayedTask(Runnable runnable) {
		this(runnable, 1);
	}
	
	public AsyncDelayedTask(Runnable runnable, long delay) {
		ProMcGames instance = ProMcGames.getInstance();
		if(instance.isEnabled()) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(instance, runnable, delay);
		} else {
			runnable.run();
		}
	}
}
