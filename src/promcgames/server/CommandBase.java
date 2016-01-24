package promcgames.server;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.tasks.DelayedTask;

public abstract class CommandBase implements CommandExecutor {
	private List<String> delayedPlayers = null;
	private Ranks requiredRank = Ranks.PLAYER;
	private int delay = 0;
	private int minArguments = 0;
	private int maxArguments = 0;
	private boolean playerOnly = false;
	
	public CommandBase(String command) {
		this(command, 0);
	}
	
	public CommandBase(String command, boolean playerOnly) {
		this(command, 0, playerOnly);
	}
	
	public CommandBase(String command, int requiredArguments) {
		this(command, requiredArguments, requiredArguments);
	}
	
	public CommandBase(String command, int minArguments, int maxArguments) {
		this(command, minArguments, maxArguments, false);
	}
	
	public CommandBase(String command, int requiredArguments, boolean playerOnly) {
		this(command, requiredArguments, requiredArguments, playerOnly);
	}
	
	public CommandBase(String command, int minArguments, int maxArguments, boolean playerOnly) {
		this.minArguments = minArguments;
		this.maxArguments = maxArguments;
		this.playerOnly = playerOnly;
		try {
			ProMcGames.getInstance().getCommand(command).setExecutor(this);
		} catch(Exception e) {
			
		}
	}
	
	public CommandBase enableDelay(int delay) {
		this.delay = delay;
		this.delayedPlayers = new ArrayList<String>();
		return this;
	}
	
	public CommandBase removeFromDelay(Player player) {
		if(this.delayedPlayers != null) {
			this.delayedPlayers.remove(player.getName());
		}
		return this;
	}
	
	public CommandBase setRequiredRank(Ranks requiredRank) {
		this.requiredRank = requiredRank;
		return this;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] arguments) {
		if(arguments.length < minArguments || (arguments.length > maxArguments && maxArguments != -1)) {
			return false;
		} else {
			if(playerOnly && !(sender instanceof Player)) {
				MessageHandler.sendPlayersOnly(sender);
			} else {
				if(Disguise.getRealRank(sender).isAboveRank(requiredRank)) {
					if(delayedPlayers != null && sender instanceof Player) {
						final Player player = (Player) sender;
						if(delayedPlayers.contains(Disguise.getName(player))) {
							if(delay == 1) {
								MessageHandler.sendMessage(player, "&cThis command has a cool down of &e" + delay + "&c second");
							} else {
								MessageHandler.sendMessage(player, "&cThis command has a cool down of &e" + delay + "&c seconds");
							}
						} else {
							delayedPlayers.add(Disguise.getName(player));
							new DelayedTask(new Runnable() {
								@Override
								public void run() {
									delayedPlayers.remove(Disguise.getName(player));
								}
							}, 20 * delay);
							return execute(sender, arguments);
						}
					} else {
						return execute(sender, arguments);
					}
				} else {
					MessageHandler.sendMessage(sender, "&cYou must have " + requiredRank.getPrefix() + "&cto use this command");
				}
			}
			return true;
		}
	}
	
	public abstract boolean execute(CommandSender sender, String [] arguments);
}
