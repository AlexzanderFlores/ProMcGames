package promcgames.gameapi.games.survivalgames.mapeffects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;

public class MapEffectHandler {
	private static List<MapEffectsBase> effects = null;
	
	public MapEffectHandler(World world) {
		new BasicFallingChests();
		new BreezeIslandTwo();
		new Collapse();
		new DryboneValley();
		new Haunted_Isles();
		new Highway();
		new Mars();
		new MoonBase9();
		new Turbulence();
		new Zone85();
		for(MapEffectsBase effect : effects) {
			if(effect.getName() != null && effect.getName().equals(world.getName())) {
				effect.execute(world);
				if(world.getName().equals("Highway")) {
					return; // Highway uses its own chest spawning animation, return before the execution of the BasicFallingChests class
				} else {
					break;
				}
			}
		}
		new BasicFallingChests().execute(world);
		effects.clear();
		effects = null;
	}
	
	public static void addEffect(MapEffectsBase effect) {
		if(effects == null) {
			effects = new ArrayList<MapEffectsBase>();
		}
		effects.add(effect);
	}
}
