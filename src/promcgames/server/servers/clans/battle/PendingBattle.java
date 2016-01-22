package promcgames.server.servers.clans.battle;

public class PendingBattle {
	private String founder1 = null;
	private String founder2 = null;
	
	public PendingBattle(String founder1, String founder2) {
		this.founder1 = founder1;
		this.founder2 = founder2;
		BattleHandler.add(this);
	}
	
	public String getFounderOne() {
		return founder1;
	}
	
	public String getFounderTwo() {
		return founder2;
	}
	
	public void remove() {
		founder1 = null;
		founder2 = null;
	}
}
