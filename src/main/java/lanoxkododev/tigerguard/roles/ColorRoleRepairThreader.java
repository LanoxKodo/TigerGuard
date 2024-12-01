package lanoxkododev.tigerguard.roles;

import java.awt.Color;
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

public class ColorRoleRepairThreader extends Thread {

	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	StringSelectInteractionEvent event;
	ArrayList<Integer> numberedMissing = new ArrayList<>();
	ArrayList<String[]>colorSpecs;
	Guild guild;

	public ColorRoleRepairThreader(StringSelectInteractionEvent inputEvent, Guild inputGuild, ArrayList<String[]> colors)
	{
		event = inputEvent;
		guild = inputGuild;
		colorSpecs = colors;
	}

	@Override
	public void run()
	{
		repairColorRoles();
	}

	private void repairColorRoles()
	{
		if (!tigerguardDB.checkRow("colorRoles", "id", event.getGuild().getIdLong()))
		{
			tigerguardDB.createGuildColorRolesEntry(event.getGuild().getIdLong());
		}

		//Storage for the roles we know
		LinkedHashMap<Integer, Long> roleItems = new LinkedHashMap<>();
		List<Role> presentRoles = guild.getRoles();

		EmbedMessageFactory embedder = new EmbedMessageFactory();

		event.getChannel().sendMessageEmbeds(embedder.simpleEmbed("I will now begin checking and repairing the color roles", null, null, ColorCodes.CONFIRMATION,
				"Note: I will be repairing and/or resetting the color roles back to default. If you modified these at any point and want those again you will need to modify them once more!" +
				"\n\nPlease wait for me to send a confirmation message of the repair process being completed!\n\n" +
				"This message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));

		//First, obtain all the roles from the DB table, detect if columns are missing
		for (int a = 0; a < colorSpecs.get(0).length; a++)
		{
			try
			{
				if (tigerguardDB.checkRow("colorRoles", "id", guild.getIdLong()))
				{
					Long checked = tigerguardDB.getSingularRole("colorRoles", "color" + (a+1), guild.getIdLong());

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

		for (int a = 0; a < roleItems.size(); a++)
		{
			if (roleItems.get(a) == 0)
			{
				String colorTitle = colorSpecs.get(0)[a];
				guild.createRole().setName(colorTitle).setColor(Color.decode(colorSpecs.get(1)[a])).queue();
				process(colorTitle, a);
			}
			else
			{
				Role role = guild.getRoleById(roleItems.get(a));

				if (role == null)
				{
					String colorTitle = colorSpecs.get(0)[a];
					guild.createRole().setName(colorTitle).setColor(Color.decode(colorSpecs.get(1)[a])).queue();
					process(colorTitle, a);
				}
			}
		}

		event.getChannel().sendMessageEmbeds(embedder.simpleEmbed("Color roles have been repaired", null, null, ColorCodes.FINISHED,
			"I have finished repairing the color roles for this server; any that were missing have been formatted as \"Color Name\".\nYou may change the names and colors however you'd like; " +
			"If you need to reset a color, simply delete the role and run this command again!\n\nThis message will auto-delete in 1 minute.")).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
	}

	private void process(String colorTitle, int a)
	{
		System.out.println(colorTitle + " & " + a + " & color" + (a+1));
		try {
			Thread.sleep(3000);
		}
		catch (InterruptedException e)
		{
			logger.logErr(LogType.WARNING, "Failure putting repairColorRoles thread to sleep while creating roles", null, e);
		}

		Long roleId = guild.getRolesByName(colorTitle, false).get(0).getIdLong();

		try
		{
			tigerguardDB.basicUpdate("colorRoles", " color" + (a+1), roleId, "id", guild.getIdLong());
		}
		catch (Exception e)
		{
			logger.log(LogType.DATABASE_ERROR, "Failure to put new role value into database for guild " + guild.getIdLong());
		}
	}
}