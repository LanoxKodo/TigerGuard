package lanoxkododev.tigerguard.roles;

import java.awt.Color;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Triplet;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

public class PromptColorThreader extends Thread {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	StringSelectInteractionEvent event;
	ArrayList<String[]>colorSpecs;
	Guild guild;

	public PromptColorThreader(StringSelectInteractionEvent inputEvent, ArrayList<String[]> colors)
	{
		event = inputEvent;
		guild = inputEvent.getGuild();
		colorSpecs = colors;
	}

	@Override
	public void run()
	{
		readyPrompt();
	}

	private void readyPrompt()
	{
		MessageChannel channel = event.getChannel();
		InputStream icon = getClass().getResourceAsStream("/assets/misc/rainbow.png");

		ArrayList<Long> roleList = tigerguardDB.selectColorRoles(guild.getIdLong());

		String fillStatementBase = "UPDATE tigerguarddb.colorRoles SET";
		String fillStatementAdd = "";

		for (int a = 0; a < colorSpecs.get(0).length; a++)
		{
			if (roleList.get(a) == null || guild.getRoleById(roleList.get(a)) == null)
			{
				String colorTitle = colorSpecs.get(0)[a];
				guild.createRole().setName(colorTitle).setColor(Color.decode(colorSpecs.get(1)[a])).queue();

				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}

				Long roleId = guild.getRolesByName(colorTitle, false).get(0).getIdLong();

				if (a == (colorSpecs.get(0).length-1))
				{
					fillStatementAdd += " color" + (a+1) + " = " + roleId + " WHERE id = " + guild.getIdLong() + ";";
				}
				else
				{
					fillStatementAdd += " color" + (a+1) + " = " + roleId + ",";
				}

				try {
					tigerguardDB.setGuildColorRolesEntry(fillStatementBase+fillStatementAdd);
				} catch (Exception e) {
					logger.logErr(LogType.ERROR, "Failure attempting to access database to set color values", fillStatementBase+fillStatementAdd, e);
				}

				roleList.remove(a);
				roleList.add(a, roleId);
			}
		}

		ArrayList<String> emojis = new ArrayList<>();
		emojis.addAll(List.of("🍎","🥭","🍊","🍋","🍌","🍈","🥦","🧊","🍶","🍦","🍇","🎂","🍑","🍓","🥥","🥑","🍧","🍆","🧽"));

		channel.sendMessageEmbeds(embedder.colorEmbed(Triplet.with("╔═════════╗\n        𝘾𝙊𝙇𝙊𝙍𝙎\n╚═════════╝", ColorCodes.TIGER_FUR.value.toString(),
			"**Care for a color change to your name?**"), roleList, emojis)).addFiles(FileUpload.fromData(icon, "rainbow.png")).queue(a -> {

				if (!tigerguardDB.checkForTable(event.getGuild().getIdLong() + "embeds"))
				{
					tigerguardDB.createTable(event.getGuild().getIdLong() + "embeds (name varchar(20), type varchar(10), id varchar(45), title varchar(100), color varchar(7), body varchar(1900));");
				}

				for (String emojiItem : emojis)
				{
					a.addReaction(Emoji.fromFormatted(emojiItem)).queue();
				}

			tigerguardDB.basicUpdate("colorRoles", "embed", a.getIdLong(), "guild", guild.getIdLong());
		});
	}
}
