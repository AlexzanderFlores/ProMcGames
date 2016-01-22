package promcgames.gameapi.games.factions.spawn;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import promcgames.customevents.game.GameDeathEvent;
import promcgames.gameapi.games.factions.CoinHandler;
import promcgames.gameapi.games.factions.VIPHandler;
import promcgames.player.MessageHandler;
import promcgames.server.DB;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class SkullShop implements Listener {
	private List<Block> skulls = null;
	private List<String> recent = null;
	
	public SkullShop() {
		World world = Bukkit.getWorlds().get(0);
		new NPCEntity(EntityType.SKELETON, "&cShop Keeper", new Location(world, -294.5, 66, 335.5, -301.0f, 0.0f)) {
			@Override
			public void onInteract(Player player) {
				ItemStack item = player.getItemInHand();
				if(item == null || (item.getType() != Material.SKULL_ITEM && item.getData().getData() != 3)) {
					MessageHandler.sendMessage(player, "&cSorry, I only accept player skulls here");
				} else {
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					if(meta.getOwner() == null) {
						MessageHandler.sendMessage(player, "&cSorry, I only accept player skulls here");
					} else {
						final String owner = meta.getOwner();
						int amount = VIPHandler.isVIPPlus(player) ? 1000 : 500;
						CoinHandler.addCoins(player, amount);
						MessageHandler.sendMessage(player, "You have sold &b" + owner + "&a's skull for &e$" + amount + " Coins");
						player.setItemInHand(new ItemStack(Material.AIR));
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								DB.PLAYERS_FACTIONS_SKULLS.insert("'" + owner + "'");
								update();
							}
						});
					}
				}
			}
		};
		skulls = new ArrayList<Block>();
		skulls.add(new Location(world, -298, 67, 338).getBlock());
		skulls.add(new Location(world, -298, 67, 337).getBlock());
		skulls.add(new Location(world, -298, 69, 338).getBlock());
		skulls.add(new Location(world, -298, 69, 337).getBlock());
		skulls.add(new Location(world, -299, 67, 336).getBlock());
		skulls.add(new Location(world, -299, 69, 336).getBlock());
		skulls.add(new Location(world, -302, 67, 335).getBlock());
		skulls.add(new Location(world, -302, 67, 334).getBlock());
		skulls.add(new Location(world, -302, 67, 333).getBlock());
		skulls.add(new Location(world, -302, 69, 335).getBlock());
		skulls.add(new Location(world, -302, 69, 334).getBlock());
		skulls.add(new Location(world, -302, 69, 333).getBlock());
		skulls.add(new Location(world, -298, 67, 328).getBlock());
		skulls.add(new Location(world, -297, 67, 328).getBlock());
		skulls.add(new Location(world, -296, 67, 328).getBlock());
		skulls.add(new Location(world, -298, 69, 328).getBlock());
		skulls.add(new Location(world, -297, 69, 328).getBlock());
		skulls.add(new Location(world, -296, 69, 328).getBlock());
		recent = new ArrayList<String>();
		update();
		EventUtil.register(this);
	}
	
	private void update() {
		recent.clear();
		List<String> table = DB.PLAYERS_FACTIONS_SKULLS.getOrdered("id", "name", 18, true);
		for(int a = 0; a < table.size(); ++a) {
			String name = table.get(a);
			Block block = skulls.get(a);
			Skull skull = (Skull) block.getState();
			skull.setOwner(name);
			skull.update();
			recent.add(name);
			block = block.getRelative(1, -1, 0);
			Sign sign = null;
			if(block.getType() == Material.WALL_SIGN) {
				sign = (Sign) block.getState();
			} else {
				block = block.getRelative(-1, 1, 0).getRelative(0, -1, 1);
				sign = (Sign) block.getState();
			}
			sign.setLine(1, name);
			sign.setLine(2, "Buy for $1000");
			sign.update();
		}
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		Location location = event.getPlayer().getLocation();
		Block block = location.getBlock().getRelative(0, 1, 0);
		block.setType(Material.SKULL);
		block.setData((byte) 1);
		Skull skull = (Skull) block.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setOwner(event.getPlayer().getName());
		skull.update();
	}
}
