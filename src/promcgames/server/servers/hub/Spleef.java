package promcgames.server.servers.hub;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import promcgames.player.MessageHandler;
import promcgames.server.nms.npcs.NPCEntity;

public class Spleef {
	public Spleef() {
		new NPCEntity(EntityType.SKELETON, "&bSpleef", new Location(Bukkit.getWorlds().get(0), -52.5, 127, -201.5)) {
			@Override
			public void onInteract(Player player) {
				MessageHandler.sendMessage(player, "&cComing soon");
			}
		};
	}
}
