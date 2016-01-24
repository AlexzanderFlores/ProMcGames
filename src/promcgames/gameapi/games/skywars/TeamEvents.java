package promcgames.gameapi.games.skywars;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import promcgames.customevents.game.GameKillEvent;
import promcgames.gameapi.StatsHandler;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.player.MessageHandler;
import promcgames.player.PartyHandler;
import promcgames.player.PartyHandler.Party;
import promcgames.player.account.AccountHandler;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;

public class TeamEvents implements Listener {
	public TeamEvents() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED) {
			UUID uuid = event.getPlayer().getUniqueId();
			if(DB.NETWORK_PLAYER_PARTIES.isUUIDSet(uuid)) {
				int ID = DB.NETWORK_PLAYER_PARTIES.getInt("uuid", uuid.toString(), "party_id");
				if(DB.NETWORK_PLAYER_PARTIES.getSize("party_id", String.valueOf(ID)) == 2) {
					return;
				}
			}
			event.setKickMessage("You must have a party of two players to join " + ProMcGames.getMiniGame().getDisplayName() + ChatColor.GREEN + " /party");
			event.setResult(Result.KICK_OTHER);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			Party party = PartyHandler.getParty(player);
			if(party != null) {
				Player damager = null;
				if(event.getDamager() instanceof Player) {
					damager = (Player) event.getDamager();
				} else if(event.getDamager() instanceof Projectile) {
					Projectile projectile = (Projectile) event.getDamager();
					if(projectile.getShooter() instanceof Player) {
						damager = (Player) projectile.getShooter();
					}
				}
				if(damager != null) {
					Party damagerParty = PartyHandler.getParty(damager);
					if(damagerParty != null && party.getID() == damagerParty.getID()) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				List<Player> players = ProPlugin.getPlayers();
				if(players.size() == 2) {
					List<Party> parties = new ArrayList<Party>();
					for(Player player : players) {
						Party party = PartyHandler.getParty(player);
						if(party != null && !parties.contains(party)) {
							parties.add(party);
						}
					}
					if(parties.size() == 1) {
						for(Player player : ProPlugin.getPlayers()) {
							MessageHandler.alert(AccountHandler.getPrefix(player) + " &ahas won the game");
							StatsHandler.addWin(player);
						}
						ProMcGames.getMiniGame().setGameState(GameStates.ENDING);
					}
				}
			}
		});
	}
}
