package lanoxkododev.tigerguard.commands;

import java.util.List;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class TgViewConfig implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();

	@Override
	public String getName()
	{
		return "tg-view-config";
	}

	@Override
	public String getDescription()
	{
		return "View the current configurations TigerGuard is using for this server.";
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
			event.deferReply().setEphemeral(true).queue();

			String info =
				//"**" + tigerguardDB.getGuildTimeZone(guild.getIdLong()) + "** | Server Timezone\n" +
				"__**TigerGaurd Providable Roles**__\n" +
				"**Level Roles:** ***" + tigerGuardDB.getGuildKnownLevelUpRoleCount(event.getGuild().getIdLong()) + "***\n" +
				"For color roles, use /tg-update-config > TigerGuard-made roles features\n\n" +

				"__**Categories/Channels**__\n" +
				"**" + tigerGuardDB.getGuildCustomvcCategory(event.getGuild().getIdLong()) + "** | CustomVC category\n" +
				"**" + tigerGuardDB.getGuildCustomvcChannel(event.getGuild().getIdLong()) + "** | CustomVC voice channel\n" +
				"**" + tigerGuardDB.getGuildMusicChannel(event.getGuild().getIdLong()) + "** | Music channel\n" +
				"**" + tigerGuardDB.getGuildBotSpamChannel(event.getGuild().getIdLong()) + "** | Testing or 'Bot-spam' channel\n" +
				"**" + tigerGuardDB.getGuildSizeChannel(event.getGuild().getIdLong()) + "** | Guild Size Counter voice channel\n" +
				"**" + tigerGuardDB.getGuildLevelChannel(event.getGuild().getIdLong()) + "** | Guild Level channel\n\n" +
				//Rules Channel - unused: "\n**" + tigerGuardDB.getGuildRuleChannel(event.getGuild()) + "** | Rules channel" +

				"__**Administrative Roles**__\n" +
				"**" + tigerGuardDB.getGuildAdminRole(event.getGuild().getIdLong()) + "** | Admin role\n" +
				"\n**" + tigerGuardDB.getGuildStaffRole(event.getGuild().getIdLong()) + "** | Primary Staff role" +
				"\n**" + tigerGuardDB.getGuildSupportingStaffRole(event.getGuild().getIdLong()) + "** | Secondary Staff role" +
				"\n**" + tigerGuardDB.getGuildMemberRole(event.getGuild().getIdLong()) + "** | Member role" +
				"\n**" + tigerGuardDB.getGuildNSFWStatusRole(event.getGuild().getIdLong()) + "** | NSFW Access role\n\n" +


				"Slots with '0' means nothing was detected for that item.\n" +
				"If you need to modify one of these entries, use /tg-update-config > 'subset menu' entries!";
			event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Here is the following info I have for " + event.getGuild().getName() + ":", null, null, ColorCodes.TIGER_FUR, info)).queue();
		}
		else
		{
			event.replyEmbeds(embedder.accessErrorEmbed()).setEphemeral(true).queue();
		}
	}
}