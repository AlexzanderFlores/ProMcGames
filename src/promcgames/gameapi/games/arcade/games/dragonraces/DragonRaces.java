package promcgames.gameapi.games.arcade.games.dragonraces;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import promcgames.customevents.game.GameStartEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpawnPointHandler;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.arcade.ArcadeGame;
import promcgames.player.bossbar.BossBar;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.DelayedTask;

public class DragonRaces extends ArcadeGame {
	public DragonRaces() {
		super("Dragon Races");
	}
	
	@Override
	public void enable() {
		super.enable();
		ProMcGames.getMiniGame().setCanJoinWhileStarting(false);
		List<Location> spawns = new SpawnPointHandler(getWorld()).getSpawns();
		int counter = 0;
		for(Player player : ProPlugin.getPlayers()) {
			player.setAllowFlight(true);
			player.setFlying(true);
			Location spawn = spawns.get(counter++);
			spawn.setYaw(-181.76958f);
			spawn.setPitch(5.671953f);
			player.teleport(spawn);
		}
		new CheckPointHandler();
		new LightHandler(getWorld());
		new ShortcutHandler(getWorld());
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				ProMcGames.getMiniGame().setCounter(20);
				BossBar.setCounter(ProMcGames.getMiniGame().getCounter());
			}
		});
	}
	
	@Override
	public void disable(Player winner) {
		super.disable(winner);
		DragonHandler.disable();
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			Location location = player.getLocation();
			location.setYaw(-181.76958f);
			location.setPitch(5.671953f);
			player.teleport(location);
		}
		BossBar.remove();
		new PerkHandler();
		new DragonHandler(getWorld());
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!SpectatorHandler.contains(event.getPlayer()) && event.getTo().getWorld().getName().equals(getWorld().getName())) {
			if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
				PlayerMoveEvent.getHandlerList().unregister(this);
			} else if(event.getTo().getX() != event.getFrom().getX() || event.getTo().getY() != event.getFrom().getY() || event.getTo().getZ() != event.getFrom().getZ()) {
				event.setTo(event.getFrom());
			}
		}
	}
}
