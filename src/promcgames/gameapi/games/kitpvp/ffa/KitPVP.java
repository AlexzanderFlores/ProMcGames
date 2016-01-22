package promcgames.gameapi.games.kitpvp.ffa;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;

import promcgames.ProPlugin;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.StatsHandler;
import promcgames.gameapi.games.kitpvp.AutoRegenHandler;
import promcgames.gameapi.games.kitpvp.Chicken;
import promcgames.gameapi.games.kitpvp.Events;
import promcgames.gameapi.games.kitpvp.KillstreakHandler;
import promcgames.gameapi.games.kitpvp.Shop;
import promcgames.gameapi.games.kitpvp.SpawnHandler;
import promcgames.gameapi.games.kitpvp.kits.Archer;
import promcgames.gameapi.games.kitpvp.kits.Default;
import promcgames.gameapi.games.kitpvp.kits.Tank;
import promcgames.gameapi.games.kitpvp.kits.Voter;
import promcgames.gameapi.games.kitpvp.kits.Warrior;
import promcgames.gameapi.games.kitpvp.trophies.BlazeOfGlory;
import promcgames.gameapi.games.kitpvp.trophies.ChickenHunt;
import promcgames.gameapi.games.kitpvp.trophies.Enchanted;
import promcgames.gameapi.games.kitpvp.trophies.ExplosiveBow;
import promcgames.gameapi.games.kitpvp.trophies.ExtraHealth;
import promcgames.gameapi.games.kitpvp.trophies.Juggernaut;
import promcgames.gameapi.games.kitpvp.trophies.KillSeeker1;
import promcgames.gameapi.games.kitpvp.trophies.KillSeeker2;
import promcgames.gameapi.games.kitpvp.trophies.KillSeeker3;
import promcgames.gameapi.games.kitpvp.trophies.LevelUp1;
import promcgames.gameapi.games.kitpvp.trophies.LevelUp2;
import promcgames.gameapi.games.kitpvp.trophies.LevelUp3;
import promcgames.gameapi.games.kitpvp.trophies.PoisonBow;
import promcgames.gameapi.games.kitpvp.trophies.Revenge;
import promcgames.gameapi.games.kitpvp.trophies.SlimeTime;
import promcgames.gameapi.games.kitpvp.trophies.SnowballFight;
import promcgames.gameapi.games.kitpvp.trophies.Speed;
import promcgames.gameapi.games.kitpvp.trophies.Strength;
import promcgames.gameapi.kits.KitShop;
import promcgames.player.trophies.KitPVPTrophies;
import promcgames.server.DB;
import promcgames.server.ServerLogger;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.world.CPSDetector;

public class KitPVP extends ProPlugin {
	private static KitShop shop = null;
	
	public KitPVP() {
		super("Kit PVP FFA");
		addGroup("24/7");
		setAllowBowShooting(true);
		setAllowEntityDamage(true);
		setAllowEntityDamageByEntities(true);
		setAllowBlockGrow(false);
		setAllowItemBreaking(false);
		setAllowArmorBreaking(false);
		setUseTop8(true);
		setKickDefaultsForPros(false);
		setKillEmeralds(2);
		World world = Bukkit.getWorlds().get(0);
		world.setSpawnLocation(12, SpawnHandler.spawnY, -158);
		world.setGameRuleValue("keepInventory", "true");
		for(Entity entity : world.getEntities()) {
			if(entity instanceof LivingEntity && !(entity instanceof Player)) {
				entity.remove();
			}
		}
		new SpawnHandler(12.5, 112, -157.5, -270.0f, 0.0f, 40);
		new AutoRegenHandler(3.5, 113, -167.5);
		new SpectatorHandler();
		new Events();
		new StatsHandler(DB.PLAYERS_STATS_KIT_PVP, DB.PLAYERS_STATS_KIT_PVP_MONTHLY);
		new KillstreakHandler(3, 113, -162);
		new Chicken(87.5, 52, -239.5);
		new Shop(2, 113, -161);
		new ServerLogger();
		// Kit Shop
		shop = new KitShop();
		new Default();
		new Archer();
		new Warrior();
		new Tank();
		new Voter();
		// Trophies
		new KitPVPTrophies();
		new ChickenHunt();
		new Enchanted();
		new LevelUp1();
		new LevelUp2();
		new LevelUp3();
		new BlazeOfGlory();
		new Revenge();
		new KillSeeker1();
		new KillSeeker2();
		new KillSeeker3();
		new PoisonBow();
		new ExtraHealth();
		new Strength();
		new Speed();
		new Juggernaut();
		new ExplosiveBow();
		new SnowballFight();
		new SlimeTime();
		Location location = new Location(world, 2.5, 113, -154.5);//-0.5, 82, 7.5);
		Skeleton skeleton = (Skeleton) new NPCEntity(EntityType.SKELETON, "&bKit Selector", location) {
			@Override
			public void onInteract(Player player) {
				shop.open(player);
			}
		}.getLivingEntity();
		skeleton.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		Villager villager = (Villager) new NPCEntity(EntityType.VILLAGER, "&bTrophies", new Location(world, 21.5, 114, -167.5/*-10.5, 83, -10.5*/), Material.GOLD_INGOT) {
			@Override
			public void onInteract(Player player) {
				player.chat("/trophies");
			}
		}.getLivingEntity();
		villager.setProfession(Profession.LIBRARIAN);
		new CPSDetector(new Location(world, 21.5, 114, -149.5));
	}
	
	public static KitShop getShop() {
		return shop;
	}
}
