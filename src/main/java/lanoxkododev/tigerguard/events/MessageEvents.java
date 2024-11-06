package lanoxkododev.tigerguard.events;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Random;

import lanoxkododev.tigerguard.ArrayUtilities;
import lanoxkododev.tigerguard.ThreadUtilities;
import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.MessageFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class MessageEvents extends ListenerAdapter {

	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();

	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		Member member = event.getMember();

		if (member != null && !member.getUser().isBot())
		{
			//If the message starts with TigerGuard's prefix - for debugging or testing commands
			if (event.getMessage().getContentStripped().startsWith(TigerGuard.prefix))
			{
				prefixContent(member, event);
			}
			else //Everything else
			{
				if (tigerGuardDB.checkForTable(event.getGuild().getIdLong() + "xp"))
				{
					if (event.getChannel().getIdLong() != tigerGuardDB.getGuildBotSpamChannel(event.getGuild().getIdLong()) && event.getChannel().getIdLong() != tigerGuardDB.getGuildRuleChannel(event.getGuild().getIdLong())
						&& event.getChannel().getIdLong() != tigerGuardDB.getGuildSizeChannel(event.getGuild().getIdLong()))
					{
						if (!tigerGuardDB.checkRow(event.getGuild().getIdLong() + "xp", "id", member.getIdLong()))
						{
							tigerGuardDB.insertUserIntoGuildXPTable(event.getGuild().getIdLong() + "xp", member.getIdLong());
						}
						//Check if user is in the xpThrottle array or not. If not, grant xp and then add them to the xpThrottle array for a temp duration to prevent xp spam.
						if (!ArrayUtilities.xpThrottle.contains(member))
						{
							tigerGuardDB.updateGuildRankXp(event.getGuild(), member, new Random().nextInt(5, 26), event, null);

							ArrayUtilities.xpThrottle.add(member);
							ThreadUtilities.createNewThread(a -> ArrayUtilities.xpThrottle.remove(member), a -> logger.log(LogType.RANK_ERROR, "Issue involving xpThrottle arraylist."), Duration.ofSeconds(30), false, false);
						}
					}
				}
			}
		}
	}

	/*
	 * Commands accessible via the Prefix defined in the main file, usually designed for commandline/terminal/ide output logs only.
	 */
	private void prefixContent(Member member, MessageReceivedEvent event)
	{
		Guild guild = event.getGuild();

		String context = event.getMessage().getContentStripped();

		event.getMessage().delete().queue();
		switch (context)
		{
			case "~tgtestvoice":
				tigerGuardDB.voiceStatusBegin(member.getIdLong(), event.getGuild().getIdLong());
				break;
			case "~tgendvoice":
				tigerGuardDB.voiceStatusEnd(member.getIdLong());
				break;
			case "~tgtestposition":
				int roleSize = event.getGuild().getRoles().size();
				System.out.println("------------\n" + roleSize + "\n" + (roleSize-1) + "\n" + (roleSize-2));
				event.getGuild().getRoles().forEach(a -> System.out.println(a.getPosition() + " @ " + a));
				break;
			case "~tgcheckxpstatus":
				tigerGuardDB.updateGuildRankXp(event.getGuild(), event.getMember(), 0, event, null);
				break;
			case "~tgleveluptest":
				MessageCreateBuilder msgBuilder = new MessageCreateBuilder();
				try {
					msgBuilder.addFiles(FileUpload.fromData(new MessageFactory().createRanklevelUpImage(guild, event.getMember(), guild.getRoles().get(new Random().nextInt(1, guild.getRoles().size())).getName(), 1, true), "rankCard.png"));
				}
				catch (IOException e)
				{
					logger.logErr(LogType.ERROR, "~tgleveluptest failed to add image file", null, e);
				}
				event.getChannel().sendMessage(msgBuilder.build()).queue(a -> a.getChannel().sendMessage("[DEBUG] test-command=" + context + ". This is a test message with random variables at play for testing purposes.").queue());
				break;
			case "~tgRoles":
				List<Role> guildRoles = guild.getRoles();

				System.out.println("Guild roles:");
				int roles = 0;
				for (Role guildRole : guildRoles)
				{
					roles += 1;
					System.out.println(guildRole);
				}
				System.out.println("roles counted: " + (roles-1));
				break;
			case "~tgclear":
				MessageHistory history = MessageHistory.getHistoryFromBeginning(event.getChannel()).complete();
				List<Message> mess = history.getRetrievedHistory();

				//Check if message is by this bot, if yes then delete that message and reiterate through generating the message again with the new code.
				for (Message msg : mess)
				{
					if (msg.getAuthor().equals(User.fromId(994551850072277082L)))
					{
						System.out.println("Clearing message: " + msg.getIdLong() + " by user: " + msg.getAuthor());
						msg.delete().complete();
					}
				}
				break;
			case "~tgmembercount":
				guild.loadMembers().onSuccess(members -> {
					long memberCount = members.stream().filter(a -> !a.getUser().isBot()).count();
					System.out.println("Number of Members in this guild: " + memberCount);

					List<Member> memberList = members.stream().filter(a -> !a.getUser().isBot()).toList();
					for (Member memberI : memberList)
					{
						System.out.println("User ID and User Name: " + memberI.getIdLong() + " | " + memberI.getEffectiveName());
					}
				});
				break;
			case "~tgguildsize":
				ArrayUtilities.guildMemberCounter.add(guild);

				JoinLeaveEvents jole = new JoinLeaveEvents();
				ThreadUtilities.createNewThread(a -> jole.updateMemberCountChannel(guild), a -> logger.log(LogType.ERROR, "Issue involving guildMemberCounter arraylist."), Duration.ofSeconds(600), false, false);
				break;
			case "~tgsetguildinxpdatabase":
				tigerGuardDB.createGuildXpTable(guild);
				break;
			case "~tgrolecounter":
				List<Role> roleAmount = event.getGuild().getRoles();
				int roleCount = roleAmount.size();

				int a = 1;
				for (Role role : roleAmount)
				{
					System.out.println(role + " | " + a);
					a++;
				}

				System.out.println(roleCount);
				break;
		}
	}
}