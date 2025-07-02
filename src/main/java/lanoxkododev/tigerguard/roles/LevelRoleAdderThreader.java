package lanoxkododev.tigerguard.roles;

import java.util.List;
import java.util.concurrent.TimeUnit;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

public class LevelRoleAdderThreader extends Thread {

	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	ModalInteractionEvent event;
	int roleAmount;
	int knownLevelRoles;
	Guild guild;

	public LevelRoleAdderThreader(ModalInteractionEvent inputEvent, Guild inputGuild, int inputAmount, int inputKnownLevelRoles)
	{
		event = inputEvent;
		guild = inputGuild;
		roleAmount = inputAmount;
		knownLevelRoles = inputKnownLevelRoles;
	}

	@Override
	public void run()
	{
		createLevelUpRoles();
	}

	private void createLevelUpRoles()
	{
		boolean xpTableCheck = tigerguardDB.checkForTable(guild.getIdLong() + "xp");

		if (!xpTableCheck)
		{
			tigerguardDB.createGuildXpTable(event.getGuild());
		}

		String statementBase = "UPDATE tigerguarddb.levelRoles SET ";
		String statementAdd = "";

		if (!tigerguardDB.checkRow("levelRoles", "guild", guild.getIdLong()))
		{
			tigerguardDB.insertGuildIntoLevelRoleTable(guild.getIdLong());
		}

		for (int a = 1 + knownLevelRoles; a <= roleAmount + knownLevelRoles; a++)
		{
			//Create the role in format of "Level_#_Role (Lvl #)", queue to REST protocol.
			guild.createRole().setName("Level_" + a + "_Role (Lvl " + a + ")").queue();

			//Put thread to sleep every 5 seconds to not spam the REST protocol, thus avoiding rate limits of adding data.
			//Then upon awaking we will then grab the ID and follow through the rest of the loop logic.
			try
			{
				Thread.sleep(5000);
			}
			catch (Exception e)
			{
				logger.logErr(LogType.WARNING, "Failure putting createLevelUpRoles thread to sleep while creating roles", null, e);
			}

			//Find the role we just made.
			List<Role> role = guild.getRolesByName("Level_" + a + "_Role (Lvl " + a + ")", true);
			Long roleId = role.get(0).getIdLong();

			String logicBase = "role" + a + " = " + roleId;
			if (roleAmount == 1)
			{
				statementAdd += logicBase + ";";
			}
			else
			{
				if (a == roleAmount + knownLevelRoles) //final entry, append semicolon to end sql statement
				{
					statementAdd += logicBase + " WHERE id = " + guild.getIdLong();
				}
				else //if starting or continuing entries, append comma at end for next iteration segment
				{
					statementAdd += logicBase + ", ";
				}
			}
		}

		//Try to update the table and then fill it with the created data, then pause after first step to allow time to properly account for delays if they occur.
		try
		{
			tigerguardDB.setGuildKnownLevelUpRoleCount(guild.getIdLong(), knownLevelRoles+roleAmount);
			Thread.sleep(3000);
		}
		catch (Exception e)
		{
			logger.logErr(LogType.WARNING, "Failure to sleep while configuring knownLevelRole amount", null, e);
		}

		try
		{
			tigerguardDB.setGuildLevelUpRoles(guild.getIdLong(), statementBase+statementAdd);
			Thread.sleep(3000);
		}
		catch (Exception e)
		{
			logger.log(LogType.WARNING, "Failure to sleep while configuring knownLevelRole amount.");
		}

		event.getChannel().sendMessageEmbeds(new EmbedMessageFactory().simpleEmbed("Level roles have finished being added to the server", null, null, ColorCodes.FINISHED,
			"I have finished creating the number of roles you requested! Each one was formatted as \"Level_#_Role (Lvl #)\".\nYou may change these names however you'd like; " +
			"If any of these end up being accidently deleted, run the repair command the same way you initiated this command!\n\nThis message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
	}
}