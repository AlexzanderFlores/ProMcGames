package promcgames.gameapi.games.survivalgames.deathmatch;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.MiniGame;
import promcgames.player.MessageHandler;
import promcgames.player.PlayerMove;
import promcgames.player.bossbar.BossBar;
import promcgames.server.util.CountDownUtil;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

public class DeathmatchStartingHandler extends CountDownUtil implements Listener {
	public DeathmatchStartingHandler() {
		super(10);
		BossBar.setCounter(getCounter());
		EventUtil.register(new PlayerMove(false));
		MiniGame miniGame = (MiniGame) ProMcGames.getMiniGame();
		miniGame.setAllowEntityDamage(false);
		miniGame.setAllowBowShooting(false);
		miniGame.setAllowPlayerInteraction(false);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(getCounter() <= 0) {
			HandlerList.unregisterAll(this);
			PlayerMoveEvent.getHandlerList().unregister(PlayerMove.getInstance());
			EffectUtil.playSound(Sound.EXPLODE);
			new DeathmatchStartedHandler();
		} else {
			if(canDisplay()) {
				EffectUtil.playSound(Sound.CLICK);
			}
			if(getCounter() <= 3) {
				MessageHandler.alert("Deathmatch Starting in " + getCounterAsString());
			}
			BossBar.display("&cDeathmatch Starting in " + getCounterAsString());
			ProMcGames.getSidebar().update("&aDM in " + getCounterAsString());
			decrementCounter();
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if(item != null && item.getType().isEdible()) {
			event.setCancelled(false);
		}
	}
}
