package promcgames.gameapi.games.skywars.cages;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;

import promcgames.ProPlugin;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.game.PostGameStartingEvent;
import promcgames.customevents.player.SpecialKitDenyEvent;
import promcgames.gameapi.games.skywars.cages.effects.BasicColor;
import promcgames.gameapi.games.skywars.cages.effects.BottomUpChangingColor;
import promcgames.gameapi.games.skywars.cages.effects.HorizontalStrobePattern;
import promcgames.gameapi.games.skywars.cages.effects.RapidlyChangingRandomColor;
import promcgames.gameapi.games.skywars.cages.effects.RapidlyChangingSolidColor;
import promcgames.gameapi.games.skywars.cages.effects.SpiralMovingLights;
import promcgames.gameapi.games.skywars.cages.effects.TopRowMovingLight;
import promcgames.gameapi.games.skywars.cages.effects.VerticalStrobePattern;
import promcgames.gameapi.games.skywars.islands.IslandHandler;
import promcgames.gameapi.kits.KitBase;
import promcgames.gameapi.kits.KitShop;
import promcgames.player.MessageHandler;
import promcgames.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class CageHandler implements Listener {
	private static World arena = null;
	private KitShop shop = null;
	
	public CageHandler() {
		shop = new KitShop("Cage Effect Shop", 9 * 4);
		new TopRowMovingLight();
		new SpiralMovingLights();
		new BottomUpChangingColor();
		new RapidlyChangingSolidColor();
		new RapidlyChangingRandomColor();
		new VerticalStrobePattern();
		new HorizontalStrobePattern();
		new BasicColor(15, "&0", "Black", 18);
		new BasicColor(11, "&1", "Dark Blue");
		new BasicColor(13, "&2", "Dark Green");
		new BasicColor(3, "&3", "Dark Aqua");
		new BasicColor(14, "&4", "Dark Red");
		new BasicColor(10, "&5", "Dark Purple");
		new BasicColor(1, "&6", "Gold");
		new BasicColor(8, "&7", "Gray");
		new BasicColor(7, "&8", "Dark Gray");
		new BasicColor(9, "&9", "Blue", KitBase.getLastSlot() + 2);
		new BasicColor(5, "&a", "Lime Green");
		new BasicColor(3, "&b", "Aqua");
		new BasicColor(6, "&c", "Red");
		new BasicColor(2, "&d", "Purple");
		new BasicColor(4, "&e", "Yellow");
		new BasicColor(0, "&f", "White");
		EventUtil.register(this);
	}
	
	public static List<Block> getCage(Player player) {
		Location location = IslandHandler.spawns.get(player.getName());
		if(location == null || arena == null) {
			return null;
		}
		Block spawn = location.getBlock();
		List<Block> blocks = new ArrayList<Block>();
		Block min = spawn.getRelative(-3, -1, -3);
		Block max = spawn.getRelative(3, 4, 3);
		int x1 = min.getX();
		int y1 = min.getY();
		int z1 = min.getZ();
		int x2 = max.getX();
		int y2 = max.getY();
		int z2 = max.getZ();
		for(int x = x1; x <= x2; ++x) {
			for(int y = y1; y <= y2; ++y) {
				for(int z = z1; z <= z2; ++z) {
					Block block = arena.getBlockAt(x, y, z);
					if(block.getType() != Material.AIR) {
						blocks.add(block);
					}
				}
			}
		}
		return blocks;
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		arena = event.getWorld();
	}
	
	@EventHandler
	public void onPostGameStarting(PostGameStartingEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			for(KitBase kit : KitBase.getKits()) {
				if(kit.getShop() == shop && kit.has(player)) {
					kit.execute(player);
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		Random random = new Random();
		for(Player player : ProPlugin.getPlayers()) {
			try {
				Block spawn = IslandHandler.spawns.get(player.getName()).getBlock();
				int y = spawn.getY();
				for(Block block : getCage(player)) {
					if(block.getY() == y && random.nextInt(100) <= 20) {
						FallingBlock fallingBlock = arena.spawnFallingBlock(block.getLocation().add(0, -1, 0), block.getType(), block.getData());
						fallingBlock.setDropItem(false);
					}
					block.setType(Material.AIR);
					block.setData((byte) 0);
				}
			} catch(Exception e) {
				
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		
	}
	
	@EventHandler
	public void onSpecialKitDeny(SpecialKitDenyEvent event) {
		if(event.getKit().getIcon().getType().equals(Material.QUARTZ_BLOCK)) {
			MessageHandler.sendMessage(event.getPlayer(), "&cYou can only unlock this kit in &eHub Sponsors &b/vote");
			event.setCancelled(true);
		}
	}
}
