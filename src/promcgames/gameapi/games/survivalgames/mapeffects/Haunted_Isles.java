package promcgames.gameapi.games.survivalgames.mapeffects;

import org.bukkit.World;

public class Haunted_Isles extends MapEffectsBase {
	public Haunted_Isles() {
		super("Haunted_Isles");
	}
	
	@Override
	public void execute(World world) {
		world.setGameRuleValue("doDaylightCycle", "false");
		world.setTime(18000);
	}
}
