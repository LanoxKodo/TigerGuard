package lanoxkododev.tigerguard.commands;

import java.util.List;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;

public class VcNSFW implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();

	@Override
	public String getName()
	{
		return "vcnsfw";
	}

	@Override
	public String getDescription()
	{
		return "Change the custom voice channel to be marked as NSFW.";
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
		if(!event.getMember().getVoiceState().inAudioChannel()) event.replyEmbeds(embedder.voiceErrorEmbed()).setEphemeral(true).queue();
		else
		{
			AudioChannelUnion vc = event.getMember().getVoiceState().getChannel();
			if (vc != null && vc.getParentCategory().getIdLong() == tigerGuardDB.getGuildCustomvcCategory(event.getGuild().getIdLong()) &&
					vc.getIdLong() != tigerGuardDB.getGuildCustomvcChannel(event.getGuild().getIdLong()))
			{
				Role nsfwRoleFound = event.getGuild().getRoleById(tigerGuardDB.getGuildNSFWStatusRole(event.getGuild().getIdLong()));

				if (nsfwRoleFound != null && event.getMember().getRoles().contains(nsfwRoleFound))
				{
					VoiceChannelManager vcManager = vc.asVoiceChannel().getManager();
					vcManager.setName("NSFW " + vc.getName()).queue();
					vcManager.setNSFW(true).queue();

					event.replyEmbeds(embedder.simpleEmbed("The voice channel has been marked as NSFW.", null, null, ColorCodes.FINISHED, "The channel is now in NSFW mode")).setEphemeral(true).queue();
				}
				else
				{
					event.replyEmbeds(embedder.simpleEmbed("Permission usage error", null, null, ColorCodes.ERROR,
						"This command can only be used if you have this server's <@&" + tigerGuardDB.getGuildNSFWStatusRole(event.getGuild().getIdLong()) +
						"> role")).setEphemeral(true).queue();
				}
			}
			else
			{
				event.replyEmbeds(embedder.simpleEmbed("This command can only be done while in a CustomVC channel!", null, null, ColorCodes.MEH_NOTICE,
					"Hop into a customVC voice channel to use this command!")).setEphemeral(true).queue();
			}
		}
	}
}