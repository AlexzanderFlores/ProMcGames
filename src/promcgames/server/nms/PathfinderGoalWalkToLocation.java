package promcgames.server.nms;

import net.minecraft.server.v1_7_R4.EntityInsentient;
import net.minecraft.server.v1_7_R4.PathfinderGoal;

import org.bukkit.Location;

public class PathfinderGoalWalkToLocation extends PathfinderGoal {
	private EntityInsentient entityInsentient;
	private float speed;
	private Location target;
	
	public PathfinderGoalWalkToLocation(EntityInsentient entityInsentient, float speed, Location target) {
		this.entityInsentient = entityInsentient;
		this.speed = speed;
		this.target = target;
	}
	
	@Override
	public boolean a() {
		c();
		return true;
	}
	
	@Override
	public void c() {
		int x = target.getBlockX();
		int y = target.getBlockY();
		int z = target.getBlockZ();
		this.entityInsentient.world.getWorld().loadChunk(x, z);
		this.entityInsentient.getNavigation().a(x, y, z, speed);
	}
}
