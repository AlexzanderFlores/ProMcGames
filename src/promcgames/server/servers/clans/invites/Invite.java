package promcgames.server.servers.clans.invites;

public class Invite {
	private String inviter = null;
	private String invitee = null;
	private String clanName = null;
	
	public Invite(String inviter, String invitee, String clanName) {
		this.inviter = inviter;
		this.invitee = invitee;
		this.clanName = clanName;
	}
	
	public String getInviter() {
		return inviter;
	}
	
	public String getInvitee() {
		return invitee;
	}
	
	public String getClanName() {
		return clanName;
	}
}