package promcgames.gameapi.games.skywars.islands.islands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.ItemCreator;

public class Cloud extends KitBase {
	public Cloud() {
		super(new ItemCreator(Material.WOOL).setName("Cloud").setLores(new String [] {
			ChatColor.AQUA + "Price: 500",
			"",
			"&6Cloud Themed Island"
		}).getItemStack(), getLastSlot() + 3);
	}

	@Override
	public String getPermission() {
		return "sky_wars_cloud";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		
	}
}
