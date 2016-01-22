package promcgames.gameapi.games.skywars.islands.islands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.ItemCreator;

public class Swamp extends KitBase {
	public Swamp() {
		super(new ItemCreator(Material.WOOD, 1).setName("Swamp").setLores(new String [] {
			ChatColor.AQUA + "Price: 500",
			"",
			"&6Swamp Themed Island"
		}).getItemStack());
	}

	@Override
	public String getPermission() {
		return "sky_wars_swamp";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		
	}
}
