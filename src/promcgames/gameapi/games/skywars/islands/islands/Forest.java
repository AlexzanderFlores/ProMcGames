package promcgames.gameapi.games.skywars.islands.islands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.ItemCreator;

public class Forest extends KitBase {
	public Forest() {
		super(new ItemCreator(Material.LOG).setName("Forest").setLores(new String [] {
			ChatColor.AQUA + "Price: 0",
			"",
			"&6Forest Themed Island",
			"",
			"&6Default Island"
		}).getItemStack(), 10);
	}

	@Override
	public String getPermission() {
		return "sky_wars_forest";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		
	}
}
