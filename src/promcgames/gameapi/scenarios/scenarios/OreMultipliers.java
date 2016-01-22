package promcgames.gameapi.scenarios.scenarios;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.scenarios.Scenario;

public class OreMultipliers extends Scenario {
	private static OreMultipliers instance = null;
	private static int multiplier = 0;
	
	public OreMultipliers(String name, int multiplier, Material material) {
		this(name, multiplier, new ItemStack(material));
	}
	
	public OreMultipliers(String name, int multiplier, ItemStack item) {
		super(name, item);
		instance = this;
		setMultiplier(multiplier);
	}
	
	public static OreMultipliers getInstance(String name, int multiplier, Material material) {
		return getInstance(name, multiplier, new ItemStack(material));
	}
	
	public static OreMultipliers getInstance(String name, int multiplier, ItemStack item) {
		if(instance == null) {
			new OreMultipliers(name, multiplier, item);
		}
		return instance;
	}
	
	public static void setMultiplier(int multiplier) {
		OreMultipliers.multiplier = multiplier;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.isCancelled()) {
			Block block = event.getBlock();
			if(multiplier >= 2 && block.getType().toString().endsWith("_ORE")) {
				for(int a = 0; a < multiplier - 1; ++a) {
					for(ItemStack drop : block.getDrops()) {
						block.getWorld().dropItem(block.getLocation(), drop);
					}
				}
			}
		}
	}
}
