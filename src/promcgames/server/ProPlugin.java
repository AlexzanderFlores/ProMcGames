package promcgames.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.EnumClientCommand;
import net.minecraft.server.v1_7_R4.PacketPlayInClientCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import promcgames.customevents.ServerRestartEvent;
import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.game.GameKillEvent;
import promcgames.customevents.game.GameLossEvent;
import promcgames.customevents.game.GameWinEvent;
import promcgames.customevents.game.SpawnTNTBlocksEvent;
import promcgames.customevents.player.AsyncPlayerJoinEvent;
import promcgames.customevents.player.AsyncPlayerLeaveEvent;
import promcgames.customevents.player.AsyncPostPlayerJoinEvent;
import promcgames.customevents.player.PlayerHeadshotEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerServerJoinEvent;
import promcgames.customevents.player.PostPlayerJoinEvent;
import promcgames.customevents.player.WaterSplashEvent;
import promcgames.customevents.timed.FiveMinuteTaskEvent;
import promcgames.customevents.timed.FiveSecondTaskEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.versus.LobbyHandler;
import promcgames.player.Disguise;
import promcgames.player.EmeraldsHandler;
import promcgames.player.EmeraldsHandler.EmeraldReason;
import promcgames.player.MessageHandler;
import promcgames.player.TitleDisplayer;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB.Databases;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.networking.Instruction;
import promcgames.server.networking.Instruction.Inst;
import promcgames.server.nms.npcs.NPCRegistrationHandler.NPCs;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.Pets;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ConfigurationUtil;
import promcgames.server.util.CountDownUtil;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.FileHandler;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.StringUtil;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

@SuppressWarnings("deprecation")
public class ProPlugin extends CountDownUtil implements Listener {
	private String name = null;
	private List<FallingBlock> waterEffectBlocks = null;
	private List<String> importedWorlds = null;
	private static List<String> networkingGroups = null;
	private boolean beta = false;
	private boolean allowEntityDamage = false;
	private boolean allowEntityDamageByEntities = false;
	private boolean allowFoodLevelChange = false;
	private boolean allowDroppingItems = false;
	private boolean allowPickingUpItems = false;
	private boolean allowItemSpawning = false;
	private boolean dropItemsOnLeave = false;
	private boolean allowBuilding = false;
	private boolean allowHealthRegeneration = true;
	private boolean allowDefaultMobSpawning = false;
	private boolean allowEntityCombusting = false;
	private boolean allowBowShooting = false;
	private boolean allowPlayerInteraction = false;
	private boolean allowBlockBurning = false;
	private boolean allowBlockFading = false;
	private boolean allowBlockForming = false;
	private boolean allowBlockFromTo = false;
	private boolean allowBlockGrow = false;
	private boolean allowBlockSpread = false;
	private boolean allowLeavesDecay = false;
	private boolean allowEntityChangeBlock = false;
	private boolean allowEntityCreatePortal = false;
	private boolean allowBedEntering = false;
	private boolean allowHangingBreakByEntity = false;
	private boolean removeEntitiesUponLoadingWorld = true;
	private boolean resetPlayerUponJoining = true;
	private boolean allowInventoryClicking = false;
	private boolean allowItemBreaking = true;
	private boolean allowArmorBreaking = true;
	private boolean staffGetAllKits = true;
	private boolean useTop8 = false;
	private boolean autoVanishStaff = false;
	private boolean kickDefaultsForPros = true;
	private boolean doDaylightCycle = false;
	private static boolean restarting = false;
	private int winEmeralds = 0;
	private int killEmeralds = 0;
	private int lossEmeralds = 0;
	private int flintAndSteelUses = 4;
	private boolean debug = false;
	
	public Random random = new Random();
	
	public ProPlugin(String name) {
		ProMcGames.setProPlugin(this);
		setName(name);
		EventUtil.register(this);
	}
	
	public void disable() {
		for(NPCs npc : NPCs.values()) {
			npc.unregister();
		}
		for(Pets pet : Pets.values()) {
			pet.unregister();
		}
	}
	
	public void resetFlags() {
		setAllowEntityDamage(false);
		setAllowEntityDamageByEntities(false);
		setAllowFoodLevelChange(false);
		setAllowDroppingItems(false);
		setAllowPickingUpItems(false);
		setAllowItemSpawning(false);
		setDropItemsOnLeave(false);
		setAllowBuilding(false);
		setAllowHealthRegeneration(true);
		setAllowDefaultMobSpawning(false);
		setAllowEntityCombusting(false);
		setAllowBowShooting(false);
		setAllowPlayerInteraction(false);
		setAllowBlockBurning(false);
		setAllowBlockFading(false);
		setAllowBlockForming(false);
		setAllowBlockFromTo(false);
		setAllowBlockGrow(false);
		setAllowBlockSpread(false);
		setAllowLeavesDecay(false);
		setAllowEntityChangeBlock(false);
		setAllowEntityCreatePortal(false);
		setAllowBedEntering(false);
		setAllowHangingBreakByEntity(false);
		setRemoveEntitiesUponLoadingWorld(true);
		setResetPlayerUponJoining(true);
		setAllowInventoryClicking(false);
		setAllowItemBreaking(true);
		setFlintAndSteelUses(4);
		setStaffGetAllKits(true);
		setUseTop8(false);
		setAutoVanishStaff(false);
		setKickDefaultsForPros(true);
		setDoDaylightCycle(false);
		setWinEmeralds(0);
		setKillEmeralds(0);
		setLossEmeralds(0);
	}
	
