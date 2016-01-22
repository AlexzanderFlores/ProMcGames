package promcgames.gameapi.games.skywars.islands.islands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.ItemCreator;

public class Chikara extends KitBase {
	public Chikara() {
		super(new ItemCreator(Material.STAINED_CLAY, 14).setName("Chikara").setLores(new String [] {
			ChatColor.AQUA + "Price: 500",
			"",
			"&6Japanese Themed Island"
		}).getItemStack());
	}

	@Override
	public String getPermission() {
		return "sky_wars_chikara";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		
	}
}
