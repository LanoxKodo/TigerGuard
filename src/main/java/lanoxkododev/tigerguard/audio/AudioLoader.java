package lanoxkododev.tigerguard.audio;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.SearchResult;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.player.TrackLoaded;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import lanoxkododev.tigerguard.messages.ColorCodes;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class AudioLoader extends AbstractAudioLoadResultHandler {
	
	EmbedMessageFactory embedder = new EmbedMessageFactory();

	private final SlashCommandInteractionEvent event;
    private final GuildMusicManager manager;

    public AudioLoader(SlashCommandInteractionEvent event, GuildMusicManager mngr)
    {
        this.event = event;
        manager = mngr;
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded result) {
        final Track track = result.getTrack();
        TrackInfo info = track.getInfo();
        
        manager.scheduler.enqueue(track);
        requestAcceptedEmbed(info);
    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded result)
    {
        TrackInfo info = result.getTracks().get(0).getInfo();

        manager.scheduler.enqueuePlaylist(result.getTracks());
        requestAcceptedEmbed(info);
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult result)
    {
        final List<Track> tracks = result.getTracks();

        if (tracks.isEmpty())
        {
            event.getHook().sendMessage("No tracks found!").queue();
            return;
        }

        final Track firstTrack = tracks.get(0);
        TrackInfo info = firstTrack.getInfo();

        manager.scheduler.enqueue(firstTrack);
        requestAcceptedEmbed(info);
    }

    @Override
    public void noMatches()
    {
    	requestFailureEmbed("No matches found for your input!", ColorCodes.UNABLE,
            "I was unable to retrieve a result for your request; please try again or submit a bug report in my support server!");
    }

    @Override
    public void loadFailed(@NotNull LoadFailed result)
    {
    	requestFailureEmbed("Failed to load track!", ColorCodes.ERROR, result.getException().getMessage());
    }
    
    /**
     * Reply to our event with a success embed.
     * 
     * @param info
     */
    private void requestAcceptedEmbed(TrackInfo info)
    {
    	event.getHook().sendMessageEmbeds(embedder.tigerEmbed("Request accepted", ColorCodes.CONFIRMATION, "Requested: " + info.getTitle())).queue();
    }
    
    /**
     * Reply to our event with a failure embed.
     * 
     * @param title
     * @param color
     * @param body
     */
    private void requestFailureEmbed(String title, ColorCodes color, String body)
    {
    	event.getHook().sendMessageEmbeds(embedder.tigerEmbed(title, color, body)).queue();
    }
}