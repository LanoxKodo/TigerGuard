package lanoxkododev.tigerguard.commands;

import java.util.ArrayList;
import java.util.List;

import lanoxkododev.tigerguard.PermissionValidator;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class TGFeatures implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	PermissionValidator permValidator = new PermissionValidator();
	
	@Override
	public String getName()
	{
		return "tg-feature-manager";
	}
	
	@Override
	public String getDescription()
	{
		return "Enable or disable features Tigerguard can do. All start as disabled by default.";
	}
	
	@Override
	public List<SubcommandData> getSubcommands()
	{
		List<SubcommandData> subcommands = new ArrayList<>();
		subcommands.add(new SubcommandData("birthdays", "Boolean for enabling the Birthday features")
			.addOption(OptionType.BOOLEAN, "boolean", "True or False?", true));
		
		return subcommands;
	}
	
	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		if (permValidator.administrativeAccessElevated(event.getGuild(), event.getMember()))
		{
			String subcommand = event.getSubcommandName();
			
			if (subcommand == null)
			{
				event.replyEmbeds(embedder.simpleEmbed("Invalid command usage", null, null, ColorCodes.ERROR,
					"Your input for the command appears to be wrong. Please try again")).setEphemeral(true).queue();
				return;
			}
			
			long guildID = event.getGuild().getIdLong();
			
			switch (subcommand)
			{
				case "birthdays" -> {
					boolean bool = event.getOption("boolean").getAsBoolean();
					
					tigerGuardDB.basicUpdate("guildFeatures", "birthdayFeature", bool, "guild", guildID);
					
					if (!tigerGuardDB.checkForTable(guildID + "bdays"))
					{
						tigerGuardDB.createTable(guildID + "bdays (`member` VARCHAR(45) NOT NULL, `date` VARCHAR(10) NOT NULL, PRIMARY KEY (`member`));");
					}
					
					event.replyEmbeds(embedder.simpleEmbed("Birthday features enabled", null, null, ColorCodes.FINISHED,
						"All birthday features are now enabled for this server. If you ever wish to disable this feature, run the command again and "
						+ "pass `false` as the parameter")).setEphemeral(true).queue();
				}
				default -> event.replyEmbeds(embedder.simpleEmbed("Unknown subcommand", null, null, ColorCodes.ERROR,
					"subcommand entered does not match expected options.")).setEphemeral(true).queue();
			}
		}
		else event.replyEmbeds(embedder.accessErrorEmbed()).setEphemeral(true).queue();
	}
}
