package promcgames.server.servers.hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.FiveTickTaskEvent;
import promcgames.customevents.timed.OneMinuteTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

@SuppressWarnings("all")
public class ColorEgg implements Listener {
	private Map<Block, String> blocks = null;
	private List<String> players = null;
	private Sheep sheep = null;
	
	public ColorEgg() {
		blocks = new HashMap<Block, String>();
		players = new ArrayList<String>();
		sheep = (Sheep) new NPCEntity(EntityType.SHEEP, "&bColor Egg", new Location(Bukkit.getWorlds().get(0), -133.5, 126, -146.5)) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendMessage(player, "Click the glass to change its color");
				MessageHandler.sendMessage(player, "See how many you can get per minute");
			}
		}.getLivingEntity();
		EventUtil.register(this);
	}
	
	private int countBlocks(Player player) {
		int counter = 0;
		for(String name : blocks.values()) {
			if(name.equals(player.getName())) {
				++counter;
			}
		}
		return counter;
	}
	
	@EventHandler
	public void onFiveTickTask(FiveTickTaskEvent event) {
		if(sheep != null && !sheep.isDead()) {
			sheep.setColor(DyeColor.values()[new Random().nextInt(DyeColor.values().length)]);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();
		if((action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType() == Material.STAINED_GLASS) {
			Block block = event.getClickedBlock();
			int x = block.getX();
			int y = block.getY();
			int z = block.getZ();
			if(x >= -148 && x <= -134 && y >= 126 && y <= 144 && z >= -146 && z <= -132) {
				byte data = 0;
				Random random = new Random();
				do {
					data = (byte) random.nextInt(15);
				} while(data == block.getData());
				block.setData(data);
				if(random.nextBoolean()) {
					EffectUtil.playSound(player, Sound.NOTE_PIANO);
				} else {
					EffectUtil.playSound(player, Sound.NOTE_PLING);
				}
				blocks.put(block, player.getName());
				if(!players.contains(player.getName())) {
					players.add(player.getName());
				}
			}
		}
	}
	
	@EventHandler
	public void onOneMinuteTask(OneMinuteTaskEvent event) {
		World world = Bukkit.getWorlds().get(0);
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(players.contains(player.getName())) {
				MessageHandler.sendMessage(player, "Egg Coloring Score: &e" + countBlocks(player));
			}
		}
		blocks.clear();
		players.clear();
		for(int x = -148; x <= -134; ++x) {
			for(int z = -146; z <= -132; ++z) {
				for(int y = 126; y <= 144; ++y) {
					Block block = world.getBlockAt(x, y, z);
					if(block.getType() == Material.STAINED_GLASS && block.getData() != 3) {
						block.setData((byte) 3);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		players.remove(event.getPlayer().getName());
	}
}
