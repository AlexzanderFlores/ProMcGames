package promcgames.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProPlugin;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PrivateMessageEvent;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.staff.ViolationPrevention;
import promcgames.staff.mute.MuteHandler;
import promcgames.staff.mute.ShadowMuteHandler;

public class PrivateMessaging implements Listener {
	private Map<String, String> lastMessaged = null;
	private List<String> checkedForDisabled = null;
	private List<String> disabled = null;
	private List<String> toldAboutDisabling = null;
	
	public PrivateMessaging() {
		lastMessaged = new HashMap<String, String>();
		checkedForDisabled = new ArrayList<String>();
		disabled = new ArrayList<String>();
		toldAboutDisabling = new ArrayList<String>();
		new CommandBase("msg", 2, -1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = (Player) sender;
						if(MuteHandler.checkMute(player)) {
							MuteHandler.display(player);
						} else {
							Player target = ProPlugin.getPlayer(arguments[0]);
							if(target == null) {
								MessageHandler.sendMessage(player, "&c" + arguments[0] + " is not online");
							} else if(isDisabled(player)) {
								MessageHandler.sendMessage(player, "&cYou have your messages disabled, do &a/togglePMs");
							} else if(isDisabled(target) && !Ranks.isStaff(player)) {
								MessageHandler.sendMessage(player, "&c" + target.getName() + " has messages disabled");
							} else if(IgnoreHandler.isIgnored(target, player)) {
								MessageHandler.sendMessage(player, "&c" + target.getName() + " has you ignored");
							} else if(IgnoreHandler.isIgnored(player, target)) {
								MessageHandler.sendMessage(player, "&cYou are ignoring " + target.getName());
							} else {
								PrivateMessageEvent event = new PrivateMessageEvent(player);
								Bukkit.getPluginManager().callEvent(event);
								if(!event.isCancelled()) {
									String message = "";
									for(int a = 1; a < arguments.length; ++a) {
										message += arguments[a] + " ";
									}
									MessageHandler.sendMessage(player, "&bYou -> " + target.getName() + ": &f" + message);
									if(!ShadowMuteHandler.contains(player) && !ViolationPrevention.contains(message)) {
										MessageHandler.sendMessage(target, "&b" + player.getName() + " -> You: &f" + message);
										lastMessaged.put(player.getName(), target.getName());
										lastMessaged.put(target.getName(), player.getName());
										tellAboutToggling(target);
									}
								}
							}
						}
					}
				});
				return true;
			}
		}.enableDelay(1);
		new CommandBase("r", 1, -1, true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						Player player = (Player) sender;
						boolean ableToReply = true;
						if(MuteHandler.checkMute(player)) {
							MuteHandler.display(player);
						} else if(lastMessaged.containsKey(player.getName())){
							Player target = ProPlugin.getPlayer(lastMessaged.get(player.getName()));
							if(target == null) {
								lastMessaged.remove(player.getName());
								ableToReply = false;
							} else if(isDisabled(player)) {
								MessageHandler.sendMessage(player, "&cYou have your messages disabled, do &a/togglePMs");
							} else if(isDisabled(target) && !Ranks.isStaff(player)) {
								MessageHandler.sendMessage(player, "&c" + target.getName() + " has messages disabled");
							} else if(IgnoreHandler.isIgnored(target, player)) {
								MessageHandler.sendMessage(player, "&c" + target.getName() + " has you ignored");
							} else {
								PrivateMessageEvent event = new PrivateMessageEvent(player);
								Bukkit.getPluginManager().callEvent(event);
								if(!event.isCancelled()) {
									String message = "";
									for(String argument : arguments) {
										message += argument + " ";
									}
									MessageHandler.sendMessage(player, "&bYou -> " + target.getName() + ": &f" + message);
									if(!ShadowMuteHandler.contains(player) && !ViolationPrevention.contains(message)) {
										MessageHandler.sendMessage(target, "&b" + player.getName() + " -> You: &f" + message);
										lastMessaged.put(player.getName(), target.getName());
										lastMessaged.put(target.getName(), player.getName());
										tellAboutToggling(target);
									}
								}
							}
						} else {
							ableToReply = false;
						}
						if(!ableToReply) {
							MessageHandler.sendMessage(player, "&cYou have no one to reply to");
						}
					}
				});
				return true;
			}
		}.enableDelay(1);
		new CommandBase("togglePMs", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				final Player player = (Player) sender;
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						boolean isDisabled = DB.PLAYERS_DISABLED_MESSAGES.isUUIDSet(Disguise.getUUID(player));
						if(isDisabled(player)) {
							disabled.remove(player.getName());
							if(isDisabled) {
								DB.PLAYERS_DISABLED_MESSAGES.deleteUUID(Disguise.getUUID(player));
							}
							MessageHandler.sendMessage(player, "Private messages &eON");
						} else {
							disabled.add(player.getName());
							if(!isDisabled) {
								DB.PLAYERS_DISABLED_MESSAGES.insert("'" + Disguise.getUUID(player).toString() + "'");
							}
							MessageHandler.sendMessage(player, "Private messages &cOFF");
						}
					}
				});
				return true;
			}
		}.enableDelay(2);
		EventUtil.register(this);
	}
	
	private boolean isDisabled(final Player player) {
		if(!checkedForDisabled.contains(player.getName())) {
			checkedForDisabled.add(player.getName());
			if(DB.PLAYERS_DISABLED_MESSAGES.isUUIDSet(Disguise.getUUID(player))) {
				disabled.add(player.getName());
			}
		}
		return disabled.contains(player.getName());
	}
	
	private void tellAboutToggling(Player player) {
		if(!toldAboutDisabling.contains(player.getName())) {
			toldAboutDisabling.add(player.getName());
			MessageHandler.sendMessage(player, "&cDon't want to get private messages?");
			MessageHandler.sendMessage(player, "&cRun \"&a/togglePMs&c\" or \"&a/ignore <name>&c\"");
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		lastMessaged.remove(event.getPlayer().getName());
		checkedForDisabled.remove(event.getPlayer().getName());
		disabled.remove(event.getPlayer().getName());
		toldAboutDisabling.remove(event.getPlayer().getName());
	}
}
