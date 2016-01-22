package promcgames.server.servers.hub;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.ProRankTrial;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.DB;

public class RankTransferHandler {
	public RankTransferHandler() {
		new CommandBase("rankTransfer", 1) {
			@Override
			public boolean execute(CommandSender sender, String[] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					if(AccountHandler.getRank(player) == Ranks.PRO || AccountHandler.getRank(player) == Ranks.PRO_PLUS || AccountHandler.getRank(player) == Ranks.ELITE) {
						if(AccountHandler.getRank(player) == Ranks.PRO && ProRankTrial.isInTrial(player)) {
							MessageHandler.sendMessage(player, "&cYou cannot transfer your trial rank");
						} else if(DB.PLAYERS_BUYCRAFT_RANK_TRANSFERS.isUUIDSet(Disguise.getUUID(player))) {
							String target = arguments[0];
							UUID targetUUID = AccountHandler.getUUID(target);
							if(targetUUID == null) {
								MessageHandler.sendMessage(player, ChatColor.RED + target + " has never logged in");
							} else {
								Ranks rank = AccountHandler.getRank(targetUUID);
								if(rank == Ranks.PLAYER || rank == Ranks.PRO || rank == Ranks.ELITE) {
									int value = AccountHandler.getRank(player).ordinal();
									int targetValue = rank.ordinal();
									if(value > targetValue) {
										Ranks newRank = AccountHandler.getRank(player);
										AccountHandler.setRank(targetUUID, newRank);
										AccountHandler.setRank(Disguise.getUUID(player), Ranks.PLAYER);
										player.kickPlayer(ChatColor.GREEN + "You now have the rank " + Ranks.PLAYER.getPrefix() + "Player\n" +
														  ChatColor.GREEN + target + " now has the rank " + newRank.getPrefix() + "\n" +
														  ChatColor.YELLOW + "Please reconnect!");
										DB.PLAYERS_BUYCRAFT_RANK_TRANSFERS.deleteUUID(Disguise.getUUID(player));
										if(DB.PLAYERS_RANK_TRANSFERS.isUUIDSet(Disguise.getUUID(player))) {
											DB.PLAYERS_RANK_TRANSFERS.deleteUUID(Disguise.getUUID(player));
										}
										DB.PLAYERS_RANK_TRANSFERS.insert("'" + Disguise.getUUID(player).toString() + "', '" + targetUUID.toString() + "'");
									} else if(value == targetValue) {
										MessageHandler.sendMessage(player, ChatColor.RED + "You and " + target + " have the same rank!");
									} else {
										MessageHandler.sendMessage(player, ChatColor.RED + "Cannot transfer: " + target + " has a move valuable rank");
									}
								} else {
									MessageHandler.sendMessage(player, ChatColor.RED + target + " does not have a transferrable rank");
								}
							}
						} else {
							MessageHandler.sendMessage(player, ChatColor.RED + "You do not have a pending rank transfer");
							MessageHandler.sendMessage(player, "Transfer a rank here: http://store.promcgames.com/category/359455");
						}
					} else {
						MessageHandler.sendMessage(player, ChatColor.RED + "You do not have a transferrable rank!");
					}
				} else {
					UUID uuid = AccountHandler.getUUID(arguments[0]);
					if(uuid != null && !DB.PLAYERS_BUYCRAFT_RANK_TRANSFERS.isUUIDSet(uuid)) {
						DB.PLAYERS_BUYCRAFT_RANK_TRANSFERS.insert("'" + uuid.toString() + "'");
					}
				}
				return true;
			}
		}.enableDelay(1);
	}
}
