package promcgames.server.servers.clans;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import promcgames.ProMcGames;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.games.survivalgames.TieringHandler;
import promcgames.player.Particles.ParticleTypes;
import promcgames.server.util.EventUtil;
import promcgames.server.util.StringUtil;

public class Tiering implements Listener {
	public Tiering() {
		World world = Bukkit.getWorlds().get(0);
		Hologram first = HologramsAPI.createHologram(ProMcGames.getInstance(), new Location(world, -23.5, 10, -34.5));
		first.appendTextLine(StringUtil.color("&bTiering Information"));
		first.appendTextLine(StringUtil.color("&b(Click)"));
		Hologram second = HologramsAPI.createHologram(ProMcGames.getInstance(), new Location(world, -34.5, 10, -23.5));
		second.appendTextLine(StringUtil.color("&bTiering Information"));
		second.appendTextLine(StringUtil.color("&b(Click)"));
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		ParticleTypes.HAPPY_VILLAGER.display(new Location(Bukkit.getWorlds().get(0), -23.5, 7.5, -34.5));
		ParticleTypes.HAPPY_VILLAGER.display(new Location(Bukkit.getWorlds().get(0), -34.5, 7.5, -23.5));
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock() != null) {
			Block block = event.getClickedBlock();
			if(block.getType() == Material.CHEST) {
				int x = block.getX();
				int y = block.getY();
				int z = block.getZ();
				if((x == -24 || x == -35) && y == 8 && (z == -35 || z == -24)) {
					TieringHandler.explain(event.getPlayer());
				}
			}
		}
	}
}
