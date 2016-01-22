package promcgames.server.nms;

import net.minecraft.server.v1_7_R4.EntityInsentient;
import net.minecraft.server.v1_7_R4.PathfinderGoal;

import org.bukkit.entity.Player;

public class PathfinderGoalWalkToOwner extends PathfinderGoal {
	private EntityInsentient entityInsentient;
	private float speed;
	private Player owner;
	
	public PathfinderGoalWalkToOwner(EntityInsentient entityInsentient, float speed, Player owner) {
		this.entityInsentient = entityInsentient;
		this.speed = speed;
		this.owner = owner;
	}
	
	@Override
	public boolean a() {
		return true;
	}
	
	@Override
	public void c() {
		int x = owner.getLocation().getBlockX();
		int y = owner.getLocation().getBlockY();
		int z = owner.getLocation().getBlockZ();
		this.entityInsentient.world.getWorld().loadChunk(x, z);
		this.entityInsentient.getNavigation().a(x, y, z, speed);
	}
}
