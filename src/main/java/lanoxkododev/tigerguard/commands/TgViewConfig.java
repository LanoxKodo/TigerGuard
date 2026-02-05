package lanoxkododev.tigerguard.commands;

import lanoxkododev.tigerguard.PermissionValidator;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.TigerGuardDB.DB_Enums;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class TgViewConfig implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tgdb = TigerGuardDB.getTigerGuardDB();
	PermissionValidator permValidator = new PermissionValidator();

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
	public void execute(SlashCommandInteractionEvent event)
	{
		if (permValidator.canAccess(event.getGuild(), event.getMember(), true))
		{
			event.deferReply().setEphemeral(true).queue();

			Long guildID = event.getGuild().getIdLong();
			
			String info =
				//"**" + tgdb.getGuildTimeZone(guild.getIdLong()) + "** | Server Timezone\n" +
				"__**TigerGaurd Providable Roles**__\n" +
				"**Level Roles:** ***" + tgdb.getGuildKnownLevelUpRoleCount(event.getGuild().getIdLong()) + "***\n" +
				"For color roles, use /tg-update-config > TigerGuard-made roles features\n\n" +

				"__**Categories/Channels**__\n" +
				"**" + tgdb.getValue(DB_Enums.DYNAMIC_VC_CAT, "guild", guildID) + "** | CustomVC category\n" +
				"**" + tgdb.getValue(DB_Enums.DYNAMIC_VC_CHAN, "guild", guildID) + "** | CustomVC voice channel\n" +
				"**" + tgdb.getValue(DB_Enums.MUSIC_CHAN, "guild", guildID) + "** | Music channel\n" +
				"**" + tgdb.getValue(DB_Enums.BOT_CHAN, "guild", guildID) + "** | Testing or 'Bot-spam' channel\n" +
				"**" + tgdb.getValue(DB_Enums.GUILD_SIZE_CHAN, "guild", guildID) + "** | Guild Size Counter voice channel\n" +
				"**" + tgdb.getValue(DB_Enums.LEVEL_CHAN, "guild", guildID) + "** | Guild Level channel\n" +
				"**" + tgdb.getValue(DB_Enums.RULE_CHAN, "guild", guildID) + "** | Guild Rules channel\n\n" +

				"__**Administrative Roles**__\n" +
				"**" + tgdb.getValue(DB_Enums.ADMIN, "guild", guildID) + "** | Admin role - internally unused\n" +
				"\n**" + tgdb.getValue(DB_Enums.STAFF, "guild", guildID) + "** | Primary Staff role" +
				"\n**" + tgdb.getValue(DB_Enums.MOD, "guild", guildID) + "** | Secondary Staff role" +
				"\n**" + tgdb.getValue(DB_Enums.MEMBER, "guild", guildID) + "** | Member role" +
				"\n**" + tgdb.getValue(DB_Enums.ENTRANT, "guild", guildID) + "** | New User role\n\n" +
				"\n**" + tgdb.getValue(DB_Enums.NSFW, "guild", guildID) + "** | NSFW Access role\n\n" +
				
				/*
				"**" + tgdb.getGuildCustomvcCategory(event.getGuild().getIdLong()) + "** | CustomVC category\n" +
				"**" + tgdb.getGuildCustomvcChannel(event.getGuild().getIdLong()) + "** | CustomVC voice channel\n" +
				"**" + tgdb.getGuildMusicChannel(event.getGuild().getIdLong()) + "** | Music channel\n" +
				"**" + tgdb.getGuildBotSpamChannel(event.getGuild().getIdLong()) + "** | Testing or 'Bot-spam' channel\n" +
				"**" + tgdb.getGuildSizeChannel(event.getGuild().getIdLong()) + "** | Guild Size Counter voice channel\n" +
				"**" + tgdb.getGuildLevelChannel(event.getGuild().getIdLong()) + "** | Guild Level channel\n\n" +
				//Rules Channel - unused: "\n**" + tgdb.getGuildRuleChannel(event.getGuild()) + "** | Rules channel" +
				
				"**" + tgdb.getGuildAdminRole(event.getGuild().getIdLong()) + "** | Admin role - internally unused\n" +
				"\n**" + tgdb.getGuildStaffRole(event.getGuild().getIdLong()) + "** | Primary Staff role" +
				"\n**" + tgdb.getGuildSupportingStaffRole(event.getGuild().getIdLong()) + "** | Secondary Staff role" +
				"\n**" + tgdb.getGuildMemberRole(event.getGuild().getIdLong()) + "** | Member role" +
				"\n**" + tgdb.getGuildNSFWStatusRole(event.getGuild().getIdLong()) + "** | NSFW Access role\n\n" +
				*/

				"Slots with '0' means nothing was detected for that item.\n" +
				"If you need to modify one of these entries, use /tg-update-config > 'subset menu' entries!";
			event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Here is the following info I have for " + event.getGuild().getName() + ":", null, null, ColorCodes.TIGER_FUR, info)).queue();
		}
		else event.replyEmbeds(embedder.accessErrorEmbed()).setEphemeral(true).queue();
	}
}
