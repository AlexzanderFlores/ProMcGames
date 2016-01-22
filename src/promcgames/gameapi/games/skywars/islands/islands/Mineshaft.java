package promcgames.gameapi.games.skywars.islands.islands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import promcgames.gameapi.kits.KitBase;
import promcgames.server.util.ItemCreator;

public class Mineshaft extends KitBase {
	public Mineshaft() {
		super(new ItemCreator(Material.FENCE).setName("Mineshaft").setLores(new String [] {
			ChatColor.AQUA + "Price: 500",
			"",
			"&6Mineshaft Themed Island"
		}).getItemStack());
	}

	@Override
	public String getPermission() {
		return "sky_wars_mineshaft";
	}

	@Override
	public void execute() {
		
	}

	@Override
	public void execute(Player player) {
		
	}
}
