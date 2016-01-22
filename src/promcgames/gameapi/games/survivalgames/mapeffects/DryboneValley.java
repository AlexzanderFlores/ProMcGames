package promcgames.gameapi.games.survivalgames.mapeffects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.timed.TwoSecondTaskEvent;
import promcgames.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class DryboneValley extends MapEffectsBase implements Listener {
	private List<CowEye> cowEyes = null;
	
	private static class CowEye {
		private Location left = null;
		private Location right = null;
		private int counter = 0;
		
		public CowEye(World world, int x, int y, int z1, int z2) {
			left = new Location(world, x, y, z1);
			right = new Location(world, x, y, z2);
		}
		
		public void changeView() {
			if(counter == 0) {
				left.getBlock().setData(DyeColor.WHITE.getData());
				right.getBlock().setData(DyeColor.BLACK.getData());
				++counter;
			} else if(counter == 1){
				left.getBlock().setData(DyeColor.BLACK.getData());
				right.getBlock().setData(DyeColor.WHITE.getData());
				++counter;
			} else if(counter == 2) {
				left.getBlock().setData(DyeColor.GRAY.getData());
				right.getBlock().setData(DyeColor.GRAY.getData());
				counter = 0;
			}
		}
	}
	
	public DryboneValley() {
		super("Drybone_Valley");
	}

	@Override
	public void execute(World world) {
		cowEyes = new ArrayList<CowEye>();
		cowEyes.add(new CowEye(world, -1268, 20, 623, 624));
		cowEyes.add(new CowEye(world, -1268, 20, 629, 630));
		//A leaf that's in the way of an eye, easier to remove via plugin instead of changing the map for one block
		new Location(world, -1269, 20, 629).getBlock().setType(Material.AIR);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTwoSecondTask(TwoSecondTaskEvent event) {
		for(CowEye cowEye : cowEyes) {
			cowEye.changeView();
		}
	}
}
