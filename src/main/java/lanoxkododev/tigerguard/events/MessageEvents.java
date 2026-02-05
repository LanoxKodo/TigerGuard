package lanoxkododev.tigerguard.events;

import java.time.Duration;
import java.util.Random;

import lanoxkododev.tigerguard.ArrayUtilities;
import lanoxkododev.tigerguard.PermissionValidator;
import lanoxkododev.tigerguard.ThreadUtilities;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.TigerGuardDB.DB_Enums;
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
			if (tgdb.checkForTable(event.getGuild().getIdLong() + "xp"))
			{
				if (event.getChannel().getIdLong() != (Long)tgdb.getValue(DB_Enums.BOT_CHAN, "guild", guildID)
					&& event.getChannel().getIdLong() != (Long)tgdb.getValue(DB_Enums.RULE_CHAN, "guild", guildID)
					&& event.getChannel().getIdLong() != (Long)tgdb.getValue(DB_Enums.GUILD_SIZE_CHAN, "guild", guildID))
				{
					if (!tgdb.checkRow(guildID + "xp", "member", member.getIdLong()))
					{
						tgdb.insertUserIntoGuildXPTable(guildID + "xp", member.getIdLong());
					}
					//Check if user is in the xpThrottle array or not. If not, grant xp and then add them to the xpThrottle array for a temp duration to prevent xp spam.
					if (!ArrayUtilities.xpThrottle.contains(member))
					{
						tgdb.updateGuildRankXp(event.getGuild(), member, new Random().nextInt(5, 26), event, null);

						ArrayUtilities.xpThrottle.add(member);
						ThreadUtilities.createNewThread(_ -> ArrayUtilities.xpThrottle.remove(member), _ -> logger.log(LogType.RANK_ERROR, "Issue involving xpThrottle arraylist."), Duration.ofSeconds(30), false, false);
					}
				}
			}
		}
	}
}