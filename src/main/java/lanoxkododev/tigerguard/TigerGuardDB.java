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
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

/*
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;
import org.mariadb.jdbc.Connection;
import org.mariadb.jdbc.DatabaseMetaData;

import dev.arbjerg.lavalink.protocol.v4.Exception;
import kotlin.Deprecated;
import kotlin.time.Duration;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.MessageFactory;
import lanoxkododev.tigerguard.polls.TigerPolls;
import lanoxkododev.tigerguard.time.TimeDates;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
*/

public class TigerGuardDB {

	MessageFactory messageFactory = new MessageFactory();
	TimeDates dates = new TimeDates();
	TigerLogs logger = new TigerLogs();
	public static TigerGuardDB tigerGuardDB;
	public static int maxRankLevel = 40;
	protected static int[] rankLevels = new int[maxRankLevel]; /**@see TigerGuardDB#setupRankChartList() for implementation details.*/

	static Connection connection;
	final String db;

	/*
	 * ########################
	 * TODO: CONNECTION RELATED
	 * ########################
	 */
	public TigerGuardDB(String address, String databaseName, String databaseUser, String databasePass)
	{
		tigerGuardDB = this;
		db = databaseName + ".";
		initConnection(address, databaseName, databaseUser, databasePass);
		setupRankChartList();
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
		return tigerGuardDB;// = new TigerGuardDB();
	}

	/*
	 * ########################
	 * TODO: PERFORM STATEMENTS
	 * ########################
	 */

