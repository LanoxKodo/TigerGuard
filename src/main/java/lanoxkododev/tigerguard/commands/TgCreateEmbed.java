package lanoxkododev.tigerguard.commands;

import java.util.ArrayList;
import java.util.List;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class TgCreateEmbed implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();

	@Override
	public String getName()
	{
		return "create-embed";
	}

	@Override
	public String getDescription()
	{
		return "Provide details for a plain-embed (no images) that will be created later.";
	}

	@Override
	public boolean isNSFW()
	{
		return false;
	}

	@Override
	public List<OptionData> getOptions()
	{
		List<OptionData> options = new ArrayList<>();
		options.add(new OptionData(OptionType.STRING, "embed_name", "The name of the embed that you'll use to identify it later on.", true).setMinLength(4).setMaxLength(20));
		options.add(new OptionData(OptionType.STRING, "body", "The body of the embed, formatting supported, max 1900 characters.", true).setMinLength(1).setMaxLength(1900));
		options.add(new OptionData(OptionType.STRING, "title", "The text the embed will show. Optional, max 100 characters", false).setMinLength(1).setMaxLength(100));
		options.add(new OptionData(OptionType.STRING, "color", "The hex color that the embed will use. Example: #ff00ff", false).setMinLength(6).setMaxLength(7));

		return options;
	}
	
	@Override
	public DefaultMemberPermissions getDefaultPermission()
	{
		return DefaultMemberPermissions.DISABLED;
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		event.deferReply().queue();
		
		if (!tigerGuardDB.checkForTable(event.getGuild().getIdLong() + "embeds"))
		{
			tigerGuardDB.createTable(event.getGuild().getIdLong() + "embeds (name varchar(20), type varchar(10), id varchar(45), title varchar(100), color varchar(7), body varchar(1900));");
		}

		if (!tigerGuardDB.checkRow("tempEmbedData", "id", event.getGuild().getIdLong()))
		{
			tigerGuardDB.firstInsertion("INSERT INTO tigerguarddb.tempEmbedData (id, name, title, color, body) VALUES (" + event.getGuild().getIdLong() + ", null, null, null, null);");
		}

		if (tigerGuardDB.countRows(event.getGuild().getIdLong() + "embeds") == 3 && !tigerGuardDB.getGuildPremiumStatus(event.getGuild().getIdLong()))
		{
			event.replyEmbeds(embedder.simpleEmbed("Pardon, this command is experiemental and partially restricted.", null, null, ColorCodes.UNABLE, "For non-donation servers, while this command is experimental, there is a limit of 3 customizable embeds.\nFor servers that support the bot this restrction is lifted.")).queue();
		}
		else
		{
			String body = event.getOption("body").getAsString();
			String color = "";
			
			if (event.getOption("color") != null)
			{
				String colorTemp = event.getOption("color").getAsString();
				if (colorTemp.chars().count() < 6 || colorTemp.chars().count() > 7)
				{
					event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Unable to parse input", null, null, ColorCodes.UNABLE, "Cannot determined color from the input of '" + colorTemp + "'. Input not a hex value between #000000 and #FFFFFF.")).queue();
				}
				else
				{
					if (colorTemp.charAt(0) != '#') color = "#" + colorTemp;
					else color = colorTemp;
				}
			}
			//Do not assign ColorCode item in DB, just leave blank then do checks in embed-printing.
			
			String title = "";
			if (event.getOption("title") != null) title = event.getOption("title").getAsString();
			
			tigerGuardDB.submitEmbed(event.getGuild().getIdLong(), event.getOption("embed_name").getAsString(), "regular", color, title, body);
			
			event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Test - completed", null, null, ColorCodes.FINISHED, "Check the DB for QA")).queue();
		}
	}
}
