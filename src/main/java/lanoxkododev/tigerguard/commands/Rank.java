package lanoxkododev.tigerguard.commands;

import lanoxkododev.tigerguard.TigerGuardDB;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
	public void execute(SlashCommandInteractionEvent event)
	{
		TigerGuardDB.getTigerGuardDB().generateGuildRankCard(event);
	}
}
