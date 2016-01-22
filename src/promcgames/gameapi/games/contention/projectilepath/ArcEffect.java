package promcgames.gameapi.games.contention.projectilepath;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import promcgames.server.util.ParticleUtil;

@SuppressWarnings("deprecation")
public class ArcEffect {
	public void run(Player player) {
		float height = 2.0F;
		int particles = 100;
		Location location = player.getLocation().add(0, 1, 0);
		Location target = player.getTargetBlock(null, 1000).getLocation();
		Vector link = target.toVector().subtract(location.toVector());
        float length = (float) link.length();
        float pitch = (float) (4 * height / Math.pow(length, 2));
        for (int i = 0; i < particles; i++) {
            Vector v = link.clone().normalize().multiply((float) length * i / particles);
            float x = ((float) i / particles) * length - length / 2;
            float y = (float) (-pitch * Math.pow(x, 2) + height);
            location.add(v).add(0, y, 0);
            ParticleUtil.FLAME.display(location, 0, 0, 0, 0, 1, player);
            location.subtract(0, y, 0).subtract(v);
        }
	}
}
