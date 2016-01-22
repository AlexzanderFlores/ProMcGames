package promcgames.gameapi.games.skywars.islands.islands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.ItemCreator;

public class Dungeon extends KitBase {
	public Dungeon() {
		super(new ItemCreator(Material.MOB_SPAWNER).setName("Dungeon").setLores(new String [] {
			ChatColor.AQUA + "Price: 500",
			"",
			"&6Dungeon Themed Island"
		}).getItemStack());
	}

	@Override
	public String getPermission() {
		return "sky_wars_dungeon";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		
	}
}
