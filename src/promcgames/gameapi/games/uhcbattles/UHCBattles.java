package promcgames.gameapi.games.uhcbattles;

import promcgames.gameapi.MiniGame;

public class UHCBattles extends MiniGame {
	public UHCBattles() {
		super("UHC Battles");
		setRequiredPlayers(4);
		setUseTop8(true);
		setKillEmeralds(3);
		setWinEmeralds(10);
		new Events();
		new WorldHandler();
	}
}
