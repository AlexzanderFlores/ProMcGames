package promcgames.gameapi.games.survivalgames.mapeffects;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Collapse extends MapEffectsBase {
	public Collapse() {
		super("Collapse");
	}

	@Override
	public void execute(World world) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999999, 100));
		}
	}
}
