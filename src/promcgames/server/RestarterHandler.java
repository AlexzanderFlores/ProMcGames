package promcgames.server;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.ProPlugin;
import promcgames.customevents.RestartAnnounceEvent;
import promcgames.customevents.timed.FiveMinuteTaskEvent;
import promcgames.customevents.timed.FiveSecondTaskEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.util.CountDownUtil;
import promcgames.server.util.EventUtil;

public class RestarterHandler extends CountDownUtil implements Listener {
	private boolean running = false;
	
	public RestarterHandler() {
		EventUtil.register(this);
		new CommandBase("restartServer", 1, 2) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				if(Bukkit.getOnlinePlayers().isEmpty()) {
					ProPlugin.restartServer();
				} else {
					try {
						setCounter(Integer.valueOf(arguments[0]) * 60);
						running = true;
					} catch(NumberFormatException e) {
						if(arguments[0].equalsIgnoreCase("stop")) {
							if(running) {
								running = false;
								MessageHandler.alert("&c" + ProMcGames.getServerName() + "'s restart has been cancelled");
							} else {
								MessageHandler.sendMessage(sender, "&cThere is no running update");
							}
						} else {
							return false;
						}
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.DEV);
	}
	
	@EventHandler
	public void onFiveMinuteTask(FiveMinuteTaskEvent event) {
		if(Bukkit.getOnlinePlayers().isEmpty() && ProMcGames.getMiniGame() != null && ProMcGames.getPlugin() != Plugins.UHC) {
			ProPlugin.restartServer();
		}
	}
	
	@EventHandler
	public void onFiveSecondTask(FiveSecondTaskEvent evnet) {
		if(PerformanceHandler.getMemory() >= 70 && !running && ProMcGames.getMiniGame() == null && ProMcGames.getPlugin() != Plugins.UHC) {
			setCounter(60);
			running = true;
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(running) {
			if(getCounter() <= 0) {
				ProPlugin.restartServer();
			} else {
				if(canDisplay()) {
					MessageHandler.alert("&c" + ProMcGames.getServerName() + " is restarting in " + getCounterAsString());
					Bukkit.getPluginManager().callEvent(new RestartAnnounceEvent(getCounter()));
				}
				decrementCounter();
			}
		}
	}
}
