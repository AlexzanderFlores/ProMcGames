package promcgames.player;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.AsyncPlayerJoinEvent;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.PlayerAFKEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.TenTickTaskEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader;
import promcgames.server.servers.hub.items.cosmetic.PerkLoader.Perk;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.ItemUtil;
import de.slikey.effectlib.util.ParticleEffect;

public class Particles implements Listener {
	private static String name = null;
	private static Map<String, ParticleTypes> types = null;
	
	public enum ParticleTypes {
		FLAME(Material.BLAZE_ROD, Effect.MOBSPAWNER_FLAMES),
		SMOKE(Material.WEB, Effect.SMOKE),
		FIREWORK_SPARK(Material.FIREWORK, ParticleEffect.FIREWORKS_SPARK),
		MOB_SPELL(Material.POTION, ParticleEffect.MOB_SPELL),
		SPELL(Material.POTION, ParticleEffect.SPELL),
		INSTANT_SPELL(Material.POTION, ParticleEffect.INSTANT_SPELL),
		WITCH_MAGIC(Material.POTION, ParticleEffect.WITCH_MAGIC),
		NOTE(Material.JUKEBOX, ParticleEffect.NOTE),
		PORTAL(Material.PORTAL, ParticleEffect.PORTAL),
		EXPLODE(Material.TNT, ParticleEffect.EXPLODE),
		LAVA(Material.LAVA_BUCKET, ParticleEffect.LAVA),
		LARGE_SMOKE(Material.WEB, ParticleEffect.LARGE_SMOKE),
		CLOUD(Material.WEB, ParticleEffect.CLOUD),
		RED_DUST(Material.REDSTONE, ParticleEffect.RED_DUST),
		SNOWBALL_POOF(Material.SNOW_BALL, ParticleEffect.SNOWBALL_POOF),
		DRIP_WATER(Material.WATER, ParticleEffect.DRIP_WATER),
		DRIP_LAVA(Material.LAVA, ParticleEffect.DRIP_LAVA),
		SNOW_SHOVEL(Material.IRON_SPADE, ParticleEffect.SNOW_SHOVEL),
		SLIME(Material.SLIME_BALL, ParticleEffect.SLIME),
		HEART(Material.RED_ROSE, ParticleEffect.HEART),
		ANGRY_VILLAGER(Material.REDSTONE_BLOCK, ParticleEffect.ANGRY_VILLAGER),
		HAPPY_VILLAGER(Material.EMERALD_BLOCK, ParticleEffect.HAPPY_VILLAGER);
		
		private Effect effect = null;
		private ParticleEffect particlEffect = null;
		private ItemStack item = null;
		
		private ParticleTypes(Material material, Effect effect) {
			this.item = new ItemCreator(material).setName("&a" + getName()).getItemStack();
			this.effect = effect;
		}
		
		private ParticleTypes(Material material, ParticleEffect particleEffect) {
			this.item = new ItemCreator(material).setName("&a" + getName()).getItemStack();
			this.particlEffect = particleEffect;
		}
		
		private String getName() {
			return toString().substring(0, 1).toUpperCase() + toString().substring(1, toString().length()).toLowerCase().replace("_", " ");
		}
		
		public ItemStack getItem() {
			return item;
		}
		
		public void display(Location location) {
			if(particlEffect != null) {
				try {
					ParticleEffect.valueOf(particlEffect.toString().toUpperCase()).display(location.add(0, 2, 0), 1, 0, 1, 0, 3);
				} catch(Exception e) {
					
				}
			} else if(effect != null) {
				EffectUtil.playEffect(effect, location.add(0, 1, 0));
			}
		}
		
		public void displaySpiral(Location location) {
			displaySpiral(location, 5);
		}
		
		public void displaySpiral(Location location, double height) {
			displaySpiral(location, 5, 2);
		}
		
		public void displaySpiral(Location location, double height, double radius) {
			String particle = effect == null ? particlEffect.getName() : toString().toLowerCase();
			for(double y = 0; y < height; y += 0.05) {
				double x = radius * Math.cos(y);
				double z = radius * Math.sin(y);
				PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, (float) (location.getX() + x), (float) (location.getY() + y), (float) (location.getZ() + z), 0, 0, 0, 0, 1);
				for(Player player : Bukkit.getOnlinePlayers()) {
					CraftPlayer craftPlayer = (CraftPlayer) player;
					craftPlayer.getHandle().playerConnection.sendPacket(packet);
				}
			}
		}
	}
	
	public Particles() {
		new CommandBase("particles", true) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				Player player = (Player) sender;
				player.openInventory(getParticlesMenu(player, getName()));
				return true;
			}
		}.setRequiredRank(Ranks.ELITE);
		types = new HashMap<String, ParticleTypes>();
		EventUtil.register(this);
	}
	
	public static void setType(Player player, ParticleTypes type) {
		types.put(player.getName(), type);
	}
	
	public static Inventory getParticlesMenu(Player player, String title) {
		int size = ItemUtil.getInventorySize(ParticleTypes.values().length + 1);
		Inventory inventory = Bukkit.createInventory(player, size, title == null ? ChatColor.stripColor(getName()) : ChatColor.stripColor(title));
		inventory.addItem(new ItemCreator(Material.WATER_BUCKET).setName("&bRemove particles").getItemStack());
		for(ParticleTypes types: ParticleTypes.values()) {
			inventory.addItem(types.getItem());
		}
		return inventory;
	}
	
	public static String getName() {
		if(name == null) {
			name = ChatColor.AQUA + "Particle Selector";
		}
		return name;
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		if(event.getTitle().startsWith(ChatColor.stripColor(getName()))) {
			Player player = event.getPlayer();
			String name = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());
			if(name.equals("Remove particles")) {
				types.remove(player.getName());
				if(DB.HUB_PARTICLES.isUUIDSet(player.getUniqueId())) {
					DB.HUB_PARTICLES.deleteUUID(player.getUniqueId());
				}
			} else {
				ParticleTypes type = ParticleTypes.valueOf(name.toUpperCase().replace(" ", "_"));
				types.put(player.getName(), type);
				MessageHandler.sendMessage(player, "You selected &b" + name);
				if(DB.HUB_PARTICLES.isUUIDSet(player.getUniqueId())) {
					DB.HUB_PARTICLES.updateString("particle_type", type.toString(), "uuid", player.getUniqueId().toString());
				} else {
					DB.HUB_PARTICLES.insert("'" + player.getUniqueId().toString() + "', '" + type.toString() + "'");
				}
			}
			player.closeInventory();
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTenTickTask(TenTickTaskEvent event) {
		if(types != null && !types.isEmpty()) {
			for(String name : types.keySet()) {
				Player player = ProPlugin.getPlayer(name);
				if(player != null && !PlayerAFKEvent.isAFK(player) && !SpectatorHandler.contains(player)) {
					types.get(name).display(player.getLocation());
				}
			}
		}
	}
	
	@EventHandler
	public void onAsyncPlayerJoin(AsyncPlayerJoinEvent event) {
		if(ProMcGames.getPlugin() == Plugins.HUB || ProMcGames.getPlugin() == Plugins.SGHUB) {
			Player player = event.getPlayer();
			if(Ranks.PRO_PLUS.hasRank(player) && DB.HUB_PARTICLES.isUUIDSet(player.getUniqueId())) {
				PerkLoader.addPerkToQueue(player, Perk.PARTICLES);
			}
		} else {
			AsyncPlayerJoinEvent.getHandlerList().unregister(this);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(types != null) {
			types.remove(event.getPlayer().getName());
		}
	}
}