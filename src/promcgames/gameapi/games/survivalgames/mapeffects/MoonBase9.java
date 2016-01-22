package promcgames.gameapi.games.survivalgames.mapeffects;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MoonBase9 extends MapEffectsBase {
	public MoonBase9() {
		super("Moon_Base_9");
	}

	@Override
	public void execute(World world) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999999, 100));
		}
	}
}
