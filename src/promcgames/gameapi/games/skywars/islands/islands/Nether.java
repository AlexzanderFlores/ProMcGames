package promcgames.gameapi.games.skywars.islands.islands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.ItemCreator;

public class Nether extends KitBase {
	public Nether() {
		super(new ItemCreator(Material.NETHERRACK).setName("Nether").setLores(new String [] {
			ChatColor.AQUA + "Price: 500",
			"",
			"&6Nether Themed Island"
		}).getItemStack());
	}

	@Override
	public String getPermission() {
		return "sky_wars_nether";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		
	}
}
