package promcgames.player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.server.v1_7_R4.EntityHuman;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedEntitySpawn;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.org.apache.commons.lang3.RandomStringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameEndingEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PostPlayerJoinEvent;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class Disguise implements Listener {
	private static List<Plugins> plugins = Arrays.asList(Plugins.SURVIVAL_GAMES);
	private static Map<String, String> names = null; // Fake name, real name
	private static Map<UUID, UUID> uuids = null; // Fake UUID, real UUID
	private static Map<String, Ranks> ranks = null; // Fake name, real  rank
	
	public Disguise() {
		new CommandBase("disguise", true) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				if(ProMcGames.getPlugin() == Plugins.HUB || ProMcGames.getPlugin() == Plugins.SGHUB) {
					new AsyncDelayedTask(new Runnable() {
						@Override
						public void run() {
							Player player = (Player) sender;
							if(DB.PLAYERS_DISGUISES.isUUIDSet(getUUID(player))) {
								DB.PLAYERS_DISGUISES.deleteUUID(getUUID(player));
								MessageHandler.sendMessage(player, "You will no longer be disguised in the following games:");
								for(Plugins plugin : plugins) {
									MessageHandler.sendMessage(player, "&e" + plugin.toString().replace("_", " "));
								}
							} else {
								DB.PLAYERS_DISGUISES.insert("'" + getUUID(player).toString() + "', '" + null + "'");
								MessageHandler.sendMessage(player, "You will now be disguised in the following games:");
								for(Plugins plugin : plugins) {
									MessageHandler.sendMessage(player, "&e" + plugin.toString().replace("_", " "));
								}
							}
						}
					});
				} else {
					MessageHandler.sendMessage(sender, "&cYou can only do this in hub servers");
				}
				return true;
			}
		}.enableDelay(2).setRequiredRank(Ranks.ELITE);
		new CommandBase("getDisguise", 1) {
			@Override
			public boolean execute(final CommandSender sender, final String [] arguments) {
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						String fakeName = arguments[0];
						Player target = ProPlugin.getPlayer(fakeName);
						if(target == null) {
							if(DB.PLAYERS_DISGUISES.isKeySet("fakeName", fakeName)) {
								UUID uuid = UUID.fromString(DB.PLAYERS_DISGUISES.getString("fakeName", fakeName, "uuid"));
								MessageHandler.sendMessage(sender, "The real name for " + fakeName + " is " + AccountHandler.getName(uuid));
							} else {
								MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never been a disguised name");
							}
						} else {
							MessageHandler.sendMessage(sender, "The real name for " + fakeName + " is " + Disguise.getName(target));
						}
					}
				});
				return true;
			}
		}.enableDelay(2).setRequiredRank(Ranks.HELPER);
		/*new CommandBase("test", 1, true) { // Untested
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				String name = arguments[0];
				UUID uuid = AccountHandler.getUUID(name);
				DisguisePlayer(player, uuid, name);
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);*/
		names = new HashMap<String, String>();
		uuids = new HashMap<UUID, UUID>();
		ranks = new HashMap<String, Ranks>();
		if(canRunDisguise()) {
			EventUtil.register(this);
		}
	}
	
	public static boolean canRunDisguise() {
		return plugins.contains(ProMcGames.getPlugin());
	}
	
	public Disguise(Player player) {
		String name = null;
		UUID uuid = player.getUniqueId();
		do {
			name = RandomStringUtils.randomAlphanumeric(new Random().nextInt(5) + 6);
		} while(ProPlugin.getPlayer(name) != null || DB.PLAYERS_DISGUISES.isKeySet("fakeName", name));
		DB.PLAYERS_DISGUISES.insert("'" + player.getUniqueId().toString() + "', '" + name + "'");
		names.put(name, player.getName());
		uuids.put(uuid, player.getUniqueId());
		try {
			DisguisePlayer(player, uuid, name);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void DisguisePlayer(Player player, UUID uuid, String name) {
		try {
			CraftPlayer craftPlayer = (CraftPlayer) player;
			ranks.put(name, AccountHandler.getRank(player));
			GameProfile profile = new GameProfile(uuid, name);
			Field entityHumanField = EntityHuman.class.getDeclaredField("i");
			entityHumanField.setAccessible(true);
			entityHumanField.set(craftPlayer.getHandle(), profile);
			Packet packet = new PacketPlayOutNamedEntitySpawn(craftPlayer.getHandle());
			Field gameProfileField = PacketPlayOutNamedEntitySpawn.class.getDeclaredField("b");
			gameProfileField.setAccessible(true);
			gameProfileField.set(packet, profile);
			for(Player online : Bukkit.getOnlinePlayers()) {
				if(!getName(player).equals(getName(online))) {
					craftPlayer.getHandle().playerConnection.sendPacket(packet);
					online.hidePlayer(player);
					online.showPlayer(player);
				}
			}
			AccountHandler.setRank(player, AccountHandler.getRank(name));
			String listName = AccountHandler.getRank(name).getColor() + name;
			if(listName.length() > 16) {
				listName = listName.substring(0, 16);
			}
			player.setPlayerListName(listName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void unDesguise(Player player) {
		if(names.containsKey(player.getName())) {
			DisguisePlayer(player, getUUID(player), getName(player));
		}
		names.remove(player.getName());
		uuids.remove(player.getUniqueId());
		ranks.remove(player.getName());
	}
	
	public static String getName(Player player) {
		return getName(player, true);
	}
	
	public static String getName(Player player, boolean realName) {
		if(realName && names.containsKey(player.getName())) {
			return names.get(player.getName());
		} else {
			return player.getName();
		}
	}
	
	public static UUID getUUID(CommandSender commandSender) {
		return getUUID(commandSender, true);
	}
	
	public static UUID getUUID(CommandSender commandSender, boolean realUUID) {
		if(commandSender instanceof Player) {
			Player player = (Player) commandSender;
			if(realUUID && uuids.containsKey(player.getUniqueId())) {
				return uuids.get(player.getUniqueId());
			} else {
				return player.getUniqueId();
			}
		}
		return null;
	}
	
	public static Ranks getRealRank(CommandSender sender) {
		return ranks.containsKey(sender.getName()) ? ranks.get(sender.getName()) : AccountHandler.getRank(sender);
	}
	
	public static boolean isDisguised(Player player) {
		return isDisguised(getUUID(player));
	}
	
	public static boolean isDisguised(UUID uuid) {
		return uuids.containsKey(uuid);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PostPlayerJoinEvent event) {
		if(plugins.contains(ProMcGames.getPlugin()) && DB.PLAYERS_DISGUISES.isUUIDSet(event.getPlayer().getUniqueId())) {
			new Disguise(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			unDesguise(player);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		unDesguise(event.getPlayer());
	}
	
}
