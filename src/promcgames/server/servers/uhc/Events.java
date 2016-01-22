package promcgames.server.servers.uhc;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import promcgames.ProMcGames;
import promcgames.customevents.player.WaterSplashEvent;
import promcgames.customevents.player.timed.PlayerFiveSecondConnectedOnceEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.world.CPSDetector;

public class Events implements Listener {
	public Events() {
		new CPSDetector(new Location(Bukkit.getWorlds().get(0), 4.5, 6, 20.5));
		EventUtil.register(this);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = (Player) event.getPlayer();
		Random random = new Random();
		Location spawn = player.getWorld().getSpawnLocation();
		int range = 7;
		spawn.setX(spawn.getBlockX() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		spawn.setY(spawn.getY() + 2.5d);
		spawn.setZ(spawn.getBlockZ() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		spawn.setYaw(-360.0f);
		spawn.setPitch(0.0f);
		player.teleport(spawn);
		if(Ranks.PRO.hasRank(player)) {
			player.setAllowFlight(true);
		}
		player.setScoreboard(ProMcGames.getScoreboard());
		player.setLevel(UHCHub.hubNumber);
	}
	
	@EventHandler
	public void onPlayerFiveSecondConnectedOnceEvent(PlayerFiveSecondConnectedOnceEvent event) {
		Player player = event.getPlayer();
		MessageHandler.sendLine(player, "&b");
		MessageHandler.sendMessage(player, "");
		MessageHandler.sendMessage(player, "&b&lHere for a hosted UHC?");
		MessageHandler.sendMessage(player, "&b&lJoin it by clicking the &c&lcompass&b&l!");
		MessageHandler.sendMessage(player, "");
		MessageHandler.sendLine(player, "&b");
	}
	
	@EventHandler
	public void onWaterSplash(WaterSplashEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItem(4);
		if(item == null || item.getType() != Material.COBBLESTONE) {
			player.setVelocity(new Vector(0, 4, 0));
			EffectUtil.playSound(player, Sound.WATER);
			EffectUtil.playSound(player, Sound.SLIME_WALK);
		}
	}
}
