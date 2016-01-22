package promcgames.gameapi.games.survivalgames.mapeffects;

import org.bukkit.World;

public class Mars extends MapEffectsBase {
	public Mars() {
		super("Mars");
	}
	
	@Override
	public void execute(World world) {
		world.setGameRuleValue("doDaylightCycle", "false");
		world.setTime(18000);
	}
}
