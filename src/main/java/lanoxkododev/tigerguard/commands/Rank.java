package lanoxkododev.tigerguard.commands;

import java.util.List;

import lanoxkododev.tigerguard.TigerGuardDB;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Rank implements TGCommand {

	@Override
	public String getName()
	{
		return "rank";
	}

	@Override
	public String getDescription()
	{
		return "Display your rank card for this guild!";
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
		TigerGuardDB.getTigerGuardDB().generateGuildRankCard(event);
	}
}
