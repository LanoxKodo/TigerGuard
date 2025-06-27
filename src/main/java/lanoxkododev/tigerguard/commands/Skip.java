package lanoxkododev.tigerguard.commands;

import java.util.concurrent.TimeUnit;

import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.audio.AudioComplex;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Skip implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	private final AudioComplex ac;

	public Skip(AudioComplex acIn)
	{
		ac = acIn;
	}

	@Override
	public String getName()
	{
		return "skip";
	}

	@Override
	public String getDescription()
	{
		return "Skip the current song.";
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		if (!event.getMember().getVoiceState().inAudioChannel()) event.replyEmbeds(embedder.voiceErrorEmbed()).setEphemeral(true).queue();
		else
		{
			event.deferReply().queue();
			ac.acquireMusicManager(event.getGuild()).skip();

			event.getHook().sendMessageEmbeds(embedder.simpleEmbed("║ Skipping track.", null,
				TigerGuard.TigerGuardInstance.getSelf().getEffectiveAvatarUrl(), ColorCodes.CONFIRMATION, "This message will auto-delete in 30 seconds.")).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
		}
	}

}
