package promcgames.server.servers.testing;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import promcgames.gameapi.games.contention.projectilepath.ProjectilePath;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.ProPlugin;
import promcgames.server.Tweeter;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.StringUtil;

public class Testing extends ProPlugin {
	public Testing() {
		super("Testing");
		addGroup("24/7");
		new ProjectilePath();
		removeFlags();
		setAllowEntityDamage(true);
		setAllowEntityDamageByEntities(true);
		new CommandBase("nameItem", 1, -1, true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				ItemStack item = player.getItemInHand();
				if(item == null || item.getType() == Material.AIR) {
					MessageHandler.sendMessage(player, "&cYou must be holding an item");
					return true;
				}
				ItemMeta meta = item.getItemMeta();
				if(arguments[0].equalsIgnoreCase("lore")) {
					List<String> lores = meta.getLore();
					if(lores == null) {
						lores = new ArrayList<String>();
					}
					String lore = "";
					for(int a = 1; a < arguments.length; ++a) {
						lore += arguments[a] + " ";
					}
					lores.add(StringUtil.color(lore.substring(0, lore.length() - 1)));
					meta.setLore(lores);
				} else {
					String name = "";
					for(int a = 0; a < arguments.length; ++a) {
						name += arguments[a] + " ";
					}
					meta.setDisplayName(StringUtil.color(name.substring(0, name.length() - 1)));
				}
				item.setItemMeta(meta);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				//Rant:
				new Tweeter("A4hXKqZpO8l5AYfzljdQ9iBAL", "x3PSDOdrxghoeM7t1KTzRj08CACVB2YHlqDi9r9xJSXuzzJdKG", "3314576964-W12yOTlLWP7nyUyXheRF625P95YCodEYzVG12mT", "Kn9FHhBvoZHkyiCUPSfquRqQ4pyoVTcSIxQmqtDhtF1Qn");
				new CommandBase("test", 1) {
					@Override
					public boolean execute(CommandSender sender, String [] arguments) {
						Tweeter.tweet("TwitterAPI Testing", arguments[0]);
						return true;
					}
				};
			}
		}, 20 * 2);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		super.onPlayerJoin(event);
		event.getPlayer().teleport(new Location(event.getPlayer().getWorld(), 181, 4, -659));
	}
}
