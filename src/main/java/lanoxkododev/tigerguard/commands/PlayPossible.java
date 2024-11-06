package lanoxkododev.tigerguard.commands;

import dev.arbjerg.lavalink.client.Link;
import lanoxkododev.tigerguard.audio.AudioComplex;
import lanoxkododev.tigerguard.audio.AudioLoader;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PlayPossible {
	
	private final AudioComplex am;
	private final SlashCommandInteractionEvent event;
	
	public PlayPossible(AudioComplex amIn, SlashCommandInteractionEvent eventIn)
	{
		am = amIn;
		event = eventIn;
		System.out.println(am + "\n" + AudioComplex.getInstance() + "\n" + am.getMusicManagers().size());
		
        if (event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) event.deferReply(false).queue();
        else event.getGuild().getJDA().getDirectAudioController().connect(event.getMember().getVoiceState().getChannel());

        final String identifier = "ytsearch:" + event.getOption("query").getAsString();
        final long guildId = event.getGuild().getIdLong();
        final Link link = am.getClient().getOrCreateLink(guildId);
        final var mngr = am.getOrCreateMusicManager(event.getGuild());
        
        //This may need to be handled differently
        link.loadItem(identifier).subscribe(new AudioLoader(event, mngr));
	}
}
