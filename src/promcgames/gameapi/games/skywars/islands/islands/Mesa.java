package promcgames.gameapi.games.skywars.islands.islands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.ItemCreator;

public class Mesa extends KitBase {
	public Mesa() {
		super(new ItemCreator(Material.HARD_CLAY).setName("Mesa").setLores(new String [] {
			ChatColor.AQUA + "Price: 500",
			"",
			"&6Mesa Themed Island"
		}).getItemStack());
	}

	@Override
	public String getPermission() {
		return "sky_wars_mesa";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		
	}
}
