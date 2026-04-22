package lanoxkododev.tigerguard.events;

import java.time.Duration;

import lanoxkododev.tigerguard.ArrayUtilities;
import lanoxkododev.tigerguard.PermissionValidator;
import lanoxkododev.tigerguard.ThreadUtilities;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageEvents extends ListenerAdapter {

	TigerGuardDB tgdb = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	PermissionValidator permValidator = new PermissionValidator();

	@Override
	public void onMessageReceived(MessageReceivedEvent event)
	{
		Member member = event.getMember();

		if (member != null && !member.getUser().isBot())
		{
			Long guildID = event.getGuild().getIdLong();
			if (tgdb.checkForTable(guildID + "xp"))
			{
				if (!tgdb.checkRow(guildID + "xp", "member", member.getIdLong()))
				{
					tgdb.insertUserIntoGuildXPTable(guildID + "xp", member.getIdLong());
				}

				if (!ArrayUtilities.xpThrottle.contains(member))
				{
					long messageInput = event.getMessage().getContentRaw().chars().count();
					if (messageInput > 2000) messageInput = 2000;
					int calculatedVal = (messageInput <= 100) ? 10 : (int)Math.round(messageInput * .1);

					tgdb.updateGuildRankXp(event.getGuild(), member, calculatedVal, event, null);

					ArrayUtilities.xpThrottle.add(member);
					ThreadUtilities.createNewThread(_ -> ArrayUtilities.xpThrottle.remove(member), _ -> logger.log(LogType.RANK_ERROR, "Issue involving xpThrottle arraylist."), Duration.ofSeconds(30), false, false);
				}
			}
		}
	}
}