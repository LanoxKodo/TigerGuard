package lanoxkododev.tigerguard.roles;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class LevelRoleRepairThreader extends Thread {

	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	StringSelectInteractionEvent event;
	int knownLevelRoles;
	ArrayList<Integer> numberedMissing = new ArrayList<>();
	Guild guild;

	public LevelRoleRepairThreader(StringSelectInteractionEvent inputEvent, Guild inputGuild)
	{
		event = inputEvent;
		guild = inputGuild;
		knownLevelRoles = tigerguardDB.getGuildKnownLevelUpRoleCount(guild.getIdLong());
	}

	@Override
	public void run()
	{
		repairLevelUpRoles();
	}

	private void repairLevelUpRoles()
	{
		//Storage for the roles we know
		LinkedHashMap<Integer, Long> roleItems = new LinkedHashMap<>();
		List<Role> presentRoles = guild.getRoles();

		EmbedMessageFactory embedder = new EmbedMessageFactory();

		if (!tigerguardDB.checkRow(guild.getIdLong() + "lvlroles", "id", guild.getIdLong()))
		{
			event.getChannel().sendMessageEmbeds(embedder.simpleEmbed("No roles to repair", null, null, ColorCodes.MEH_NOTICE, "It appears I do not see any level roles for this server. If you have not set level roles before then use my other command for creating them; otherwise if this is an error than please report this on my support server!" +
				"\n\nThis message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
		}
		else
		{
			boolean voiceTableCheck = tigerguardDB.checkForTable("voiceTracker");
			if (!voiceTableCheck)
			{
				tigerguardDB.createTable("CREATE TABLE tigerguarddb." + "voiceTracker (id VARCHAR(45), init VARCHAR(45), guild VARCHAR(45);");
			}

			event.getChannel().sendMessageEmbeds(embedder.simpleEmbed("I will now begin repairing level roles", null, null, ColorCodes.CONFIRMATION, "For roles that I find need repairing/restoring, they will be named in the format of \"Level_#_Role (Lvl #)\\\".\\nYou may edit these however you wish!\\n\\nPlease wait for me to send a confirmation message of the repair process being completed!" +
				"This message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));

			//First, obtain all the roles from the DB table, detect if columns are missing
			for (int a = 1; a <= knownLevelRoles; a++)
			{
				try
				{
					if (tigerguardDB.hasValue("levelRoles", "role"+a, "id", guild.getIdLong()))
					{
						Long checked = tigerguardDB.getGuildSingularLevelUpRole(guild.getIdLong(), "role"+a);

						Role sample = presentRoles.stream().filter(role -> role.getId().equals(checked.toString())).findFirst().orElse(null);//role.getName().equals(roleName)).findFirst().orElse(null);

						if (sample == null)
						{
							roleItems.put(a, checked);
							numberedMissing.add(a);
						}
					}
					else
					{
						System.out.println("Unable to get the database column");
					}
				}
				catch (Exception e){}
			}

			roleItems.forEach((a, b) -> {
				guild.createRole().setName("Level_" + a + "_Role (Lvl " + a + ")").queue();

				try {
					Thread.sleep(3000);
				}
				catch (InterruptedException e)
				{
					logger.logErr(LogType.WARNING, "Failure putting repairLevelUpRoles thread to sleep while creating roles", null, e);
				}

				Long roleId = guild.getRolesByName("Level_" + a + "_Role (Lvl " + a + ")", false).get(0).getIdLong();

				String fillColumnData = "UPDATE tigerguarddb." + guild.getIdLong() + "lvlroles SET role" + a + " = " + roleId + ";";

				try
				{
					tigerguardDB.setGuildSingularLevelUpRole(fillColumnData);
				}
				catch (Exception e)
				{
					logger.logErr(LogType.DATABASE_ERROR, "Failure attempting to access database to set new level role value for guild " + guild.getIdLong(), null, e);
				}
			});

			event.getChannel().sendMessageEmbeds(embedder.simpleEmbed("Level roles have been repaired", null, null, ColorCodes.FINISHED, "I have finished repairing the level roles for this server; any that were missing have been formatted as \"Level_#_Role (Lvl #)\". " +
				"You may change these names however you'd like; If you need to reset a color, simply delete the role and run this command again!\n\nThis message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
		}
	}
}
