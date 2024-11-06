package lanoxkododev.tigerguard.messages;

import java.util.ArrayList;

import org.javatuples.Quartet;
import org.javatuples.Triplet;

import lanoxkododev.tigerguard.TigerGuardDB;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

public class EmbedPrinterThreader extends Thread {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	StringSelectInteractionEvent event;
	String input;

	public EmbedPrinterThreader(StringSelectInteractionEvent eventIn, String inputIn)
	{
		event = eventIn;
		input = inputIn;
	}

	@Override
	public void run()
	{
		review();
	}

	private void review()
	{
		Quartet<String, String, String, String> embedData = tigerGuardDB.getEmbedData(input, event.getGuild().getIdLong());
		
		System.out.println("EmbedPrinterThreadder Debug :: Type=" +
				embedData.getValue0() + " | Title=" +
				embedData.getValue1() + " | Color=" +
				embedData.getValue2() + " | Body=" +
				embedData.getValue3());
		
		String embedType = embedData.getValue0();
		
		if (embedType.equalsIgnoreCase("regular"))
		{
			regularEmbedWithTitle(embedData.getValue1(), embedData.getValue2(), embedData.getValue3());
		}
		else
		{
			roleEmbedSection(embedData);
		}
	}
	
	private void regularEmbedWithTitle(String title, String color, String body)
	{
		event.getChannel().sendMessageEmbeds(embedder.regularEmbed(title, color, body)).queue();
	}
	
	private void roleEmbedSection(Quartet<String, String, String, String> embedData)
	{
		ArrayList<String> roles = new ArrayList<>();
		ArrayList<String> emojis = new ArrayList<>();
		String[] dataParts = embedData.getValue3().split("\\s+");

		if (dataParts[0].contains("<@&")) //If first index is a role
		{
			for (int a = 0; a < dataParts.length; a++)
			{
				if (a % 2 == 0) roles.add(dataParts[a]);
				else emojis.add(dataParts[a]);
			}
		}
		else //If first index is an emoji
		{
			for (int a = 0; a < dataParts.length; a++)
			{
				if (a % 2 == 0) emojis.add(dataParts[a]);
				else roles.add(dataParts[a]);
			}
		}

		String color = embedData.getValue2();
		if (color.charAt(0) != '#') color = "#" + color;

		event.getChannel().sendMessageEmbeds(embedder.roleEmbed(Triplet.with(embedData.getValue1(), embedData.getValue2(), embedData.getValue3()), roles, emojis)).queue(a -> { //embedData.getValue3 was getValue4 here. Update as needed
			for (String emojiItem : emojis)
			{
				a.addReaction(Emoji.fromFormatted(emojiItem)).queue();
			}

			tigerGuardDB.setEmbedId(event.getGuild().getIdLong(), "embeds", a.getIdLong(), input);
		});
	}
}
