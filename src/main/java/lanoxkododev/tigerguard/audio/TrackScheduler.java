package lanoxkododev.tigerguard.audio;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import lanoxkododev.tigerguard.TigerGuardDB;
import lanoxkododev.tigerguard.logging.TigerLogs;
import lanoxkododev.tigerguard.messages.EmbedMessageFactory;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class TrackScheduler {
	
	TigerGuardDB tigerguardDB = TigerGuardDB.getTigerGuardDB();
	TigerLogs logger = new TigerLogs();
	EmbedMessageFactory embedder = new EmbedMessageFactory();
    private final GuildMusicManager guildMusicManager;
    public final Queue<Track> queue = new LinkedList<>();

    public TrackScheduler(GuildMusicManager guildMusicManager)
    {
        this.guildMusicManager = guildMusicManager;
    }

    public void enqueue(Track track)
    {
    	boolean wasEmpty = queue.isEmpty();
    	if (queue.size() == 0) startTrack(track);
    	else queue.offer(track);
    	
    	if (wasEmpty) startNextTrack();
    	/* Leaving this code as reference for now.
        guildMusicManager.getPlayer().ifPresentOrElse(player -> {
        	if (player.getTrack() == null) startTrack(track);
        	else queue.offer(track);
        }, () -> {
        	startTrack(track);
        });
        */
    }

    public void enqueuePlaylist(List<Track> tracks)
    {
    	boolean wasEmpty = queue.isEmpty();
        queue.addAll(tracks);
        
        if (wasEmpty) startNextTrack();
        
        /* Leaving this code as reference for now.
        guildMusicManager.getPlayer().ifPresentOrElse(player -> {
        	if (player.getTrack() == null) startTrack(queue.poll());
        }, () -> {
        	startTrack(queue.poll());
        });
        */
    }

    //TODO - Placeholder, logic that would be here is handled in startTrack(Track track) a few methods below
    public void onTrackStart(Track track)
    {
    	
    }
    
    public void onTrackEnd()
    {
    	final var nextTrack = queue.poll();
    	if (nextTrack != null) startTrack(nextTrack);
    	else guildMusicManager.stop();
    }

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason)
    {
    	if (endReason.getMayStartNext())
        {
            final var nextTrack = queue.poll();
            if (nextTrack != null) startTrack(nextTrack);
            else guildMusicManager.stop();
        }
    	else guildMusicManager.stop();
    }
    
    public void startNextTrack()
    {
    	Track nextTrack = queue.poll();
    	
    	guildMusicManager.getPlayer().ifPresentOrElse(player -> {
        	if (player.getTrack() == null) startTrack(nextTrack);
        	else queue.offer(nextTrack);
        }, () -> {
        	startTrack(nextTrack);
        });
    }

    private void startTrack(Track track)
    {
        liveMusicEmbed(track.getInfo());
        guildMusicManager.getLink().ifPresent(
            (link) -> link.createOrUpdatePlayer()
                .setTrack(track)
                .setVolume(100)
                .subscribe()
        );
    }
    
    /**
     * Send a message (not a response to the event), to the guild, preferably a designated music channel.
     * 
     * @param info
     */
    private void liveMusicEmbed(TrackInfo info)
    {
    	deleteMusicEmbed();
    	
    	getOutputChannel().sendMessageEmbeds(embedder.musicNowPlaying(info, queue.size())).addActionRow(embedder.musicActionRowButtonProvider()).queue(a -> {
    		tigerguardDB.setGuildLiveMusicMessage(guildMusicManager.getGuild().getIdLong(), a.getIdLong());
    	});
    }
    
    public void deleteMusicEmbed()
    {
    	Guild guild = guildMusicManager.getGuild();
    	Long messageId = tigerguardDB.getGuildLiveMusicMessage(guild.getIdLong());

    	if (messageId != null && messageId != 0)
    	{
    		GuildMessageChannel channel = getOutputChannel();
    		MessageHistory history = MessageHistory.getHistoryAround(channel, messageId.toString()).complete();
			history.getMessageById(messageId).delete().queue();
			tigerguardDB.setGuildLiveMusicMessage(guild.getIdLong(), 0);
    	}
    }
    
    private GuildMessageChannel getOutputChannel()
    {
    	Guild guild = guildMusicManager.getGuild();
    	Long defined = tigerguardDB.getGuildMusicChannel(guild.getIdLong());
    	
    	if (defined != null && defined != 0) return guild.getTextChannelById(defined);
    	else return guild.getSelfMember().getVoiceState().getChannel().asGuildMessageChannel();
    }
}
