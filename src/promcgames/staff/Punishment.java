package promcgames.staff;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import promcgames.ProPlugin;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.AutoBroadcasts;
import promcgames.server.ChatClickHandler;
import promcgames.server.CommandBase;
import promcgames.server.DB;
import promcgames.server.util.EventUtil;
import promcgames.staff.ban.BanHandler;
import promcgames.staff.ban.BanHandler.Violations;
import promcgames.staff.ban.UnBanHandler;
import promcgames.staff.kick.KickHandler;
import promcgames.staff.kick.UndoKick;
import promcgames.staff.mute.MuteHandler;
import promcgames.staff.mute.ServerMute;
import promcgames.staff.mute.ShadowMuteHandler;
import promcgames.staff.mute.UnMuteHandler;

public class Punishment implements Listener {
	public enum ChatViolations {
		DISRESPECT,
		RACISM,
		DEATH_COMMENTS,
		INAPPROPRIATE_COMMENTS,
		SPAMMING,
		SOCIAL_MEDIA_ADVERTISEMENT,
		WEBSITE_ADVERTISEMENT,
		SERVER_ADVERTISEMENT,
		HACKUSATIONS,
		DDOS_THREATS,
	}
	
	public class PunishmentExecuteReuslts {
		private boolean valid = false;
		private UUID uuid = null;
		
		public PunishmentExecuteReuslts(boolean valid, UUID uuid) {
			this.valid = valid;
			this.uuid = uuid;
		}
		
		public boolean isValid() {
			return this.valid;
		}
		
		public UUID getUUID() {
			return this.uuid;
		}
	}
	
	public static final String appeal = "https://promcgames.com/forum/view_forum/?fid=12";
	private String name = null;
	
	public Punishment() {
		new BanHandler();
		new UnBanHandler();
		new KickHandler();
		new MuteHandler();
		new UnMuteHandler();
		new SpamPrevention();
		new StaffChat();
		new TicketHandler();
		new UndoKick();
		new LogViewing();
		new ServerMute();
		new ViolationPrevention();
		new StaffPager();
		new CommandLogger();
		new ShadowMuteHandler();
		new DataChecker();
		AutoBroadcasts.addAlert("Want to be staff? &b/apply &c+ &b/canApply");
		new CommandBase("clearChat") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				for(int a = 0; a < 100; ++a) {
					MessageHandler.alert(" ");
				}
				MessageHandler.alert("Chat has been cleared by " + AccountHandler.getPrefix(sender));
				return true;
			}
		}.setRequiredRank(Ranks.SENIOR_MODERATOR);
		new CommandBase("clearStaff") {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				int counter = 0;
				for(String uuid : DB.STAFF_ONLINE.getAllStrings("uuid")) {
					DB.STAFF_ONLINE.delete("uuid", uuid);
					++counter;
				}
				MessageHandler.sendMessage(sender,"Deleted &e" + counter + " &alogged staff");
				return true;
			}
		}.setRequiredRank(Ranks.DEV);
	}
	
	public Punishment(String name) {
		this.name = name;
		EventUtil.register(this);
	}
	
	protected String getName() {
		return this.name;
	}
	
	protected String getReason(Ranks rank, String [] arguments, String reason, PunishmentExecuteReuslts result) {
		return getReason(rank, arguments, reason, result, false);
	}
	
	protected String getReason(Ranks rank, String [] arguments, String reason, PunishmentExecuteReuslts result, boolean reversingPunishment) {
		Ranks playerRank = AccountHandler.getRank(result.getUUID());
		String proof = "";
		if(!(reversingPunishment || arguments.length == 1 || arguments.length == 2 || !reason.equals(Violations.HACKING.toString()))) {
			proof = " " + ChatColor.DARK_GREEN + arguments[2];
		}
		String message = ChatColor.WHITE + playerRank.getPrefix() + arguments[0] + ChatColor.WHITE + " was " + getName();
		if(reason != null && !reason.equals("")) {
			message += ": " + ChatColor.RED + reason;
		}
		if(proof != null && !proof.equals("")) {
			message += proof;
		}
		return message;
	}
	
	protected PunishmentExecuteReuslts executePunishment(CommandSender sender, String [] arguments, boolean reversingPunishment) {
		if(arguments.length == 3 && arguments[2].toLowerCase().contains("gyazo")) {
			MessageHandler.sendMessage(sender, "&cGyazo links are not allowed for proof");
		} else {
			UUID uuid = null;
			Player player = ProPlugin.getPlayer(arguments[0]);
			if(player == null) {
				uuid = AccountHandler.getUUID(arguments[0]);
			} else {
				uuid = Disguise.getUUID(player);
			}
			if(uuid == null) {
				MessageHandler.sendMessage(sender, "&c" + arguments[0] + " has never logged in before");
			} else {
				if(Bukkit.getPlayer(uuid) == null && DB.PLAYERS_LOCATIONS.isUUIDSet(uuid)) {
					MessageHandler.sendMessage(sender, "&cCannot punish! Player is online on another server!");
					String text = DB.PLAYERS_LOCATIONS.getString("uuid", uuid.toString(), "location");
					if(sender instanceof Player) {
						Player staff = (Player) sender;
						String server = text.split(ChatColor.RED.toString())[1];
						ChatClickHandler.sendMessageToRunCommand(staff, " &c&lCLICK TO JOIN", "Click to teleport to " + server, "/join " + server, text);
					} else {
						MessageHandler.sendMessage(sender, text);
					}
				} else {
					return new PunishmentExecuteReuslts(true, uuid);
				}
			}
		}
		return new PunishmentExecuteReuslts(false, null);
	}
}
