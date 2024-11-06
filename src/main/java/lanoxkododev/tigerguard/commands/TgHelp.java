package lanoxkododev.tigerguard.commands;

import java.util.List;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class TgHelp implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();

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
		return DefaultMemberPermissions.DISABLED;
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		if (event.getMember() == event.getGuild().getOwner() ||
			event.getMember().getRoles().contains(event.getGuild().getRoleById(tigerGuardDB.getGuildAdminRole(event.getGuild().getIdLong()))) ||
			event.getMember().getRoles().contains(event.getGuild().getRoleById(tigerGuardDB.getGuildStaffRole(event.getGuild().getIdLong()))) ||
			event.getMember().getRoles().contains(event.getGuild().getRoleById(tigerGuardDB.getGuildSupportingStaffRole(event.getGuild().getIdLong()))))
		{
			event.replyEmbeds(embedder.createHelpEmbed()).queue();
		}
		else
		{
			event.replyEmbeds(embedder.accessErrorEmbed()).setEphemeral(true).queue();
		}
	}

}
