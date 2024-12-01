package lanoxkododev.tigerguard.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import lanoxkododev.tigerguard.TigerGuard;
import lanoxkododev.tigerguard.Utils;
import lanoxkododev.tigerguard.audio.AudioComplex;
import lanoxkododev.tigerguard.audio.AudioLoader;
import lanoxkododev.tigerguard.logging.LogType;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Play implements TGCommand {

	EmbedMessageFactory embedder = new EmbedMessageFactory();
	TigerLogs logger = new TigerLogs();
	private final AudioComplex ac;

	public Play(AudioComplex acIn)
	{
		ac = acIn;
	}

	@Override
	public String getName()
	{
		return "play";
	}

	@Override
	public String getDescription()
	{
		return "Specify a song to play in a voice channel.";
	}

	@Override
	public boolean isNSFW()
	{
		return false;
	}

	@Override
	public List<OptionData> getOptions()
	{
		List<OptionData> options = new ArrayList<>();
		options.add(new OptionData(OptionType.STRING, "query", "The item to play. Can be a song or playlist (insert links for playlists).", true).setMinLength(1).setMaxLength(600));

		return options;
	}

	@Override
	public DefaultMemberPermissions getDefaultPermission()
	{
		return DefaultMemberPermissions.ENABLED;
	}

	@Override
	public void execute(SlashCommandInteractionEvent event)
	{
		if (event.getMember().getVoiceState().inAudioChannel()) //If member is in audio channel
		{
			event.deferReply().setEphemeral(true).queue();
			Guild guild = event.getGuild();

			if (!guild.getSelfMember().getVoiceState().inAudioChannel())
			{
				final AudioChannel voiceChannel = event.getMember().getVoiceState().getChannel();

				try
				{
					guild.getJDA().getDirectAudioController().connect(voiceChannel);
					String request = event.getOption("query").getAsString();

					if (!(new Utils().isUrl(request))) request = "ytsearch: " + request;
					else
					{
						if (request.contains("youtu.be") || request.contains("youtube.com"))
						{
							String title = getVideoTitle(request);
							if (TigerGuard.isDebugMode()) logger.log(LogType.DEBUG, "Request link: " + request + " | Request title: " + title);
							request = "ytsearch: " + title;
						}
					}

					ac.getClient().getOrCreateLink(guild.getIdLong()).loadItem(request).subscribe(new AudioLoader(event, ac.acquireMusicManager(guild)));
				}
				catch (InsufficientPermissionException e)
				{
					event.getHook().sendMessageEmbeds(embedder.simpleEmbed("Error connecting to voice channel: Lack of permission to view/access the voice channel",
						null, null, ColorCodes.UNABLE, "I could not connect due to lack of permissions to the voice channel \"__*" + voiceChannel.getName() +
						"*__\" under category \"__*" + voiceChannel.getParentCategory().getName() + "*__\". Check this voice channel's permissions and verify " +
						"I have the 'VOICE_CONNECT' permission for it.")).setEphemeral(true).queue();
				}
			}
		}
		else event.replyEmbeds(embedder.voiceErrorEmbed()).setEphemeral(true).queue();
	}

	private String getVideoTitle(String url)
	{
		Element title = null;
		
		try
		{
			title = Jsoup.connect(url).get().selectFirst("meta[name=title]");
		}
		catch (IOException e)
		{
			logger.logErr(LogType.ERROR, "Failure grabbing video data", "Requested URL: " + url, e);
		}

		return title != null ? title.attr("content") : null;
	}
}
