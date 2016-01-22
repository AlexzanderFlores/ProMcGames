package promcgames.gameapi.games.versus;

import org.bukkit.event.Listener;

public class VersusElo implements Listener {
	/*private static Map<String, Map<VersusKit, Integer>> elo = null;
	private static int starting = 1000;
	
	public VersusElo() {
		elo = new HashMap<String, Map<VersusKit,Integer>>();
		new CommandBase("elo", -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				MessageHandler.sendMessage(sender, "&f/versusStats");
				return true;
			}
		};
		EventUtil.register(this);
	}
	
	public static int getElo(Player player, VersusKit kit) {
		if(elo != null && player != null && elo.containsKey(player.getName())) {
			Map<VersusKit, Integer> amounts = elo.get(player.getName());
			if(amounts.containsKey(kit)) {
				return amounts.get(kit);
			}
		}
		return starting;
	}
	
	private static int add(Player player, VersusKit kit, int amount) {
		elo.get(player.getName()).put(kit, getElo(player, kit) + amount);
		return getElo(player, kit);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final String name = event.getPlayer().getName();
		final UUID uuid = event.getPlayer().getUniqueId();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				String [] keys = new String [] {"uuid", "kit"};
				Map<VersusKit, Integer> amounts = new HashMap<VersusKit, Integer>();
				for(VersusKit kit : VersusKit.getKits()) {
					String [] values = new String [] {uuid.toString(), kit.getName()};
					if(DB.PLAYERS_VERSUS_ELO.isKeySet(keys, values)) {
						amounts.put(kit, DB.PLAYERS_VERSUS_ELO.getInt(keys, values, "amount"));
					} else {
						amounts.put(kit, starting);
						DB.PLAYERS_VERSUS_ELO.insert("'" + uuid.toString() + "', '" + kit.getName() + "', '" + starting + "'");
					}
				}
				elo.put(name, amounts);
			}
		});
	}
	
	@EventHandler
	public void onBattleEnd(BattleEndEvent event) {
		Player winner = event.getWinner();
		Player loser = event.getLoser();
		VersusKit kit = event.getKit();
		int elo1 = getElo(loser, kit);
		int elo2 = getElo(winner, kit);
		int K = 32;
		int diff = elo1 - elo2;
		double percentage = 1 / (1 + Math.pow(10, diff / 400));
		int amount = (int) Math.round(K * (1 - percentage));
		if(amount < 1) {
			amount = 1;
		}
		int winnerResult = add(winner, kit, amount);
		int loserResult = add(loser, kit, -amount);
		String newWinner = AccountHandler.getPrefix(winner) + " &6" + winnerResult + " &a(+" + amount + ")";
		String newLoser = AccountHandler.getPrefix(loser) + " &6" + loserResult + " &c(" + amount * -1 + ")";
		for(Player player : new Player [] {winner, loser}) {
			MessageHandler.sendLine(player);
			MessageHandler.sendMessage(player, newWinner);
			MessageHandler.sendMessage(player, newLoser);
			ChatClickHandler.sendMessageToRunCommand(player, " &cClick here", "Click to view elo", "/versusStats", "&aView elo: &f/versusStats [name] &aor");
			MessageHandler.sendLine(player);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(elo.containsKey(player.getName())) {
			final UUID uuid = player.getUniqueId();
			final String name = player.getName();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String [] keys = new String [] {"uuid", "kit"};
					Map<VersusKit, Integer> amounts = elo.get(name);
					for(VersusKit kit : amounts.keySet()) {
						int amount = amounts.get(kit);
						String [] values = new String [] {uuid.toString(), kit.getName()};
						if(DB.PLAYERS_VERSUS_ELO.isKeySet(keys, values)) {
							DB.PLAYERS_VERSUS_ELO.updateInt("amount", amount, keys, values);
						} else {
							DB.PLAYERS_VERSUS_ELO.insert("'" + uuid.toString() + "', '" + kit.getName() + "', '" + amount + "'");
						}
					}
					elo.remove(name);
				}
			});
		}
	}*/
}
