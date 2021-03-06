package promcgames.gameapi.games.versus.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import promcgames.ProPlugin;
import promcgames.customevents.player.InventoryItemClickEvent;
import promcgames.customevents.player.MouseClickEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.games.versus.kits.VersusKit;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class KitVoter implements Listener {
	
	private static KitVoter instance = null;
	private List<VersusKit> kits = null;
	private List<KitVote> votes = null;
	private List<String> watching = null;
	private static String inventoryName = null;
	private static ItemStack item = null;
	
	public static class KitVote {
		
		private String playerName = null;
		private VersusKit kit = null;
		private int votes = 0;
		
		public KitVote(String playerName, VersusKit kit, int votes) {
			this.playerName = playerName;
			this.kit = kit;
			this.votes = votes;
		}
		
		public String getPlayerName() {
			return playerName;
		}
		
		public VersusKit getKit() {
			return kit;
		}
		
		public int getVotes() {
			return votes;
		}
		
	}
	
	public KitVoter() {
		if(instance == null) {
			instance = this;
			votes = new ArrayList<>();
			watching = new ArrayList<>();
			item = new ItemCreator(Material.NETHER_STAR).setName("&a" + inventoryName).getItemStack();
			inventoryName = "Versus Tournament Kit Vote";
			addKits();
			EventUtil.register(this);
		}
	}
	
	@EventHandler
	public void onMouseClick(MouseClickEvent event) {
		Player player = event.getPlayer();
		if(TournamentQueueHandler.getInstance().getQueue().contains(player.getName()) && player.getItemInHand() != null && player.getItemInHand().getType() == Material.NETHER_STAR) {
			Inventory inv = Bukkit.createInventory(player, 18, inventoryName);
			KitVote vote = getKitVote(player);
			VersusKit votedKit = null;
			if(vote != null) {
				votedKit = vote.getKit();
			}
			for(VersusKit kit : kits) {
				if(votedKit != null && kit == votedKit) {
					inv.addItem(new ItemCreator(kit.getIcon()).setAmount(getVotesForKit(kit)).addLore("&bYou voted for this kit").getItemStack());
				} else {
					inv.addItem(new ItemCreator(kit.getIcon()).setAmount(getVotesForKit(kit)).addLore("&bClick to vote for this kit").getItemStack());
				}
			}
			player.openInventory(inv);
			watching.add(player.getName());
		}
	}
	
	@EventHandler
	public void onInventoryItemClick(InventoryItemClickEvent event) {
		Player player = event.getPlayer();
		if(TournamentQueueHandler.getInstance().getQueue().contains(player.getName()) && event.getTitle().equals(inventoryName)) {
			event.setCancelled(true);
			VersusKit kit = kits.get(event.getSlot());
			KitVote vote = getKitVote(player);
			if(vote != null && vote.getKit() == kit) {
				MessageHandler.sendMessage(player, "&cYou've already voted for this kit");
			} else {
				int votes = Ranks.getVotes(player);
				if(vote != null) {
					instance.votes.remove(vote);
				}
				instance.votes.add(new KitVote(player.getName(), kit, votes));
				MessageHandler.sendMessage(player, "Voted for the &b" + kit.getName() + " &akit");
			}
			player.closeInventory();
		}
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		for(String playerName : watching) {
			Player player = ProPlugin.getPlayer(playerName);
			if(player != null) {
				InventoryView view = player.getOpenInventory();
				if(view != null) {
					int a = 0;
					for(VersusKit kit : kits) {
						view.getItem(a++).setAmount(getVotesForKit(kit));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		KitVote vote = getKitVote(player);
		if(vote != null) {
			votes.remove(vote);
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			if(watching.contains(player.getName())) {
				watching.remove(player.getName());
			}
		}
	}
	
	public void disable() {
		HandlerList.unregisterAll(this);
		kits.clear();
		votes.clear();
		watching.clear();
		kits = null;
		votes = null;
		watching = null;
		for(Player player : ProPlugin.getPlayers()) {
			if(player.getInventory().contains(item)) {
				player.getInventory().remove(item);
			}
		}
		instance = null;
	}
	
	public void addKits() {
		if(kits == null) {
			kits = new ArrayList<>();
		} else {
			kits.clear();
		}
		for(VersusKit kit : VersusKit.getKits()) {
			String name = kit.getName();
			if(!name.equals("TNT") && !name.equals("One Hit Wonder") && !name.equals("Quickshot")) {
				kits.add(kit);
			}
		}
	}
	
	public void chooseWinningKit() {
		List<VersusKit> winning = new ArrayList<>();
		int winningVotes = 0;
		for(VersusKit kit : kits) {
			int votes = getVotesForKit(kit);
			if(votes > winningVotes) {
				winning.clear();
				winning.add(kit);
				winningVotes = votes;
			} else if(votes == winningVotes) {
				winning.add(kit);
			}
		}
		VersusKit winningKit = null;
		if(winning.size() == 1) {
			winningKit = winning.get(0);
		} else if(winning.size() >= 2) {
			winningKit = winning.get(new Random().nextInt(winning.size()));
		} else {
			winningKit = kits.get(new Random().nextInt(kits.size()));
		}
		VersusTournament.getInstance().setKit(winningKit);
		MessageHandler.alert("The &b" + winningKit.getName() + " &akit won with &b" + winningVotes + " votes");
		disable();
	}
	
	public int getVotesForKit(VersusKit kit) {
		int total = 0;
		for(KitVote vote : votes) {
			if(vote.getKit() == kit) {
				total += vote.getVotes();
			}
		}
		return total;
	}
	
	public KitVote getKitVote(Player player) {
		for(KitVote vote : votes) {
			if(vote.getPlayerName().equalsIgnoreCase(player.getName())) {
				return vote;
			}
		}
		return null;
	}
	
	public List<VersusKit> getKits() {
		return kits;
	}
	
	public List<KitVote> getVotes() {
		return votes;
	}
	
	public static ItemStack getItem() {
		return item;
	}
	
	public static KitVoter getInstance() {
		return instance;
	}
	
}