package promcgames.server.networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.networking.Instruction.Inst;

public class Client {
	
	private String ip = null;
	private int port = 0;
	private int timeout = 5000; // connection timeout in milliseconds
	private static int attemptInterval = 15000;
	private static boolean enabled = false;
	private Socket socket = null;
	
	public Client(String ip, int port) {
		this.ip = ip;
		this.port = port;
		if(!enabled) {
			enabled = true;
			new CommandBase("connection") {
				@Override
				public boolean execute(CommandSender sender, String[] arguments) {
					if(sender instanceof Player) {
						Player player = (Player) sender;
						if(!Ranks.DEV.hasRank(player)) {
							MessageHandler.sendUnknownCommand(player);
							return true;
						}
					}
					if(arguments.length == 0 || arguments[0].equalsIgnoreCase("help")) {
						MessageHandler.sendMessage(sender, "/connection connect");
						MessageHandler.sendMessage(sender, "/connection disconnect");
						MessageHandler.sendMessage(sender, "/connection status");
					} else if(arguments[0].equalsIgnoreCase("connect")) {
						Client client = ProMcGames.getClient();
						if(client.isSocketValid()) {
							MessageHandler.sendMessage(sender, "&cConnection already valid");
						} else {
							client.start();
							MessageHandler.sendMessage(sender, "Connection starting");
						}
					} else if(arguments[0].equalsIgnoreCase("disconnect")) {
						Client client = ProMcGames.getClient();
						if(client.isSocketValid()) {
							client.shutdown(true);
							MessageHandler.sendMessage(sender, "Connection ending");
						} else {
							MessageHandler.sendMessage(sender, "&cConnection already invalid");
						}
					} else if(arguments[0].equalsIgnoreCase("status")) {
						
					} else {
						MessageHandler.sendMessage(sender, "&cIncorrect argument &e/connection help");
					}
					return true;
				}
			};
		}
	}
	
	public Client(String ip, int port, int timeout) {
		this(ip, port);
		this.timeout = timeout;
	}
	
	/*
	 * Starts the client connection to the server
	 * Will attempt to connect to the server
	 */
	public void start() {
		new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						socket = new Socket();
						Bukkit.getLogger().info("Attempting to connect to server...");
						socket.connect(new InetSocketAddress(ip, port), timeout);
						Bukkit.getLogger().info("Connected!");
						List<String> groups = ProPlugin.getGroups();
						if(groups == null || groups.isEmpty()) {
							sendMessageToServer(new Instruction(new String [] {Inst.CLIENT_INIT.toString(), ProMcGames.getServerName()}));
						} else {
							int size = groups.size() + 2;
							String [] inst = new String [size];
							inst[0] = Inst.CLIENT_INIT.toString();
							inst[1] = ProMcGames.getServerName();
							int counter = 2;
							for(String group : groups) {
								inst[counter++] = group;
							}
							sendMessageToServer(new Instruction(inst));
						}
						listen();
						break;
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(attemptInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	private void listen() {
		new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
						Object object = in.readObject();
						if(object instanceof Instruction) {
							Instruction inst = (Instruction) object;
							// If true, the connection to the server will be terminated.
							if(inst.getData()[0].equalsIgnoreCase(Inst.SERVER_SHUTDOWN.toString()) || inst.getData()[0].equalsIgnoreCase(Inst.CLIENT_FORCESHUTDOWN.toString())) {
								shutdown(false);
								start();
								break;
							} else {
								process(inst);
							}
						}
					} catch (IOException e) {
						if(!e.getMessage().equalsIgnoreCase("Socket closed")) {
							e.printStackTrace();
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	public void shutdown(boolean sendMessage) {
		if(isSocketValid()) {
			if(sendMessage) {
				sendMessageToServer(new Instruction(new String []{Inst.CLIENT_SHUTDOWN.toString()}));
			}
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		socket = null;
	}
	
	public void process(Instruction inst) {
		if(inst.getData()[0].equalsIgnoreCase(Inst.CLIENT_COMMAND.toString())) {
			if(inst.getData().length == 2) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), inst.getData()[1]);
			}
		}
	}
	
	public void sendMessageToServer(Instruction inst) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(inst);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public boolean isSocketValid() {
		try {
			return socket != null && !socket.isClosed() && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown();
		} catch(NullPointerException e) {
			return false;
		}
	}
	
	public String getIP() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
}