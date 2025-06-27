package lanoxkododev.tigerguard.commands;

import java.util.concurrent.TimeUnit;

import lanoxkododev.tigerguard.audio.AudioComplex;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Resume implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	private final AudioComplex ac;

	public Resume(AudioComplex acIn)
	{
		ac = acIn;
	}
	@Override
	public String getName()
	{
		return "resume";
	}

	@Override
	public String getDescription()
	{
		return "Resume the song queue that is currently paused.";
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		if (!event.getMember().getVoiceState().inAudioChannel()) event.replyEmbeds(embedder.voiceErrorEmbed()).setEphemeral(true).queue();
		else
		{
			event.deferReply().queue();
			ac.acquireMusicManager(event.getGuild()).setPauseStatus(false);

			event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Audio playback has resumed!", null, null, ColorCodes.FINISHED,
				"This message will auto-delete in 30 seconds.")).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
		}
	}
}
