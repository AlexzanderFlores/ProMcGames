package promcgames.gameapi;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import promcgames.customevents.game.GracePeriodEndingEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.TitleDisplayer;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.player.bossbar.BossBar;
import promcgames.server.CommandBase;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.CountDownUtil;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

public class GracePeriod extends CountDownUtil implements Listener {
	private static GracePeriod instance = null;
	private static boolean isRunning = false;
	
	public GracePeriod(int seconds) {
		super(seconds);
		instance = this;
		isRunning = true;
		BossBar.setCounter(getCounter());
		ProMcGames.getProPlugin().setAllowEntityDamage(false);
		ProMcGames.getProPlugin().setAllowEntityDamageByEntities(false);
		EventUtil.register(instance);
		new CommandBase("grace", 1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				try {
					setCounter(Integer.valueOf(arguments[0]));
					BossBar.setCounter(getCounter());
					return true;
				} catch(NumberFormatException e) {
					return false;
				}
			}
		}.setRequiredRank(Ranks.DEV);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(getCounter() <= 0) {
			isRunning = false;
			HandlerList.unregisterAll(instance);
			ProMcGames.getProPlugin().setAllowEntityDamage(true);
			ProMcGames.getProPlugin().setAllowEntityDamageByEntities(true);
			ProMcGames.getProPlugin().setAllowBowShooting(true);
			String text = "&cGrace period is over!";
			MessageHandler.alert(text);
			BossBar.display(text);
			BossBar.setCounter(ProMcGames.getMiniGame().getCounter());
			for(Player player : ProPlugin.getPlayers()) {
				new TitleDisplayer(player, "&cGrace ended", "&bPVP Enabled").setFadeIn(0).setStay(30).setFadeOut(0).display();
			}
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					BossBar.remove();
				}
			}, 20);
			EffectUtil.playSound(Sound.ENDERDRAGON_GROWL);
			Bukkit.getPluginManager().callEvent(new GracePeriodEndingEvent());
		} else {
			BossBar.display("&bGrace period ending in " + getCounterAsString());
			if(canDisplay()) {
				MessageHandler.alert("&bGrace period ending in " + getCounterAsString());
			}
		}
		decrementCounter();
	}
	
	public static boolean isRunning() {
		return isRunning;
	}
	
	public static String getGraceCounterString() {
		return instance.getCounterAsString();
	}
	
	public static int getGraceCounter() {
		return instance.getCounter();
	}
}
