package promcgames.gameapi;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.Particles.ParticleTypes;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.FileHandler;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;
import promcgames.server.util.StringUtil;
import de.slikey.effectlib.EffectLib;
import de.slikey.effectlib.EffectManager;
import de.slikey.effectlib.effect.TextLocationEffect;

public class VotingHandler implements Listener {
	public class VoteData {
		private String name = null;
		private int votes = 0;
		
		public VoteData(String name) {
			setName(name);
			voteData.put(getName(), this);
		}
		
		public String getName() {
			return this.name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public int getVotes() {
			return this.votes;
		}
		
		public void vote(Player player) {
			if(playerVotes.containsKey(Disguise.getName(player))) {
				playerVotes.get(Disguise.getName(player)).removeVote(player);
			}
			playerVotes.put(Disguise.getName(player), this);
			int votes = Ranks.getVotes(player);
			this.votes += votes;
			MessageHandler.sendMessage(player, "+" + votes + " &avotes for &e" + getName());
			if(AccountHandler.getRank(player) == Ranks.PLAYER && !Disguise.isDisguised(player)) {
				MessageHandler.sendMessage(player, "&eRanks give you more map votes! &b/buy");
			}
		}
		
		public void removeVote(Player player) {
			if(playerVotes.containsKey(Disguise.getName(player)) && playerVotes.get(Disguise.getName(player)).getName().equals(this.getName())) {
				int votes = Ranks.getVotes(player);
				this.votes -= votes;
				MessageHandler.sendMessage(player, "&c-" + votes + "&a votes for &e" + playerVotes.get(Disguise.getName(player)).getName());
			}
		}
	}
	
	private static EffectManager manager = null;
	private static TextLocationEffect effect = null;
	private List<String> recentlyClicked = null;
	private static List<String> possibleMaps = null;
	private static List<String> delayed = null;
	private static Map<String, VoteData> voteData = null;
	private static Map<String, VoteData> playerVotes = null;
	private static Creeper mapCreeper = null;
	private static String title = null;
	private static int delay = 3;
	
	public VotingHandler(final List<String> options) {
		title = "Map Voting";
		recentlyClicked = new ArrayList<String>();
		voteData = new HashMap<String, VoteData>();
		playerVotes = new HashMap<String, VoteData>();
		World world = ProMcGames.getMiniGame().getLobby();
		for(Entity entity : world.getEntities()) {
			if(entity instanceof Creeper) {
				entity.remove();
			}
		}
		Location [] locations = new Location [] {
			new Location(world, -16.5, 28, -48.5), new Location(world, -10.5, 28, -47.5),
			new Location(world, -4.5, 28, -44.5), new Location(world, -1.5, 28, -38.5),
			new Location(world, 0.5, 28, -32.5)
		};
		if(possibleMaps == null) {
			for(String map : options) {
				new VoteData(StringUtil.color("&a" + map.replace("_", " ")));
			}
		} else {
			for(String map : possibleMaps) {
				new VoteData(StringUtil.color("&a" + map.replace("_", " ")));
			}
		}
		for(int a = 0; a < locations.length && a < options.size(); ++a) {
			String option = StringUtil.color("&a" + options.get(a).replace("_", " "));
			new NPCEntity(EntityType.CREEPER, option, locations[a]) {
				@Override
				public void onInteract(final Player player) {
					String map = getName();
					Creeper creeper = (Creeper) getLivingEntity();
					if(map.contains("Click For Other Maps")) {
						if(mapCreeper == null) {
							mapCreeper = creeper;
						}
						if(delayed == null) {
							delayed = new ArrayList<String>();
						}
						if(!delayed.contains(player.getName())) {
							final String name = player.getName();
							delayed.add(name);
							new DelayedTask(new Runnable() {
								@Override
								public void run() {
									delayed.remove(name);
								}
							}, 20 * delay);
							if(DB.PLAYERS_MAP_VOTE_PASSES.isUUIDSet(player.getUniqueId())) {
								Inventory inventory = Bukkit.createInventory(player, ItemUtil.getInventorySize(possibleMaps.size() - options.size()), "Map Voting");
								for(String possibleMap : possibleMaps) {
									if(!options.contains(possibleMap)) {
										String mapName = "&a" + possibleMap.replace("_", " ");
										inventory.addItem(new ItemCreator(Material.STAINED_GLASS, 4).setName(mapName).getItemStack());
									}
								}
								player.openInventory(inventory);
							} else {
								MessageHandler.sendMessage(player, "&cYou do not have any map voting passes! Get &e10 &cwith &e/vote");
							}
						}
					} else {
						voteData.get(map).vote(player);
						playEffect(player, creeper, map);
					}
				}
			};
		}
		if(manager == null) {
			manager = new EffectManager(EffectLib.instance());
		}
		effect = new TextLocationEffect(manager, new Location(world, 952.5, 105, 435.5, -180.599996f, -1.9499832f));
		effect.text = "Voting";
		effect.infinite();
		effect.start();
		EventUtil.register(this);
	}
	
