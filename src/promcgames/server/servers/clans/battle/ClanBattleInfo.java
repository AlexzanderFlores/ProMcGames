package promcgames.server.servers.clans.battle;

public class ClanBattleInfo {
	private int enemyClanID = 0;
	private int result = 0;
	private int playersLeft = 0;
	private int ranked = 0;
	
	public ClanBattleInfo(int enemyClanID, int result, int playersLeft, int ranked) {
		this.enemyClanID = enemyClanID;
		this.result = result;
		this.playersLeft = playersLeft;
		this.ranked = ranked;
	}
	
	public int getEnemyClanID() {
		return enemyClanID;
	}
	
	public int getResult() {
		return result;
	}
	
	public int getPlayersLeft() {
		return playersLeft;
	}
	
	public int getRanked() {
		return ranked;
	}
}