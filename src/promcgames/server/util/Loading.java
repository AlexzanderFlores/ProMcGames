package promcgames.server.util;

import promcgames.player.MessageHandler;

public class Loading {
	public Loading(String text) {
		this(text, null);
	}
	
	public Loading(String text, String command) {
		MessageHandler.alert("&eLoading... &b" + text + (command == null ? "" : " (&e" + command + "&b)"));
	}
}