	private void playEffect(final Player player,final Creeper creeper, String map) {
		EffectUtil.playSound(player, Sound.LEVEL_UP);
		ParticleTypes.FLAME.displaySpiral(creeper.getLocation());
		if(!creeper.isPowered()) {
			creeper.setPowered(true);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					creeper.setPowered(false);
				}
			}, 30);
		}
		if(!Disguise.isDisguised(player) && !recentlyClicked.contains(Disguise.getName(player)) && Ranks.PRO_PLUS.hasRank(player, true)) {
			recentlyClicked.add(Disguise.getName(player));
			MessageHandler.alert(AccountHandler.getPrefix(player) + "&e has voted for &c" + map);
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					recentlyClicked.remove(Disguise.getName(player));
				}
			}, 20 * 5);
		}
	}
	
	private static String getGame() {
		Plugins plugin = ProMcGames.getPlugin();
		if(ProMcGames.getPlugin() == Plugins.SKY_WARS_TEAMS) {
			plugin = Plugins.SKY_WARS;
		}
		return plugin.toString().toLowerCase().replace("_", "");
	}
	
	public static void loadMaps() {
		loadMaps(getGame());
	}
	
	public static void loadMaps(String game) {
		if(possibleMaps == null) {
			possibleMaps = new ArrayList<String>();
		}
		if(game.equals("skywarsteams")) {
			game = "skywars";
		}
		File file = new File(Bukkit.getWorldContainer().getPath() + "/../resources/maps/" + game);
		String [] folders = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		List<String> maps = new ArrayList<String>();
		for(String folder : folders) {
			if(new File(file.getPath() + "/" + folder + "/spawns.yml").exists()) {
				possibleMaps.add(folder);
			}
		}
		int max = 4;
		if(possibleMaps.size() <= max) {
			new VotingHandler(possibleMaps);
		} else {
			for(int a = 0; a < max && a < possibleMaps.size() && maps.size() <= max; ++a) {
				String map = null;
				do {
					map = possibleMaps.get(new Random().nextInt(possibleMaps.size()));
				} while(maps.contains(map));
				maps.add(map);
			}
			maps.add("Click For Other Maps");
			new VotingHandler(maps);
		}
	}
	
	public static VoteData getWinner() {
		VoteData winner = null;
		for(VoteData data : voteData.values()) {
			if((winner == null || data.getVotes() > winner.getVotes()) || (data.getVotes() == winner.getVotes() && new Random().nextBoolean())) {
				winner = data;
			}
		}
		winner.setName(ChatColor.stripColor(winner.getName()));
		World world = ProMcGames.getMiniGame().getLobby();
		for(Entity entity : world.getEntities()) {
			if(entity instanceof Creeper) {
				entity.remove();
			}
		}
		if(possibleMaps != null) {
			possibleMaps.clear();
			possibleMaps = null;
		}
		voteData.clear();
		voteData = null;
		playerVotes = null;
		if(effect != null) {
			effect.cancel();
		}
		if(manager != null) {
			manager.dispose();
			manager = null;
		}
		return winner;
	}
	
	public static World loadWinningWorld() {
		return loadWinningWorld(getGame());
	}
	
	public static World loadWinningWorld(String game) {
		VoteData voteData = getWinner();
		final String worldName = voteData.getName().replace(" ", "_");
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String game = ProMcGames.getPlugin().toString();
				String [] keys = new String [] {"game_name", "map"};
				String [] values = new String [] {game, worldName};
				if(DB.NETWORK_MAP_VOTES.isKeySet(keys, values)) {
					int times = DB.NETWORK_MAP_VOTES.getInt(keys, values, "times_voted") + 1;
					DB.NETWORK_MAP_VOTES.updateInt("times_voted", times, keys, values);
				} else {
					DB.NETWORK_MAP_VOTES.insert("'" + game + "', '" + worldName + "', '1'");
				}
			}
		});
		File world = new File(Bukkit.getWorldContainer().getPath() + "/" + worldName);
		if(world.exists()) {
			FileHandler.delete(world);
		}
		FileHandler.copyFolder(Bukkit.getWorldContainer().getPath() + "/../resources/maps/" + game + "/" + worldName, world.getPath());
		MessageHandler.alert(worldName.replace("_", " ") + " has won with " + voteData.getVotes() + " votes");
		MessageHandler.alert("Want more votes? &b/buy");
		World arena = Bukkit.createWorld(new WorldCreator(worldName));
		for(Entity entity : arena.getEntities()) {
			if(entity instanceof LivingEntity) {
				entity.remove();
			}
		}
		return arena;
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().equals(title)) {
			Player player = event.getPlayer();
			voteData.get(event.getItemTitle()).vote(player);
			int passes = DB.PLAYERS_MAP_VOTE_PASSES.getInt("uuid", player.getUniqueId().toString(), "amount") - 1;
			if(passes <= 0) {
				DB.PLAYERS_MAP_VOTE_PASSES.deleteUUID(player.getUniqueId());
			} else {
				DB.PLAYERS_MAP_VOTE_PASSES.updateInt("amount", passes, "uuid", player.getUniqueId().toString());
			}
			MessageHandler.sendMessage(player, "&c-1 &aMap vote pass. You now have &e" + passes);
			playEffect(player, mapCreeper, "&aAnother Map &ewith &b/vote");
			player.closeInventory();
			event.setCancelled(true);
		}
	}
}
