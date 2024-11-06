package lanoxkododev.tigerguard.polls;

import org.javatuples.Sextet;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.TigerLogs;
import net.dv8tion.jda.api.entities.channel.ChannelType;

public class TigerPolls {

	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();

	/**
	 * Method for initiating the review of all known polls that are not closed. The thread called from here will be passed the information to
	 * acquire a larger set on it's own and then check if the current time in millis is within 10 minutes of when the each polls expected close is.
	 * If the poll needs to be closed within 10 minutes then the thread will spawn a new thread for each poll as needed. When a poll's time is up, the
	 * thread will close the poll and recall the results and spit out an embed with such information.
	 */
	public void beginPollReview(boolean bootRunIn)
	{
		new Thread(new PollReviewThread(tigerGuardDB.getPollTablesBasicData(), bootRunIn)).start();
	}

	/**
	 * Method for immediately starting a thread for the poll. This is used for when a poll, mostly the 5 minute polls, is created because it can start
	 * and end prior to the 10 minute loop check for {@link #beginPollReview()} would take place, causing these poll items to be delayed otherwise if not
	 * called here.
	 *
	 * @param guild - The guild the poll is for.
	 * @param pollId - The ID of the poll (ie the embed message the poll is made from).
	 * @param channelType - The channelType that the poll is created in.
	 * @param channel - The channel ID the poll is in.
	 * @param type - The string identifier of which poll type is represented for logic purposes.
	 * @param endTime - The time the poll will end.
	 */
	public void designatedQuickStart(long guild, long pollId, ChannelType channelType, long channel, String type, long endTime)
	{
		new Thread(new PollClosureThread(Sextet.with(guild, pollId, channelType, channel, type, endTime))).start();
	}
}
