package lanoxkododev.tigerguard.commands;

import java.util.EnumSet;

import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;

public class Info implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	
	@Override
	public String getName()
	{
		return "info";
	}

	@Override
	public String getDescription()
	{
		return "See TigerGuard's info.";
	}
	
	@Override
	public EnumSet<InteractionContextType> getContexts()
	{
		return EnumSet.of(InteractionContextType.GUILD,
			InteractionContextType.BOT_DM,
			InteractionContextType.PRIVATE_CHANNEL);
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		event.replyEmbeds(embedder.infoEmbed("TigerGuard - " + TigerGuard.getTigerGuard().getVersion(),
			null, null, ColorCodes.TIGER_FUR, "Written in Java")).queue();
	}
}