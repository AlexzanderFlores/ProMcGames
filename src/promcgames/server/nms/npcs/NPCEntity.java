package promcgames.server.nms.npcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.server.v1_7_R4.EntityLiving;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.SpectatorHandler;
import promcgames.player.MessageHandler;
import promcgames.server.nms.npcs.NPCRegistrationHandler.NPCs;
import promcgames.server.nms.npcs.entities.BatNPC;
import promcgames.server.nms.npcs.entities.BlazeNPC;
import promcgames.server.nms.npcs.entities.CaveSpiderNPC;
import promcgames.server.nms.npcs.entities.ChickenNPC;
import promcgames.server.nms.npcs.entities.CowNPC;
import promcgames.server.nms.npcs.entities.CreeperNPC;
import promcgames.server.nms.npcs.entities.EndermanNPC;
import promcgames.server.nms.npcs.entities.GhastNPC;
import promcgames.server.nms.npcs.entities.HorseNPC;
import promcgames.server.nms.npcs.entities.IronGolemNPC;
import promcgames.server.nms.npcs.entities.MagmaCubeNPC;
import promcgames.server.nms.npcs.entities.MushroomCowNPC;
import promcgames.server.nms.npcs.entities.OcelotNPC;
import promcgames.server.nms.npcs.entities.PigNPC;
import promcgames.server.nms.npcs.entities.PigZombieNPC;
import promcgames.server.nms.npcs.entities.SheepNPC;
import promcgames.server.nms.npcs.entities.SilverfishNPC;
import promcgames.server.nms.npcs.entities.SkeletonNPC;
import promcgames.server.nms.npcs.entities.SlimeNPC;
import promcgames.server.nms.npcs.entities.SnowmanNPC;
import promcgames.server.nms.npcs.entities.SpiderNPC;
import promcgames.server.nms.npcs.entities.SquidNPC;
import promcgames.server.nms.npcs.entities.VillagerNPC;
import promcgames.server.nms.npcs.entities.WitchNPC;
import promcgames.server.nms.npcs.entities.WolfNPC;
import promcgames.server.nms.npcs.entities.ZombieNPC;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.VectorUtil;

public abstract class NPCEntity implements Listener {
	public static final int ableToMove = 20;
	private static List<LivingEntity> entities = null;
	private static Map<LivingEntity, NPCEntity> npcEntities = null;
	private String name = null;
	//private Hologram hologram = null;
	private Location location = null;
	private Location target = null;
	private ItemStack itemStack = null;
	private LivingEntity livingEntity = null;
	private boolean spawnZombie = true;
	
	public NPCEntity(EntityType entityType, String name, Location location) {
		this(entityType, name, location, new Location(null, 0, 0, 0));
	}
	
	public NPCEntity(EntityType entityType, String name, Location location, Material material) {
		this(entityType, name, location, null, material);
	}
	
	public NPCEntity(EntityType entityType, String name, Location location, ItemStack itemStack) {
		this(entityType, name, location, null, itemStack);
	}
	
	public NPCEntity(EntityType entityType, String name, Location location, Location target) {
		this(entityType, name, location, target == null || target.getWorld() == null ? null : target, Material.AIR);
	}
	
	public NPCEntity(EntityType entityType, String name, Location location, Location target, Material material) {
		this(entityType, name, location, target, new ItemStack(material));
	}
	
	public NPCEntity(EntityType entityType, String name, Location location, Location target, ItemStack itemStack) {
		NPCs.valueOf(entityType.toString()).register();
		this.name = name == null ? "" : name;
		this.location = location;
		this.target = target;
		this.itemStack = itemStack;
		EventUtil.register(this);
		location.getChunk().load();
		location.getWorld().spawnEntity(location, entityType);
		CreatureSpawnEvent.getHandlerList().unregister(this);
	}
	
	public static boolean isNPC(LivingEntity livingEntity) {
		return entities != null && entities.contains(livingEntity);
	}
	
