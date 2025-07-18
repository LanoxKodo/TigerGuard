package lanoxkododev.tigerguard.commands;

import java.util.EnumSet;

import lanoxkododev.tigerguard.TigerGuardResponses;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;

public class PingZeTiger implements TGCommand {

	TigerGuardResponses tgResponses = new TigerGuardResponses();

	@Override
	public String getName()
	{
		return "ping-ze-tiger";
	}

	@Override
	public String getDescription()
	{
		return "Ping ze Tiger, get a response if active. Simple!";
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
		event.reply(tgResponses.banter(event.getMember().getEffectiveName())).setEphemeral(true).queue();
	}
}