package promcgames.gameapi.games.survivalgames.kits.premium;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import promcgames.customevents.game.GameKillEvent;
import promcgames.gameapi.kits.KitBase;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.DB;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class PainKillerKit extends KitBase implements Listener {
	private static KitBase instance = null;
	private boolean used = false;
	
	public PainKillerKit() {
		super(new ItemCreator(new Potion(PotionType.REGEN, 1, true).toItemStack(1)).setName("&aPain Killer").setLores(new String[] {
			"&bPrice: 0",
			"",
			"&6Use of this kit will give all",
			"&6players who kill another player",
			"&6regeneration &e2 &6for &e5 &6seconds",
			"",
			"&eNote: &bThis is very useful",
			"&bto avoid clean ups!",
			"",
			"&cPurchase with &e/buy"
		}).getItemStack(), 30, true);
		instance = this;
		EventUtil.register(this);
	}
	
	@Override
	public void use(Player player) {
		if(Ranks.ELITE.hasRank(player, true)) {
			super.use(player);
		} else {
			String [] keys = new String [] {"uuid", "kit"};
			String [] values = new String [] {Disguise.getUUID(player).toString(), getPermission()};
			if(DB.PLAYERS_KITS.isKeySet(keys, values)) {
				super.use(player);
			} else {
				MessageHandler.sendMessage(player, "&cYou do not own this kit! You can purchase it here:");
				MessageHandler.sendMessage(player, "http://store.promcgames.com/category/372707");
			}
		}
	}
	
	@Override
	public String getPermission() {
		return "survival_games.pain_killer";
	}

	@Override
	public void execute() {
		if(getPlayers().size() > 0) {
			used = true;
			MessageHandler.alert("&l&n" + getName() + "&e is in use due to 1 or more players using it");
			MessageHandler.alert("Everyone will gain regeneration II for 5 seconds upon killing");
			MessageHandler.alert("Use this kit any time: &ehttp://store.promcgames.com/category/372707");
		}
	}

	@Override
	public void execute(Player player) {
		
	}
	
	public static KitBase getInstance() {
		return instance;
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		if(used) {
			Player player = event.getPlayer();
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 2 - 1));
			MessageHandler.sendMessage(player, "Due to one or more players using \"" + getName() + "\" you've been given regeneration " + 2 + " for " + 5 + " seconds");
			MessageHandler.sendMessage(player, "Use this kit any time: &ehttp://store.promcgames.com/category/372707");
		}
	}
}
