package promcgames.server.nms.npcs;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.server.v1_7_R4.EntityBat;
import net.minecraft.server.v1_7_R4.EntityBlaze;
import net.minecraft.server.v1_7_R4.EntityCaveSpider;
import net.minecraft.server.v1_7_R4.EntityChicken;
import net.minecraft.server.v1_7_R4.EntityCow;
import net.minecraft.server.v1_7_R4.EntityCreeper;
import net.minecraft.server.v1_7_R4.EntityEnderman;
import net.minecraft.server.v1_7_R4.EntityGhast;
import net.minecraft.server.v1_7_R4.EntityHorse;
import net.minecraft.server.v1_7_R4.EntityInsentient;
import net.minecraft.server.v1_7_R4.EntityIronGolem;
import net.minecraft.server.v1_7_R4.EntityMagmaCube;
import net.minecraft.server.v1_7_R4.EntityMushroomCow;
import net.minecraft.server.v1_7_R4.EntityOcelot;
import net.minecraft.server.v1_7_R4.EntityPig;
import net.minecraft.server.v1_7_R4.EntityPigZombie;
import net.minecraft.server.v1_7_R4.EntitySheep;
import net.minecraft.server.v1_7_R4.EntitySilverfish;
import net.minecraft.server.v1_7_R4.EntitySkeleton;
import net.minecraft.server.v1_7_R4.EntitySlime;
import net.minecraft.server.v1_7_R4.EntitySnowman;
import net.minecraft.server.v1_7_R4.EntitySpider;
import net.minecraft.server.v1_7_R4.EntitySquid;
import net.minecraft.server.v1_7_R4.EntityTypes;
import net.minecraft.server.v1_7_R4.EntityVillager;
import net.minecraft.server.v1_7_R4.EntityWitch;
import net.minecraft.server.v1_7_R4.EntityWolf;
import net.minecraft.server.v1_7_R4.EntityZombie;

import org.bukkit.entity.EntityType;

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

@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
public class NPCRegistrationHandler {
	public enum NPCs {
		BAT(EntityBat.class, BatNPC.class),
		BLAZE(EntityBlaze.class, BlazeNPC.class),
		CAVE_SPIDER(EntityCaveSpider.class, CaveSpiderNPC.class),
		CHICKEN(EntityChicken.class, ChickenNPC.class),
		COW(EntityCow.class, CowNPC.class),
		CREEPER(EntityCreeper.class, CreeperNPC.class),
		ENDERMAN(EntityEnderman.class, EndermanNPC.class),
		GHAST(EntityGhast.class, GhastNPC.class),
		HORSE(EntityHorse.class, HorseNPC.class),
		IRON_GOLEM(EntityIronGolem.class, IronGolemNPC.class),
		MAGMA_CUBE(EntityMagmaCube.class, MagmaCubeNPC.class),
		MUSHROOM_COW(EntityMushroomCow.class, MushroomCowNPC.class),
		OCELOT(EntityOcelot.class, OcelotNPC.class),
		PIG(EntityPig.class, PigNPC.class),
		PIG_ZOMBIE(EntityPigZombie.class, PigZombieNPC.class),
		SHEEP(EntitySheep.class, SheepNPC.class),
		SILVERFISH(EntitySilverfish.class, SilverfishNPC.class),
		SKELETON(EntitySkeleton.class, SkeletonNPC.class),
		SLIME(EntitySlime.class, SlimeNPC.class),
		SNOWMAN(EntitySnowman.class, SnowmanNPC.class),
		SPIDER(EntitySpider.class, SpiderNPC.class),
		SQUID(EntitySquid.class, SquidNPC.class),
		VILLAGER(EntityVillager.class, VillagerNPC.class),
		WITCH(EntityWitch.class, WitchNPC.class),
		WOLF(EntityWolf.class, WolfNPC.class),
		ZOMBIE(EntityZombie.class, ZombieNPC.class);
		
		private Class<? extends EntityInsentient> defaultClass = null;
		private Class<? extends EntityInsentient> customClass = null;
		
		private NPCs(Class<? extends EntityInsentient> defaultClass, Class<? extends EntityInsentient> customClass) {
			this.defaultClass = defaultClass;
			this.customClass = customClass;
		}
		
		public void register() {
			try {
				((Map) getPrivateStatic(EntityTypes.class, "c")).put(toString(), customClass);
				((Map) getPrivateStatic(EntityTypes.class, "d")).put(customClass, toString());
				int typeId = Integer.valueOf(EntityType.valueOf(toString()).getTypeId());
				((Map) getPrivateStatic(EntityTypes.class, "e")).put(typeId, customClass);
				((Map) getPrivateStatic(EntityTypes.class, "f")).put(customClass, typeId);
				((Map) getPrivateStatic(EntityTypes.class, "g")).put(toString(), typeId);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		public void unregister() {
			try {
				((Map) getPrivateStatic(EntityTypes.class, "c")).put(toString(), defaultClass);
				((Map) getPrivateStatic(EntityTypes.class, "d")).put(defaultClass, toString());
				int typeId = Integer.valueOf(EntityType.valueOf(toString()).getTypeId());
				((Map) getPrivateStatic(EntityTypes.class, "e")).put(typeId, defaultClass);
				((Map) getPrivateStatic(EntityTypes.class, "f")).put(defaultClass, typeId);
				((Map) getPrivateStatic(EntityTypes.class, "g")).put(toString(), typeId);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		private static Object getPrivateStatic(Class clazz, String f) throws Exception {
			Field field = clazz.getDeclaredField(f);
			field.setAccessible(true);
			return field.get(null);
		}
	}
}
