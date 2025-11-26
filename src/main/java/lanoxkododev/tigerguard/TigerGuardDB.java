package lanoxkododev.tigerguard;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;
import org.mariadb.jdbc.Connection;
import org.mariadb.jdbc.DatabaseMetaData;

import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.MessageFactory;
import lanoxkododev.tigerguard.polls.TigerPolls;
import lanoxkododev.tigerguard.time.TimeDates;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class TigerGuardDB {

	MessageFactory messageFactory = new MessageFactory();
	TimeDates dates = new TimeDates();
	TigerLogs logger = new TigerLogs();
	public static TigerGuardDB tigerGuardDB;
	public static int maxLevel = 40;
	protected static int[] levelExpCaps = new int[maxLevel]; /**@see TigerGuardDB#setupRankChartList() for implementation details.*/

	static Connection connection;
	final String db;

	/*
	 * ##################
	 * CONNECTION RELATED
	 * ##################
	 */
	public TigerGuardDB(String address, String databaseName, String databaseUser, String databasePass)
	{
		tigerGuardDB = this;
		db = databaseName + ".";
		initConnection(address, databaseName, databaseUser, databasePass);
		setupRankChartList();
	}
	
	/**
	 * Simple boolean returning method for if the first number provided or equal to or greater than the second number.
	 * @param num1
	 * @param num2
	 * @return
	 */
	private boolean meetsLevelUp(int num1, int num2)
	{
		if (num1 >= num2) return true;
		else return false;
	}
	
	/**
	 * Function for correcting any mis-aligned data based on levelExpCaps and the member's xp value.
	 * Generally not expected to be used whatsoever unless a bug in the level system is detected.
	 * Thus no calls are made to this method during normal operation but left as part of internal testing
	 * when things appear to have gone awry at somepoint.
	 * 
	 * @param guild - the guild being iterated on.
	 */
	public void vetUserLevels(Guild guild)
	{
		String guildMesh = guild.getIdLong() + "xp";
		String dbQuery = String.format("SELECT * FROM %s%s;", db, guildMesh);
		ResultSet rs = performQuery(dbQuery);
		
		String memberID = "";
		int memberLevel = 0;
		int memberXP = 0;
		
		try
		{
			logger.debug("Verifying XP and Level data.");
			while (rs.next())
			{
				memberID = rs.getString("member");
				memberLevel = rs.getInt("level");
				memberXP = rs.getInt("xp");
				
				if (memberXP != 0)
				{
					int currentLevel = 1;
					
					for (int a = 0; a < levelExpCaps.length; a++)
					{
						int levelUpAtLevel = levelExpCaps[a];
						
						if (meetsLevelUp(memberXP, levelUpAtLevel)) currentLevel++;
						else
						{
							if (memberLevel != currentLevel)
							{
								String statement = "UPDATE " + db + guildMesh + " SET level = " + currentLevel + " WHERE member = " + memberID + ";";
								performUpdate(statement, LogType.DATABASE_ERROR);
								break;
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.RANK_ERROR, dbQuery, "Error during verification of xp to level data.", e);
		}

		ResultSet rs2 = performQuery(dbQuery);
		try
		{
			logger.debug("Beginning verification of level roles per member to ensure they all are correct.");
			while (rs2.next())
			{
				memberID = rs2.getString("member");
				memberLevel = rs2.getInt("level");

				LevelRoleProgressionHandler(guild, guild.getMemberById(Long.parseLong(memberID)), memberLevel);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.RANK_ERROR, dbQuery, "Error trying to verify and reprovision level roles.", e);
		}
		
		logger.debug("All processes for vetting have concluded.");
	}

	protected void initConnection(String address, String databaseName, String databaseUser, String databasePass)
	{
		logger.log(LogType.INFO, "Attempting Database login with credentials from TigerGuardConfig.txt");
		try
		{
			connection = (Connection) DriverManager.getConnection(String.format("jdbc:mariadb://%s/", address) + databaseName, databaseUser, databasePass);
		}
		catch (SQLException e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error signing into database with TigerGuard credentials.", null, e);
		}
		logger.log(LogType.INFO, "Database login successful!");
	}

	protected void closeConnection()
	{
		try
		{
			connection.commit();
			connection.close();
			logger.log(LogType.INFO, "Database connection closed.");
		}
		catch (SQLException e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error closing connection.", null, e);
		}
	}

	public static TigerGuardDB getTigerGuardDB()
	{
		return tigerGuardDB;
	}

	/*
	 * ##################
	 * PERFORM STATEMENTS
	 * ##################
	 */

	/**
	 * Method to handle all update events. Replaces {@link #prepare(String)}
	 *
	 * @param statement	- The statement to perform
	 * @param type		- The LogType that the error might raise during a failure
	 */
	private void performUpdate(String statement, LogType type)
	{
		try
		{
			connection.prepareStatement(statement).executeUpdate();
		}
		catch (Exception e)
		{
			logger.logErr(type, "Failure updating value in database", statement, e);
		}
	}

	/**
	 * Method to query data from the database. Replaces {{@link #prepare(String)}
	 *
	 * @param query	- The statement used for the query process.
	 * @return		- The PreparedStatement result
	 */
	private ResultSet performQuery(String query)
	{
		ResultSet queryResult = null;

		try
		{
			queryResult = connection.prepareStatement(query).executeQuery();
		}
		catch (SQLException e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Could not complete the query with the provided statement", query, e);
		}

		return queryResult;
	}

	/**
	 * Method to handle batch execution for the passed prepared statement
	 *
	 * @param ps - The PreparedStatement object
	 */
	private void performBatch(PreparedStatement ps)
	{
		try
		{
			ps.executeBatch();
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error executing batch with the provided statement", ps.toString(), e);
		}
	}

	private PreparedStatement perform(String statement)
	{
		PreparedStatement ps = null;

		try
		{
			ps = connection.prepareStatement(statement);
		}
		catch (SQLException e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Could not create the PreparedStatment with the provided statement", statement, e);
		}

		return ps;
	}
	
	/**
	 * Check if the provided table exists in the database.
	 *
	 * @param table - title of the table to be found.
	 */
	public boolean checkForTable(String table)
	{
		boolean found = false;

		try
		{
			DatabaseMetaData data = connection.getMetaData();
			ResultSet rs = data.getTables(null, null, table, null);

			while (rs.next())
		    {
		    	found = true;
		    	break;
		    }
		}
		catch (SQLException e)
		{
			logger.logErr(LogType.DATABASE_WARNING, "Error occurred while checking if the table " + table + " exists in the database", null, e);
		}

		return found;
	}
	
	/**
	 * Basic boolean returning method.
	 *
	 * @param table		 - The table to search within.
	 * @param column	 - The column that the return would be in
	 * @param where		 - The where clause constraint.
	 * @param whereInput - The where clause's condition.
	 * @return
	 */
	public boolean checkIfValueExists(String table, String column, String where, Object whereVal)
	{
		boolean found = false;
		String statement = "SELECT EXISTS(SELECT " + column + " FROM " + db + table + " WHERE `" + where + "` = '" + whereVal + "') AS EXISTS_BY_NAME;";
		ResultSet rs = performQuery(statement);

		try
		{
			found = rs.next();
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error encountered while checking table for a value", statement, e);
		}

		return found;
	}
	
	/**
	 * Basic boolean returning method.
	 *
	 * @param table		- The table to search within
	 * @param column	- The column that the return would be in
	 * @param whereColA	- The first where clause constraint
	 * @param whereValA - The first where clause's condition
	 * @param whereColA	- The second where clause constraint
	 * @param whereValA - The second where clause's condition
	 * @return
	 */
	public boolean checkIfValueExists(String table, String column, String whereColA, Object whereValA, String whereColB, Object whereValB)
	{
		boolean found = false;
		String statement = "SELECT EXISTS(SELECT " + column + " FROM " + db + table + " WHERE `" + whereColA + "` = '" + whereValA + "' AND `" + whereColB + "` = '" + whereValB + "') AS EXISTS_BY_NAME;";
		
		try
		{
			found = performQuery(statement).next();
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error encountered while checking table for a value", statement, e);
		}
		
		return found;
	}

	public Integer countRows(String table)
	{
		int count = 0;
		String statement = "SELECT COUNT(*) FROM " + db + table + ";";

		try
		{
			ResultSet query = performQuery(statement);

			while (query.next())
			{
				count = query.getInt(1);
			}
		}
		catch (SQLException e)
		{
			logger.logErr(LogType.DATABASE_WARNING, "Failure to grab row count from table " + table, statement, e);
		}

		return count;
	}

	/**
	 * Method for creating a table in the DB.
	 *
	 * @param statement	- The statement for creating the table.
	 */
	public void createTable(String statement)
	{
		performUpdate("CREATE TABLE " + db + statement, LogType.DATABASE_ERROR);
	}

	/**
	 * Check if the specified row is found in the specified table in the DB with a specifier in the where clause.
	 *
	 * @param table		 - The table to search in.
	 * @param column	 - The column to search by.
	 * @param searchItem - The data the column should have if it exists.
	 * @return
	 */
	public boolean checkRow(String table, String column, Object searchItem)
	{
		if (!checkForTable(table)) return false;
		
		boolean found = false;
		String statement = String.format("SELECT EXISTS(SELECT * FROM " + db + "%s WHERE %s = %s) AS EXISTS_BY_NAME", table, column, searchItem);

		try
		{
			ResultSet rs = performQuery(statement);
			while (rs.next())
			{
				found = rs.getBoolean(1);
				break;
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_WARNING, "Failure attempting to find row in the database", statement, e);
		}

		return found;
	}

	/**
	 * Search the database by table and column.
	 *
	 * @param table  - The table to look for/within.
	 * @param column - The column to look for inside the table.
	 * @return
	 */
	public boolean checkColumn(String table, String column)
	{
		boolean found = false;

		try
		{
			found = connection.getMetaData().getColumns(null, null, table, column).first();
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_WARNING, "Failure finding column using the inputs of " + table + ", and " + column, null, e);
		}

		return found;
	}

	/*
	 * ################
	 * CORE GET METHODS
	 * ################
	 */
	
	/**
	 * Get sought value as Booealn.
	 * 
	 * @param table		- The table to search within
	 * @param column	- The column that the value would be in
	 * @param where		- The where clause constraint
	 * @param whereVal	- The where clause's condition
	 * @return	- Boolean result
	 */
	public Boolean getValueBoolean(String table, String column, String where, Object whereVal)
	{
		String statement = "SELECT `" + column + "` FROM " + db + table + " WHERE `" + where + "` = '" + whereVal + "';";
		
		try
		{
			ResultSet rs = performQuery(statement);
			while (rs.next()) return rs.getBoolean(1);
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure executing statement", statement, e);
		}
		
		return false;
	}
	
	/**
	 * Get sought value as Integer.
	 * 
	 * @param table		- The table to search within
	 * @param column	- The column that the value would be in
	 * @param where		- The where clause constraint
	 * @param whereVal	- The where clause's condition
	 * @return	- Integer result
	 */
	public Integer getValueInteger(String table, String column, String where, Object whereVal)
	{
		String statement = "SELECT `" + column + "` FROM " + db + table + " WHERE `" + where + "` = '" + whereVal + "';";
		
		try
		{
			ResultSet rs = performQuery(statement);
			while (rs.next()) return rs.getInt(1);
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure executing statement", statement, e);
		}
		
		return null;
	}
	
	/**
	 * Get sought value as Long.
	 * 
	 * @param table		- The table to search within
	 * @param column	- The column that the value would be in
	 * @param where		- The where clause constraint
	 * @param whereVal	- The where clause's condition
	 * @return	- Long result
	 */
	public Long getValueLong(String table, String column, String where, Object whereVal)
	{
		String statement = "SELECT `" + column + "` FROM " + db + table + " WHERE `" + where + "` = '" + whereVal + "';";
		
		try
		{
			ResultSet rs = performQuery(statement);
			while (rs.next()) return rs.getLong(1);
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure executing statement", statement, e);
		}
		
		return null;
	}
	
	/**
	 * Get sought value as String.
	 * 
	 * @param table			- The table to search within
	 * @param column		- The column that the value would be in
	 * @param where			- The constraint column in the WHERE portion
	 * @param whereVal		- The constraint item in the WHERE portion
	 * @return	- String result
	 */
	public String getValueString(String table, String column, String where, Object whereVal)
	{
		String statement = String.format("SELECT %s FROM " + db + "%s WHERE %s = %s", column, table, where, whereVal.toString());

		try
		{
			ResultSet rs = performQuery(statement);
			while (rs.next()) return rs.getString(1);
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure executing statement", statement, e);
		}

		return null;
	}
	
	/**
	 * Get sought data as ArrayList of String.
	 * 
	 * @param table		- The table to search within
	 * @param column	- The column that the data would be in
	 * @return	- ArrayList of String result(s)
	 */
	public ArrayList<String> getValueStringArray(String table, String column)
	{
		String statement = String.format("SELECT %s FROM %s%s", column, db, table);
		ArrayList<String> array = new ArrayList<>();

		try
		{
			ResultSet rs = performQuery(statement);
			while (rs.next())
			{
				array.add(rs.getString(1));
			}
			
			return array;
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure executing statement", statement, e);
		}
		
		return null;
	}

	/**
	 * Get the channel the bot should message to for announcement-like events.
	 *
	 * @param guild	- The ID of the guild
	 * @return
	 */
	public Long getServerMessagingChannel(Long guild)
	{
		Long channel = getValueLong("guildInfo", "announcementChannel", "guild", guild);

		if (channel != null) return channel;
		else
		{
			Long botChannel = getGuildBotSpamChannel(guild);

			if (botChannel != null) return botChannel;
			else return null;
		}
	}


	/**
	 * The Guild's premium status
	 * <br>NOTE: Bot currently does not utilize this feature, this is a placeholder should I open the bot
	 * 		 up for paid features and such, for now nothing besides debug features use this, if at all.
	 */
	public boolean getGuildPremiumStatus(Long guild)
	{
		return getValueBoolean("guildInfo", "premium", "guild", guild);
	}

	/*
	 * The Guild's defined Admin role. TODO Admin role is not really implemented as the usage of this lines up with what the primary Staff role desigination does; thus all references to this designation in general should be removed eventually.
	 */
	public Long getGuildAdminRole(Long guild)
	{
		return getValueLong("guildInfo", "adminRole", "guild", guild);
	}

	/*
	 * The Guild's defined Admin role.
	 */
	public void setGuildAdminRole(Long guild, Long role)
	{
		String statement = "UPDATE " + db + "guildInfo SET adminRole = " + role + " WHERE guild = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/*
	 * The Guild's defined Primary Staff role.
	 */
	public Long getGuildStaffRole(Long guild)
	{
		return getValueLong("guildInfo", "primaryStaffRole", "guild", guild);
	}

	/*
	 * The Guild's defined Primary Staff role.
	 */
	public void setGuildStaffRole(Long guild, Long role)
	{
		String statement = "UPDATE " + db + "guildInfo SET primaryStaffRole = " + role + " WHERE guild = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/*
	 * The Guild's defined Secondary Staff role.
	 */
	public Long getGuildSupportingStaffRole(Long guild)
	{
		return getValueLong("guildInfo", "secondaryStaffRole", "guild", guild);
	}

	/*
	 * The Guild's defined Secondary Staff role.
	 */
	public void setGuildSupportingStaffRole(Long guild, Long role)
	{
		String statement = "UPDATE " + db + "guildInfo SET secondaryStaffRole = " + role + " WHERE guild = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/*
	 * The Guild's defined CustomVC category.
	 */
	public Long getGuildCustomvcCategory(Long guild)
	{
		return getValueLong("guildInfo", "dynamicVcCategory", "guild", guild);
	}

	/*
	 * The Guild's defined CustomVC category.
	 */
	public void setGuildCustomvcCategory(Long guild, Long category)
	{
		String statement = "UPDATE " + db + "guildInfo SET dynamicVcCategory = " + category + " WHERE guild = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/*
	 * The Guild's defined CustomVC voice channel.
	 */
	public Long getGuildCustomvcChannel(Long guild)
	{
		return getValueLong("guildInfo", "dynamicVcChannel", "guild", guild);
	}

	/*
	 * The Guild's defined CustomVC voice channel.
	 */
	public void setGuildCustomvcChannel(Long guild, Long channel)
	{
		String statement = "UPDATE " + db + "guildInfo SET dynamicVcChannel = " + channel + " WHERE guild = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/*
	 * The Guild's defined Music text channel.
	 */
	public Long getGuildMusicChannel(Long guild)
	{
		return getValueLong("guildInfo", "musicChannel", "guild", guild);
	}

	/*
	 * The Guild's defined Music text channel.
	 */
	public void setGuildMusicChannel(Long guild, Long channel)
	{
		String statement = "UPDATE " + db + "guildInfo SET musicChannel = " + channel + " WHERE guild = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/*
	 * The Guild's defined Member role.
	 */
	public Long getGuildMemberRole(Long guild)
	{
		return getValueLong("guildInfo", "memberRole", "guild", guild);
	}

	/*
	 * The Guild's defined Member role.
	 */
	public void setGuildMemberRole(Long guild, Long input)
	{
		String statement = "UPDATE " + db + "guildInfo SET memberRole = " + input + " WHERE guild = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/*
	 * The Guild's defined NSFW role.
	 */
	public Long getGuildNSFWStatusRole(Long guild)
	{
		return getValueLong("guildInfo", "nsfwRole", "guild", guild);
	}

	/*
	 * The Guild's defined NSFW role.
	 */
	public void setGuildNSFWStatusRole(Long guild, Long input)
	{
		String statement = "UPDATE " + db + "guildInfo SET nsfwRole = " + input + " WHERE guild = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/**
	 * Set temp embed data into the temp data file
	 *
	 * @param input - Quintet of strings for: name, title, color, data (roles and emojis in one string), description
	 */
	public void setEmbedTempData(Quartet<String, String, String, String> input, Long guild)
	{
		String statement = "UPDATE " + db + "tempEmbedData SET name = ?, title = ?, color = ?, body = ? WHERE guild = " + guild + ";";

		try
		{
			PreparedStatement ps = perform(statement);
			ps.setString(1, input.getValue0());
			ps.setString(2, input.getValue1());
			ps.setString(3, input.getValue2());
			ps.setString(4, input.getValue3());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure setting values into tempEmbedData table", statement, e);
		}
	}

	public Quartet<String, String, String, String> getEmbedTempData(Long guild)
	{
		String statement = "SELECT name, title, color, body FROM " + db + "tempEmbedData WHERE guild = " + guild + ";";
		ResultSet rs = performQuery(statement);

		try
		{
			while (rs.next())
			{
				return Quartet.with(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure getting values from tempEmbedData table", statement, e);
		}

		return null;
	}

	public void setReactionRoleEmbed(Long guild, String type)
	{
		Quartet<String, String, String, String> data = getEmbedTempData(guild);
		String statement = "INSERT INTO " + db + guild + "embeds (name, type, message, title, color, body) VALUES (?,?,?,?,?,?);";

		try
		{
			PreparedStatement ps = perform(statement);
			ps.setString(1, data.getValue0());
			ps.setString(2, type);
			ps.setString(3, "null");
			ps.setString(4, data.getValue1());
			ps.setString(5, data.getValue2());
			ps.setString(6, data.getValue3());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure setting values from tempEmbedData into embeds for guild " + guild, statement, e);
		}
	}

	public void setEmbedId(Long guild, String table, Long message, String name)
	{
		String statement = "UPDATE " + db + guild + "embeds SET message = " + message + " WHERE name = '" + name + "';";
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	public ArrayList<String> getEmbedNames(Long guild)
	{
		return getValueStringArray(guild + "embeds", "name");
	}

	/**
	 * Get the details for the specified embed. Returns the following: Title, Color, Data (Description), and the 'divider' role if applicable.
	 *
	 * @param embedName	- The name of the embed as seen in the DB.
	 * @param guild		- The guild the embed is being called for.
	 * @return Quartet of Strings of: Type, Title, Color, Body
	 */
	public Quartet<String, String, String, String> getEmbedData(String embedName, Long guild)
	{
		String statement = "SELECT type, title, color, body FROM " + db + guild + "embeds WHERE name = '" + embedName + "';";
		ResultSet rs = performQuery(statement);

		try
		{
			while (rs.next())
			{
				return Quartet.with(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure getting embed data for guild " + guild + " from embed " + embedName, statement, e);
		}

		return null;
	}

	public String getEmbedBodyData(Long guild, Long embed)
	{
		return getValueString(guild + "embeds", "body", "message", embed);
	}

	/*
	 * The Guild's defined Testing and/or Bot text channel.
	 */
	public Long getGuildBotSpamChannel(Long guild)
	{
		return getValueLong("guildInfo", "botSpamChannel", "guild", guild);
	}

	/*
	 * The Guild's defined Testing and/or Bot text channel.
	 */
	public void setGuildBotSpamChannel(Long guild, Long channel)
	{
		String statement = "UPDATE " + db + "guildInfo SET botSpamChannel = " + channel + " WHERE guild = " + guild + ";";
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	public Long getGuildLevelChannel(Long guild)
	{
		return getValueLong("guildInfo", "levelChannel", "guild", guild);
	}

	public void setGuildLevelChannel(Long guild, Long channel)
	{
		String statement = "UPDATE " + db + "guildInfo SET levelChannel = " + channel + " WHERE guild = " + guild + ";";
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/*
	 * The Guild's AudioManger Live Music message.
	 */
	public Long getGuildLiveMusicMessage(Long guild)
	{
		return getValueLong("guildInfo", "musicMessage", "guild", guild);
	}

	/*
	 * The Guild's AudioManger Live Music message.
	 */
	public void setGuildLiveMusicMessage(Long guild, Long message)
	{
		performUpdate("UPDATE " + db + "guildInfo SET musicMessage = " + message + " WHERE guild = " + guild + ";", LogType.DATABASE_ERROR);
	}

	/*
	 * The Guild's defined Rules text channel.
	 */
	public Long getGuildRuleChannel(Long guild)
	{
		return getValueLong("guildInfo", "ruleChannel", "guild", guild);
	}

	/*
	 * The Guild's defined Rules text channel.
	 */
	public void setGuildRuleChannel(Long guild, Long channel)
	{
		performUpdate("UPDATE " + db + "guildInfo SET ruleChannel = " + channel + " WHERE guild = " + guild + ";", LogType.DATABASE_ERROR);
	}

	public void setGuildKnownLevelUpRoleCount(Long guild, Integer number)
	{
		performUpdate("UPDATE " + db + "levelRoles SET knownLevelRoles = " + number + " WHERE guild = " + guild + ";", LogType.DATABASE_ERROR);
	}

	public Integer getGuildKnownLevelUpRoleCount(Long guild)
	{
		return getValueInteger("levelRoles", "knownLevelRoles", "guild", guild);
	}

	public void setGuildLevelUpRoleColumnBlankData(String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	public void setGuildLevelUpRoles(Long guild, String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	public Long getSingularRole(String tableName, String column, Long guild)
	{
		return getValueLong(tableName, column, "guild", guild);
	}

	public Long getGuildSingularLevelUpRole(Long guild, String column)
	{
		return getValueLong("levelRoles", column, "guild", guild);
	}

	public void setGuildSingularLevelUpRole(String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	public void setGuildColorRolesEntry(String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	public void setGuildSizeChannel(Long guild, Long input)
	{
		performUpdate("UPDATE " + db + "guildInfo SET serverSizeChannel = " + input + " WHERE guild = " + guild + ";", LogType.DATABASE_ERROR);
	}

	public Long getGuildSizeChannel(Long guild)
	{
		return getValueLong("guildInfo", "guildSizeChannel", "guild", guild);
	}

	/*
	 * ####################
	 * CHECKERS AND HELPERS
	 * ####################
	 */

	public void deleteColumn(String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	public void deleteRow(String table, String column, Long input)
	{
		performUpdate("DELETE FROM " + db + table + " WHERE " + column + " = " + input + ";", LogType.DATABASE_ERROR);
	}

	public void deleteRow(String table, String column, String input)
	{
		performUpdate("DELETE FROM " + db + table + " WHERE " + column + " = '" + input + "';", LogType.DATABASE_ERROR);
	}

	/**
	 * Basic update method.
	 *
	 * @param table		 - The table to update within.
	 * @param column	 - The column that needs to have an updated value inserted.
	 * @param input		 - The value to put into the specified column.
	 * @param where		 - The where clause constraint column.
	 * @param whereInput - The where clause's specifier.
	 */
	public void basicUpdate(String table, String column, String input, String where, String whereInput)
	{
		String statement = "UPDATE " + db + table + " SET " + column + " = " + input + " WHERE " + where + " = '" + whereInput + "';";
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/**
	 * Basic update method.
	 *
	 * @param table		 - The table to update within.
	 * @param column	 - The column that needs to have an updated value inserted.
	 * @param input		 - The value to put into the specified column.
	 * @param where		 - The where clause constraint column.
	 * @param whereInput - The where clause's specifier.
	 */
	public void basicUpdate(String table, String column, Object input, String where, Object whereInput)
	{
		String statement = "UPDATE " + db + table + " SET " + column + " = " + input + " WHERE " + where + " = '" + whereInput + "';";
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/**
	 * Method to insert data into a row or column as specified by the passed statement for the first time
	 *
	 * @param statement - The statement handling the first-insertion
	 */
	public void firstInsertion(String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/**
	 * Check database for any entries in the voiceTracker table.
	 */
	public ArrayList<Triplet<Long, Long, Long>> bootVoiceVerify()
	{
		String statement = "SELECT * FROM " + db + "voiceTracker";
		ArrayList<Triplet<Long, Long, Long>> array = new ArrayList<>();
		try
		{
			ResultSet rs = performQuery(statement);
			//ResultSet rs = prepare(statement).executeQuery();

			while (rs.next())
			{
				array.add(Triplet.with(rs.getLong(1), rs.getLong(2), rs.getLong(3)));
				break;
			}

			return array;
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error getting data from voiceTracker using", statement, e);
		}

		return null;
	}

	/**
	 * Method for submitting embed data.
	 *
	 * @param guild - The guild the embed is for.
	 * @param embedName - The internal name of the embed.
	 * @param color - The color of the embed.
	 * @param title - The title-text of the embed.
	 * @param body - The body-text of the embed.
	 */
	public void submitEmbed(Long guild, String embedName, String type, String color, String title, String body)
	{
		String statement = String.format("INSERT INTO " + db + guild + "embeds (`name`, `type`, title, color, body) VALUES ('%s','%s','%s','%s','%s');", embedName, type, title, color, body);
		performUpdate(statement, LogType.DATABASE_ERROR);
	}



	public void basicDelete(String table, String column, Object input)
	{
		String statement = "DELETE FROM " + db + table + " WHERE " + column + " = " + input + ";";
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	public Integer selectColorRolesCount(Long guild)
	{
		String statement = "SELECT color1, color2, color3, color4, color5, color6, color7, color8, color9, color10, color11, color12, color13, color14, color15, color16, color17, color18 FROM " + db + "colorRoles WHERE guild = " + guild;
		ResultSet rs = performQuery(statement);

		try
		{
			int counted = 0;
			int a = 1;
			rs.first();
			if (rs.getRow() != 0)
			{
				do
				{
					if (rs.getLong(a) != 0)
					{
						counted++;
					}
					a++;
				} while (a < 19);
			}

			return counted;
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error encountered while grabbing count for input of", statement, e);
		}

		return null;
	}

	public ArrayList<Long> selectColorRoles(Long guild)
	{
		String statement = "SELECT color1, color2, color3, color4, color5, color6, color7, color8, color9, color10, color11, color12, color13, color14, color15, color16, color17, color18 FROM " + db + "colorRoles WHERE guild = " + guild;
		ResultSet rs = performQuery(statement);

		try
		{
			ArrayList<Long> array = new ArrayList<>();

			int a = 1;
			rs.first();
			do
			{
				array.add(rs.getLong(a));
				a++;
			} while (a < 19);

			return array;
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error encountered while getting data from table for input of", statement, e);
		}

		return null;
	}

	/*
	 * ##############
	 * TABLE HANDLERS
	 * ##############
	 */

	public void newGuildEntry(Long guild)
	{
		performUpdate("INSERT INTO " + db + "guildInfo (guild) VALUES (" + guild + ");", LogType.DATABASE_ERROR);
	}

	public void createGuildColorRolesEntry(Long guild)
	{
		String statement = "INSERT INTO " + db + "colorRoles (guild, embed, color1, color2, color3, color4, color5, color6, color7, color8, color9, color10, color11, color12, color13, color14, color15, color16, color17, color18)"
				+ "VALUES (" + guild + ", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);";

		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	/*
	 * ##################################
	 * SERVER LEVELING AND XP TABLE LOGIC
	 * ##################################
	 */

	/**
	 * Fill in the RankChart using the formula. The first value of the array will be the base value of 75.
	 * maxLevel is set to 40 but due to Java int[] it will read as 0-39, our logic will accommodate level requirements
	 *
	 * For max level handling in action:
	 * @see TigerGuardDB#generateRankCard(SlashCommandInteractionEvent)
	 * @see TigerGuardDB#updateRankXp(Guild, Member, int, MessageReceivedEvent, GuildVoiceUpdateEvent)
	 *
	 * Formula used changes depending on the level, if the equation were a flat one such as, the scaling between levels would lead to
	 * issues where the xp needed to level up from 39 to 40 could be as large as the total xp from 0 to 35. So different equations are
	 * involved the further it goes.
	 */
	protected void setupRankChartList()
	{
		for (int a = 0; a < maxLevel; a++)
		{
			if (a <= 3)
			{
				if (a != 0) levelExpCaps[a] = (Math.round((levelExpCaps[a-1]+(a*135))) + 4) / 5 * 5;
				else levelExpCaps[a] = 75;
			}
			
			else if (a < 11) levelExpCaps[a] = (int) (Math.round((levelExpCaps[a-1]+(a*(135+(0.03*a)))*(1.15+(0.02*a)))) + 4) / 5 * 5;
			else if (a < 21) levelExpCaps[a] = (int) (Math.round((levelExpCaps[a-1]+(a*(165+(0.03*a)))*(1.17+(0.02*a)))) + 4) / 5 * 5;
			else levelExpCaps[a] = (int) (Math.round((levelExpCaps[a-1]+(a*(165+(0.05*a)))*(1.19+(0.03*a)))) + 4) / 5 * 5;
		}
		
		if (TigerGuard.isDebugMode())
		{
			logger.log(LogType.RANK_INFO, "Max Level:" + maxLevel);
			logger.log(LogType.RANK_INFO, "Format:\nLevel # | XP to level up | Total XP");
			
			StringBuilder data = new StringBuilder();
			for (int a = 0; a < maxLevel; a++)
			{
				String formatter = "Level %1$-2s | %2$6s | %3$s";
				if (a != (maxLevel-1)) formatter += "\n";
				int xpGainReqCalc = (a == 0) ? levelExpCaps[a] : levelExpCaps[a]-levelExpCaps[a-1];
				data.append(String.format(formatter, (a+1), xpGainReqCalc, levelExpCaps[a]));
			}
			logger.log(data.toString());
		}
	}

	public int getMaxLevel()
	{
		return maxLevel;
	}

	/*
	 * Per-server xp table
	 */
	public void createGuildXpTable(Guild guild)
	{
		String guildMeshXp = guild.getIdLong() + "xp";
		String statement = "CREATE TABLE " + db + guildMeshXp + "(member varchar(45), level int(11), xp int(11), activeRole varchar(45);";

		try
		{
			performUpdate(statement, LogType.XP_DATABASE_ERROR);

			guild.loadMembers().onSuccess(members -> {
				List<Member> memberList = members.stream().filter(a -> !a.getUser().isBot()).toList();

				PreparedStatement ps = perform(statement);

				for (Member member : memberList)
				{
					String innerStatement = "INSERT INTO " + db + guildMeshXp + " (member, level, xp, activeRole) VALUES (" + member.getIdLong() + "0, 0, null);";

					try
					{
						ps.addBatch(innerStatement);
					}
					catch (Exception e)
					{
						logger.logErr(LogType.DATABASE_ERROR, "Failure adding batch to statment", innerStatement, e);
					}
				}

				performBatch(ps);
			});
		}
		catch (Exception e)
		{
			logger.logErr(LogType.XP_DATABASE_ERROR, "Unable to create database table for guild " + guild.getIdLong(), statement, e);
		}
	}

	/*
	 * Per-server insert user into server table, usually for after the table has already been created.
	 */
	public void insertUserIntoGuildXPTable(String table, Long user)
	{
		performUpdate("INSERT INTO " + db + table + " (member, level, xp, activeRole) VALUES (" + user + ", 1, 0, null);", LogType.XP_DATABASE_ERROR);
	}

	/*
	 * Insert a Guild into the LevelRole table.
	 */
	public void insertGuildIntoLevelRoleTable(Long guild)
	{
		performUpdate("INSERT INTO " + db + "levelRoles SET guild = " + guild + ";", LogType.DATABASE_ERROR);
	}

	public void firstInsertionGuildKnownLevelRoleValue(Long guild, int initialInput)
	{
		performUpdate("UPDATE " + db + "levelRoles SET knownLevelRoles = " + initialInput + " WHERE guild = " + guild + ");", LogType.DATABASE_ERROR);
	}

	public void generateGuildRankCard(SlashCommandInteractionEvent event)
	{
		event.deferReply().queue();
		checkIfUserExistsInGlobalData(event.getMember().getIdLong());

		//Setup Rank Card base data parts
		Guild guild = event.getGuild();
		Member member = event.getMember();
		MessageFactory messageFactory = new MessageFactory();
		int memberXP = 0; //To be member's current XP value
		int memberLevel = 0; //To be member's current Level
		String table = guild.getIdLong() + "xp";
		MessageCreateBuilder msgBuilder = new MessageCreateBuilder();
		InteractionHook hook = event.getHook();

		try
		{
			ResultSet rs = performQuery("SELECT level, xp FROM " + db + table + " WHERE member = " + member.getIdLong() + ";");

			while (rs.next())
			{
				memberLevel = rs.getInt("level");
				memberXP = rs.getInt("xp");
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to get the xp value and level for user " + member.getIdLong() + " in guild " + guild.getIdLong(), null, e);
		}

		logger.debug("Member's level and xp are: " + memberLevel + " | " + memberXP);

		Long levelRole = null;
		if (memberLevel != 0)
		{
			levelRole = getGuildLevelRoleFromGuild(guild.getIdLong(), member.getIdLong(), memberLevel);
		}
		else if (guild.getRoleById(getGuildMemberRole(guild.getIdLong())).getName() != null)
		{
			levelRole = guild.getRoleById(getGuildMemberRole(guild.getIdLong())).getIdLong();
		}

		//If memberlevel is less than max level
		if (memberLevel < maxLevel)
		{
			try
			{
				int previousCap = (memberLevel <= 1) ? 0 : levelExpCaps[memberLevel-2];
	            msgBuilder.addFiles(FileUpload.fromData(messageFactory.createRankImage(member, guild.getRoleById(levelRole).getName(), memberLevel, memberXP, levelExpCaps[memberLevel-1], previousCap, false), "rankCard.png"));

	            hook.sendMessage(msgBuilder.build()).queue(msg -> {
	            	msg.delete().queueAfter(60, TimeUnit.SECONDS);
	            });
	        }
			catch (Exception e)
			{
				logger.logErr(LogType.RANK_ERROR, "Unable to create the rank card for " + member.getEffectiveName() + " in guild " + guild.getIdLong(), null, e);
	        }
		}
		//Else, member level >= (forced equals) to max level
		else
		{
			try
			{
	            msgBuilder.addFiles(FileUpload.fromData(messageFactory.createRankImage(member, guild.getRoleById(levelRole).getName(), memberLevel, memberXP, levelExpCaps[maxLevel-1], levelExpCaps[memberLevel-1], true), "rankCard.png"));

	            hook.sendMessage(msgBuilder.build()).queue(msg -> {
	            	msg.delete().queueAfter(60, TimeUnit.SECONDS);
	            });
	        }
			catch (Exception e)
			{
				logger.logErr(LogType.RANK_ERROR, "Unable to create rank card for " + member.getEffectiveName() + " in guild " + guild.getIdLong(), null, e);
	        }
		}
	}

	private Long getGuildLevelRoleFromGuild(Long guild, Long member, int memberLevel)
	{
		String query = "SELECT role" + memberLevel + " FROM " + db + "levelRoles";
		try
		{
			ResultSet rs = performQuery(query);
			while (rs.next())
			{
				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.RANK_ERROR, "Unable to get the level role for role" + memberLevel + " from guild " + guild, query, e);
		}

		return null;
	}

	public void voiceStatusBegin(Long member, Long guild)
	{
		if (!checkRow("voiceTracker", "member", member))
		{
			performUpdate(String.format("INSERT INTO %svoiceTracker (member, init, guild) VALUES (%s, %d, %d);", db, member, System.currentTimeMillis(), guild), LogType.DATABASE_ERROR);
		}
	}

	public Long voiceStatusEnd(Long member)
	{
		String queryS = "SELECT init FROM " + db + "voiceTracker WHERE member = " + member + ";";
		ResultSet rs = performQuery(queryS);
		Long query = 0L;

		try
		{
			while (rs.next())
			{
				query = rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure getting data for " + member + " from voicetracker table", queryS, e);
		}

		String statement = "DELETE FROM " + db + "voiceTracker WHERE member = " + member + ";";
		try
		{
			performUpdate(statement, LogType.DATABASE_ERROR);
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure removing member " + member + " from voiceTrack table", statement, e);
		}

		return query;
	}

	/**
	 * Check if the specified user is found in the DB for the global data table.
	 * @param user - The user to search for.
	 */
	public void checkIfUserExistsInGlobalData(Long member)
	{
		if (!checkRow("globalUserData", "member", member))
		{
			performUpdate("INSERT INTO " + db + "globalUserData (member, level, xp, bgimage) VALUES (" + member + ", 1, 0, 1);", LogType.DATABASE_ERROR);
		}
	}

	public void updateUserRankImage(Long member, int value)
	{
		performUpdate("UPDATE " + db + "globalUserData SET bgimage = " + value + " WHERE member = " + member + ";", LogType.DATABASE_ERROR);
	}

	public Integer getUserRankImage(Long member)
	{
		String statement = "SELECT bgimage FROM " + db + "globalUserData WHERE member = " + member + ";";
		ResultSet rs = performQuery(statement);
		int query = 0;

		try
		{
			while (rs.next())
			{
				return rs.getInt(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.RANK_ERROR, "Failure pulling image data for " + member + " from globalUserData", statement, e);
		}

		return query;
	}

	/**
	 * Updates the rank for a user by first checking the current level and xp value for the user and logically updating depending on the variables returned.
	 *
	 * @param guild  - The guild the member's values are being updated for.
	 * @param member - The member whom is gaining xp.
	 * @param xpGain - The amount of xp that the user gained.
	 */
	public void updateGuildRankXp(Guild guild, Member member, int xpGain, MessageReceivedEvent messageEvent, GuildVoiceUpdateEvent voiceEvent)
	{
		checkIfUserExistsInGlobalData(member.getIdLong());

		if (dates.getBoostDateStatus()) xpGain = (int)Math.round(xpGain * dates.getBoostValue());

		String guildMesh = guild.getIdLong() + "xp";
		int memberXP = 0;
		int memberLevel = 1;
		MessageCreateBuilder msgBuilder = new MessageCreateBuilder();

		if (getGuildKnownLevelUpRoleCount(guild.getIdLong()) != 0)
		{
			String initialQuery = String.format("SELECT level, xp FROM %s%s WHERE member = %s", db, guildMesh, member.getIdLong());
			ResultSet rs = performQuery(initialQuery);

			try
			{
				while (rs.next())
				{
					memberLevel = rs.getInt("level");
					memberXP = rs.getInt("xp");
				}
			}
			catch (Exception e)
			{
				logger.logErr(LogType.RANK_ERROR, "Failure retrieving xp_data for " + member.getIdLong() + " for guild " + guild.getIdLong(), initialQuery, e);
			}
			
			//Max level pre-catch, exit if member is max level. Does not catch those leveling from 39 to 40 in this instance.
			if (memberLevel == maxLevel) return;

			//Begin checking if the current xp amount plus gained xp is more than 1 level worth of increase.
			int combinedXP = memberXP + xpGain;
			boolean checkFinished = false;
			while (!checkFinished)
			{
				if (meetsLevelUp(combinedXP, levelExpCaps[memberLevel-1]))
				{
					memberLevel++;
					
					if (memberLevel == (maxLevel-1))
					{
						if (combinedXP > levelExpCaps[maxLevel-1])
						{
							combinedXP = levelExpCaps[maxLevel-1];
							break;
						}
					}
					
					continue;
				}
				else break;
			}

			Long levelRole = getGuildLevelRoleFromGuild(guild.getIdLong(), member.getIdLong(), memberLevel);

			//Perform level-role removal and provisioning checks
			LevelRoleProgressionHandler(guild, member, memberLevel);
			
			//Finalize all logic
			String statementNew = String.format("UPDATE %s%s SET xp = %s, level = %s, WHERE member = %s;", db, guildMesh, combinedXP, memberLevel, member.getIdLong());
			performUpdate(statementNew, LogType.DATABASE_ERROR);
			
			//Create level-card after all things have been finalized.
			try
			{
				msgBuilder.addFiles(FileUpload.fromData(messageFactory.createRanklevelUpImage(guild, member, guild.getRoleById(levelRole).getName(), memberLevel, false), "rankCard.png"));
			}
			catch (Exception e)
			{
				logger.logErr(LogType.ERROR, "Failure generating rank card from updateRankXp method.", statementNew, e);
			}
			
			//Where to send the level-up-card
			if (voiceEvent != null) TigerGuard.TigerGuardInstance.getJDA().getVoiceChannelById(voiceEvent.getChannelLeft().getIdLong()).sendMessage(msgBuilder.build()).queue();
			else
			{
				TextChannel specifiedChannel = null;
				
				if (checkIfValueExists("guildInfo", "LevelChannel", "guild", guild.getIdLong()))
					specifiedChannel = guild.getTextChannelById(getGuildLevelChannel(guild.getIdLong()));
				else if (checkIfValueExists("guildInfo", "BotChannel", "guild", guild.getIdLong()))
					specifiedChannel = guild.getTextChannelById(getGuildBotSpamChannel(guild.getIdLong()));
				else if (messageEvent != null)
					specifiedChannel = messageEvent.getChannel().asTextChannel();
				
				specifiedChannel.sendMessage(msgBuilder.build()).queue();
			}
		}
	}

	/**
	 * Conditional method for handling role provisioning depending on whether a user set a preferred role or not.
	 *
	 * @param guild - The guild the level up occurred in.
	 * @param member - The member in question.
	 * @param level - The level of the user after leveling up.
	 */
	public void verifyLevelRoleProvision(Guild guild, Member member)
	{
		ArrayList<Long> roles = getGuildLevelRoleIDs(guild.getIdLong());
		List<Role> memberRoles = member.getRoles();
		System.out.println(memberRoles.size());

		int level = getGuildMemberLevel(guild.getIdLong(), member.getIdLong());

		//If user does NOT have a preferred role selected - ie never set one
		if (checkIfValueExists(guild.getIdLong() + "xp", "activeRole", "member", member.getIdLong()))
		{
			logger.debug("Member " + member.getEffectiveName() + " is level " + level);

			for (int a = 0; a < (level-1); a++)
			{
				Role lvlRole = guild.getRoleById(roles.get(a));
				logger.debug("Iterating through..." + a + " >> " + lvlRole.getName());

				if (memberRoles.contains(lvlRole))
				{
					guild.removeRoleFromMember(member, lvlRole).queue();
				}
			}
		}

		//TODO - Possible edge-case identified, test in next iteration - relates to StringSelect file.
	}
	
	private void LevelRoleProgressionHandler(Guild guild, Member member, int memberLevel)
	{
		ArrayList<Long> lvlRoleIDs = getGuildLevelRoleIDs(guild.getIdLong());
		ArrayList<Role> lvlRoles = new ArrayList<Role>();
		
		lvlRoleIDs.forEach(id -> {
			lvlRoles.add(guild.getRoleById(id));
		});
		
		if (member != null && !member.getUser().isBot())
		{
			List<Role> memberRoles = member.getRoles();
			
			lvlRoles.forEach(lvlRole -> {
				if (memberRoles.contains(lvlRole))
				{
					logger.debug(String.format("Removing role `%s` (%s) from member %s", lvlRole.getName(), lvlRole.getIdLong(), member.getIdLong()));
					guild.removeRoleFromMember(member, lvlRole).queue();
				}
			});

			logger.debug(String.format("Adding role `%s` (%s) to member", lvlRoles.get(memberLevel-1), lvlRoles.get(memberLevel-1).getIdLong()));
			guild.addRoleToMember(member, lvlRoles.get(memberLevel-1)).queue();
		}
		else logger.debug(String.format("Member `%s` not found in server.", member.getIdLong()));
	}

	/**
	 * Used to get the level roles for a server
	 *
	 * @param guild
	 * @return
	 */
	public ArrayList<Long> getGuildLevelRoleIDs(Long guild)
	{
		ArrayList<Long> results = new ArrayList<>();
		String statement = "SELECT * FROM " + db + "levelRoles WHERE guild = " + guild + ";";

		try
		{
			ResultSet rs = performQuery(statement);

			while (rs.next())
			{
				Long count = rs.getLong(2);

				for (int a = 1; a <= count; a++)
				{
					results.add(rs.getLong(rs.findColumn("role" + a)));
				}
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error getting levelRoles using statement", statement, e);
		}

		return results;
	}

	/**
	 * Get the level for the member within the specified guild
	 *
	 * @param guild		- The ID of the guild
	 * @param member	- The ID of the member
	 * @return
	 */
	public int getGuildMemberLevel(Long guild, Long member)
	{
		int result = 0;
		String statement = String.format("SELECT level FROM " + db + "%s WHERE guild = %d", guild + "xp", member);
		
		try
		{
			ResultSet rs = performQuery(statement);

			while (rs.next())
			{
				result = rs.getInt(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error getting member's level using", statement, e);
		}

		return result;
	}

	/**
	 * ################
	 * POLL TABLE LOGIC
	 * ################
	 */

	public void createGuildPollTable(Long guild)
	{
		performUpdate("CREATE TABLE " + db + guild + "polls (message VARCHAR(45), channeltype VARCHAR(45), channel VARCHAR(45), polltype VARCHAR(20), endtime VARCHAR(45), initiated VARCHAR(10));", LogType.DATABASE_ERROR);
		performUpdate("UPDATE " + db + "guildInfo SET pollTable = '" + guild + "polls' WHERE guild = " + guild, LogType.DATABASE_ERROR);
	}

	public void addGuildToTempPollTable(Long guild)
	{
		performUpdate("INSERT INTO " + db + "tempPollData (guild) VALUES (" + guild + ")", LogType.DATABASE_ERROR);
	}

	public void setPollTempTimeData(Long guild, int value, String type)
	{
		String statement = String.format("UPDATE " + db + "tempPollData SET timetype = '%s', amount = '%d' WHERE guild = %d", type, value, guild);
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	public Pair<String, Integer> getPollTempData(Long guild)
	{
		String statement = "SELECT timetype, amount FROM " + db + "tempPollData WHERE guild = " + guild + ";";
		ResultSet rs = performQuery(statement);

		try
		{
			while (rs.next())
			{
				return new Pair<>(rs.getString(1), rs.getInt(2));
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure returning temp poll data", statement, e);
		}

		return null;
	}

	/*
	 * TODO: this method might be able to be simplified/compacted
	 */
	public void pollCreation(Long guild, Long poll, ChannelType channelType, Long channel, String pollType)
	{
		if (!checkForTable(guild + "polls")) createGuildPollTable(guild);

		Pair<String, Integer> data = getPollTempData(guild);
		String timeType = data.getValue0();
		String statementCreate = "CREATE TABLE " + db + "poll" + poll + "(voter VARCHAR(45), vote VARCHAR(1));";

		if (timeType.equals("minute") && data.getValue1() <= 10) //If Poll might end and be missed between the poll-check loop
		{
			String statementB = "INSERT INTO " + db + guild + "polls (poll, channeltype, channel, polltype, endtime, initiated) VALUES ('" + poll + "', '" + channelType.toString().toLowerCase() + "', '" + channel + "', '" + pollType + "', '" + (System.currentTimeMillis() + (data.getValue1() * 60000)) + "', true);";

			performUpdate(statementCreate, LogType.DATABASE_ERROR);
			performUpdate(statementB, LogType.DATABASE_ERROR);

			new TigerPolls().designatedQuickStart(guild, poll, channelType, channel, pollType, (System.currentTimeMillis() + (data.getValue1() * 60000)));
		}
		else //All other polls (those that won't miss the next poll-check loop prior to expiring)
		{
			Long currentTime = System.currentTimeMillis();
			Long endTime = 0L;

			switch (timeType)
			{
				case "minute":
					endTime = currentTime + (data.getValue1() * 60000);
					break;
				case "hour":
					endTime = currentTime + Duration.ofHours(data.getValue1()).toMillis();
					break;
				case "day":
					endTime = currentTime + Duration.ofDays(data.getValue1()).toMillis();
					break;
			}

			String statementB = "INSERT INTO " + db + guild + "polls` (poll, channeltype, channel, polltype, endtime, initiated) VALUES ('" + poll + "', '" + channelType.toString().toLowerCase() + "', '" + channel + "', '" + pollType + "', '" + endTime + "', false);";

			performUpdate(statementCreate, LogType.DATABASE_ERROR);
			performUpdate(statementB, LogType.DATABASE_ERROR);
		}
	}

	public void pollUpdateInitiatedCheck(Long guild, Long poll)
	{
		String statement = "UPDATE " + db + guild + "polls SET initiated = true WHERE poll = " + poll + ";";
		performUpdate(statement, LogType.DATABASE_ERROR);
	}

	public void pollVoteUpdate(Long poll, Long member, char vote)
	{
		String statement = "SELECT 1 FROM " + db + "poll" + poll + " WHERE voter = " + member + " LIMIT 1;";
		ResultSet rs = performQuery(statement);

		try
		{
			if (!rs.first())
			{
				performUpdate(String.format("INSERT INTO %s (voter, vote) VALUES (%s, '%c');", db + "poll" + poll, member, vote), LogType.DATABASE_ERROR);
			}
			else
			{
				performUpdate(String.format("UPDATE %s SET vote = '%c' WHERE voter = %s;", db + "poll" + poll, vote, member), LogType.DATABASE_ERROR);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure returning poll vote status for [Poll] " + poll + " | [Member]" + member + " | [vote] " + vote, statement, e);
		}
	}

	public ArrayList<Pair<Long, String>> getPollTablesBasicData()
	{
		ArrayList<Pair<Long, String>> polls = new ArrayList<>();
		String statement = "SELECT guild, pollTable FROM " + db + "guildInfo";
		ResultSet rs = performQuery(statement);

		try
		{
			while (rs.next())
			{
				if (rs.getString(2) != null)
				{
					polls.add(Pair.with(rs.getLong(1), rs.getString(2)));
				}
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to recall data about polls", statement, e);
		}

		return polls;
	}

	public ArrayList<Quintet<Long, ChannelType, Long, String, Long>> getPollsData(String pollTable, boolean bootRun)
	{
		ArrayList<Quintet<Long, ChannelType, Long, String, Long>> dataList = new ArrayList<>();
		String statement = "SELECT poll, channeltype, channel, polltype, endtime, initiated FROM " + db + pollTable + ";";
		ResultSet rs = performQuery(statement);

		try
		{
			while (rs.next())
			{
				if (!rs.getBoolean(6) || bootRun) //bootRun (true only on first run) will allow this code to work for polls that have the setting of 'true' set in the DB. This ensures polls don't get perma-stuck.
				{
					ChannelType type = null;
					switch (rs.getString(2))
					{
						case "text":
							type = ChannelType.TEXT;
							break;
						case "forum":
							type = ChannelType.FORUM;
							break;
						case "news":
							type = ChannelType.NEWS;
							break;
						case "stage":
							type = ChannelType.STAGE;
							break;
						case "voice":
							type = ChannelType.VOICE;
							break;
					}

					dataList.add(Quintet.with(rs.getLong(1), type, rs.getLong(3), rs.getString(4), rs.getLong(5)));
				}
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to recall advanced data about polls", statement, e);
		}

		return dataList;
	}

	public Pair<Integer, Integer> pollCollectResultsDuo(Long guild, Long poll)
	{
		int yay = 0;
		int nay = 0;

		String statement = "SELECT vote FROM " + db + "poll" + poll + ";";
		ResultSet rs = performQuery("SELECT vote FROM " + db + "poll" + poll + ";");

		try
		{
			while (rs.next())
			{
				if (rs.getString(1).equalsIgnoreCase("y")) yay++;
				else nay++;
			}

			pollDeletion(guild, poll);

		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure finalizing results for the poll "  + poll, statement, e);
		}

		return Pair.with(yay, nay);
	}

	public Triplet<Integer, Integer, Integer> pollCollectResultsTrio(Long guild, Long poll)
	{
		int yay = 0;
		int abs = 0;
		int nay = 0;

		String statement = "SELECT vote FROM " + db + "poll" + poll + ";";
		ResultSet rs = performQuery(statement);

		try
		{
			while (rs.next())
			{
				if (rs.getString(1).equalsIgnoreCase("y")) yay++;
				else if (rs.getString(1).equalsIgnoreCase("a")) abs++;
				else nay++;
			}

			pollDeletion(guild, poll);

		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure finalizing results for the poll "  + poll, statement, e);
		}

		return Triplet.with(yay, abs, nay);
	}

	private void pollDeletion(Long guild, Long poll)
	{
		performUpdate(String.format("DELETE FROM %s WHERE poll = %s", db + guild + "polls", poll), LogType.DATABASE_ERROR);
		performUpdate(String.format("DROP TABLE %s;", db + "poll" + poll), LogType.DATABASE_ERROR);
	}
}