	public static NPCEntity getNPC(LivingEntity livingEntity) {
		if(npcEntities == null) {
			return null;
		} else {
			return npcEntities.get(livingEntity);
		}
	}
	
	public void remove() {
		npcEntities.remove(livingEntity);
		livingEntity.remove();
		HandlerList.unregisterAll(this);
		/*if(hologram != null) {
			hologram.delete();
			hologram = null;
		}*/
		location = null;
		itemStack = null;
		livingEntity = null;
	}
	
	public void setName(String text) {
		livingEntity.setCustomNameVisible(true);
		livingEntity.setCustomName(text);
		/*hologram.clearLines();
		TextLine textLine = hologram.appendTextLine(text);
		textLine.setTouchHandler(new TouchHandler() {
			@Override
			public void onTouch(Player player) {
				onInteract(player);
			}
		});*/
	}
	
	public String getName() {
		return livingEntity.getCustomName();
		/*String name = hologram.getLine(0).toString().split("=")[1];
		return name.substring(0, name.length() - 1);*/
	}
	
	public LivingEntity getLivingEntity() {
		return livingEntity;
	}
	
	public NPCEntity setSpawnZombie(boolean spawnZombie) {
		this.spawnZombie = spawnZombie;
		return this;
	}
	
	public boolean getSpawnZombie() {
		return spawnZombie;
	}
	
