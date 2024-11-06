package lanoxkododev.tigerguard.commands;

import java.util.List;

import lanoxkododev.tigerguard.TigerGuardResponses;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

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
	public boolean isNSFW()
	{
		return false;
	}

	@Override
	public List<OptionData> getOptions()
	{
		return null;
	}
	
	@Override
	public DefaultMemberPermissions getDefaultPermission()
	{
		return DefaultMemberPermissions.ENABLED;
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		event.reply(tgResponses.banter(event.getMember().getEffectiveName())).setEphemeral(true).queue();
	}
}