	public void removeFlags() {
		setAllowEntityDamage(true);
		setAllowEntityDamageByEntities(true);
		setAllowFoodLevelChange(true);
		setAllowDroppingItems(true);
		setAllowPickingUpItems(true);
		setAllowItemSpawning(true);
		setDropItemsOnLeave(false);
		setAllowBuilding(true);
		setAllowHealthRegeneration(true);
		setAllowDefaultMobSpawning(true);
		setAllowEntityCombusting(true);
		setAllowBowShooting(true);
		setAllowPlayerInteraction(true);
		setAllowBlockBurning(true);
		setAllowBlockFading(true);
		setAllowBlockForming(true);
		setAllowBlockFromTo(true);
		setAllowBlockGrow(true);
		setAllowBlockSpread(true);
		setAllowLeavesDecay(true);
		setAllowEntityChangeBlock(true);
		setAllowEntityCreatePortal(true);
		setAllowBedEntering(true);
		setAllowHangingBreakByEntity(true);
		setRemoveEntitiesUponLoadingWorld(false);
		setResetPlayerUponJoining(false);
		setAllowInventoryClicking(true);
		setAllowItemBreaking(true);
		setFlintAndSteelUses(0);
		setStaffGetAllKits(false);
		setUseTop8(false);
		setAutoVanishStaff(false);
		setKickDefaultsForPros(true);
		setDoDaylightCycle(true);
		setWinEmeralds(0);
		setKillEmeralds(0);
		setLossEmeralds(0);
	}
	
	public static List<String> getGroups() {
		return networkingGroups;
	}
	
	public static void addGroup(String group) {
		if(networkingGroups == null) {
			networkingGroups = new ArrayList<String>();
		}
		networkingGroups.add(group);
	}
	
	public String getDisplayName() {
		return this.name;
	}
	
