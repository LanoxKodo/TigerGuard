package lanoxkododev.tigerguard.commands;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.PermissionValidator;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class TgHelp implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();
	PermissionValidator permValidator = new PermissionValidator();

	@Override
	public String getName()
	{
		return "tg-help";
	}

	@Override
	public String getDescription()
	{
		return "Informational embed message regarding TigerGuard setup commands.";
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		if (permValidator.administrativeAccessBase(event.getGuild(), event.getMember())) event.replyEmbeds(embedder.createHelpEmbed()).queue();
		else event.replyEmbeds(embedder.accessErrorEmbed()).setEphemeral(true).queue();
	}
}
