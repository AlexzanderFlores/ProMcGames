package promcgames.gameapi.games.survivalgames.deathmatch;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.MiniGame;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.player.MessageHandler;
import promcgames.player.TitleDisplayer;
import promcgames.player.account.AccountHandler;
import promcgames.player.bossbar.BossBar;
import promcgames.server.util.CountDownUtil;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

public class DeathmatchStartedHandler extends CountDownUtil implements Listener {
	public DeathmatchStartedHandler() {
		super(60 * 5);
		BossBar.setCounter(getCounter());
		MiniGame miniGame = (MiniGame) ProMcGames.getMiniGame();
		miniGame.setAllowEntityDamage(true);
		miniGame.setAllowBowShooting(true);
		miniGame.setAllowPlayerInteraction(true);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecond(OneSecondTaskEvent event) {
		final MiniGame miniGame = ProMcGames.getMiniGame();
		if(miniGame.getGameState() == GameStates.ENDING) {
			List<Player> players = ProPlugin.getPlayers();
			if(players.size() == 1) {
				Player winner = players.get(0);
				BossBar.display(AccountHandler.getPrefix(winner) +  " &ehas won " + miniGame.getDisplayName() + "!");
				for(Player online : Bukkit.getOnlinePlayers()) {
					new TitleDisplayer(online, AccountHandler.getRank(winner).getColor() + "&l" + winner.getName() + " has won", "&eThe " + miniGame.getDisplayName());
				}
				OneSecondTaskEvent.getHandlerList().unregister(this);
			}
		} else {
			if(getCounter() <= 0) {
				miniGame.setGameState(GameStates.ENDING);
				ProMcGames.getSidebar().update("&cServer Restarting");
			} else {
				if(canDisplay()) {
					EffectUtil.playSound(Sound.CLICK);
				}
				if(getCounter() <= 5 || getCounter() % 60 == 0 || (getCounter() < 60 && getCounter() % 10 == 0)) {
					MessageHandler.alert("Deathmatch ending in " + getCounterAsString());
				}
				BossBar.display("&cDeathmatch ending in " + getCounterAsString());
				ProMcGames.getSidebar().update("&aDM ending in " + getCounterAsString());
				decrementCounter();
				/*for(Player player : ProPlugin.getPlayers()) {
					if(player.getLocation().getY() >= 11) {
						player.damage(2.0);
						MessageHandler.sendMessage(player, "&cYou cannot be at this height");
					}
				}*/
			}
		}
	}
}