	public String getName() {
		return getDisplayName().toLowerCase().replace(" ", "").replace("_", "");
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean getBeta() {
		return this.beta;
	}
	
	public void setBeta(boolean beta) {
		this.beta = beta;
	}
	
	public boolean getAllowEntityDamage() {
		return this.allowEntityDamage;
	}
	
	public void setAllowEntityDamage(boolean allowEntityDamage) {
		this.allowEntityDamage = allowEntityDamage;
	}
	
	public boolean getAllowEntityDamageByEntities() {
		return this.allowEntityDamageByEntities;
	}
	
	public void setAllowEntityDamageByEntities(boolean allowEntityDamageByEntities) {
		this.allowEntityDamageByEntities = allowEntityDamageByEntities;
	}
	
	public boolean getAllowFoodLevelChange() {
		return this.allowFoodLevelChange;
	}
	
	public void setAllowFoodLevelChange(boolean allowFoodLevelChange) {
		this.allowFoodLevelChange = allowFoodLevelChange;
	}
	
	public boolean getAllowDroppingItems() {
		return this.allowDroppingItems;
	}
	
	public void setAllowDroppingItems(boolean allowDroppingItems) {
		this.allowDroppingItems = allowDroppingItems;
	}
	
	public boolean getAllowPickingUpItems() {
		return this.allowPickingUpItems;
	}
	
	public void setAllowPickingUpItems(boolean allowPickingUpItems) {
		this.allowPickingUpItems = allowPickingUpItems;
	}
	
	public boolean getAllowItemSpawning() {
		return this.allowItemSpawning;
	}
	
	public void setAllowItemSpawning(boolean allowItemSpawning) {
		this.allowItemSpawning = allowItemSpawning;
	}
	
	public boolean getDropItemsOnLeave() {
		return this.dropItemsOnLeave;
	}
	
	public void setDropItemsOnLeave(boolean dropItemsOnLeave) {
		this.dropItemsOnLeave = dropItemsOnLeave;
	}
	
	public boolean getAllowBuilding() {
		return this.allowBuilding;
	}
	
	public void setAllowBuilding(boolean allowBuilding) {
		this.allowBuilding = allowBuilding;
	}
	
	public boolean getAllowHealthRegeneration() {
		return this.allowHealthRegeneration;
	}
	
	public void setAllowHealthRegeneration(boolean allowHealthRegeneration) {
		this.allowHealthRegeneration = allowHealthRegeneration;
	}
	
	public boolean getAllowDefaultMobSpawning() {
		return this.allowDefaultMobSpawning;
	}
	
	public void setAllowDefaultMobSpawning(boolean allowDefaultMobSpawning) {
		this.allowDefaultMobSpawning = allowDefaultMobSpawning;
	}
	
	public boolean getAllowEntityCombusting() {
		return this.allowEntityCombusting;
	}
	
	public void setAllowEntityCombusting(boolean allowEntityCombusting) {
		this.allowEntityCombusting = allowEntityCombusting;
	}
	
	public boolean getAllowBowShooting() {
		return this.allowBowShooting;
	}
	
	public void setAllowBowShooting(boolean allowBowShooting) {
		this.allowBowShooting = allowBowShooting;
		if(getAllowBowShooting()) {
			setAllowPlayerInteraction(true);
		}
	}
	
	public boolean getAllowPlayerInteraction() {
		return this.allowPlayerInteraction;
	}
	
	public void setAllowPlayerInteraction(boolean allowPlayerInteraction) {
		this.allowPlayerInteraction = allowPlayerInteraction;
	}
	
	public boolean getAllowBlockBurning() {
		return this.allowBlockBurning;
	}
	
	public void setAllowBlockBurning(boolean allowBlockBurning) {
		this.allowBlockBurning = allowBlockBurning;
	}
	
	public boolean getAllowBlockFading() {
		return this.allowBlockFading;
	}
	
	public void setAllowBlockFading(boolean allowBlockFading) {
		this.allowBlockFading = allowBlockFading;
	}
	
	public boolean getAllowBlockForming() {
		return this.allowBlockForming;
	}
	
	public void setAllowBlockForming(boolean allowBlockForming) {
		this.allowBlockForming = allowBlockForming;
	}
	
	public boolean getAllowBlockFromTo() {
		return this.allowBlockFromTo;
	}
	
	public void setAllowBlockFromTo(boolean allowBlockFromTo) {
		this.allowBlockFromTo = allowBlockFromTo;
	}
	
	public boolean getAllowBlockGrow() {
		return this.allowBlockGrow;
	}
	
	public void setAllowBlockGrow(boolean allowBlockGrow) {
		this.allowBlockGrow = allowBlockGrow;
	}
	
	public boolean getAllowBlockSpread() {
		return this.allowBlockSpread;
	}
	
	public void setAllowBlockSpread(boolean allowBlockSpread) {
		this.allowBlockSpread = allowBlockSpread;
	}
	
	public boolean getAllowLeavesDecay() {
		return this.allowLeavesDecay;
	}
	
	public void setAllowLeavesDecay(boolean allowLeavesDecay) {
		this.allowLeavesDecay = allowLeavesDecay;
	}
	
	public boolean getAllowEntityChangeBlock() {
		return allowEntityChangeBlock;
	}
	
	public void setAllowEntityChangeBlock(boolean allowEntityChangeBlock) { 
		this.allowEntityChangeBlock = allowEntityChangeBlock;
	}
	
	public boolean getAllowEntityCreatePortal() {
		return this.allowEntityCreatePortal;
	}
	
	public void setAllowEntityCreatePortal(boolean allowEntityCreatePortal) {
		this.allowEntityCreatePortal = allowEntityCreatePortal;
	}
	
	public boolean getAllowBedEntering() {
		return this.allowBedEntering;
	}
	
	public void setAllowBedEntering(boolean allowBedEntering) {
		this.allowBedEntering = allowBedEntering;
	}
	
	public boolean getAllowHangingBreakByEntity() {
		return this.allowHangingBreakByEntity;
	}
	
	public void setAllowHangingBreakByEntity(boolean allowHangingBreakByEntity) {
		this.allowHangingBreakByEntity = allowHangingBreakByEntity;
	}
	
	public boolean getRemoveEntitiesUponLoadingWorld() {
		return this.removeEntitiesUponLoadingWorld;
	}
	
	public void setRemoveEntitiesUponLoadingWorld(boolean removeEntitiesUponLoadingWorld) {
		this.removeEntitiesUponLoadingWorld = removeEntitiesUponLoadingWorld;
	}
	
	public boolean getResetPlayerUponJoining() {
		return this.resetPlayerUponJoining;
	}
	
	public void setResetPlayerUponJoining(boolean resetPlayerUponJoining) {
		this.resetPlayerUponJoining = resetPlayerUponJoining;
	}
	
	public boolean getAllowInventoryClicking() {
		return this.allowInventoryClicking;
	}
	
	public void setAllowInventoryClicking(boolean allowInventoryClicking) {
		this.allowInventoryClicking = allowInventoryClicking;
	}
	
	public boolean getAllowItemBreaking() {
		return this.allowItemBreaking;
	}
	
	public void setAllowItemBreaking(boolean allowItemBreaking) {
		this.allowItemBreaking = allowItemBreaking;
	}
	
	public boolean getAllowArmorBreaking() {
		return this.allowArmorBreaking;
	}
	
	public void setAllowArmorBreaking(boolean allowArmorBreaking) {
		this.allowArmorBreaking = allowArmorBreaking;
	}
	
	public boolean getStaffGetAllKits() {
		return this.staffGetAllKits;
	}
	
	public void setStaffGetAllKits(boolean staffGetAllKits) {
		this.staffGetAllKits = staffGetAllKits;
	}
	
	public boolean getUseTop8() {
		return this.useTop8;
	}
	
	public void setUseTop8(boolean useTop8) {
		this.useTop8 = useTop8;
	}
	
	public boolean getAutoVanishStaff() {
		return this.autoVanishStaff;
	}
	
	public void setAutoVanishStaff(boolean autoVanishStaff) {
		this.autoVanishStaff = autoVanishStaff;
	}
	
	public boolean getKickDefaultsForPros() {
		return this.kickDefaultsForPros;
	}
	
	public void setKickDefaultsForPros(boolean kickDefaultsForPros) {
		this.kickDefaultsForPros = kickDefaultsForPros;
	}
	
	public boolean getDoDaylightCycle() {
		return this.doDaylightCycle;
	}
	
	public void setDoDaylightCycle(boolean doDaylightCycle) {
		this.doDaylightCycle = doDaylightCycle;
	}
	
	public int getWinEmeralds() {
		return this.winEmeralds;
	}
	
	public void setWinEmeralds(int winEmeralds) {
		this.winEmeralds = winEmeralds;
	}
	
	public int getKillEmeralds() {
		return this.killEmeralds;
	}
	
	public void setKillEmeralds(int killEmeralds) {
		this.killEmeralds = killEmeralds;
	}
	
	public int getLossEmeralds() {
		return this.lossEmeralds;
	}
	
	public void setLossEmeralds(int lossEmeralds) {
		this.lossEmeralds = lossEmeralds;
	}
	
	public int getFlintAndSteelUses() {
		return this.flintAndSteelUses;
	}
	
	public void setFlintAndSteelUses(int flintAndSteelUses) {
		this.flintAndSteelUses = flintAndSteelUses;
	}
	
	public boolean getDebug() {
		return this.debug;
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public static boolean isServerFull() {
		return Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers();
	}
	
	public static List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(!SpectatorHandler.contains(player)) {
				players.add(player);
			}
		}
		return players;
	}
	