	/**
	 * Method to handle all update events. Replaces {@link #prepare(String)}
	 *
	 * @param statement	- The statement to perform
	 * @param type		- The LogType that the error might raise during a failure
	 * @param error		- The error message that will be output along with the stacktrace for clarity
	 */
	private void performUpdate(String statement, LogType type, String error)
	{
		try
		{
			connection.prepareStatement(statement).executeUpdate();
		}
		catch (Exception e)
		{
			logger.logErr(type, error, statement, e);
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

	/*
	 * #######################
	 * TODO: GETTERS & SETTERS
	 * #######################
	 */

	/**
	 * Basic boolean returning method.
	 *
	 * @param table		 - The table to search within.
	 * @param column	 - The column that the
	 * @param where		 - The where clause constraint.
	 * @param whereInput - The where clause's specifier.
	 * @return
	 */
	public Boolean hasValue(String table, String column, String where, String whereInput)
	{
		boolean found = false;
		String statement = "SELECT EXISTS(SELECT " + column + " FROM " + db + table + " WHERE `" + where + "` = '" + whereInput + "') AS EXISTS_BY_NAME;";
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
	 * @param table		 - The table to search within.
	 * @param column	 - The column that the
	 * @param where		 - The where clause constraint.
	 * @param whereInput - The where clause's specifier.
	 * @return
	 */
	public Boolean hasValue(String table, String column, String where, Long whereInput)
	{
		boolean found = false;
		String statement = "SELECT EXISTS(SELECT " + column + " FROM " + db + table + " WHERE `" + where + "` = '" + whereInput + "') AS EXISTS_BY_NAME;";
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
	 * @param table		 	- The table to search within.
	 * @param column	 	- The column that the
	 * @param whereColumn	- The where clause constraint column.
	 * @param whereInput 	- The where clause's condition.
	 * @return
	 */
	public Integer getValue(String table, String column, String where, Object whereInput)
	{
		String statement = "SELECT EXISTS(SELECT " + column + " FROM " + db + table + " WHERE `" + where + "` = '" + whereInput + "') AS EXISTS_BY_NAME;";
		ResultSet rs = performQuery(statement);

		try
		{
			if (rs.next())
			{
				return 1;
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error encountered while checking table for a value", statement, e);
		}

		return 0;
	}

	/**
	 * Basic Long returning method.
	 *
	 * @param table		 	- The table to search within.
	 * @param column	 	- The column that the return needs to be.
	 * @param whereColumn	- The where clause constraint column.
	 * @param whereInput 	- The where clause's condition.
	 * @return
	 */
	public Long basicSelectLong(String table, String column, String where, Long whereInput)
	{
		String statement = "SELECT `" + column + "` FROM " + db + table + " WHERE `" + where + "` = '" + whereInput + "';";
		ResultSet rs = performQuery(statement);
		try
		{
			while (rs.next())
			{
				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error encountered while getting data from table " + table, statement, e);
		}

		return null;
	}

	/**
	 * Get the channel the bot should message to for announcement-like events.
	 *
	 * @param guild	- The ID of the guild
	 * @return
	 */
	public Long getServerMessagingChannel(long guild)
	{
		//Long channel = this.basicSelectLong("guildInfo", "announcemenChannel", "id", guild);
		Long channel = (Long) selectSingle("guildInfo", "announcemenChannel", "id", guild, "long");

		if (channel != null)
		{
			return channel;
		}
		else
		{
			Long botChannel = getGuildBotSpamChannel(guild);

			if (botChannel != null)
			{
				return botChannel;
			}
			else
			{
				return null;
			}
		}
	}


	/**
	 * The Guild's premium status
	 * <br>NOTE: Bot currently does not utilize this feature, this is a placeholder should I open the bot
	 * 		 up for paid features and such, for now nothing besides debug features use this, if at all.
	 */
	public boolean getGuildPremiumStatus(long guild)
	{
		String statement = "SELECT premium FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		boolean isPremium = false;
		try
		{
			while (rs.next())
			{
				return rs.getBoolean(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for premium field for guild " + guild, statement, e);
		}

		return isPremium;
	}

	/*
	 * The Guild's defined Admin role.
	 */
	public Long getGuildAdminRole(long guild)
	{
		String statement = "SELECT adminRole FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		Long query = null;
		try
		{
			while (rs.next())
			{
				query = rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for adminRole for guild " + guild, statement, e);
		}

		return query;
	}

	/*
	 * The Guild's defined Admin role.
	 */
	public void setGuildAdminRole(long guild, long role)
	{
		String statement = "UPDATE " + db + "guildInfo SET adminRole = " + role + " WHERE id = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set value for adminRole for guild " + guild);
	}

	/*
	 * The Guild's defined Primary Staff role.
	 */
	public Long getGuildStaffRole(long guild)
	{
		String statement = "SELECT primaryStaffRole FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		Long query = null;
		try
		{
			while (rs.next())
			{
				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for primaryStaffRole from the guild " + guild, statement, e);
		}

		return query;
	}

	/*
	 * The Guild's defined Primary Staff role.
	 */
	public void setGuildStaffRole(long guild, long role)
	{
		String statement = "UPDATE " + db + "guildInfo SET primaryStaffRole = " + role + " WHERE id = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set value for primaryStaffRole for guild " + guild);
	}

	/*
	 * The Guild's defined Secondary Staff role.
	 */
	public Long getGuildSupportingStaffRole(long guild)
	{
		String statement = "SELECT secondaryStaffRole FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		Long query = null;
		try
		{
			while (rs.next())
			{
				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for secondaryStaffRole for guild " + guild, statement, e);
		}

		return query;
	}

	/*
	 * The Guild's defined Secondary Staff role.
	 */
	public void setGuildSupportingStaffRole(long guild, long role)
	{
		String statement = "UPDATE " + db + "guildInfo SET secondaryStaffRole = " + role + " WHERE id = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set value for secondaryStaffRole for guild " + guild);
	}

	/*
	 * The Guild's defined CustomVC category.
	 */
	public Long getGuildCustomvcCategory(long guild)
	{
		String statement = "SELECT dynamicVcCategory FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		Long query = null;
		try
		{
			while (rs.next())
			{
				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for dynamicVcCategory for guild " + guild, statement, e);
		}

		return query;
	}

	/*
	 * The Guild's defined CustomVC category.
	 */
	public void setGuildCustomvcCategory(long guild, long category)
	{
		String statement = "UPDATE " + db + "guildInfo SET dynamicVcCategory = " + category + " WHERE id = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set value for dynamicVcCategory for guild " + guild);
	}

	/*
	 * The Guild's defined CustomVC voice channel.
	 */
	public Long getGuildCustomvcChannel(long guild)
	{
		String statement = "SELECT dynamicVcChannel FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		Long query = null;
		try
		{
			while (rs.next())
			{
				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for dynamicVcChannel for guild " + guild, statement, e);
		}

		return query;
	}

	/*
	 * The Guild's defined CustomVC voice channel.
	 */
	public void setGuildCustomvcChannel(long guild, long channel)
	{
		String statement = "UPDATE " + db + "guildInfo SET dynamicVcChannel = " + channel + " WHERE id = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set value for dynamicVcChannel for guild " + guild);
	}

	/*
	 * The Guild's defined Music text channel.
	 */
	public Long getGuildMusicChannel(long guild)
	{
		String statement = "SELECT musicChannel FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		Long query = null;
		try
		{
			while (rs.next())
			{
				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for musicChannel for guild " + guild, statement, e);
		}

		return query;
	}

	/*
	 * The Guild's defined Music text channel.
	 */
	public void setGuildMusicChannel(long guild, long channel)
	{
		String statement = "UPDATE " + db + "guildInfo SET musicChannel = " + channel + " WHERE id = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set value for musicChannel for guild " + guild);
	}

	/*
	 * The Guild's defined Member role.
	 */
	public Long getGuildMemberRole(long guild)
	{
		String statement = "SELECT memberRole FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		try
		{
			while (rs.next())
			{
				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for memberRole for guild " + guild, statement, e);
		}

		return null;
	}

	/*
	 * The Guild's defined Member role.
	 */
	public void setGuildMemberRole(long guild, long input)
	{
		String statement = "UPDATE " + db + "guildInfo SET memberRole = " + input + " WHERE id = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set value for memberRole for guild " + guild);
	}

	/*
	 * The Guild's defined NSFW role.
	 */
	public Long getGuildNSFWStatusRole(long guild)
	{
		String statement = "SELECT nsfwRole FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		Long query = null;
		try
		{
			while (rs.next())
			{
				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for nsfwRole from the guild " + guild, statement, e);
		}

		return query;
	}

	/*
	 * The Guild's defined NSFW role.
	 */
	public void setGuildNSFWStatusRole(long guild, long input)
	{
		String statement = "UPDATE " + db + "guildInfo SET nsfwRole = " + input + " WHERE id = " + guild;
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set value for nsfwRole for guild " + guild);
	}

	/**
	 * Set temp embed data into the temp data file
	 *
	 * @param input - Quintet of strings for: name, title, color, data (roles and emojis in one string), description
	 */
	public void setEmbedTempData(Quartet<String, String, String, String> input, long guild)
	{
		String statement = "UPDATE " + db + "tempEmbedData SET name = ?, title = ?, color = ?, datas = ?, descr = ?, divider = ? WHERE id = " + guild + ";";

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

	public Quartet<String, String, String, String> getEmbedTempData(long guild)
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

	public void setReactionRoleEmbed(long guild, String type)
	{
		Quartet<String, String, String, String> data = getEmbedTempData(guild);
		String statement = "INSERT INTO " + db + guild + "embeds (name, type, id, title, color, body) VALUES (?,?,?,?,?,?);";

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

	public void setEmbedId(long guild, String table, long id, String name)
	{
		String statement = "UPDATE " + db + guild + "embeds SET id = " + id + " WHERE name = '" + name + "';";
		performUpdate(statement, LogType.DATABASE_ERROR, "Failures setting id for embed for guild " + guild);
	}

	public ArrayList<String> getEmbedNames(long guild)
	{
		String statement = "SELECT name FROM " + db + guild + "embeds;";
		ResultSet rs = performQuery(statement);
		ArrayList<String> embeds = new ArrayList<>();

		try
		{
			while (rs.next())
			{
				embeds.add(rs.getString(1));
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure getting name of embed for guild " + guild, statement, e);
		}

		return embeds;
	}

	/**
	 * Get the details for the specified embed. Returns the following: Title, Color, Data (Description), and the 'divider' role if applicable.
	 *
	 * @param embedName	- The name of the embed as seen in the DB.
	 * @param guild		- The guild the embed is being called for.
	 * @return Quartet of Strings of: Type, Title, Color, Body
	 */
	public Quartet<String, String, String, String> getEmbedData(String embedName, long guild)
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

	public String getColorEmbedBodyData(long guild)
	{
		String statement = "SELECT embed FROM " + db + guild + "embeds WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		try
		{
			while (rs.next())
			{
				return rs.getString(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure getting color embed body data using", statement, e);
		}

		return null;
	}

	public String getEmbedBodyData(long guild, long embed)
	{
		String statement = "SELECT body FROM " + db + guild + "embeds WHERE id = " + embed + ";";
		ResultSet rs = performQuery(statement);

		try
		{
			while (rs.next())
			{
				return rs.getString(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure getting embed body data for guild " + guild + " from embed " + embed, statement, e);
		}

		return null;
	}

	/*
	 * The Guild's defined Testing and/or Bot text channel.
	 */
	public Long getGuildBotSpamChannel(long guild)
	{
		String statement = "SELECT botSpamChannel FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		Long query = null;
		try
		{
			while (rs.next())
			{
				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for botSpamChannel for guild " + guild, statement, e);
		}

		return query;
	}

	/*
	 * The Guild's defined Testing and/or Bot text channel.
	 */
	public void setGuildBotSpamChannel(long guild, long channel)
	{
		performUpdate("UPDATE " + db + "guildInfo SET botSpamChannel = " + channel + " WHERE id = " + guild + ";", LogType.DATABASE_ERROR, "Unable to set value for botSpamChannel");
	}

	public Long getGuildLevelChannel(long guild)
	{
		String statement = "SELECT levelChannel FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		Long query = null;
		try
		{
			while (rs.next())
			{
				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for levelChannel for guild " + guild + "", statement, e);
		}

		return query;
	}

	public void setGuildLevelChannel(long guild, long channel)
	{
		performUpdate("UPDATE " + db + "guildInfo SET levelChannel = " + channel + " WHERE id = " + guild + ";", LogType.DATABASE_ERROR, "Unable to set value for levelChannel");
	}

	/*
	 * The Guild's AudioManger Live Music message.
	 */
	public Long getGuildLiveMusicMessage(long guild)
	{
		String statement = "SELECT musicMessage FROM " + db + "guildInfo WHERE `id` = " + guild + ";";
		ResultSet rs = performQuery(statement);

		try
		{
			while (rs.next())
			{
				Object val = rs.getObject(1);
				if (val == null)
				{
					return null;
				}

				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for musicMessage for guild " + guild, statement, e);
		}

		return null;
	}

	/*
	 * The Guild's AudioManger Live Music message.
	 */
	public void setGuildLiveMusicMessage(long guild, long messageId)
	{
		performUpdate("UPDATE " + db + "guildInfo SET musicMessage = " + messageId + " WHERE `id` = " + guild + ";", LogType.DATABASE_ERROR, "Unable to set value for musicMessage");
	}

	/*
	 * The Guild's defined Rules text channel.
	 */
	public Long getGuildRuleChannel(long guild)
	{
		String statement = "SELECT ruleChannel FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		Long query = null;
		try
		{
			while (rs.next())
			{
				return rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for ruleChannel for guild " + guild, statement, e);
		}

		return query;
	}

	/*
	 * The Guild's defined Rules text channel.
	 */
	public void setGuildRuleChannel(long guild, long channelId)
	{
		performUpdate("UPDATE " + db + "guildInfo SET ruleChannel = " + channelId + " WHERE id = " + guild + ";", LogType.DATABASE_ERROR, "Unable to set value for ruleChannel for guild " + guild);
	}

	public void setGuildKnownLevelUpRoleCount(long guild, int number)
	{
		performUpdate("UPDATE " + db + "levelRoles SET knownLevelRoles = " + number + " WHERE id = " + guild + ";", LogType.DATABASE_ERROR, "Unable to set value for knownLevelRoles for guild " + guild);
	}

	public int getGuildKnownLevelUpRoleCount(long guild)
	{
		String statement = "SELECT knownLevelRoles FROM " + db + "levelRoles WHERE id = " + guild + ";";
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
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for knownLevelRoles for guild " + guild, statement, e);
		}

		return query;
	}

	/**
	 * @deprecated - This was used for the legacy logic where each guild had their own lvlRoles table. Universal table now exists
	 * @param statement
	 */
	@Deprecated
	public void setGuildLevelUpRoleColumns(String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set level role value");
	}

	public void setGuildLevelUpRoleColumnBlankData(String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set blank level role value");
	}

	/**
	 * @deprecated - replaced by {@link #getGuildKnownLevelUpRoleCount(long)}
	 * @param guild
	 * @return
	 */
	@Deprecated
	public int getGuildLevelUpRoles(long guild)
	{
		String statement = "SELECT knownLevelRoles FROM " + db + "levelRoles WHERE id = " + guild + ";";
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
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for knownLevelRoles for guild " + guild, statement, e);
		}

		return query;
	}

	public void setGuildLevelUpRoles(long guild, String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set guild level roles for guild " + guild);
	}

	public Long getSingularRole(String tableName, String column, Long guild)
	{
		String statement = "SELECT " + column + " FROM " + db + tableName + " WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		Long query = null;
		try
		{
			while (rs.next())
			{
				query = rs.getLong(column);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the singular role with the provided statment", statement, e);
		}

		return query;
	}

	public Long getGuildSingularLevelUpRole(Long guild, String column)
	{
		String statement = "SELECT " + column + " FROM " + db + "lvlroles WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		Long query = null;
		try
		{
			while (rs.next())
			{
				query = rs.getLong(column);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the singular role using", statement, e);
		}

		return query;
	}

	public void setGuildSingularLevelUpRole(String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set the level role data");
	}

	public void setGuildColorRolesEntry(String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to set the color roles data");
	}

	public void setGuildSizeChannel(long guild, long inputId)
	{
		performUpdate("UPDATE " + db + "guildInfo SET serverSizeChannel = " + inputId + " WHERE id = " + guild + ";", LogType.DATABASE_ERROR, "Unable to set value for serverSizeChannel for guild " + guild);
	}

	public Long getGuildSizeChannel(long guild)
	{
		Long query = null;
		String statement = "SELECT guildSizeChannel FROM " + db + "guildInfo WHERE id = " + guild + ";";
		ResultSet rs = performQuery(statement);

		try
		{
			while (rs.next())
			{
				query = rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Unable to find the resource for guildSizeChannel for guild " + guild, statement, e);
		}

		return query;
	}

	/*
	 * ##########################
	 * TODO: CHECKERS AND HELPERS
	 * ##########################
	 */

	public void deleteColumn(long guild, String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to delete the requested column from the table for guild " + guild);
	}

	public void deleteRow(String table, String column, Long input)
	{
		performUpdate("DELETE FROM " + db + table + " WHERE " + column + " = " + input + ";", LogType.DATABASE_ERROR, "Unable to delete row using provided statment");
	}

	public void deleteRow(String table, String column, String input)
	{
		performUpdate("DELETE FROM " + db + table + " WHERE " + column + " = '" + input + "';", LogType.DATABASE_ERROR, "Unable to delete row using provided statment");
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
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to complete singular statement with the provided statement");
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
		performUpdate(statement, LogType.DATABASE_ERROR, "Unable to complete singular statement with the provided statement");
	}

	public int countRows(String table)
	{
		int count = 0;
		String statement = "SELECT COUNT(*) FROM " + db + table + ";";

		try
		{
			//ResultSet query = performQuery("SELECT COUNT(*) FROM tigerguard_db." + table).executeQuery();
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
	 * Check if the provided table exists in the database.
	 *
	 * @param table - title of the table to be found.
	 * @return		- Boolean, true is table exists, else false.
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
	 * Method to insert data into a row or column as specified by the passed statement for the first time
	 *
	 * @param statement - The statement handling the first-insertion
	 */
	public void firstInsertion(String statement)
	{
		performUpdate(statement, LogType.DATABASE_ERROR, "Failure with first-insertion using the provided statement");
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
	 * Method for creating a table in the DB.
	 *
	 * @param statement	- The statement for creating the table.
	 */
	public void createTable(String statement)
	{
		performUpdate("CREATE TABLE " + db + statement, LogType.DATABASE_ERROR, "Failure creating table from the provided statement");
	}

	/**
	 * Check if the guild is in the guildInfo database file.
	 *
	 * @param guild - Long ID of the guild to search for.
	 * @return	    - Boolean, true is guild is present, else false.
	 */
	public boolean checkForGuild(Long guild)
	{
		boolean found = false;
		String statement = "SELECT EXISTS(SELECT * FROM " + db + "guildInfo WHERE id = " + guild + ") AS EXISTS_BY_NAME;";

		try
		{
			ResultSet rs = performQuery(statement);
			found = rs.next();
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_WARNING, "Error occurred while checking if guild " + guild + " exists in database", statement, e);
		}

		return found;
	}

	/**
	 * Check if the specified row is found in the specified table in the DB with a specifier in the where clause.
	 *
	 * @param table		 - The table to search in.
	 * @param column	 - The column to search by.
	 * @param searchItem - The data the column should have if it exists.
	 * @return
	 */
	public boolean checkRow(String table, String column, Long searchItem)
	{
		boolean found = false;
		String statement = String.format("SELECT EXISTS(SELECT * FROM " + db + "%s WHERE %s = %d) AS EXISTS_BY_NAME", table, column, searchItem);

		try
		{
			ResultSet rs = performQuery(statement);
			//ResultSet rs = prepare(input).executeQuery();
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
	 * Check if the specified row is found in the specified table in the DB with a specifier in the where clause.
	 *
	 * @param table		 - The table to search in.
	 * @param column	 - The column to search by.
	 * @param searchItem - The data the column should have if it exists.
	 * @return
	 */
	public boolean checkRow(String table, String column, String searchItem)
	{
		boolean found = false;
		String statement = String.format("SELECT EXISTS(SELECT * FROM " + db + "%s WHERE `%s` = '%s') AS EXISTS_BY_NAME", table, column, searchItem);

		try
		{
			ResultSet rs = performQuery(statement);
			//ResultSet rs = prepare(input).executeQuery();
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

	/**
	 * Return the Long value of the selection statement.
	 *
	 * @param guild		 - The guild ID to look by.
	 * @param searchItem - The ID in question to look for.
	 * @return
	 */
	public Long selectSingle(String table, long guild, long searchItem)
	{
		Long result = null;
		String statement = String.format("SELECT %s FROM " + db + "%s WHERE id = %d", searchItem, table, guild);
		try
		{
			ResultSet rs = performQuery(statement);
			//ResultSet query = prepare(statement).executeQuery();

			while (rs.next())
			{
				result = rs.getLong(1);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Error getting SELECT request using statement", statement, e);
		}

		return result;
	}

	/**
	 * @param table				- The table the data is within
	 * @param column			- The column the data is within
	 * @param constraintColumn	- The constraint column in the WHERE portion
	 * @param constraintData	- The constraint item in the WHERE portion
	 * @param objectType		- String or Long
	 * @return
	 */
	public Object selectSingle(Object table, Object column, Object constraintColumn, Object constraintData, String objectType)
	{
		String statement = String.format("SELECT %s FROM " + db + "%s WHERE %s = %s", column.toString(), table.toString(), constraintColumn.toString(), constraintData.toString());

		try
		{
			ResultSet rs = performQuery(statement);
			//ResultSet query = prepare(statement).executeQuery();

			while (rs.next())
			{
				switch (objectType.toLowerCase())
				{
					case "string":
					{
						return rs.getString(1);
					}
					case "long":
					{
						return rs.getLong(1);
					}
					default:
						throw new IllegalArgumentException("Unsupported data type: " + objectType);
				}
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure with select statement to return a value", statement, e);
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
	public void submitEmbed(long guild, String embedName, String type, String color, String title, String body)
	{
		String statement = String.format("INSERT INTO " + db + guild + "embeds (`name`, `type`, title, color, body) VALUES ('%s','%s','%s','%s','%s');", embedName, type, title, color, body);

		performUpdate(statement, LogType.DATABASE_ERROR, "Failure setting embed data for guild " + guild);
	}



	public void basicDelete(String table, String column, Object input)
	{
		String statement = "DELETE FROM " + db + table + " WHERE " + column + " = " + input + ";";
		performUpdate(statement, LogType.DATABASE_ERROR, "Failure deleting entry from database");
	}

	public Integer selectColorRolesCount(Long guild)
	{
		String statement = "SELECT color1, color2, color3, color4, color5, color6, color7, color8, color9, color10, color11, color12, color13, color14, color15, color16, color17, color18 FROM " + db + "colorRoles WHERE id = " + guild;
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
		String statement = "SELECT color1, color2, color3, color4, color5, color6, color7, color8, color9, color10, color11, color12, color13, color14, color15, color16, color17, color18 FROM " + db + "colorRoles WHERE id = " + guild;
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
	 * ####################
	 * TODO: TABLE HANDLERS
	 * ####################
	 */

	public void newGuildEntry(Long guild)
	{
		performUpdate("INSERT INTO " + db + "guildInfo (id) VALUES (" + guild + ");", LogType.DATABASE_ERROR, "Failure setting guild " + guild + " into DB.");
	}

	public void createGuildColorRolesEntry(Long guild)
	{
		String statement = "INSERT INTO " + db + "colorRoles (id, embed, color1, color2, color3, color4, color5, color6, color7, color8, color9, color10, color11, color12, color13, color14, color15, color16, color17, color18)"
				+ "VALUES (" + guild + ", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);";

		performUpdate(statement, LogType.DATABASE_ERROR, "Error inserting default color null entries for guild " + guild + "\n SQL Statement: " + statement);
	}

	/*
	 * ########################################
	 * TODO: SERVER LEVELING AND XP TABLE LOGIC
	 * ########################################
	 */

	/**
	 * Fill in the RankChart using the formula. The first value of the array will be the base value of 75.
	 * maxRankLevel is set to 40 but due to Java int[] it will read as 0-39, our logic will accommodate level requirements
	 *
	 * For max level handling in action:
	 * @see TigerGuardDB#generateRankCard(SlashCommandInteractionEvent)
	 * @see TigerGuardDB#updateRankXp(Guild, Member, int, MessageReceivedEvent, GuildVoiceUpdateEvent)
	 *
	 * Formula:
	 * rankLevels[a] 				  -> the value at index 'a' in array rankLevels[]
	 * (Math.round("math logic") + 4) -> round to nearest whole number as int (3, 7, 12, etc)
	 * / 5 * 5 						  -> modifiy number so that is always ended in 0 or 5 (5, 20, 175, etc)
	 */
	protected void setupRankChartList()
	{
		logger.log(LogType.INFO, "Max Level:" + maxRankLevel);
		logger.log(LogType.INFO, "Format:\nLevel # | Incremental req-xp increase | XP range min-max | Total XP once level up occurs");
		for (int a = 0; a <= maxRankLevel; a++)
		{
			if (a != 0) //a is not equal to 0
			{
				if (a < maxRankLevel) //ranks 1 through 1 below max
				{
					if (a <= 3) //1-3 (0 omitted due to first if check)
				    {
				    	rankLevels[a] = (Math.round((rankLevels[a-1]+(a*135))) + 4) / 5 * 5;
				    }
				    else if (a > 3 && a < 11) //4-10
				    {
				    	rankLevels[a] = (int) (Math.round((rankLevels[a-1]+(a*(135+(0.03*a)))*(1.15+(0.02*a)))) + 4) / 5 * 5;
				    }
				    else if (a > 10 && a < 21) //11-20
				    {
				    	rankLevels[a] = (int) (Math.round((rankLevels[a-1]+(a*(165+(0.03*a)))*(1.17+(0.02*a)))) + 4) / 5 * 5;
				    }
				    else //21 and beyond
				    {
				    	rankLevels[a] = (int) (Math.round((rankLevels[a-1]+(a*(165+(0.05*a)))*(1.19+(0.03*a)))) + 4) / 5 * 5;
				    }

					System.out.println(String.format("Level %1$s | %2$s | %3$s-%4$s | %4$s", a, (rankLevels[a]-rankLevels[a-1]), rankLevels[a-1], rankLevels[a]));
				}
				else //max rank
				{
					System.out.println(String.format("Level %1$s | --:-- | %2$s-%3$s | %3$s", a, rankLevels[a-1], "max"));
				}
			}
			else //rank 0
			{
				rankLevels[a] = 75; //Base XP to reach next level (ie 0 to 1)
				System.out.println(String.format("Level %1$s | %2$s | %1$s-%2$s | %2$s", a, rankLevels[a]));
			}
		}
	}

	public int getMaxRankLevel()
	{
		return maxRankLevel;
	}

	/*
	 * Per-server xp table
	 */
	public void createGuildXpTable(Guild guild)
	{
		String guildMeshXp = guild.getIdLong() + "xp";
		String statement = "CREATE TABLE " + db + guildMeshXp + "(id varchar(45), level int(11), xp int(11), activeRole varchar(45);";

		try
		{
			performUpdate(statement, LogType.XP_DATABASE_ERROR, "Error creating guild xp table using");

			guild.loadMembers().onSuccess(members -> {
				List<Member> memberList = members.stream().filter(a -> !a.getUser().isBot()).toList();

				PreparedStatement ps = perform(statement);

				for (Member member : memberList)
				{
					String innerStatement = "INSERT INTO " + db + guildMeshXp + " (id, level, xp, activeRole) VALUES (" + member.getIdLong() + "0, 0, null);";

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
	public void insertUserIntoGuildXPTable(String table, long user)
	{
		performUpdate("INSERT INTO " + db + table + " (id, level, xp, activeRole) VALUES (" + user + ", 0, 0, null);", LogType.XP_DATABASE_ERROR, "Unable to insert user " + user + " into the table " + table);
	}

	/*
	 * Insert a Guild into the LevelRole table.
	 */
	public void insertGuildIntoLevelRoleTable(Long guild)
	{
		performUpdate("INSERT INTO " + db + "levelRoles SET id = " + guild + ";", LogType.DATABASE_ERROR, "Error inserting new entry into levelRoles table for guild " + guild);
	}

	public void firstInsertionGuildKnownLevelRoleValue(Long guild, int initialInput)
	{
		performUpdate("UPDATE " + db + "lvlroles SET knownLevelRoles = " + initialInput + " WHERE id = " + guild + ");", LogType.DATABASE_ERROR, "Error inserting knownLevelRoles value for guild " + guild);
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
			ResultSet rs = performQuery("SELECT level, xp FROM " + db + table + " WHERE id = " + member.getIdLong() + ";");

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

		if (TigerGuard.isDebugMode())
		{
			logger.log(LogType.DEBUG, "member's level and xp are: " + memberLevel + " | " + memberXP);
		}

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
		if (memberLevel < maxRankLevel)
		{
			try
			{
	            msgBuilder.addFiles(FileUpload.fromData(messageFactory.createRankImage(member, guild.getRoleById(levelRole).getName(), memberLevel, memberXP, rankLevels[memberLevel], rankLevels[memberLevel-1], false), "rankCard.png"));

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
	            msgBuilder.addFiles(FileUpload.fromData(messageFactory.createRankImage(member, guild.getRoleById(levelRole).getName(), memberLevel, memberXP, rankLevels[maxRankLevel-1], rankLevels[memberLevel-1], true), "rankCard.png"));

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

	private Long getGuildLevelRoleFromGuild(long guild, long member, int memberLevel)
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

	public void voiceStatusBegin(long member, long guild)
	{
		if (!checkRow("voiceTracker", "id", member))
		{
			performUpdate(String.format("INSERT INTO %svoiceTracker (id, init, guild) VALUES (%s, %d, %d);", db, member, System.currentTimeMillis(), guild), LogType.DATABASE_ERROR, "Unable to set member " + member + " into timeTracker");
		}
	}

	public long voiceStatusEnd(long member)
	{
		String queryS = "SELECT init FROM " + db + "voiceTracker WHERE id = " + member + ";";
		ResultSet rs = performQuery(queryS);
		long query = 0;

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

		String statement = "DELETE FROM " + db + "voiceTracker WHERE id = " + member + ";";
		try
		{
			performUpdate(statement, LogType.DATABASE_ERROR, "Failure deleting entry for " + member + " from time tracker table.");
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
	public void checkIfUserExistsInGlobalData(long member)
	{
		if (!checkRow("globalUserData", "id", member))
		{
			performUpdate("INSERT INTO " + db + "globalUserData (id, level, xp, bgimage) VALUES (" + member + ", 0, 0, 1);", LogType.DATABASE_ERROR, "Unable to set member " + member + " into globalUserData");
		}
	}

	public void updateUserRankImage(long member, int value)
	{
		performUpdate("UPDATE " + db + "globalUserData SET bgimage = " + value + " WHERE id = " + member + ";", LogType.DATABASE_ERROR, "Unable to set bgimage for member " + member + " in globalUserData for the input of " + value);
	}

	public Integer getUserRankImage(long member)
	{
		String statement = "SELECT bgimage FROM " + db + "globalUserData WHERE id = " + member + ";";
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

		if (dates.getBoostDateStatus())
		{
			xpGain = (int)Math.round(xpGain * dates.getBoostValue());
		}

		String guildMesh = guild.getIdLong() + "xp";
		int memberXP = 0;
		int memberLevel = 0;
		MessageCreateBuilder msgBuilder = new MessageCreateBuilder();

		if (getGuildKnownLevelUpRoleCount(guild.getIdLong()) != 0)
		{
			String initialQuery = String.format("SELECT level, xp FROM %s%s WHERE id = %s", db, guildMesh, member.getIdLong());
			ResultSet rs = performQuery(initialQuery);

			try
			{
				while (rs.next())
				{
					//Set our temp instance ints to be equal to the returned db values.
					memberLevel = rs.getInt("level");
					memberXP = rs.getInt("xp");
				}
			}
			catch (Exception e)
			{
				logger.logErr(LogType.RANK_ERROR, "Failure retrieving xp_data for " + member.getIdLong() + " for guild " + guild.getIdLong(), initialQuery, e);
			}

			//Begin checking if the current xp amount plus gained xp is more than 1 level worth of increase.
			int increasedAmount = 0;
			int checker = memberLevel;
			boolean checkFinished = false;

			while (!checkFinished)
			{
				if ((memberXP + xpGain) > rankLevels[checker])
				{
					checker++;
					increasedAmount++;
				}
				else
				{
					memberLevel += increasedAmount;
					checkFinished = true;
					break;
				}

				if (TigerGuard.isDebugMode())
				{
					logger.log(LogType.DEBUG, "loop check: " + memberLevel + " | " + checker + " | " + increasedAmount);
				}
			}
			Long levelRole = null;
			if (memberLevel != 0)
			{
				levelRole = getGuildLevelRoleFromGuild(guild.getIdLong(), member.getIdLong(), memberLevel);
			}

			//If userLevel is not equal to max level, max level is given artifically once the user reaches the last index of rankLevels[] xp requirement.
			if (memberLevel != maxRankLevel)
			{
				//If user's level is 1 less than max level and xpGain would lead to leveling up. Cap xp to max total xp possible and update level to max level.
				//maxRankLevel-1 is being used because our scale starts at 0 and goes up to max 39 (max, this equals 40). the minus 1 gives up the last xp level up req. for the highest level in the rankLevels array. Then we give 40 artifically.
				if ((memberLevel == (maxRankLevel-1)) && ((memberXP + xpGain) >= rankLevels[maxRankLevel-1]))
				{
					String statement = "UPDATE " + db + guildMesh + " SET xp = " + rankLevels[maxRankLevel-1] + ", level = " + maxRankLevel + " WHERE id = " + member.getIdLong() + ";";
					performUpdate(statement, LogType.DATABASE_ERROR, "Failure updating xp data");

					try
					{
						msgBuilder.addFiles(FileUpload.fromData(messageFactory.createRanklevelUpImage(guild, member, guild.getRoleById(levelRole).getName(), memberLevel, false), "rankCard.png"));
					}
					catch (Exception e)
					{
						logger.logErr(LogType.ERROR, "Failure generating rank card from updateRankXp method.", statement, e);
					}

					if (hasValue("guildInfo", "botChannel", "id", guild.getIdLong()))
					{
						guild.getTextChannelById(getGuildLevelChannel(guild.getIdLong())).sendMessage(msgBuilder.build()).queue();
					}
					else
					{
						if (messageEvent != null)
						{
							messageEvent.getChannel().sendMessage(msgBuilder.build()).queue();
						}
						else
						{
							if (getGuildBotSpamChannel(guild.getIdLong()) != null)
							{
								guild.getTextChannelById(getGuildLevelChannel(guild.getIdLong())).sendMessage(msgBuilder.build()).queue();
							}
							else
							{
								guild.getDefaultChannel().asTextChannel().sendMessage(msgBuilder.build()).queue();
							}
						}
					}

					/*
					 * TODO - advanced logic for preference role(s) might need to be handled here. Debug for edge-cases
					 * For now, the following will eventually be expanded to actually handle these cases, but commented out for now.
					 */
					//if (!hasValue(guild.getIdLong() + "xp", "activeRole", "id", member.getIdLong()))
					//else ...

					//Current usage, test for edge cases
					guild.addRoleToMember(member, guild.getRoleById(getGuildLevelRoleFromGuild(guild.getIdLong(), member.getIdLong(), maxRankLevel))).queue();
				}
				//Else, user's level is not 1 less than max level, do...
				else
				{
					//If user's xp plus the gianed xp is less than the level up requirement, update only the xpValue.
					//if ((userXP + xpGain) < ((rankLevels[userLevel]-rankLevels[userLevel-1])))
					if ((memberXP + xpGain) < (rankLevels[memberLevel]) && increasedAmount == 0)
					{
						String statement = "UPDATE " + db + guildMesh + " SET xp = " + (memberXP+xpGain) + " WHERE id = " + member.getIdLong() + ";";
						performUpdate(statement, LogType.DATABASE_ERROR, "Failure updating xp data using prompt");
					}

					//Else, user's xp plus the gained xp is equal to or greater than the level up requirement, update the xpValue and xpLevel.
					else
					{
						//memberLevel+1
						String statement = "UPDATE " + db + guildMesh + " SET xp = " + (memberXP+xpGain) + ", level = " + memberLevel + " WHERE id = " + member.getIdLong() + ";";
						performUpdate(statement, LogType.DATABASE_ERROR, "Failure updating xp data using prompt");

						try
						{
							msgBuilder.addFiles(FileUpload.fromData(messageFactory.createRanklevelUpImage(guild, member, guild.getRoleById(levelRole).getName(), memberLevel, false), "rankCard.png"));
						}
						catch (Exception e)
						{
							logger.log(LogType.ERROR, "Failure generating rank card from updateRankXp method.");
							e.printStackTrace();
						}

						if (messageEvent != null)
						{
							Long levelChannel = getGuildLevelChannel(guild.getIdLong());
							if (levelChannel != null)
							{
								guild.getTextChannelById(levelChannel).sendMessage(msgBuilder.build()).queue();
							}
							else
							{
								messageEvent.getChannel().sendMessage(msgBuilder.build()).queue();
							}
						}
						else
						{
							if (getGuildBotSpamChannel(guild.getIdLong()) != null)
							{
								guild.getTextChannelById(getGuildLevelChannel(guild.getIdLong())).sendMessage(msgBuilder.build()).queue();
							}
							else
							{
								guild.getDefaultChannel().asTextChannel().sendMessage(msgBuilder.build()).queue();
							}
						}

					}

					if (memberLevel != 0)
					{
						if (increasedAmount != 0)
						{
							for (int a = 1; a <= memberLevel; a++)
							{
								guild.addRoleToMember(member, guild.getRoleById(getGuildLevelRoleFromGuild(guild.getIdLong(), member.getIdLong(), a))).queue();
							}
						}
						else
						{
							guild.addRoleToMember(member, guild.getRoleById(getGuildLevelRoleFromGuild(guild.getIdLong(), member.getIdLong(), memberLevel))).queue();
						}
					}
				}
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
		if (hasValue(guild.getIdLong() + "xp", "activeRole", "id", member.getIdLong()))
		{
			logger.log(LogType.DEBUG, "Member " + member.getEffectiveName() + " is level " + level);

			for (int a = 0; a < (level-1); a++)
			{
				Role lvlRole = guild.getRoleById(roles.get(a));
				logger.log(LogType.DEBUG, "Iterating through..." + a + " >> " + lvlRole.getName());

				if (memberRoles.contains(lvlRole))
				{
					guild.removeRoleFromMember(member, lvlRole).queue();
				}
			}
		}

		//TODO - Possible edge-case identified, test in next iteration - relates to StringSelect file.
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
		String statement = "SELECT * FROM " + db + "lvlroles WHERE id = " + guild + ";";

		try
		{
			ResultSet rs = performQuery(statement);

			while (rs.next())
			{
				long count = rs.getLong(2);

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
	public int getGuildMemberLevel(long guild, long member)
	{
		int result = 0;

		String statement = String.format("SELECT level FROM " + db + "%s WHERE id = %d", guild + "xp", member);
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
	 * ######################
	 * TODO: POLL TABLE LOGIC
	 * ######################
	 */

	//TODO - fix the reference to 'polltable' - current guildInfo table will not support this
	public void createGuildPollTable(long guild)
	{
		performUpdate("CREATE TABLE " + db + guild + "polls (id VARCHAR(45), channeltype VARCHAR(45), channel VARCHAR(45), polltype VARCHAR(20), endtime VARCHAR(45), initiated VARCHAR(10));", LogType.DATABASE_ERROR, "Failure creating poll table for guild " + guild);
		performUpdate("UPDATE " + db + "guildInfo SET pollTable = '" + guild + "polls' WHERE id = " + guild, LogType.DATABASE_ERROR, "Failuring updating poll table for guild " + guild + " after table creation");
	}

	public void addGuildToTempPollTable(long guild)
	{
		performUpdate("INSERT INTO " + db + "tempPollData (id) VALUES (" + guild + ")", LogType.DATABASE_ERROR, "Failure inserting guild " + guild + " into tempPollData.");
	}

	public void setPollTempTimeData(long guild, int value, String type)
	{
		String statement = String.format("UPDATE " + db + "tempPollData SET timetype = '%s', amount = '%d' WHERE id = %d", type, value, guild);
		performUpdate(statement, LogType.DATABASE_ERROR, "Failure setting temp poll data");
	}

	public Pair<String, Integer> getPollTempData(long guild)
	{
		String statement = "SELECT timetype, amount FROM " + db + "tempPollData WHERE id = " + guild + ";";
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
	public void pollCreation(long guild, long pollId, ChannelType channelType, long channel, String pollType)
	{
		if (!checkForTable(guild + "polls"))
		{
			this.createGuildPollTable(guild);
		}

		Pair<String, Integer> data = getPollTempData(guild);
		String timeType = data.getValue0();
		String statementCreate = "CREATE TABLE " + db + "poll" + pollId + "(voter VARCHAR(45), vote VARCHAR(1));";

		if (timeType.equals("minute") && data.getValue1() <= 10) //If Poll might end and be missed between the poll-check loop
		{
			String statementB = "INSERT INTO " + db + guild + "polls (id, channeltype, channel, polltype, endtime, initiated) VALUES ('" + pollId + "', '" + channelType.toString().toLowerCase() + "', '" + channel + "', '" + pollType + "', '" + (System.currentTimeMillis() + (data.getValue1() * 60000)) + "', true);";

			performUpdate(statementCreate, LogType.DATABASE_ERROR, "Failure creating guild poll table");
			performUpdate(statementB, LogType.DATABASE_ERROR, "Failure inserting into guild poll table");

			new TigerPolls().designatedQuickStart(guild, pollId, channelType, channel, pollType, (System.currentTimeMillis() + (data.getValue1() * 60000)));
		}
		else //All other polls (those that won't miss the next poll-check loop prior to expiring)
		{
			long currentTime = System.currentTimeMillis();
			long endTime = 0;

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

			String statementB = "INSERT INTO " + db + guild + "polls` (id, channeltype, channel, polltype, endtime, initiated) VALUES ('" + pollId + "', '" + channelType.toString().toLowerCase() + "', '" + channel + "', '" + pollType + "', '" + endTime + "', false);";

			performUpdate(statementCreate, LogType.DATABASE_ERROR, "Failure creating guild poll table");
			performUpdate(statementB, LogType.DATABASE_ERROR, "Failure inserting into guild poll table");
		}
	}

	public void pollUpdateInitiatedCheck(long guild, long poll)
	{
		String statement = "UPDATE " + db + guild + "polls SET initiated = true WHERE id = " + poll + ";";
		performUpdate(statement, LogType.DATABASE_ERROR, "Failure updating poll table to show the poll is being checked");
	}

	public void pollVoteUpdate(long pollId, long member, char vote)
	{
		String statement = "SELECT 1 FROM " + db + "poll" + pollId + " WHERE voter = " + member + " LIMIT 1;";
		ResultSet rs = performQuery(statement);

		try
		{
			if (!rs.first())
			{
				performUpdate(String.format("INSERT INTO %s (voter, vote) VALUES (%s, '%c');", db + "poll" + pollId, member, vote), LogType.DATABASE_ERROR, "Failure inserting member vote status in poll:\n[Poll] " + pollId + " | [Member]" + member + " | [vote] " + vote);
			}
			else
			{
				performUpdate(String.format("UPDATE %s SET vote = '%c' WHERE voter = %s", db + "poll" + pollId, vote, member), LogType.DATABASE_ERROR, "Failure updating member vote status in poll:\n[Poll] " + pollId + " | [Member]" + member + " | [vote] " + vote);
			}
		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure returning poll vote status for [Poll] " + pollId + " | [Member]" + member + " | [vote] " + vote, statement, e);
		}
	}

	public ArrayList<Pair<Long, String>> getPollTablesBasicData()
	{
		ArrayList<Pair<Long, String>> polls = new ArrayList<>();
		String statement = "SELECT id, pollTable FROM " + db + "guildInfo";
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
		String statement = "SELECT id, channeltype, channel, polltype, endtime, initiated FROM " + db + pollTable + ";";
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

	public Pair<Integer, Integer> pollCollectResultsDuo(long guild, long pollId)
	{
		int yay = 0;
		int nay = 0;

		String statement = "SELECT vote FROM " + db + "poll" + pollId + ";";
		ResultSet rs = performQuery("SELECT vote FROM " + db + "poll" + pollId + ";");

		try
		{
			while (rs.next())
			{
				if (rs.getString(1).equals("y"))
				{
					yay++;
				}
				else
				{
					nay++;
				}
			}

			pollDeletion(guild, pollId);

		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure finalizing results for the poll "  + pollId, statement, e);
		}

		return Pair.with(yay, nay);
	}

	public Triplet<Integer, Integer, Integer> pollCollectResultsTrio(long guild, long pollId)
	{
		int yay = 0;
		int abs = 0;
		int nay = 0;

		String statement = "SELECT vote FROM " + db + "poll" + pollId + ";";
		ResultSet rs = performQuery(statement);

		try
		{
			while (rs.next())
			{
				if (rs.getString(1).equals("y"))
				{
					yay++;
				}
				else if (rs.getString(1).equals("a"))
				{
					abs++;
				}
				else
				{
					nay++;
				}
			}

			pollDeletion(guild, pollId);

		}
		catch (Exception e)
		{
			logger.logErr(LogType.DATABASE_ERROR, "Failure finalizing results for the poll "  + pollId, statement, e);
		}

		return Triplet.with(yay, abs, nay);
	}

	private void pollDeletion(long guild, long poll)
	{
		String statementDelete = String.format("DELETE FROM %s WHERE id = %s", db + guild + "polls", poll);
		String statementDrop = String.format("DROP TABLE %s;", db + "poll" + poll);

		performUpdate(statementDelete, LogType.DATABASE_ERROR, "Failure deleting poll from database");
		performUpdate(statementDrop, LogType.DATABASE_ERROR, "Failuring dropping table from database");
	}
}