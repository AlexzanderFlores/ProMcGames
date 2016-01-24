package promcgames.gameapi.games.arcade.games;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.customevents.timed.TenTickTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.arcade.ArcadeGame;
import promcgames.player.Disguise;
import promcgames.player.account.AccountHandler;
import promcgames.player.bossbar.BossBar;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;

public class ParkourRunner extends ArcadeGame {
	private int z = -9;
	
	public ParkourRunner() {
		super("Parkour Runner");
	}
	
	@Override
	public void enable() {
		super.enable();
		for(Player player : ProPlugin.getPlayers()) {
			player.teleport(new Location(getWorld(), 0.5, 10, 0.5));
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		BossBar.remove();
	}
	
	@EventHandler
	public void onTenTickTask(TenTickTaskEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			for(int x = -13; x <= 13; ++x) {
				for(int y = 0; y <= 48; ++y) {
					getWorld().getBlockAt(x, y, z).setType(Material.AIR);
				}
			}
			++z;
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			ProMcGames.getSidebar().setText(AccountHandler.getRank(player).getColor() + Disguise.getName(player), 281 - player.getLocation().getBlockX());
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player && (event.getCause() == DamageCause.VOID || event.getCause() == DamageCause.LAVA)) {
			Player player = (Player) event.getEntity();
			if(!SpectatorHandler.contains(player)) {
				SpectatorHandler.add(player);
				int left = ProPlugin.getPlayers().size();
				if(left <= 0) {
					disable(null);
				} else if(left == 1) {
					Player winner = ProPlugin.getPlayers().get(0);
					disable(winner);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!SpectatorHandler.contains(event.getPlayer()) && event.getTo().getWorld().getName().equals(getWorld().getName())) {
			if(ProMcGames.getMiniGame().getGameState() != GameStates.STARTED && event.getTo().getZ() >= 3) {
				event.setTo(event.getFrom());
			} else if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED && event.getTo().getZ() >= 281) {
				disable(event.getPlayer());
			}
		}
	}
}