	public static void sendPlayerToServer(Player player, String server) {
		sendPlayerToServer(player, server, false);
	}
	
	public static void sendPlayerToServer(Player player, String server, boolean partySend) {
		if(!partySend) {
			PlayerServerJoinEvent event = new PlayerServerJoinEvent(player, server);
			Bukkit.getPluginManager().callEvent(event);
			if(event.isCancelled()) {
				return;
			}
		}
		Plugins plugin = ProMcGames.getPlugin();
		boolean goToSubHub = false;
		if(server.equalsIgnoreCase("hub") && (plugin == Plugins.CLAN_BATTLES || plugin == Plugins.SURVIVAL_GAMES)) {
			server = "sghub";
			goToSubHub = true;
		}
		/*if(server.equalsIgnoreCase("hub") && (plugin == Plugins.UHC || plugin == Plugins.UHC_BATTLES)) {
			server = "uhchub";
			goToSubHub = true;
		}*/
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(server);
		player.sendPluginMessage(ProMcGames.getInstance(), "BungeeCord", out.toByteArray());
		if(goToSubHub) {
			final String name = player.getName();
			new DelayedTask(new Runnable() {
				@Override
				public void run() {
					Player player = ProPlugin.getPlayer(name);
					if(player != null) {
						ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF("Connect");
						out.writeUTF("hub");
						player.sendPluginMessage(ProMcGames.getInstance(), "BungeeCord", out.toByteArray());
					}
				}
			}, 20);
		}
	}
	
	public static Player getPlayer(String name) {
		return getPlayer(name, false);
	}
	
