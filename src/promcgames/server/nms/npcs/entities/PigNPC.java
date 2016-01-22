package promcgames.server.nms.npcs.entities;

import java.lang.reflect.Field;

import net.minecraft.server.v1_7_R4.EntityPig;
import net.minecraft.server.v1_7_R4.PathfinderGoalSelector;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.craftbukkit.v1_7_R4.util.UnsafeList;
import org.bukkit.entity.LivingEntity;

import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.util.ReflectionUtil;

public class PigNPC extends EntityPig {
	public PigNPC(World world) {
		super(world);
		try {
			for(String fieldName : new String [] {"b", "c"}) {
				Field field = ReflectionUtil.getDeclaredField(PathfinderGoalSelector.class, fieldName);
				field.setAccessible(true);
				field.set(goalSelector, new UnsafeList<PathfinderGoalSelector>());
				field.set(targetSelector, new UnsafeList<PathfinderGoalSelector>());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String t() {
		return null;
	}
	
	@Override
	public void g(double x, double y, double z) {
		LivingEntity livingEntity = (LivingEntity) getBukkitEntity();
		if(getBukkitEntity().getTicksLived() <= NPCEntity.ableToMove && NPCEntity.getNPC(livingEntity).getSpawnZombie()) {
			super.g(x, y, z);
		}
	}
	
	@Override
	public void move(double x, double y, double z) {
		LivingEntity livingEntity = (LivingEntity) getBukkitEntity();
		if(getBukkitEntity().getTicksLived() <= NPCEntity.ableToMove && NPCEntity.getNPC(livingEntity).getSpawnZombie()) {
			super.move(x, y, z);
		}
	}
}
