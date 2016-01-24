package promcgames.server.servers.hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.servers.hub.events.PlayerRidePlayerEvent;
import promcgames.server.servers.hub.items.HubItemBase;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;

@SuppressWarnings("deprecation")
public class SnowballFight extends HubItemBase implements Listener {
	private static HubItemBase instance = null;
	private ItemStack exit = null;
	private Random random = null;
	private static List<String> players = null;
	private List<String> wasFlying = null;
	private List<String> delayed = null;
	private Map<String, List<BlockState>> playersBlocks = null;
	
	public SnowballFight() {
		super(new ItemCreator(Material.SNOW_BALL).setName("&aEnable Snowball Fight Mode"), 4);
		instance = this;
		exit = new ItemCreator(Material.BEDROCK).setName("&cExit Snowball Fight Mode").getItemStack();
		random = new Random();
		players = new ArrayList<String>();
		wasFlying = new ArrayList<String>();
		delayed = new ArrayList<String>();
		playersBlocks = new HashMap<String, List<BlockState>>();
		new CommandBase("snowball", 0, 2, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(arguments.length == 1) {
					Block block = player.getTargetBlock(null, 10);
					if(block == null || block.getType() != Material.SNOW) {
						MessageHandler.sendMessage(player, "&cYou are not looking at a snow block");
					} else {
						String targetName = null;
						BlockState blockState = null;
						int x = block.getX();
						int y = block.getY();
						int z = block.getZ();
						for(String name : playersBlocks.keySet()) {
							Bukkit.getLogger().info(name);
							for(BlockState state : playersBlocks.get(name)) {
								if(state.getX() == x && state.getY() == y && state.getZ() == z) {
									targetName = name;
									blockState = state;
									break;
								}
							}
							if(targetName != null) {
								break;
							}
						}
						Player target = ProPlugin.getPlayer(targetName);
						if(target == null) {
							if(blockState == null) {
								MessageHandler.sendMessage(player, "&cCould not find the player that block was placed by");
							} else {
								block.setType(blockState.getType());
								block.setData(blockState.getData().getData());
								MessageHandler.sendMessage(player, "&cCould not find the player that block was placed by... removing block");
							}
						} else {
							MessageHandler.sendMessage(player, "That block was placed by &e" + target);
						}
					}
					return true;
				} else if(arguments.length == 2 && arguments[0].equalsIgnoreCase("ban")) {
					String name = arguments[1];
					UUID uuid = AccountHandler.getUUID(name);
					if(uuid == null) {
						MessageHandler.sendMessage(player, "&c" + name + " has never logged in before");
					} else if(DB.HUB_SNOWBALL_BANNED.isUUIDSet(uuid)) {
						MessageHandler.sendMessage(player, "&c" + name + " is already banned");
					} else {
						DB.HUB_SNOWBALL_BANNED.insert("'" + uuid.toString() + "'");
						Player target = Bukkit.getPlayer(uuid);
						if(target != null) {
							if(isPlaying(target)) {
								remove(target);
							}
							MessageHandler.sendMessage(target, "&cYou have been BANNED from the &eSnowball Fight &cmode for inappropriate builds");
						}
						MessageHandler.sendMessage(player, "You have BANNED " + name + " from the &eSnowball Fight &amode for inappropriate builds");
					}
					return true;
				} else if(arguments.length == 2 && arguments[0].equalsIgnoreCase("unBan")) {
					String name = arguments[1];
					UUID uuid = AccountHandler.getUUID(name);
					if(uuid == null) {
						MessageHandler.sendMessage(player, "&c" + name + " has never logged in before");
					} else if(DB.HUB_SNOWBALL_BANNED.isUUIDSet(uuid)) {
						DB.HUB_SNOWBALL_BANNED.deleteUUID(uuid);
						Player target = Bukkit.getPlayer(uuid);
						if(target != null) {
							MessageHandler.sendMessage(target, "You have been UNBANNED from the &eSnowball Fight &amode");
						}
						MessageHandler.sendMessage(player, "You have UNBANNED " + name + " from the &eSnowball Fight &amode");
					} else {
						MessageHandler.sendMessage(player, "&c" + name + " is not banned in the &eSnowball Fight &cmode");
					}
					return true;
				}
				displayHelp(player);
				return true;
			}
		}.setRequiredRank(Ranks.HELPER);
	}
	
	private void displayHelp(Player player) {
		MessageHandler.sendMessage(player, "/snowBall get &eGets the placer of the block you are looking at");
		MessageHandler.sendMessage(player, "/snowBall ban <name> &eBans a player for inappropriate builds");
		MessageHandler.sendMessage(player, "/snowBall unBan <name> &eUnbans a player");
	}
	
	public static HubItemBase getInstance() {
		return instance;
	}
	
	public static boolean isPlaying(Player player) {
		return players != null && players.contains(player.getName());
	}
	
	private void remove(Player player) {
		giveOriginalHotBar(player);
		players.remove(player.getName());
		if(wasFlying.contains(player.getName())) {
			player.setAllowFlight(true);
			wasFlying.remove(player.getName());
		}
		if(playersBlocks.containsKey(player.getName())) {
			List<BlockState> blocks = playersBlocks.get(player.getName());
			if(blocks != null && !blocks.isEmpty()) {
				for(BlockState block : blocks) {
					block.getBlock().setType(block.getType());
					block.getBlock().setData(block.getData().getData());
				}
				blocks.clear();
				playersBlocks.remove(player.getName());
			}
		}
	}
	
	@Override
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		giveItem(event.getPlayer());
	}
	
	@Override
	@EventHandler(priority = EventPriority.HIGH)
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(isItem(player)) {
			if(player.getLocation().getY() <= 130) {
				if(!delayed.contains(player.getName())) {
					final String name = player.getName();
					delayed.add(name);
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							delayed.remove(name);
						}
					}, 20 * 5);
					if(DB.HUB_SNOWBALL_BANNED.isUUIDSet(player.getUniqueId())) {
						MessageHandler.sendMessage(player, "&cCannot join &eSnowball Fight &cmode: You are banned for inappropriate builds");
					} else {
						if(player.getVehicle() != null) {
							player.leaveVehicle();
						}
						if(player.getPassenger() != null) {
							player.eject();
						}
						player.getInventory().clear();
						player.getInventory().setItem(8, exit);
						if(player.getAllowFlight()) {
							wasFlying.add(player.getName());
							player.setFlying(false);
							player.setAllowFlight(false);
						}
						MessageHandler.sendMessage(player, "Snowball Fight Mode &eON");
						MessageHandler.sendMessage(player, "Break snow for snowballs & snow blocks!");
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								players.add(name);
							}
						});
						if(Ranks.isStaff(player)) {
							displayHelp(player);
						}
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou must be below Y 130 to join the snowball fight");
			}
			player.updateInventory();
			event.setCancelled(true);
		} else if(ItemUtil.isItem(player.getItemInHand(), exit)) {
			remove(player);
			MessageHandler.sendMessage(player, "Snowball Fight Mode &cOFF");
			event.setCancelled(true);
		}
	}
	
	@Override
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		remove(event.getPlayer());
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if(block.getLocation().getY() > 140) {
			MessageHandler.sendMessage(player, "&cYou cannot build above Y-140");
		} else {
			Material type = block.getType();
			if(players.contains(player.getName()) && (type == Material.SNOW || type == Material.SNOW_BLOCK)) {
				World world = player.getWorld();
				Vector blockVector = block.getLocation().toVector();
				if(blockVector.isInSphere(world.getSpawnLocation().toVector(), 30)) {
					MessageHandler.sendMessage(player, "&cCannot build this close to spawn");
					return;
				} else {
					for(Entity entity : world.getEntities()) {
						if(entity instanceof LivingEntity) {
							LivingEntity livingEntity = (LivingEntity) entity;
							if(NPCEntity.isNPC(livingEntity) && blockVector.isInSphere(livingEntity.getLocation().toVector(), 5)) {
								MessageHandler.sendMessage(player, "&cCannot build this close to a NPC");
								return;
							}
						}
					}
				}
				List<BlockState> blocks = playersBlocks.get(player.getName());
				if(blocks == null) {
					blocks = new ArrayList<BlockState>();
				}
				blocks.add(event.getBlockReplacedState());
				playersBlocks.put(player.getName(), blocks);
				event.setCancelled(false);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if(playersBlocks.containsKey(player.getName())) {
			Block block = event.getBlock();
			List<BlockState> states = playersBlocks.get(player.getName());
			if(states != null) {
				for(BlockState state : states) {
					if(state.getLocation().equals(block.getLocation())) {
						block.setType(state.getType());
						block.setData(state.getData().getData());
						return;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Snowball) {
			Player player = (Player) event.getEntity();
			if(players.contains(player.getName())) {
				Snowball snowball = (Snowball) event.getDamager();
				if(snowball.getShooter() instanceof Player) {
					Player damager = (Player) snowball.getShooter();
					MessageHandler.sendMessage(damager, "You knocked " + AccountHandler.getPrefix(player) + " &aout of the snowball fight!");
					MessageHandler.sendMessage(player, "You were hit with a snowball by " + AccountHandler.getPrefix(damager));
					remove(player);
					player.teleport(player.getWorld().getSpawnLocation());
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(players.contains(player.getName())) {
			if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
				Material type = event.getClickedBlock().getType();
				if((type == Material.SNOW || type == Material.SNOW_BLOCK) && players.contains(player.getName())) {
					if(random.nextBoolean()) {
						player.getInventory().addItem(new ItemStack(Material.SNOW_BALL));
					} else {
						player.getInventory().addItem(new ItemStack(Material.SNOW_BLOCK));
					}
				}
			}
			event.setCancelled(false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		Projectile projectile = event.getEntity();
		if(projectile.getShooter() instanceof Player) {
			Player player = (Player) projectile.getShooter();
			if(players.contains(player.getName())) {
				event.setCancelled(false);
			}
		}
	}
	
	@EventHandler
	public void onPlayerRidePlayer(PlayerRidePlayerEvent event) {
		if(players.contains(event.getTopPlayer().getName()) || players.contains(event.getBottomPlayer().getName())) {
			event.setCancelled(true);
		}
	}
}
