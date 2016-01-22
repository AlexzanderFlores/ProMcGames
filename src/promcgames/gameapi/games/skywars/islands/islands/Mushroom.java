package promcgames.gameapi.games.skywars.islands.islands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.ItemCreator;

public class Mushroom extends KitBase {
	public Mushroom() {
		super(new ItemCreator(Material.RED_MUSHROOM).setName("Mushroom").setLores(new String [] {
			ChatColor.AQUA + "Price: 500",
			"",
			"&6Mushroom Themed Island"
		}).getItemStack());
	}

	@Override
	public String getPermission() {
		return "sky_wars_mushroom";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		
	}
}
