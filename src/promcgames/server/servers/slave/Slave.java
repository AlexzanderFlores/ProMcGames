package promcgames.server.servers.slave;

import promcgames.ProPlugin;
import promcgames.server.networking.Server;

public class Slave extends ProPlugin {
	private static Server server = null;
	
	public Slave() {
		super("Slave");
		server = new Server(4500);
		server.start();
		new Voting();
		new Void();
	}
	
	public static Server getServer() {
		return server;
	}
	
	public static void initServer() {
		server = new Server(4500);
		server.start();
	}
}