package promcgames.gameapi.games.skywars.islands.islands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.ItemCreator;

public class Ender extends KitBase {
	public Ender() {
		super(new ItemCreator(Material.ENDER_STONE).setName("Ender").setLores(new String [] {
			ChatColor.AQUA + "Price: 500",
			"",
			"&6Ender Themed Island"
		}).getItemStack());
	}

	@Override
	public String getPermission() {
		return "sky_wars_ender";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		
	}
}
