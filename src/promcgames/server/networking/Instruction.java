package promcgames.server.networking;

import java.io.Serializable;

public class Instruction implements Serializable {
	// Set instructions.
	public static enum Inst {
		CLIENT_SHUTDOWN, // message from client to server saying that the client is shutting down
		CLIENT_FORCESHUTDOWN, // message from server to client to force client to shutdown
		SERVER_SHUTDOWN, // message from server to all clients saying that the server is shutting down
		CLIENT_INIT, // message from client to server initializing the server details
		SERVER_SENDTOALL, // message from client to server to send an instruction to all clients (including self)
		CLIENT_COMMAND, // message from server to client to dispatch a command
		SERVER_SENDTOCLIENT, // message from client to server to send an instruction to a specific client (server specified)
		SERVER_SENDTOGROUP // message from client to server to send an instruction to all clients of a specific group (example: SG)
	}

	private static final long serialVersionUID = -785604691741278616L;
	private String [] data = null;
	
	public Instruction(String[] data) {
		this.data = data;
	}
	
	public String [] getData() {
		return data;
	}
}