package lanoxkododev.tigerguard.commands;

import java.util.List;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public class TgUpdateConfig implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();

	@Override
	public String getName()
	{
		return "tg-update-config";
	}

	@Override
	public String getDescription()
	{
		return "Launch the TigerGuard config dropdown editor to change a config for this server.";
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
			event.replyEmbeds(embedder.simpleEmbed("Select which operation you'd like to use", null, null, ColorCodes.TIGER_FUR, null)).addActionRow(StringSelectMenu.create("s1-tg-update-config")
				.addOption("Identify server-made permission roles", "s2-permission-roles", "Used for setting server based roles already made previously.")
				.addOption("TigerGuard-made roles features", "s2-tigerguard-items", "Select for submenu items for TigerGuard role features.")
				.addOption("TigerGuard channels/categories features", "s2-categorization-items", "Select for setting TigerGuard needed categories/channels.")
				.addOption("Embed manager", "s2-embed-manager", "Select for creating/editing reaction embed messages.").build()).queue();
		}
		else
		{
			event.replyEmbeds(embedder.accessErrorEmbed()).setEphemeral(true).queue();
		}
	}
}
