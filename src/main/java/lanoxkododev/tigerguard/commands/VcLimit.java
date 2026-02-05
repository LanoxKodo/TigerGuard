package lanoxkododev.tigerguard.commands;

import java.util.ArrayList;
import java.util.List;

import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.TigerGuardDB.DB_Enums;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class VcLimit implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerGuardDB tgdb = TigerGuardDB.getTigerGuardDB();

	@Override
	public String getName()
	{
		return "vclimit";
	}

	@Override
	public String getDescription()
	{
		return "Change the number of users permitted to a custom voice channel.";
	}

	@Override
	public List<OptionData> getOptions()
	{
		List<OptionData> options = new ArrayList<>();
		options.add(new OptionData(OptionType.INTEGER, "limit", "The number of max people allowed into this voice channel.", true));

		return options;
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		if(!event.getMember().getVoiceState().inAudioChannel()) event.replyEmbeds(embedder.voiceErrorEmbed()).setEphemeral(true).queue();
		else
		{
			AudioChannelUnion vc = event.getMember().getVoiceState().getChannel();
			
			if (vc != null && vc.getParentCategory().getIdLong() == (Long)tgdb.getValue(DB_Enums.DYNAMIC_VC_CAT, "guild", event.getGuild().getIdLong()) &&
				vc.getIdLong() != (Long)tgdb.getValue(DB_Enums.DYNAMIC_VC_CHAN, "guild", event.getGuild().getIdLong()))
			{
				int vcLimit = event.getOption("limit").getAsInt();
				vc.getManager().setUserLimit(vcLimit).queue();
				event.reply("The voice channel user limit was changed to " + vcLimit + "!").setEphemeral(true).queue();
			}
		}
	}
}
