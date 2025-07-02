package lanoxkododev.tigerguard.roles;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class ColorRoleDeleteThreader extends Thread {

	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	ButtonInteractionEvent event;
	ArrayList<String[]>colorSpecs;
	Guild guild;

	public ColorRoleDeleteThreader(ButtonInteractionEvent inputEvent, Guild inputGuild, ArrayList<String[]> colors)
	{
		event = inputEvent;
		guild = inputGuild;
		colorSpecs = colors;
	}

	@Override
	public void run()
	{
		deleteColorRoles();
	}

	private void deleteColorRoles()
	{
		EmbedMessageFactory embedder = new EmbedMessageFactory();

		if (!tigerguardDB.checkRow("colorRoles", "guild", event.getGuild().getIdLong()))
		{
			event.getChannel().sendMessageEmbeds(embedder.simpleEmbed("No roles to delete", null, null, ColorCodes.MEH_NOTICE,
				"I do not seem to see any color roles that need to be deleted. If this is an error then please inform me on my support server" +
				"\n\nThis message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
		}
		else
		{
			event.getChannel().sendMessageEmbeds(embedder.simpleEmbed("I will now begin deleting all color roles", null, null, ColorCodes.CONFIRMATION,
					"This will take a moment but once I am finished you will receive a confirmation message that the request has been completed." +
					"\n\nThis message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));

			for (int a = 0; a < colorSpecs.get(0).length; a++)
			{
				Long roleIdCheck = tigerguardDB.getSingularRole("colorRoles", "color" + (a+1), guild.getIdLong());

				if (roleIdCheck != null)
				{
					Role role = guild.getRoleById(roleIdCheck);

					System.out.println(role);
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
					logger.log(LogType.WARNING, "Failure to put deleteColorRoles thread to sleep while deleting roles.");
					e.printStackTrace();
				}
			}

			tigerguardDB.deleteRow("colorRoles", "guild", guild.getIdLong());

			event.getChannel().sendMessageEmbeds(embedder.simpleEmbed("Color roles have been deleted", null, null, ColorCodes.FINISHED,
				"The color roles have been deleted as requested, please verify in your role section that they have been. If they are still present please report this bug on my support server!" +
				"\n\nThis message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
		}
	}
}
