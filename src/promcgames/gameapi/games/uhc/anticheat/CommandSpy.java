package promcgames.gameapi.games.uhc.anticheat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import promcgames.ProPlugin;
import promcgames.gameapi.games.uhc.HostHandler;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.util.EventUtil;

public class CommandSpy implements Listener {
	private List<String> enabled = null;
	
	public CommandSpy() {
		new CommandBase("commandSpy", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				if(Ranks.MODERATOR.hasRank(player) || HostHandler.isHost(player.getUniqueId())) {
					if(enabled.contains(player.getName())) {
						enabled.remove(player.getName());
						MessageHandler.sendMessage(player, "Command Spy is now &cOFF");
					} else {
						enabled.add(player.getName());
						MessageHandler.sendMessage(player, "Command Spy is now &eON");
					}
				} else {
					MessageHandler.sendUnknownCommand(player);
				}
				return true;
			}
		};
		enabled = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		for(String name : enabled) {
			Player player = ProPlugin.getPlayer(name);
			if(player != null) {
				MessageHandler.sendMessage(player, "&6&lCS: " + AccountHandler.getPrefix(event.getPlayer()) + ": " + event.getMessage());
			}
		}
	}
}
