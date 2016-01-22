package promcgames.gameapi.games.skywars.islands.islands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.ItemCreator;

public class Desert extends KitBase {
	public Desert() {
		super(new ItemCreator(Material.SAND).setName("Desert").setLores(new String [] {
			ChatColor.AQUA + "Price: 500",
			"",
			"&6Desert Themed Island"
		}).getItemStack());
	}

	@Override
	public String getPermission() {
		return "sky_wars_desert";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		
	}
}
