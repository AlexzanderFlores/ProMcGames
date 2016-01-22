package promcgames.server.servers.hub.items.cosmetic.pro.pets;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.server.v1_7_R4.EntityChicken;
import net.minecraft.server.v1_7_R4.EntityCow;
import net.minecraft.server.v1_7_R4.EntityHorse;
import net.minecraft.server.v1_7_R4.EntityInsentient;
import net.minecraft.server.v1_7_R4.EntityMagmaCube;
import net.minecraft.server.v1_7_R4.EntityMushroomCow;
import net.minecraft.server.v1_7_R4.EntityOcelot;
import net.minecraft.server.v1_7_R4.EntityPig;
import net.minecraft.server.v1_7_R4.EntitySheep;
import net.minecraft.server.v1_7_R4.EntitySlime;
import net.minecraft.server.v1_7_R4.EntitySnowman;
import net.minecraft.server.v1_7_R4.EntitySquid;
import net.minecraft.server.v1_7_R4.EntityTypes;
import net.minecraft.server.v1_7_R4.EntityWolf;

import org.bukkit.entity.EntityType;

import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.ChickenPet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.CowPet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.HorsePet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.MagmaCubePet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.MushroomCowPet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.OcelotPet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.PigPet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.SheepPet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.SlimePet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.SnowmanPet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.SquidPet;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.entities.WolfPet;

@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
public enum Pets {
	COW(EntityCow.class, CowPet.class),
	PIG(EntityPig.class, PigPet.class),
	CHICKEN(EntityChicken.class, ChickenPet.class),
	WOLF(EntityWolf.class, WolfPet.class),
	MUSHROOM_COW(EntityMushroomCow.class, MushroomCowPet.class),
	OCELOT(EntityOcelot.class, OcelotPet.class),
	SHEEP(EntitySheep.class, SheepPet.class),
	HORSE(EntityHorse.class, HorsePet.class),
	SLIME(EntitySlime.class, SlimePet.class),
	MAGMA_CUBE(EntityMagmaCube.class, MagmaCubePet.class),
	SQUID(EntitySquid.class, SquidPet.class),
	SNOWMAN(EntitySnowman.class, SnowmanPet.class);
	
	private Class<? extends EntityInsentient> defaultClass;
	private Class<? extends EntityInsentient> customClass;
	
	private Pets(Class<? extends EntityInsentient> defaultClass, Class<? extends EntityInsentient> customClass) {
		this.defaultClass = defaultClass;
		this.customClass = customClass;
	}
	
	public void register() {
		try {
			((Map) getPrivateStatic(EntityTypes.class, "c")).put(toString(), customClass);
			((Map) getPrivateStatic(EntityTypes.class, "d")).put(customClass, toString());
			((Map) getPrivateStatic(EntityTypes.class, "e")).put(Integer.valueOf(EntityType.valueOf(toString()).getTypeId()), customClass);
			((Map) getPrivateStatic(EntityTypes.class, "f")).put(customClass, Integer.valueOf(EntityType.valueOf(toString()).getTypeId()));
			((Map) getPrivateStatic(EntityTypes.class, "g")).put(toString(), Integer.valueOf(EntityType.valueOf(toString()).getTypeId()));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void unregister() {
		try {
			((Map) getPrivateStatic(EntityTypes.class, "c")).put(toString(), defaultClass);
			((Map) getPrivateStatic(EntityTypes.class, "d")).put(defaultClass, toString());
			((Map) getPrivateStatic(EntityTypes.class, "e")).put(Integer.valueOf(EntityType.valueOf(toString()).getTypeId()), defaultClass);
			((Map) getPrivateStatic(EntityTypes.class, "f")).put(defaultClass, Integer.valueOf(EntityType.valueOf(toString()).getTypeId()));
			((Map) getPrivateStatic(EntityTypes.class, "g")).put(toString(), Integer.valueOf(EntityType.valueOf(toString()).getTypeId()));
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