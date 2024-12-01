package lanoxkododev.tigerguard.commands;

import java.util.List;
import java.util.concurrent.TimeUnit;

import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.audio.AudioComplex;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Stop implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	private final AudioComplex ac;

	public Stop(AudioComplex acIn)
	{
		ac = acIn;
	}

	@Override
	public String getName()
	{
		return "stop";
	}

	@Override
	public String getDescription()
	{
		return "Stop the bot from playing songs and disconnect it from the voice channel.";
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
		if (!event.getMember().getVoiceState().inAudioChannel()) event.replyEmbeds(embedder.voiceErrorEmbed()).setEphemeral(true).queue();
		else
		{
			event.deferReply().queue();
			ac.acquireMusicManager(event.getGuild()).stop();
			ac.getClient().getOrCreateLink(event.getGuild().getIdLong()).destroy();
			event.getGuild().getJDA().getDirectAudioController().disconnect(event.getGuild());

			event.getHook().sendMessageEmbeds(embedder.simpleEmbed("║ Leaving channel due to stop command.\n║Shutting down audio instance and queue.", null,
				TigerGuard.TigerGuardInstance.getSelf().getEffectiveAvatarUrl(), ColorCodes.CONFIRMATION, "This message will auto-delete in 30 seconds.")).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
		}
	}
}