	public abstract void onInteract(Player player);
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(!event.isCancelled() && event.getSpawnReason() == SpawnReason.CUSTOM) {
			Entity entity = event.getEntity();
			final World world = entity.getWorld();
			CraftWorld craftWorld = (CraftWorld) world;
			CraftEntity craftEntity = (CraftEntity) entity;
			EntityLiving entityLiving = null;
			if(entity.getType() == EntityType.BAT && !(craftEntity.getHandle() instanceof BatNPC)) {
				entityLiving = new BatNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.BLAZE && !(craftEntity.getHandle() instanceof BlazeNPC)) {
				entityLiving = new BlazeNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.CAVE_SPIDER && !(craftEntity.getHandle() instanceof CaveSpiderNPC)) {
				entityLiving = new CaveSpiderNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.CHICKEN && !(craftEntity.getHandle() instanceof ChickenNPC)) {
				entityLiving = new ChickenNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.COW && !(craftEntity.getHandle() instanceof CowNPC)) {
				entityLiving = new CowNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.CREEPER && !(craftEntity.getHandle() instanceof CreeperNPC)) {
				entityLiving = new CreeperNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.ENDERMAN && !(craftEntity.getHandle() instanceof EndermanNPC)) {
				entityLiving = new EndermanNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.GHAST && !(craftEntity.getHandle() instanceof GhastNPC)) {
				entityLiving = new GhastNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.HORSE && !(craftEntity.getHandle() instanceof HorseNPC)) {
				entityLiving = new HorseNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.IRON_GOLEM && !(craftEntity.getHandle() instanceof IronGolemNPC)) {
				entityLiving = new IronGolemNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.MAGMA_CUBE && !(craftEntity.getHandle() instanceof MagmaCubeNPC)) {
				entityLiving = new MagmaCubeNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.MUSHROOM_COW && !(craftEntity.getHandle() instanceof MushroomCowNPC)) {
				entityLiving = new MushroomCowNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.OCELOT && !(craftEntity.getHandle() instanceof OcelotNPC)) {
				entityLiving = new OcelotNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.PIG && !(craftEntity.getHandle() instanceof PigNPC)) {
				entityLiving = new PigNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.PIG_ZOMBIE && !(craftEntity.getHandle() instanceof PigZombieNPC)) {
				entityLiving = new PigZombieNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.SHEEP && !(craftEntity.getHandle() instanceof SheepNPC)) {
				entityLiving = new SheepNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.SILVERFISH && !(craftEntity.getHandle() instanceof SilverfishNPC)) {
				entityLiving = new SilverfishNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.SKELETON && !(craftEntity.getHandle() instanceof SkeletonNPC)) {
				entityLiving = new SkeletonNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.SLIME && !(craftEntity.getHandle() instanceof SlimeNPC)) {
				entityLiving = new SlimeNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.SNOWMAN && !(craftEntity.getHandle() instanceof SnowmanNPC)) {
				entityLiving = new SnowmanNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.SPIDER && !(craftEntity.getHandle() instanceof SpiderNPC)) {
				entityLiving = new SpiderNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.SQUID && !(craftEntity.getHandle() instanceof SquidNPC)) {
				entityLiving = new SquidNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.VILLAGER && !(craftEntity.getHandle() instanceof VillagerNPC)) {
				entityLiving = new VillagerNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.WITCH && !(craftEntity.getHandle() instanceof WitchNPC)) {
				entityLiving = new WitchNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.WOLF && !(craftEntity.getHandle() instanceof WolfNPC)) {
				entityLiving = new WolfNPC(craftWorld.getHandle());
			} else if(entity.getType() == EntityType.ZOMBIE && !(craftEntity.getHandle() instanceof ZombieNPC)) {
				entityLiving = new ZombieNPC(craftWorld.getHandle());
			}
			if(entityLiving != null) {
				if(location.getYaw() == 0.0f && location.getPitch() == 0.0f) {
					if(target == null) {
						target = location.getWorld().getSpawnLocation();
					}
					location.setDirection(VectorUtil.getDirectionVector(location, target, 5));
				}
				location.getChunk().load();
				livingEntity = (LivingEntity) entityLiving.getBukkitEntity();
				livingEntity.setRemoveWhenFarAway(false);
				livingEntity.getEquipment().setItemInHand(itemStack);
				entityLiving.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
				craftWorld.getHandle().removeEntity(craftEntity.getHandle());
				craftWorld.getHandle().addEntity(entityLiving, SpawnReason.CUSTOM);
				if(entities == null) {
					entities = new ArrayList<LivingEntity>();
				}
				entities.add(livingEntity);
				if(npcEntities == null) {
					npcEntities = new HashMap<LivingEntity, NPCEntity>();
				}
				npcEntities.put(livingEntity, this);
				if(spawnZombie && name != "") {
					new DelayedTask(new Runnable() {
						@Override
						public void run() {
							final Zombie zombie = (Zombie) world.spawnEntity(spawnZombie ? location.add(0.5, 1, 0.5) : new Location(world, 0, -30, 0), EntityType.ZOMBIE);
							new DelayedTask(new Runnable() {
								@Override
								public void run() {
									setName(ChatColor.translateAlternateColorCodes('&', name));
									zombie.remove();
								}
							}, 5);
						}
					});
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Creature) {
			final Creature creature = (Creature) event.getEntity();
			try { // TODO add in checks to find null stuff
				if(creature.equals(getLivingEntity())) {
					if(event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.VOID) {
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								creature.teleport(location);
							}
						}, 5);
					}
					event.setCancelled(true);
				}
			} catch(NullPointerException e) {
				
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof LivingEntity && event.getDamager() instanceof Player) {
			LivingEntity livingEntity = (LivingEntity) event.getEntity();
			if(livingEntity.equals(getLivingEntity())) {
				Player player = (Player) event.getDamager();
				if(SpectatorHandler.contains(player)) {
					MessageHandler.sendMessage(player, "&cYou cannot interact with NPCs as a spectator");
				} else {
					onInteract(player);
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked() instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) event.getRightClicked();
			if(livingEntity.equals(getLivingEntity())) {
				Player player = event.getPlayer();
				if(SpectatorHandler.contains(player)) {
					MessageHandler.sendMessage(player, "&cYou cannot interact with NPCs as a spectator");
				} else {
					onInteract(player);
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityCombust(EntityCombustEvent event) {
		if(event.getEntity() instanceof Creature) {
			Creature creature = (Creature) event.getEntity();
			if(creature.equals(getLivingEntity())) {
				event.setCancelled(true);
			}
		}
	}
}
