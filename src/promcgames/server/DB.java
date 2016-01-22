package promcgames.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import promcgames.server.util.ConfigurationUtil;

public enum DB {
	PLAYERS_ACCOUNTS("uuid VARCHAR(40), name VARCHAR(16), address VARCHAR(40), rank VARCHAR(20), PRIMARY KEY(uuid)"),
	PLAYERS_PLAY_TIME("uuid VARCHAR(40), play_time VARCHAR(25), PRIMARY KEY(uuid)"),
	PLAYERS_MONTHLY_PLAY_TIME("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), play_time VARCHAR(25), PRIMARY KEY(id)"),
	PLAYERS_LOCATIONS("uuid VARCHAR(40), location VARCHAR(100), PRIMARY KEY(uuid)"),
	PLAYERS_KITS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), kit VARCHAR(50), PRIMARY KEY(id)"),
	PLAYERS_COMMUNITY_LEVELS("uuid VARCHAR(40), level INT, PRIMARY KEY(uuid)"),
	PLAYERS_BUYCRAFT_RANK_TRANSFERS("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	PLAYERS_RANK_TRANSFERS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), target VARCHAR(40), PRIMARY KEY(id)"),
	PLAYERS_STAT_RESETS("uuid VARCHAR(40), unlimited BOOL, PRIMARY KEY(uuid)"),
	PLAYERS_EMERALDS_CURRENCY("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_SURVIVAL_GAMES("uuid VARCHAR(40), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_SURVIVAL_GAMES_MONTHLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_STATS_KIT_PVP("uuid VARCHAR(40), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_KIT_PVP_MONTHLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_STATS_VERSUS("uuid VARCHAR(40), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_VERSUS_MONTHLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_VERSUS_STATS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), kit VARCHAR(30), wins INT, losses INT, PRIMARY KEY(id)"),
	PLAYERS_VERSUS_KILLSTREAKS("uuid VARCHAR(40), streak INT, PRIMARY KEY(uuid)"),
	PLAYERS_VERSUS_ELO("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), kit VARCHAR(30), amount INT, PRIMARY KEY(id)"),
	//PLAYERS_VERSUS_HOT_BAR_PASSES("uuid VARCHAR(40), passes INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_CLANS("uuid VARCHAR(40), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(uuid)"),
	PLAYERS_ELO_CLANS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_ARCADE("uuid VARCHAR(40), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_SKY_WARS("uuid VARCHAR(40), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_SKY_WARS_MONTHLY("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(id)"),
	PLAYERS_VOTES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date VARCHAR(10), votes INT, PRIMARY KEY(id)"),
	PLAYERS_DAILY_VOTES("day INT, votes INT, PRIMARY KEY(day)"),
	PLAYERS_DISABLED_MESSAGES("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	PLAYERS_CHAT_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), rank VARCHAR(20), server VARCHAR(25), time VARCHAR(50), message VARCHAR(250), PRIMARY KEY(id)"),
	PLAYERS_SG_AUTO_SPONSORS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_KIT_PVP_AUTO_REGEN("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_DISGUISES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), fakeName VARCHAR(16), PRIMARY KEY(id)"),
	PLAYERS_HUB_SPONSORS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_HUB_PREMIUM_SPONSORS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_BLOCKED_MESSAGES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), server VARCHAR(25), message VARCHAR(250), PRIMARY KEY(id)"),
	PLAYERS_ARROW_TRAILS("uuid VARCHAR(40), particles VARCHAR(15), PRIMARY KEY(uuid)"),
	PLAYERS_NAME_COLORS("uuid VARCHAR(40), color VARCHAR(1), PRIMARY KEY(uuid)"),
	PLAYERS_SG_PARTICLES_SELECTED("uuid VARCHAR(40), particle VARCHAR(15), PRIMARY KEY(uuid)"),
	PLAYERS_SG_PARTICLES_UNLOCKED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), particle VARCHAR(15), PRIMARY KEY(id)"),
	PLAYERS_SKY_WARS_BEAMS_SELECTED("uuid VARCHAR(40), beam VARCHAR(15), PRIMARY KEY(uuid)"),
	PLAYERS_SKY_WARS_BEAMS_UNLOCKED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), beam VARCHAR(15), PRIMARY KEY(id)"),
	PLAYERS_CLANS("uuid VARCHAR(40), clan_id INT, clan_rank VARCHAR(15), battle_wins INT, battle_losses INT, battle_kills INT, battle_deaths INT, PRIMARY KEY(uuid)"),
	PLAYERS_CLANS_INVITES("uuid VARCHAR(40), invitesOn BOOL, PRIMARY KEY(uuid)"),
	PLAYERS_CLANS_BLOCKED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), blocked_clan_id INT, PRIMARY KEY(id)"),
	PLAYERS_CLANS_TOJOIN("id INT NOT NULL AUTO_INCREMENT, uuid varchar(40), server_name VARCHAR(15), PRIMARY KEY(id)"),
	PLAYERS_CLANS_BACKED_UP_STATS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), clan_id INT, battle_wins INT, battle_losses INT, battle_kills INT, battle_deaths INT, PRIMARY KEY(id)"),
	PLAYERS_DISABLED_PARTY_INVITES("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	PLAYERS_ACHIEVEMENTS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), game_name VARCHAR(25), achievement VARCHAR(100), PRIMARY KEY(id)"),
	PLAYERS_FRIENDS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), friend VARCHAR(40), PRIMARY KEY(id)"),
	PLAYERS_FRIEND_REQUESTS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), friend VARCHAR(40), PRIMARY KEY(id)"),
	PLAYERS_IGNORES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), ignored_uuid VARCHAR(40), PRIMARY KEY(id)"),
	PLAYERS_PRO_TRIALS("uuid VARCHAR(40), time_left INT, PRIMARY KEY(uuid)"),
	PLAYERS_KILLSTREAKS("uuid VARCHAR(40), streak INT, PRIMARY KEY(uuid)"),
	PLAYERS_KIT_PVP_LEVELS("uuid VARCHAR(40), level INT, PRIMARY KEY(uuid)"),
	PLAYERS_KIT_PVP_LEVEL_PURCHASES("uuid VARCHAR(40), purchases INT, PRIMARY KEY(uuid)"),
	PLAYERS_TEAMING_WARNINGS("id INT NOT NULL AUTO_INCREMENT, uuid_one VARCHAR(40), uuid_two VARCHAR(40), reason VARCHAR(10), PRIMARY KEY(id)"),
	PLAYERS_TEAMING_KICKS("id INT NOT NULL AUTO_INCREMENT, uuid_one VARCHAR(40), uuid_two VARCHAR(40), reason VARCHAR(10), PRIMARY KEY(id)"),
	PLAYERS_TEAM_BANS("id INT NOT NULL AUTO_INCREMENT, uuid_one VARCHAR(40), uuid_two VARCHAR(40), reason VARCHAR(10), PRIMARY KEY(id)"),
	PLAYERS_STORED_KITS("uuid VARCHAR(40), game_name VARCHAR(25), kit VARCHAR(50), PRIMARY KEY(uuid)"),
	PLAYERS_MAP_VOTE_PASSES("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_UHC_DIAMONDS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_SELECTED_KITS("kit VARCHAR(50), amount INT, PRIMARY KEY(kit)"),
	PLAYERS_FACTIONS_COINS("uuid VARCHAR(40), coins INT, PRIMARY KEY(uuid)"),
	PLAYERS_FACTIONS_SKULLS("id INT NOT NULL AUTO_INCREMENT, name VARCHAR(16), PRIMARY KEY(id)"),
	PLAYERS_FACTIONS_RANKS("uuid VARCHAR(40), rank VARCHAR(10), PRIMARY KEY(uuid)"),
	PLAYERS_FACTIONS_JELLY_LEGS("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	PLAYERS_FACTIONS_VOTING_CRATES("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_FACTIONS_PREMIUM_CRATES("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_FACTIONS_ELITE_CRATES("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_FACTIONS_SPAWNER_CRATES("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_FACTIONS_MAIN_RANK_KITS("uuid VARCHAR(40), day INT, PRIMARY KEY(uuid)"),
	PLAYERS_FACTIONS_RANK_KITS("uuid VARCHAR(40), day INT, PRIMARY KEY(uuid)"),
	PLAYERS_FACTIONS_CREEPER_EGGS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_FACTIONS_POKEBALLS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_SG_EXCLUSIVE_SPONSOR_PASSES("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	PLAYERS_STATS_FACTIONS("uuid VARCHAR(40), wins INT, losses INT, kills INT, deaths INT, PRIMARY KEY(uuid)"),
	
	NETWORK_PROXIES("server VARCHAR(25), PRIMARY KEY(server)"),
	NETWORK_POPULATIONS("server VARCHAR(25), population INT, PRIMARY KEY(server)"),
	NETWORK_COMMAND_DISPATCHER("id INT NOT NULL AUTO_INCREMENT, server VARCHAR(25), command VARCHAR(250), PRIMARY KEY(id)"),
	NETWORK_SERVER_STATUS("id INT NOT NULL AUTO_INCREMENT, game_name VARCHAR(25), server_number INT, listed_priority INT, lore VARCHAR(100), players INT, max_players INT, PRIMARY KEY(id)"),
	NETWORK_SERVER_LIST("data_type VARCHAR(15), data_value VARCHAR(50), PRIMARY KEY(data_type)"),
	NETWORK_SG_CHESTS("id INT NOT NULL AUTO_INCREMENT, map_name VARCHAR(50), location VARCHAR(20), times_opened INT, PRIMARY KEY(id)"),
	NETWORK_MINI_GAME_PERFORMANCE("id INT NOT NULL AUTO_INCREMENT, server VARCHAR(25), map VARCHAR(250), maxPlayers INT, maxMemory DOUBLE, maxMemoryTime VARCHAR(50), lowestTPS DOUBLE, PRIMARY KEY(id)"),
	NETWORK_RECENT_PURCHASES("id INT NOT NULL AUTO_INCREMENT, text VARCHAR(250), PRIMARY KEY(id)"),
	NETWORK_ANTI_CHEAT_TESTING("id INT NOT NULL AUTO_INCREMENT, text VARCHAR(200), PRIMARY KEY(id)"),
	NETWORK_MAP_VOTES("id INT NOT NULL AUTO_INCREMENT, game_name VARCHAR(25), map VARCHAR(250), times_voted INT, PRIMARY KEY(id)"),
	NETWORK_CLANS("id INT NOT NULL AUTO_INCREMENT, clan_name VARCHAR(15), last_name_change INT, status VARCHAR(11), invite_perm VARCHAR(15), battle_wins INT, battle_losses INT, color VARCHAR(20), PRIMARY KEY(id)"),
	NETWORK_CLANS_LOGOS("clan_id INT, path VARCHAR(35), PRIMARY KEY(clan_id)"),
	NETWORK_CLANS_PENDING_LOGOS("clan_id INT, path VARCHAR(35), PRIMARY KEY(clan_id)"),
	NETWORK_CLANS_BATTLES("id INT NOT NULL AUTO_INCREMENT, clan_one_id INT, clan_two_id INT, clan_one_score INT, clan_two_score INT, PRIMARY KEY(id)"),
	NETWORK_ClANS_BATTLE_HISTORY("id INT NOT NULL AUTO_INCREMENT, clan_id INT, enemy_clan_id INT, result VARCHAR(5), players_left INT, ranked INT, PRIMARY KEY(id)"),
	NETWORK_CLANS_SETUP("server_name VARCHAR(10), setup_phase VARCHAR(10), player1 VARCHAR(50), player2 VARCHAR(50), PRIMARY KEY(server_name)"),
	NETWORK_CLANS_NAME_CHANGES("id INT NOT NULL AUTO_INCREMENT, og_clan_name VARCHAR(15), new_clan_name VARCHAR(15), uuid VARCHAR(40), PRIMARY KEY(id)"),
	NETWORK_CLANS_DISBANDED("id INT, clan_name VARCHAR(15), PRIMARY KEY(id)"),
	NETWORK_UHC_HOSTS("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	NETWORK_OMN_HOSTS("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	NETWORK_GAME_NIGHT_HOSTS("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	NETWORK_AUTO_ALERTS("text_id VARCHAR(25), how_often INT, text VARCHAR(100), PRIMARY KEY(text_id)"),
	NETWORK_POLL_VOTES("uuid VARCHAR(40), vote VARCHAR(100), PRIMARY KEY(uuid)"),
	NETWORK_PARTIES("id INT NOT NULL AUTO_INCREMENT, leader_uuid VARCHAR(40), PRIMARY KEY(id)"),
	NETWORK_PLAYER_PARTIES("uuid VARCHAR(40), party_id INT, PRIMARY KEY(uuid)"),
	NETWORK_BUKKIT_COMMAND_DISPATCHER("id INT NOT NULL AUTO_INCREMENT, server VARCHAR(25), command VARCHAR(250), PRIMARY KEY(id)"),
	NETWORK_UHC_KICKS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), kicked_uuid VARCHAR(40), reason VARCHAR(100), PRIMARY KEY(id)"),
	NETWORK_UHC_KILLS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), killed_uuid VARCHAR(40), reason VARCHAR(100), PRIMARY KEY(id)"),
	NETWORK_VERSUS_TOURNAMENT_WINS("id INT NOT NULL AUTO_INCREMENT, winner_uuid VARCHAR(40), date VARCHAR(20), PRIMARY KEY(id)"),
	
	HUB_ARMOR("uuid VARCHAR(40), armor_type VARCHAR(20), PRIMARY KEY(uuid)"),
	HUB_PETS("uuid VARCHAR(40), pet_type VARCHAR(20), PRIMARY KEY(uuid)"),
	HUB_PARTICLES("uuid VARCHAR(40), particle_type VARCHAR(20), PRIMARY KEY(uuid)"),
	HUB_SPIRAL_PARTICLES("uuid VARCHAR(40), particle_type VARCHAR(20), PRIMARY KEY(uuid)"),
	HUB_HATS("uuid VARCHAR(40), hat VARCHAR(10), PRIMARY KEY(uuid)"),
	HUB_PARKOUR("uuid VARCHAR(40), check_point VARCHAR(200), PRIMARY KEY(uuid)"),
	//HUB_PARKOUR_CHECKPOINTS("uuid VARCHAR(40), check_point VARCHAR(200), PRIMARY KEY(uuid)"),
	HUB_PARKOUR_FREE_CHECKPOINTS("uuid VARCHAR(40), amount INT, PRIMARY KEY(uuid)"),
	HUB_PARKOUR_TIMES("uuid VARCHAR(40), seconds INT, PRIMARY KEY(uuid)"),
	HUB_SPONSOR_LOGS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), reward VARCHAR(100), PRIMARY KEY(id)"),
	HUB_GIFT_LOGS("uuid VARCHAR(40), week INT, PRIMARY KEY(uuid)"),
	HUB_DAILY_GIFT_LOGS("uuid VARCHAR(40), day INT, PRIMARY KEY(uuid)"),
	HUB_PLAYERS_HIDDEN("uuid VARCHAR(40), enabled INT, PRIMARY KEY(uuid)"),
	HUB_SNOWBALL_BANNED("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	
	STAFF_ONLINE("uuid VARCHAR(40), server VARCHAR(100), PRIMARY KEY(uuid)"),
	STAFF_SHADOW_MUTES("uuid VARCHAR(40), PRIMARY KEY(uuid)"),
	STAFF_CHAT("id INT NOT NULL AUTO_INCREMENT, command VARCHAR(250), PRIMARY KEY(id)"),
	STAFF_KICKS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), staff_uuid VARCHAR(40), reason VARCHAR(100), proof VARCHAR(100), date VARCHAR(10), time VARCHAR(25), PRIMARY KEY(id)"),
	STAFF_DELETED_KICKS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), staff_uuid VARCHAR(40), reason VARCHAR(100), proof VARCHAR(100), date VARCHAR(10), time VARCHAR(25), PRIMARY KEY(id)"),
	STAFF_MUTES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), staff_uuid VARCHAR(40), address VARCHAR(40), reason VARCHAR(100), proof VARCHAR(100), date VARCHAR(10), time VARCHAR(25), expires VARCHAR(25), PRIMARY KEY(id)"),
	STAFF_BANS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), staff_uuid VARCHAR(40), address VARCHAR(40), reason VARCHAR(100), proof VARCHAR(100), date VARCHAR(10), time VARCHAR(25), PRIMARY KEY(id)"),
	STAFF_BAN("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), attached_uuid VARCHAR(40), staff_uuid VARCHAR(40), who_unbanned VARCHAR(40), reason VARCHAR(100), date VARCHAR(10), time VARCHAR(25), unban_date VARCHAR(10), unban_time VARCHAR(25), day INT, active INT, PRIMARY KEY(id)"),
	STAFF_UNMUTES("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), staff_uuid VARCHAR(40), reason VARCHAR(100), date VARCHAR(10), time VARCHAR(25), PRIMARY KEY(id)"),
	STAFF_UNBANS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), staff_uuid VARCHAR(40), reason VARCHAR(100), date VARCHAR(10), time VARCHAR(25), PRIMARY KEY(id)"),
	STAFF_TICKETS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), reported_uuid VARCHAR(40), staff_uuid VARCHAR(40), reason VARCHAR(100), reason_closed VARCHAR(30), comments VARCHAR(100), play_time VARCHAR(40), proof VARCHAR(100), time_opened VARCHAR(25), date_closed VARCHAR(10), time_closed VARCHAR(25), opened BOOL, PRIMARY KEY(id)"),
	STAFF_TICKETS_CLOSED("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), date_closed VARCHAR(10), amount INT, PRIMARY KEY(id)"),
	STAFF_COMMANDS("id INT NOT NULL AUTO_INCREMENT, uuid VARCHAR(40), time VARCHAR(25), command VARCHAR(250), PRIMARY KEY(id)");
	
	private String table = null;
	private String keys = "";
	private Databases database = null;
	
	private DB(String query) {
		String databaseName = toString().split("_")[0];
		database = Databases.valueOf(databaseName);
		table = toString().replace(databaseName, "");
		table = table.substring(1, table.length()).toLowerCase();
		String [] declarations = query.split(", ");
		for(int a = 0; a < declarations.length - 1; ++a) {
			String declaration = declarations[a].split(" ")[0];
			if(!declaration.equals("id")) {
				keys += "`" + declaration + "`, ";
			}
		}
		keys = keys.substring(0, keys.length() - 2); 
		database.connect();
		try {
			if(database.getConnection() != null) {
				database.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " (" + query + ")").execute();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return table;
	}
	
	public Connection getConnection() {
		return this.database.getConnection();
	}
	
	public boolean isKeySet(String key, String value) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT COUNT(" + key + ") FROM " + getName() + " WHERE " + key + " = '" + value + "' LIMIT 1");
			resultSet = statement.executeQuery();
			return resultSet.next() && resultSet.getInt(1) > 0;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return false;
	}
	
	public boolean isKeySet(String [] keys, String [] values) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT COUNT(" + keys[0] + ") FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query + " LIMIT 1");
			resultSet = statement.executeQuery();
			return resultSet.next() && resultSet.getInt(1) > 0;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return false;
	}
	
	public boolean isUUIDSet(UUID uuid) {
		return isUUIDSet("uuid", uuid);
	}
	
	public boolean isUUIDSet(String key, UUID uuid) {
		return isKeySet(key, uuid.toString());
	}
	
	public int getInt(String key, String value, String requested) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " WHERE " + key + " = '" + value + "' LIMIT 1");
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(requested);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public int getInt(String [] keys, String [] values, String requested) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + requested + " FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(requested);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public void updateInt(String set, int update, String key, String value) {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public void updateInt(String set, int update, String [] keys, String [] values) {
		PreparedStatement statement = null;
		try {
			String query = "UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 0; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public void updateBoolean(String set, boolean update, String key, String value) {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("UPDATE " + getName() + " SET " + set + " = '" + (update ? "1" : "0") + "' WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public String getString(String key, String value, String requested) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " WHERE " + key + " = '" + value + "' LIMIT 1");
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getString(requested);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public String getString(String [] keys, String [] values, String requested) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + requested + " FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query + " LIMIT 1");
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getString(requested);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public List<String> getAllStrings(String colum) {
		return getAllStrings(colum, null, null);
	}
	
	public List<String> getAllStrings(String colum, String key, String value) {
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT " + colum + " FROM " + getName();
			if(key != null && value != null) {
				query += " WHERE " + key + " = '" + value + "'";
			}
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(colum));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return results;
	}
	
	public void updateString(String set, String update, String key, String value) {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public void updateString(String set, String update, String [] keys, String [] values) {
		PreparedStatement statement = null;
		try {
			String query = "UPDATE " + getName() + " SET " + set + " = '" + update + "' WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public boolean getBoolean(String key, String value, String requested) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " WHERE " + key + " = '" + value + "'");
			resultSet = statement.executeQuery();
			return resultSet.next() && resultSet.getBoolean(requested);
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return false;
	}
	
	public int getSize() {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT COUNT(*) FROM " + getName());
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public int getSize(String key, String value) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			statement = getConnection().prepareStatement("SELECT COUNT(" + key + ") FROM " + getName() + " WHERE " + key + " = '" + value + "'");
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public int getSize(String [] keys, String [] values) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String query = "SELECT COUNT(" + keys[0] + ") FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			resultSet = statement.executeQuery();
			if(resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return 0;
	}
	
	public List<String> getOrdered(String orderBy, String requested, String key, String value, long limit) {
		return getOrdered(orderBy, requested, key, value, limit, false);
	}
	
	public List<String> getOrdered(String orderBy, String requested, String key, String value, long limit, boolean descending) {
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String desc = descending ? " DESC " : " ASC ";
			String max = limit > 0 ? " LIMIT " + limit : "";
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " WHERE " + key + " = '" + value + "' ORDER BY " + orderBy + desc + max);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(requested));
			}
			return results;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public List<String> getOrdered(String orderBy, String requested, int limit) {
		return getOrdered(orderBy, requested, limit, false);
	}
	
	public List<String> getOrdered(String orderBy, String requested, int limit, boolean descending) {
		List<String> results = new ArrayList<String>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String desc = descending ? " DESC " : " ASC ";
			String max = limit > 0 ? " LIMIT " + limit : "";
			statement = getConnection().prepareStatement("SELECT " + requested + " FROM " + getName() + " ORDER BY " + orderBy + desc + max);
			resultSet = statement.executeQuery();
			while(resultSet.next()) {
				results.add(resultSet.getString(requested));
			}
			return results;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement, resultSet);
		}
		return null;
	}
	
	public void delete(String key, String value) {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("DELETE FROM " + getName() + " WHERE " + key + " = '" + value + "'");
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public void delete(String [] keys, String [] values) {
		PreparedStatement statement = null;
		try {
			String query = "DELETE FROM " + getName() + " WHERE " + keys[0] + " = '" + values[0] + "'";
			for(int a = 1; a < keys.length; ++a) {
				query += " AND " + keys[a] + " = '" + values[a] + "'";
			}
			statement = getConnection().prepareStatement(query);
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public void deleteUUID(UUID uuid) {
		deleteUUID("uuid", uuid);
	}
	
	public void deleteUUID(String key, UUID uuid) {
		delete(key, uuid.toString());
	}
	
	public void deleteAll() {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("DELETE FROM " + getName());
			statement.execute();
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
	}
	
	public boolean insert(String values) {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement("INSERT INTO " + getName() + " (" + keys + ") VALUES (" + values + ")");
			statement.execute();
			return true;
		} catch(SQLException e) {
			if(!e.getMessage().startsWith("Duplicate entry")) {
				e.printStackTrace();
			}
		} finally {
			close(statement);
		}
		return false;
	}
	
	public boolean execute(String sql) {
		PreparedStatement statement = null;
		try {
			statement = getConnection().prepareStatement(sql);
			statement.execute();
			return true;
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			close(statement);
		}
		return false;
	}
	
	public static void close(PreparedStatement statement, ResultSet resultSet) {
		close(statement);
		close(resultSet);
	}
	
	public static void close(PreparedStatement statement) {
		try {
			if(statement != null) {
				statement.close();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void close(ResultSet resultSet) {
		try {
			if(resultSet != null) {
				resultSet.close();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public enum Databases {
		PLAYERS, NETWORK, HUB, STAFF;
		
		private Connection connection = null;
		
		public void connect() {
			try {
				if(connection == null || connection.isClosed()) {
					ConfigurationUtil config = new ConfigurationUtil(Bukkit.getWorldContainer().getPath() + "/../db.yml");
					String address = config.getConfig().getString("address");
					String user = config.getConfig().getString("user");
					String password = config.getConfig().getString("password");
					connection = DriverManager.getConnection("jdbc:mysql://" + address + ":3306/" + "pro_mc_games_" + toString().toLowerCase(), user, password);
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		public Connection getConnection() {
			return this.connection;
		}
		
		public void disconnect() {
			if(connection != null) {
				try {
					connection.close();
				} catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