	public static Player getPlayer(String name, boolean Disguise7d) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if((Disguise7d && Disguise.getName(player).equalsIgnoreCase(name)) || (player.getName().equalsIgnoreCase(name))) {
				return player;
			}
		}
		return null;
	}
	
	public static Player getPlayerFromRealName(String realName) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(Disguise.getName(player).toLowerCase().equals(realName.toLowerCase())) {
				return player;
			}
		}
		return null;
	}
	
	public static void resetPlayer(Player player) {
		player.getInventory().clear();
		player.getInventory().setHelmet(new ItemStack(Material.AIR));
		player.getInventory().setChestplate(new ItemStack(Material.AIR));
		player.getInventory().setLeggings(new ItemStack(Material.AIR));
		player.getInventory().setBoots(new ItemStack(Material.AIR));
		player.getInventory().setHeldItemSlot(0);
		player.updateInventory();
		player.setLevel(0);
		player.setExp(0.0f);
		player.setMaxHealth(20.0d);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.setGameMode(GameMode.SURVIVAL);
		if(player.getAllowFlight()) {
			player.setFlying(false);
			player.setAllowFlight(false);
		}
		for(PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		if(player.getVehicle() != null) {
			player.leaveVehicle();
		}
		if(player.getPassenger() != null) {
			player.getPassenger().leaveVehicle();
		}
	}
	
	public static void restartServer(long delay) {
		MessageHandler.alert("Server restarting in &b" + (delay / 20) + " &aseconds!");
		EffectUtil.playSound(Sound.NOTE_STICKS);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				restartServer();
			}
		}, delay);
	}
	
	public static void restartServer() {
		restarting = true;
		FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/banned-ips.json"));
		FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/banned-ips.txt.converted"));
		FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/banned-players.json"));
		FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/banned-players.txt.converted"));
		for(Player player : Bukkit.getOnlinePlayers()) {
			sendPlayerToServer(player, "hub");
		}
		Bukkit.getPluginManager().callEvent(new ServerRestartEvent());
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				for(Player player : Bukkit.getOnlinePlayers()) {
					sendPlayerToServer(player, "hub");
				}
			}
		}, 20);
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getServer().shutdown();
			}
		}, 40);
	}
	
	public static void dispatchCommandToServer(String server, String command) {
		ProMcGames.getClient().sendMessageToServer(new Instruction(new String[]{Inst.SERVER_SENDTOCLIENT.toString(), Inst.CLIENT_COMMAND.toString(), server, command}));
	}
	
	public static void dispatchCommandToGroup(String group, String command) {
		ProMcGames.getClient().sendMessageToServer(new Instruction(new String[]{Inst.SERVER_SENDTOGROUP.toString(), Inst.CLIENT_COMMAND.toString(), group, command}));
	}
	
	public static void dispatchCommandToAll(String command) {
		ProMcGames.getClient().sendMessageToServer(new Instruction(new String[]{Inst.SERVER_SENDTOALL.toString(), Inst.CLIENT_COMMAND.toString(), command}));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!getAllowBuilding()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBurn(BlockBurnEvent event) {
		if(!getAllowBlockBurning()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockFade(BlockFadeEvent event) {
		if(!getAllowBlockFading()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockForm(BlockFormEvent event) {
		if(!getAllowBlockForming()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockFromTo(BlockFromToEvent event) {
		if(!getAllowBlockFromTo()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockGrow(BlockGrowEvent event) {
		if(!getAllowBlockGrow()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if(event.getCause() != IgniteCause.FLINT_AND_STEEL) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(!getAllowBuilding()) {
			event.setCancelled(true);
		}
		if(!event.isCancelled() && !getAllowHangingBreakByEntity()) {
			int x = event.getBlock().getLocation().getBlockX();
			int y = event.getBlock().getLocation().getBlockY();
			int z = event.getBlock().getLocation().getBlockZ();
			for(Entity entity : event.getPlayer().getWorld().getEntities()) {
				if(entity instanceof ItemFrame) {
					if(entity.getLocation().getBlockX() == x && entity.getLocation().getBlockY() == y && entity.getLocation().getBlockZ() == z) {
						MessageHandler.sendMessage(event.getPlayer(), "&cCannot place a block here as it would destroy an item frame");
						event.setCancelled(true);
						break;
					}
				}
			}
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockSpread(BlockSpreadEvent event) {
		if(!getAllowBlockSpread()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onLeavesDecay(LeavesDecayEvent event) {
		if(!getAllowLeavesDecay()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(event.getSpawnReason() != SpawnReason.CUSTOM && !getAllowDefaultMobSpawning()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityCombust(EntityCombustEvent event) {
		if(!getAllowEntityCombusting()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if(!getAllowHealthRegeneration()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityExplode(EntityExplodeEvent event) {
		if(ProMcGames.getPlugin() != Plugins.BUILDING && ProMcGames.getPlugin() != Plugins.UHC) {
			Entity entity = event.getEntity();
			if(entity != null && (entity instanceof EnderDragon || entity instanceof Wither)) {
				event.setCancelled(true);
			} else if(event.blockList().size() > 0) {
				SpawnTNTBlocksEvent spawnTNTBlock = new SpawnTNTBlocksEvent();
				Bukkit.getPluginManager().callEvent(spawnTNTBlock);
				if(!spawnTNTBlock.isCancelled()) {
					for(int a = 0; a < event.blockList().size(); ++a) {
						if(a % 2 == 0) {
							Block block = event.blockList().get(a);
							Location location = block.getLocation();
							Material material = block.getType();
							byte data = block.getData();
							FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(location.add(0, 2, 0), material, data);
							fallingBlock.setDropItem(false);
							Vector velocity = fallingBlock.getVelocity();
							velocity.setX(random.nextBoolean() ? random.nextDouble() : random.nextDouble() * -1);
							velocity.setY(random.nextDouble());
							velocity.setZ(random.nextBoolean() ? random.nextDouble() : random.nextDouble() * -1);
							fallingBlock.setVelocity(velocity);
						}
					}
					event.setCancelled(true);
				}
			}
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if(waterEffectBlocks != null && waterEffectBlocks.contains(event.getEntity())) {
			waterEffectBlocks.remove(event.getEntity());
			event.setCancelled(true);
		} else {
			EffectUtil.displayParticles(event.getTo(), event.getBlock().getLocation());
			if(!getAllowEntityChangeBlock()) {
				event.setCancelled(true);
			}
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityCreatePortal(EntityCreatePortalEvent event) {
		if(!getAllowEntityCreatePortal()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		if(restarting) {
			event.setKickMessage("This server is currently restarting");
			event.setResult(Result.KICK_OTHER);
		} else if(ProPlugin.isServerFull() && Ranks.PRO.hasRank(player)) {
			if(getKickDefaultsForPros()) {
				if(ProMcGames.getMiniGame() != null && !ProMcGames.getMiniGame().getJoiningPreGame()) {
					if(SpectatorHandler.isEnabled() && Ranks.ELITE.hasRank(player)) {
						for(Player spectator : SpectatorHandler.getPlayers()) {
							if(!Ranks.PRO.hasRank(spectator)) {
								MessageHandler.sendMessage(spectator, "You've been moved to the hub to make room for a " + AccountHandler.getRank(player).getPrefix());
								ProPlugin.sendPlayerToServer(spectator, "hub");
								event.setResult(Result.ALLOWED);
								return;
							}
						}
					}
					return;
				}
				for(Player online : Bukkit.getOnlinePlayers()) {
					if(!Ranks.PRO.hasRank(online, true)) {
						MessageHandler.sendMessage(online, "You've been moved to the hub to make room for a " + AccountHandler.getRank(player).getPrefix());
						ProPlugin.sendPlayerToServer(online, "hub");
						event.setResult(Result.ALLOWED);
						return;
					}
				}
				event.setResult(Result.KICK_FULL);
			} else {
				event.setResult(Result.ALLOWED);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHighestPlayerLogin(PlayerLoginEvent event) {
		if(event.getResult() == Result.KICK_WHITELIST) {
			event.setKickMessage(ChatColor.RED + "This server is whitelisted for either testing or a private event");
		} else if(event.getResult() == Result.KICK_FULL) {
			if(ProMcGames.getMiniGame() == null) {
				event.setKickMessage(ChatColor.RED + "This server is full! To join full servers you must be a " + Ranks.PRO.getPrefix() + ChatColor.RED + " or above! " + ChatColor.AQUA + "/buy");
			} else {
				if(ProMcGames.getMiniGame().getJoiningPreGame() || AccountHandler.getRank(event.getPlayer()) == Ranks.PLAYER) {
					event.setKickMessage(ChatColor.RED + "This server is full! To join full games you must be a " + Ranks.PRO.getPrefix() + ChatColor.RED + " or above! " + ChatColor.AQUA + "/buy");
				} else {
					event.setKickMessage(ChatColor.RED + "This server is full and you cannot join full games at this time");
				}
			}
		} else if(getBeta() && !Ranks.PRO.hasRank(event.getPlayer())) {
			event.setKickMessage(ChatColor.GOLD + "This game is currently in " + ChatColor.RED + "BETA" + ChatColor.GOLD + " mode. You must have " + Ranks.PRO.getPrefix() + ChatColor.GOLD + "or above to join! " + ChatColor.AQUA + "/buy");
			event.setResult(Result.KICK_OTHER);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(getResetPlayerUponJoining()) {
			resetPlayer(player);
			player.teleport(player.getWorld().getSpawnLocation());
		}
		event.setJoinMessage(null);
		player.setTicksLived(1);
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
		final String name = player.getName();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					Bukkit.getPluginManager().callEvent(new AsyncPlayerJoinEvent(player));
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		Player player = event.getPlayer();
		final String name = player.getName();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Player player = ProPlugin.getPlayer(name);
				if(player != null) {
					Bukkit.getPluginManager().callEvent(new AsyncPostPlayerJoinEvent(player));
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		if(!getAllowBedEntering()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(getDropItemsOnLeave() && !SpectatorHandler.contains(player)) {
			for(ItemStack itemStack : player.getInventory().getContents()) {
				if(itemStack != null && itemStack.getType() != Material.AIR) {
					player.getWorld().dropItem(player.getLocation(), itemStack);
				}
			}
			for(ItemStack itemStack : player.getInventory().getArmorContents()) {
				if(itemStack != null && itemStack.getType() != Material.AIR) {
					player.getWorld().dropItem(player.getLocation(), itemStack);
				}
			}
		}
		if(player.getVehicle() != null) {
			player.leaveVehicle();
		}
		if(player.getPassenger() != null) {
			player.eject();
		}
		player.remove();
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
		final UUID uuid = player.getUniqueId();
		final UUID realUUID = Disguise.getUUID(player, true);
		final String name = player.getName();
		final String realName = Disguise.getName(player, true);
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				Bukkit.getPluginManager().callEvent(new AsyncPlayerLeaveEvent(uuid, realUUID, name, realName));
			}
		});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(!getAllowBowShooting()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamage(EntityDamageEvent event) {
		if(!getAllowEntityDamage()) {
			event.setCancelled(true);
		}
		if(event.getCause() == DamageCause.VOID && event.isCancelled()) {
			Entity entity = event.getEntity();
			while(entity.getVehicle() != null) {
				entity = entity.getVehicle();
			}
			if(entity.getPassenger() != null) {
				entity.eject();
			}
			entity.teleport(event.getEntity().getWorld().getSpawnLocation());
			event.setCancelled(true);
		}
		if(!getAllowArmorBreaking() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			for(ItemStack armor : player.getInventory().getArmorContents()) {
				armor.setDurability((short) -1);
			}
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHighEntityDamage(EntityDamageEvent event) {
		if(!event.isCancelled() && event.getCause() == DamageCause.VOID && event.getEntity() instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) event.getEntity();
			event.setDamage(livingEntity.getHealth());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof ItemFrame) {
			event.setCancelled(!getAllowHangingBreakByEntity());
		} else if(!getAllowEntityDamageByEntities()) {
			event.setCancelled(true);
		}
		if(!event.isCancelled() && event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
			Player player = (Player) event.getEntity();
			if(!(ProMcGames.getPlugin() == Plugins.VERSUS && LobbyHandler.isInLobby(player))) {
				Projectile projectile = (Projectile) event.getDamager();
				if(!SpectatorHandler.contains(player) && projectile.getShooter() != null && projectile.getShooter() instanceof Player && projectile.getLocation().getY() - player.getLocation().getY() >= 1.35f) {
					final Player shooter = (Player) projectile.getShooter();
					PlayerHeadshotEvent headshotEvent = new PlayerHeadshotEvent(shooter, event.getDamage());
					Bukkit.getPluginManager().callEvent(headshotEvent);
					if(!headshotEvent.isCancelled()) {
						if(!player.getName().equals(shooter.getName())) {
							if(headshotEvent.getDamage() > -1) {
								event.setDamage(headshotEvent.getDamage());
							}
							EmeraldsHandler.addEmeralds(shooter, 1, EmeraldReason.HEADSHOT, true);
						}
					}
				}
			}
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(player.getKiller() != null && player.getKiller() instanceof Player) {
			Player killer = player.getKiller();
			String message = event.getDeathMessage().replace(player.getName(), AccountHandler.getPrefix(player, false) + ChatColor.WHITE);
			Bukkit.getPluginManager().callEvent(new GameDeathEvent(player, killer));
			Bukkit.getPluginManager().callEvent(new GameKillEvent(killer, player, event.getDeathMessage()));
			if(!player.getName().equals(killer.getName())) {
				event.setDeathMessage(message.replace(killer.getName(), AccountHandler.getPrefix(killer, false) + ChatColor.WHITE));
			}
		} else {
			Bukkit.getPluginManager().callEvent(new GameDeathEvent(player));
			String message = event.getDeathMessage().replace(player.getName(), AccountHandler.getPrefix(player, false));
			String [] split = message.split("entity.");
			message = split[0];
			if(split.length > 1) {
				message += ChatColor.RED + StringUtil.getFirstLetterCap(split[1].replace(".name", ""));
			}
			event.setDeathMessage(message);
		}
		event.setDeathMessage(event.getDeathMessage().split(" using ")[0]);
		final String name = player.getName();
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				Player player = ProPlugin.getPlayer(name);
				if(name != null) {
					PacketPlayInClientCommand packet = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
					CraftPlayer craftPlayer = (CraftPlayer) player;
					EntityPlayer entityPlayer = craftPlayer.getHandle();
					entityPlayer.playerConnection.a(packet);
				}
			}
		});
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if(!getAllowFoodLevelChange()) {
			event.setFoodLevel(20);
		}
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			player.setSaturation(4.0f);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onHangingBreak(HangingBreakEvent event) {
		if(event.getCause() == RemoveCause.EXPLOSION || event.getCause() == RemoveCause.ENTITY) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
		if(!getAllowHangingBreakByEntity()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getRightClicked() instanceof ItemFrame && !getAllowHangingBreakByEntity()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if(!getAllowPickingUpItems()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(!getAllowDroppingItems()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(getAllowPlayerInteraction()) {
			ItemStack item = event.getItem();
			if(item != null && item.getType() == Material.FLINT_AND_STEEL && event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.isCancelled()) {
				int uses = getFlintAndSteelUses();
				if(uses > 0) {
					item.setDurability((short) (item.getDurability() + (item.getType().getMaxDurability() / uses)));
					if(item.getDurability() >= item.getType().getMaxDurability()) {
						event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
					} else {
						event.getPlayer().setItemInHand(item);
					}
				}
			}
		} else {
			event.setCancelled(true);
		}
		if(!getAllowItemBreaking()) {
			ItemStack item = event.getItem();
			if(item != null && item.getType() != Material.FLINT_AND_STEEL) {
				event.getPlayer().getItemInHand().setDurability((short) -1);
			}
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(ProMcGames.getPlugin() != Plugins.BUILDING || ProMcGames.getPlugin() != Plugins.TESTING) {
			Material material = event.getTo().getBlock().getType();
			if(material == Material.STATIONARY_WATER && event.getFrom().getBlock().getType() == Material.AIR && event.getPlayer().getVelocity().getY() < -0.40d) {
				Player player = event.getPlayer();
				WaterSplashEvent waterSplashEvent = new WaterSplashEvent(player);
				Bukkit.getPluginManager().callEvent(waterSplashEvent);
				if(!waterSplashEvent.isCancelled()) {
					Random random = new Random();
					double value = 0.25d;
					for(int a = 0; a < 4; ++a) {
						FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(player.getLocation().add(0, 0.5, 0), Material.WATER, (byte) 0);
						fallingBlock.setDropItem(false);
						if(waterEffectBlocks == null) {
							waterEffectBlocks = new ArrayList<FallingBlock>();
						}
						waterEffectBlocks.add(fallingBlock);
						Vector velocity = fallingBlock.getVelocity();
						velocity.setX(random.nextBoolean() ? value : -value);
						velocity.setY(value);
						velocity.setZ(random.nextBoolean() ? value : -value);
						fallingBlock.setVelocity(velocity);
					}
				}
			}
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onItemSpawn(ItemSpawnEvent event) {
		if(!getAllowItemSpawning()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onWorldLoad(WorldLoadEvent event) {
		World world = event.getWorld();
		if(getDoDaylightCycle()) {
			world.setGameRuleValue("doDaylightCycle", "true");
		} else {
			world.setGameRuleValue("doDaylightCycle", "false");
		}
		world.setGameRuleValue("keepInventory", "false");
		world.setWeatherDuration(0);
		world.setThunderDuration(0);
		world.setStorm(false);
		world.setThundering(false);
		world.setAutoSave(false);
		world.setTime(6000);
		if(getRemoveEntitiesUponLoadingWorld()) {
			for(Entity entity : world.getEntities()) {
				if(entity instanceof LivingEntity && !(entity instanceof Player)) {
					entity.remove();
				}
			}
		}
		Plugins plugin = ProMcGames.getPlugin();
		if(plugin != Plugins.BUILDING && plugin != Plugins.TESTING && plugin != Plugins.UHC && plugin != Plugins.FACTIONS) {
			if(importedWorlds == null) {
				importedWorlds = new ArrayList<String>();
			}
			importedWorlds.add(world.getName());
		}
		ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/authors.yml");
		if(config.getFile().exists() && config.getConfig().contains("authors")) {
			MessageHandler.alert("&eMap Credits:");
			MessageHandler.alert("&aAuthors: &e" + config.getConfig().getString("authors"));
			for(Player player : Bukkit.getOnlinePlayers()) {
				new TitleDisplayer(player, "&c" + world.getName().replace("_", " ")).setFadeIn(40).setStay(60).setFadeOut(40).display();
			}
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onWeatherChange(WeatherChangeEvent event) {
		event.setCancelled(true);
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		if(ProMcGames.getPlugin() != Plugins.FACTIONS) {
			for(World world : Bukkit.getWorlds()) {
				String path = Bukkit.getWorldContainer().getPath() + "/" + world.getName() + "/";
				FileHandler.delete(new File(path + "playerdata"));
				FileHandler.delete(new File(path + "stats"));
				Bukkit.unloadWorld(world, false);
			}
			if(importedWorlds != null && ProMcGames.getPlugin() != Plugins.UHC) {
				for(String world : importedWorlds) {
					FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/" + world));
				}
			}
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if(!getAllowInventoryClicking()) {
			event.setCancelled(true);
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		if(event.getEntityType() == EntityType.SHEEP) {
			event.getDrops().add(new ItemCreator(Material.PORK).setName(ChatColor.WHITE + "Raw Mutton").getItemStack());
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler
	public void onFurnaceSmelt(FurnaceSmeltEvent event) {
		if(event.getSource().getItemMeta().getDisplayName() != null && event.getSource().getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Raw Mutton")) {
			event.setResult(new ItemCreator(event.getResult()).setName("&fCooked Mutton").getItemStack());
		}
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler
	public void onFiveSecondTask(FiveSecondTaskEvent event) {
		for(Databases database : Databases.values()) {
			database.connect();
		}
	}
	
	@EventHandler
	public void onFiveMinuteTask(FiveMinuteTaskEvent event) {
		FileHandler.delete(new File(Bukkit.getWorldContainer().getPath() + "/logs"));
		if(debug) {
			Bukkit.getLogger().info(event.getEventName());
		}
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(getWinEmeralds() > 0) {
			if(event.getPlayer() != null) {
				EmeraldsHandler.addEmeralds(event.getPlayer(), getWinEmeralds(), EmeraldReason.GAME_WIN, true);
			} else if(event.getTeam() != null) {
				for(OfflinePlayer offlinePlayer : event.getTeam().getPlayers()) {
					Player player = offlinePlayer.getPlayer();
					if(player != null) {
						EmeraldsHandler.addEmeralds(player, getWinEmeralds(), EmeraldReason.GAME_WIN, true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		if(getKillEmeralds() > 0) {
			EmeraldsHandler.addEmeralds(event.getPlayer(), getKillEmeralds(), EmeraldReason.GAME_KILL, true);
		}
	}
	
	@EventHandler
	public void onGameLoss(GameLossEvent event) {
		if(getLossEmeralds() > 0) {
			EmeraldsHandler.addEmeralds(event.getPlayer(), getLossEmeralds(), EmeraldReason.GAME_KILL, true);
		}
	}
}