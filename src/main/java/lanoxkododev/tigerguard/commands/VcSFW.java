package lanoxkododev.tigerguard.commands;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;

public class VcSFW implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();

	@Override
	public String getName()
	{
		return "vcsfw";
	}

	@Override
	public String getDescription()
	{
		return "Change the custom voice channel to be marked as SFW.";
	}

	@Override
	public boolean isNSFWRelated()
	{
		return true;
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
				String checkName = vc.getManager().getChannel().getName().toLowerCase();

				if (checkName.contains("nsfw "))
				{
					String sfwName = checkName.replace("nsfw ", "");
					VoiceChannelManager vcManager = vc.asVoiceChannel().getManager();
					vcManager.setName(sfwName).queue();
					event.reply("The voice channel has been marked as SFW.").setEphemeral(true).queue();
					vcManager.setNSFW(false).queue();
				}
				else
				{
					event.replyEmbeds(embedder.simpleEmbed("The channel appears to be SFW already! :)", null, null, ColorCodes.FINISHED,
						"The channel is SFW friendly!")).setEphemeral(true).queue();
				}
			}
		}
	}
}
