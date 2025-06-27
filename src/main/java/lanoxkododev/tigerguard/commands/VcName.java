package lanoxkododev.tigerguard.commands;

import java.util.ArrayList;
import java.util.List;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class VcName implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tigerGuardDB = TigerGuardDB.getTigerGuardDB();

	@Override
	public String getName()
	{
		return "vcname";
	}

	@Override
	public String getDescription()
	{
		return "Change the name of the Custom VC channel you are in.";
	}

	@Override
	public List<OptionData> getOptions()
	{
		List<OptionData> options = new ArrayList<>();
		options.add(new OptionData(OptionType.STRING, "vcname", "The name to change the channel to.", true).setMinLength(1).setMaxLength(28));

		return options;
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
				vc.getManager().setName(event.getOption("vcname").getAsString()).queue();
				event.reply("The name of the voice channel has been changed!").setEphemeral(true).queue();
			}
		}
	}
}
