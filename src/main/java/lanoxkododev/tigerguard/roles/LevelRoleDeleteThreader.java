package lanoxkododev.tigerguard.roles;

import java.util.concurrent.TimeUnit;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class LevelRoleDeleteThreader extends Thread {

	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	ButtonInteractionEvent event;
	int knownLevelRoles;
	long[] roleIdTracker = new long[knownLevelRoles];
	Guild guild;

	public LevelRoleDeleteThreader(ButtonInteractionEvent inputEvent, Guild inputGuild)
	{
		event = inputEvent;
		guild = inputGuild;
		knownLevelRoles = tigerguardDB.getGuildKnownLevelUpRoleCount(guild.getIdLong());
	}

	@Override
	public void run()
	{
		deleteLevelUpRoles();
	}

	private void deleteLevelUpRoles()
	{
		EmbedMessageFactory embedder = new EmbedMessageFactory();

		if (knownLevelRoles == 0)
		{
			event.getChannel().sendMessageEmbeds(embedder.simpleEmbed("No roles to delete", null, null, ColorCodes.MEH_NOTICE,
				"There are no level roles that I found that needed to be deleted. If this is an error then please reach out on my support server!\n\n" +
				"This message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
		}
		else
		{
			event.getChannel().sendMessageEmbeds(embedder.simpleEmbed("I will now begin deleting all level roles", null, null, ColorCodes.CONFIRMATION,
					"I will take a moment to remove each level role from this server as requested.\nThis will take a moment depending but once I am done I will send a confirmation message to let you know the process has finished!" +
					"\n\nThis message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));

			for (int a = knownLevelRoles; a >= 1; a--)
			{
				Long roleIdCheck = tigerguardDB.getGuildSingularLevelUpRole(guild.getIdLong(), ("lvlRole"+a));

				if (roleIdCheck != null)
				{
					Role role = guild.getRoleById(roleIdCheck);

					if (role != null)
					{
						role.delete().queue();
					}
				}

				try {
					Thread.sleep(3000);
				}
				catch (InterruptedException e)
				{
					logger.log(LogType.WARNING, "Failure to put deleteLevelUpRoles thread to sleep while deleting roles.");
					e.printStackTrace();
				}

				String deleteStatement = "ALTER TABLE tigerguarddb." + guild.getIdLong() + "lvlroles DROP COLUMN lvlRole" + a + ";";

				try
				{
					tigerguardDB.deleteColumn(guild.getIdLong(), deleteStatement);
				}
				catch (Exception e)
				{
					logger.log(LogType.DATABASE_ERROR, "Failure to delete column from the database for guild " + guild.getIdLong());
				}
			}

			tigerguardDB.setGuildKnownLevelUpRoleCount(guild.getIdLong(), 0);

			event.getChannel().sendMessageEmbeds(embedder.simpleEmbed("evel roles have been deleted", null, null, ColorCodes.FINISHED,
				"The level roles have been deleted as requested, please verify in your role section that they have been. If they are still present please report the bug on my support server!\n\n" +
				"This message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
		}
	}
}
