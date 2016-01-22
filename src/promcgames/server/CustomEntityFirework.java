package promcgames.server;

import java.util.List;

import net.minecraft.server.v1_7_R4.EntityFireworks;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class CustomEntityFirework extends EntityFireworks {
	private List<Player> players = null;
	private boolean gone = false;
	
	public CustomEntityFirework(World world, List<Player> players) {
		super(world);
		this.a(0.25F, 0.25F);
		this.players = players;
	}
	
	@Override
	public void h() {
		if(gone) {
			return;
		}
		if(!this.world.isStatic) {
			gone = true;
			for(Player player : players) {
				CraftPlayer craftPlayer = (CraftPlayer) player;
				craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutEntityStatus(this, (byte) 17));
			}
			this.die();
			return;
		}
		world.broadcastEntityEffect(this, (byte) 17);
		this.die();
	}
	
	public static void spawn(Location location, FireworkEffect effect, List<Player> players) {
		try {
			CraftWorld craftWorld = (CraftWorld) location.getWorld();
			CustomEntityFirework customFirework = new CustomEntityFirework(craftWorld.getHandle(), players);
			Firework firework = (Firework) customFirework.getBukkitEntity();
			FireworkMeta meta = firework.getFireworkMeta();
			meta.addEffect(effect);
			firework.setFireworkMeta(meta);
			customFirework.setPosition(location.getX(), location.getY(), location.getZ());
			if(craftWorld.getHandle().addEntity(customFirework)) {
				customFirework.setInvisible(true);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
