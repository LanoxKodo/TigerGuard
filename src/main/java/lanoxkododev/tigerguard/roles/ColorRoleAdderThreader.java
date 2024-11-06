package lanoxkododev.tigerguard.roles;

import java.awt.Color;
import java.util.ArrayList;
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

public class ColorRoleAdderThreader extends Thread {

	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	StringSelectInteractionEvent event;
	ArrayList<String[]> colorSpecs;
	Guild guild;

	public ColorRoleAdderThreader(StringSelectInteractionEvent inputEvent, Guild inputGuild, ArrayList<String[]> colors)
	{
		event = inputEvent;
		guild = inputGuild;
		colorSpecs = colors;
	}

	@Override
	public void run()
	{
		createColorRoles();
	}

	private void createColorRoles()
	{
		if (!tigerguardDB.checkRow("colorRoles", "id", event.getGuild().getIdLong()))
		{
			tigerguardDB.createGuildColorRolesEntry(event.getGuild().getIdLong());
		}

		String fillStatementBase = "UPDATE tigerguarddb.colorRoles SET";
		String fillStatementAdd = "";

		for (int a = 0; a < colorSpecs.get(0).length; a++)
		{
			String colorTitle = colorSpecs.get(0)[a];
			guild.createRole().setName(colorTitle).setColor(Color.decode(colorSpecs.get(1)[a])).queue();

			try
			{
				Thread.sleep(3000);
			}
			catch (Exception e)
			{
				logger.log(LogType.WARNING, "Failure to put createColorRoles thread to sleep.");
			}

			List<Role> role = guild.getRolesByName(colorTitle, true);
			Long roleId = role.get(0).getIdLong();

			if (a == (colorSpecs.get(0).length-1))
			{
				fillStatementAdd += " color" + (a+1) + " = " + roleId + " WHERE id = " + guild.getIdLong() + ";";
			}
			else
			{
				fillStatementAdd += " color" + (a+1) + " = " + roleId + ",";
			}
		}

		tigerguardDB.setGuildColorRolesEntry(fillStatementBase+fillStatementAdd);

		event.getChannel().sendMessageEmbeds(new EmbedMessageFactory().simpleEmbed("Color roles have finished being added to the server", null, null, ColorCodes.FINISHED,
			"I have finished creating the 18 color roles you requested! Each one was formatted as \"Color Name\".\nYou may change these names and colors however you'd like; " +
			"If any of these end up being accidently deleted, run the repair command the same way you initiated this command!\n\nThis message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
	}
}