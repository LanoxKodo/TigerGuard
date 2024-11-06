package lanoxkododev.tigerguard.polls;

import java.util.Collections;

import org.javatuples.Pair;
import org.javatuples.Sextet;
import org.javatuples.Triplet;

import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;

public class PollClosureThread extends Thread {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	Long guildVal;
	Long poll;
	ChannelType channelType;
	Long channel;
	String type;
	Long endTime;
	JDA jda;
	String title = "";

	public PollClosureThread(Sextet<Long, Long, ChannelType, Long, String, Long> pollData)
	{
		guildVal = pollData.getValue0();
		poll = pollData.getValue1();
		channelType = pollData.getValue2();
		channel = pollData.getValue3();
		type = pollData.getValue4();
		endTime = pollData.getValue5();
	}

	@Override
	public void run()
	{
		process();
	}

	private void process()
	{
		jda = TigerGuard.getTigerGuard().getJDA();

		jda.getGuildById(guildVal).getTextChannelById(channel).retrieveMessageById(poll).queue(a -> {
			title = a.getEmbeds().get(0).getTitle();
		});

		try
		{
			long sleepTime = endTime - System.currentTimeMillis();

			if (sleepTime > 0)
			{
				Thread.sleep(sleepTime);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		switch (type)
		{
			case "Duo":
				pollDuo(title);
				break;
			case "TrioA":
				pollTrioA(title);
				break;
			case "TrioB":
				pollTrioB(title);
				break;
		}
	}

	private void pollDuo(String original)
	{
		Pair<Integer, Integer> results = tigerGuardDB.pollCollectResultsDuo(guildVal, poll);
		int yay = results.getValue0();
		int nay = results.getValue1();
		int totalVotes = yay + nay;
		int winThreshold = 0;
		
		String body = "";
		
		if (totalVotes == 0)
		{
			body += "***Well, umm...*** **|** Astonishingly the vote was so fantastic that no one voted!\n"
				+ "That, or some heinous strings-attached yarn (bug) ruined the vote process! *Do report the bug if this is the case!*";
		}
		else
		{
			winThreshold = (51 * totalVotes) / 100;
			body += "*Poll title: " + original + "*\n\nTotal votes received: **" + totalVotes + "**. The winning threshold is at **" + winThreshold + "**.\n\n***Results:***\n"
				+ "**Yays:** ***" + yay + "***\n"
				+ "**Nays:** ***" + nay + "***\n\n";
			
			if (yay >= winThreshold) body += "***PASSED*** **|** The poll has passed with a majority in-favor vote count.";
			else if (nay >= winThreshold) body += "***FAILED*** **|** The poll has failed without a majority in-favor vote count.";
			else body += "***Hmm...*** **|** It seems an issue in calculating results occurred, report this as a bug with the poll result values.";
		}
		
		//jda.getGuildById(guildVal).getTextChannelById(channel).editMessageEmbedsById(poll, embedder.simpleEmbed("𝙋𝙤𝙡𝙡 𝙃𝙖𝙨 𝙁𝙞𝙣𝙞𝙨𝙝𝙚𝙙", null, null, ColorCodes.POLL, body)).setComponents(Collections.emptyList()).queue();
		jda.getGuildById(guildVal).getTextChannelById(channel).editMessageEmbedsById(poll, embedder.pollDuoEmbed("𝙋𝙤𝙡𝙡 𝙃𝙖𝙨 𝙁𝙞𝙣𝙞𝙨𝙝𝙚𝙙", ColorCodes.POLL, body, yay, nay))
			.setComponents(Collections.emptyList()).queue();
	}

	private void pollTrioA(String original)
	{
		Triplet<Integer, Integer, Integer> results = tigerGuardDB.pollCollectResultsTrio(guildVal, poll);
		int yay = results.getValue0();
		int abs = results.getValue1();
		int nay = results.getValue2();
		int totalVotes = yay + abs + nay;
		int winThreshold = 0;

		String body = "";

		if (totalVotes == 0)
		{
			body += "***Well, umm...*** **|** Astonishingly the vote was so fantastic that no one voted!\n"
				+ "That, or some heinous strings-attached yarn (bug) ruined the vote process! (*Do report the bug if this is the case!*)";
		}
		else
		{
			int modVal = yay + nay; //Abstains-ignored vote, exclude them from taking up winning threshold logic. See pollTrioB for abstain-inclusive poll logic.
			if (totalVotes == 1) winThreshold = 1;
			else winThreshold = (51 * modVal) / 100;
			
			body += "*Poll title: " + original + "*\n\nTotal votes received: **" + totalVotes + "**. The winning threshold is at **" + winThreshold + "**\n\n***Results:***\n"
				+ "**Yays:** ***" + yay + "***\n"
				+ "**Abstains:** ***" + abs + "***\n"
				+ "**Nays:** ***" + nay + "***\n\n";
			
			if (yay >= winThreshold) body += "***PASSED*** **|** The poll has passed with a majority in-favor vote count.";
			else if (!(yay >= winThreshold)) body += "***FAILED*** **|** The poll has failed without a majority in-favor vote count.";
			else body += "***Hmm...*** **|** It seems an issue in calculating results occurred, report this as a bug with the poll result values.";
		}

		endProcess(body, Triplet.with(yay, nay, abs));
	}

	private void pollTrioB(String original)
	{
		Triplet<Integer, Integer, Integer> results = tigerGuardDB.pollCollectResultsTrio(guildVal, poll);
		int yay = results.getValue0();
		int abs = results.getValue1();
		int nay = results.getValue2();
		int totalVotes = yay + abs + nay;
		int winThreshold = 0;
		
		String body = "";

		if (totalVotes == 0)
		{
			body += "***Well, umm...*** **|** Astonishingly the vote was so fantastic that no one voted!\n"
				+ "That, or some heinous strings-attached yarn (bug) ruined the vote process! (*Do report the bug if this is the case!*)";
		}
		else
		{
			if (totalVotes == 1) winThreshold = 1;
			else winThreshold = (51 * totalVotes) / 100;
			
			if (yay >= winThreshold) body += "***PASSED*** **|** The poll has passed with a majority in-favor vote count.";
			else if (abs + nay >= winThreshold) body += "***FAILED*** **|** The poll has failed without a majority in-favor vote count.";
			else body += "***Hmm...*** **|** It seems an issue in calculating results occurred, report this as a bug with the poll result values.";
		}

		endProcess(body, Triplet.with(yay, nay, abs));
	}

	private void endProcess(String body, Triplet<Integer, Integer, Integer> vals)
	{
		jda.getGuildById(guildVal).getTextChannelById(channel).editMessageEmbedsById(poll, embedder.pollTrioEmbed("𝙋𝙤𝙡𝙡 𝙃𝙖𝙨 𝙁𝙞𝙣𝙞𝙨𝙝𝙚𝙙", ColorCodes.POLL, body,
			vals.getValue0(), vals.getValue1(), vals.getValue2())).setComponents(Collections.emptyList()).queue();
	}
}
