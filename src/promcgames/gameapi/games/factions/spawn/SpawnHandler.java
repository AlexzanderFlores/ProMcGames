package promcgames.gameapi.games.factions.spawn;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.TeleportCoolDown;
import promcgames.gameapi.games.factions.spawn.crates.CrateHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.util.EventUtil;
import promcgames.staff.StaffMode;

public class SpawnHandler implements Listener {
	private static World world = null;
	private static Location spawn = null;
	public enum WorldLocation {SAFEZONE, WARZONE, WILDERNESS};
	
	public SpawnHandler() {
		world = Bukkit.getWorlds().get(0);
		spawn = new Location(world, -258.5, 71, 303.5, -180.0f, 0.0f);
		world.setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
		new CommandBase("spawn", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(getSpawnLevel(player.getLocation()) == WorldLocation.SAFEZONE) {
					spawn(player);
				} else {
					new TeleportCoolDown(player, getPlayerSpawn());
				}
				return true;
			}
		};
		new CrateHandler();
		new Spawners();
		new ShopHandler();
		new SkullShop();
		EventUtil.register(this);
	}
	
	public static Location getSpawn() {
		return spawn;
	}
	
	public static WorldLocation getSpawnLevel(Location location) {
		if(!location.getWorld().getName().equals(world.getName())) {
			return WorldLocation.WILDERNESS;
		}
		int x1 = spawn.getBlockX();
		int z1 = spawn.getBlockZ();
		int x2 = location.getBlockX();
		int z2 = location.getBlockZ();
		int x = x1 - x2;
		if(x < 0) {
			x = x * -1;
		}
		int z = z1 - z2;
		if(z < 0) {
			z = z * -1;
		}
		return x <= 50 && z <= 50 ? WorldLocation.SAFEZONE : x <= 100 && z <= 100 ? WorldLocation.WARZONE : WorldLocation.WILDERNESS;
	}
	
	private static Location getPlayerSpawn() {
		Location location = spawn.clone();
		Random random = new Random();
		int range = 3;
		location.setX(location.getBlockX() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		location.setY(location.getY() + 2.5d);
		location.setZ(location.getBlockZ() + (random.nextInt(range) * (random.nextBoolean() ? 1 : -1)));
		location.setYaw(-180.0f);
		location.setPitch(-0.0f);
		return location;
	}
	
	private static void spawn(Player player) {
		player.teleport(getSpawn());
		if(Ranks.PRO_PLUS.hasRank(player)) {
			player.setAllowFlight(true);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(getSpawnLevel(player.getLocation()) == WorldLocation.SAFEZONE) {
			spawn(player);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if(player.getAllowFlight() && !StaffMode.contains(player) && getSpawnLevel(event.getTo()) != WorldLocation.SAFEZONE) {
			player.setFlying(false);
			player.setAllowFlight(false);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		WorldLocation toLevel = getSpawnLevel(event.getTo());
		if(toLevel != WorldLocation.SAFEZONE && event.getPlayer().getAllowFlight() && !StaffMode.contains(event.getPlayer()) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			Player player = event.getPlayer();
			if(player.isFlying()) {
				MessageHandler.sendMessage(player, "&cYou cannot fly outside of the spawn");
				event.setTo(event.getFrom());
			} else {
				MessageHandler.sendMessage(player, "&cDisabling flight due to leaving spawn");
				player.setAllowFlight(false);
			}
		} else if(toLevel == WorldLocation.SAFEZONE && Ranks.PRO_PLUS.hasRank(event.getPlayer()) && !event.getPlayer().getAllowFlight()) {
			event.getPlayer().setAllowFlight(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if(player.getGameMode() != GameMode.CREATIVE) {
			Block block = event.getBlock();
			WorldLocation level = getSpawnLevel(block.getLocation());
			if(level != WorldLocation.WILDERNESS) {
				event.setCancelled(true);
			}
			if(level == WorldLocation.WARZONE && block.getType() == Material.SKULL) {
				event.setCancelled(false);
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if(player.getGameMode() != GameMode.CREATIVE) {
			Block block = event.getBlock();
			if(getSpawnLevel(block.getLocation()) != WorldLocation.WILDERNESS) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(player.getGameMode() != GameMode.CREATIVE && getSpawnLevel(player.getLocation()) == WorldLocation.SAFEZONE) {
			ItemStack item = player.getItemInHand();
			if(item != null && item.getType() == Material.MONSTER_EGG) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(getSpawnLevel(event.getEntity().getLocation()) == WorldLocation.SAFEZONE && event.getEntity() instanceof Player) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof ItemFrame && getSpawnLevel(event.getEntity().getLocation()) == WorldLocation.SAFEZONE) {
			if(event.getDamager() instanceof Player) {
				Player player = (Player) event.getDamager();
				if(player.getGameMode() == GameMode.CREATIVE) {
					return;
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked() instanceof ItemFrame && getSpawnLevel(event.getRightClicked().getLocation()) == WorldLocation.SAFEZONE && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		WorldLocation level = getSpawnLevel(event.getLocation());
		if(level == WorldLocation.SAFEZONE || level == WorldLocation.WARZONE) {
			if(event.getSpawnReason() != SpawnReason.CUSTOM) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if(getSpawnLevel(event.getLocation()) != WorldLocation.WILDERNESS) {
			event.setYield(0.0f);
			if(event.blockList() != null) {
				event.blockList().clear();
			}
		}
	}
	
	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event) {
		if(getSpawnLevel(event.getBlock().getLocation()) != WorldLocation.WILDERNESS) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockBurning(BlockBurnEvent event) {
		if(getSpawnLevel(event.getBlock().getLocation()) == WorldLocation.SAFEZONE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockFading(BlockFadeEvent event) {
		if(getSpawnLevel(event.getBlock().getLocation()) == WorldLocation.SAFEZONE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockForming(BlockFormEvent event) {
		if(getSpawnLevel(event.getBlock().getLocation()) == WorldLocation.SAFEZONE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		if(getSpawnLevel(event.getBlock().getLocation()) == WorldLocation.SAFEZONE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockGrow(BlockGrowEvent event) {
		if(getSpawnLevel(event.getBlock().getLocation()) == WorldLocation.SAFEZONE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockSpread(BlockSpreadEvent event) {
		if(getSpawnLevel(event.getBlock().getLocation()) == WorldLocation.SAFEZONE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		if(getSpawnLevel(event.getBlockClicked().getLocation()) != WorldLocation.WILDERNESS) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		if(getSpawnLevel(event.getBlockClicked().getLocation()) != WorldLocation.WILDERNESS) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPortalCreate(PortalCreateEvent event) {
		for(Block block : event.getBlocks()) {
			if(getSpawnLevel(block.getLocation()) != WorldLocation.WILDERNESS) {
				event.setCancelled(true);
				break;
			}
		}
	}
}
